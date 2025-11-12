/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.logger.tools;

import net.openhft.chronicle.logger.ChronicleLogLevel;
import net.openhft.chronicle.queue.ChronicleQueue;
import net.openhft.chronicle.queue.ExcerptTailer;
import net.openhft.chronicle.wire.DocumentContext;
import net.openhft.chronicle.wire.ValueIn;
import net.openhft.chronicle.wire.Wire;
import net.openhft.chronicle.wire.WireType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.helpers.MessageFormatter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility class that sequentially reads log entries from a Chronicle Queue.
 * Each entry is parsed and forwarded to a {@link ChronicleLogProcessor}.
 * The reader forms the basis of tools that tail or print Chronicle logs.
 */
public class ChronicleLogReader {
    private static final SimpleDateFormat tsFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
    private final ChronicleQueue cq;

    /**
     * Create a reader that expects the default wire type.
     *
     * @param path base directory of the Chronicle log storage
     */
    public ChronicleLogReader(
            @NotNull String path) {
        this(path, WireType.BINARY_LIGHT);
    }

    /**
     * Create a reader for a specific wire type.
     *
     * @param path     base directory of the Chronicle log storage
     * @param wireType wire format to read, must match the writer configuration
     */
    public ChronicleLogReader(
            @NotNull String path,
            @NotNull WireType wireType) {
        cq = ChronicleQueue.singleBuilder(path).wireType(wireType).build();
    }

    /**
     * Simple {@link ChronicleLogProcessor} implementation. Prints formatted message to stdout
     */
    public static void printf(
            long timestamp,
            ChronicleLogLevel level,
            String loggerName,
            String threadName,
            String message,
            @Nullable Throwable throwable,
            Object[] args) {

        message = MessageFormatter.arrayFormat(message, args).getMessage();

        if (throwable == null) {
            System.out.printf("%s [%s] [%s] [%s] %s%n",
                    tsFormat.format(timestamp),
                    level.toString(),
                    threadName,
                    loggerName,
                    message);

        } else {
            System.out.printf("%s [%s] [%s] [%s] %s%n%s%n",
                    tsFormat.format(timestamp),
                    level.toString(),
                    threadName,
                    loggerName,
                    message,
                    throwable.toString());
        }
    }

    /**
     * Read entries from the queue and pass them to the supplied processor.
     *
     * @param processor handler invoked for every decoded log entry
     * @param waitForIt if {@code true} wait for new data when the end is reached
     */
    public void processLogs(@NotNull ChronicleLogProcessor processor, boolean waitForIt) {
        ExcerptTailer tailer = cq.createTailer();
        for (; ; ) {
            try (DocumentContext dc = tailer.readingDocument()) {
                Wire wire = dc.wire();
                if (wire == null)
                    if (waitForIt) {
                        try {
                            Thread.sleep(50L);
                        } catch (InterruptedException ignored) {

                        }
                        continue;
                    } else {
                        break;
                    }

                long timestamp = wire.read("ts").int64();
                ChronicleLogLevel level = wire.read("level").asEnum(ChronicleLogLevel.class);
                String threadName = wire.read("threadName").text();
                String loggerName = wire.read("loggerName").text();
                String message = wire.read("message").text();
                Throwable th = null;
                List<Object> argsL = new ArrayList<>();
                while (wire.hasMore()) {
                    StringBuilder eventName = new StringBuilder();
                    ValueIn valueIn = wire.readEventName(eventName);
                    if ("throwable".contentEquals(eventName)) {
                        th = valueIn.throwable(false);
                    } else if ("args".contentEquals(eventName)) {
                        valueIn.sequence(argsL, (l, vi) -> {
                            while (vi.hasNextSequenceItem()) {
                                l.add(vi.object(Object.class));
                            }
                        });
                    } else {
                        valueIn.skipValue();
                    }
                }
                Object[] args = argsL.toArray(new Object[argsL.size()]);
                processor.process(timestamp, level, threadName, loggerName, message, th, args);
            }
        }
    }
}
