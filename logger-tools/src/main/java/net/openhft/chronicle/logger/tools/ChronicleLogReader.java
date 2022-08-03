/*
 * Copyright 2016-2022 Chronicle Software
 *
 *       https://chronicle.software
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.openhft.chronicle.logger.tools;

import net.openhft.chronicle.logger.ChronicleLogLevel;
import net.openhft.chronicle.queue.ChronicleQueue;
import net.openhft.chronicle.queue.ExcerptTailer;
import net.openhft.chronicle.wire.DocumentContext;
import net.openhft.chronicle.wire.Wire;
import net.openhft.chronicle.wire.WireType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.helpers.MessageFormatter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Generic tool allowing users to process Chronicle logs in their own way
 */
public class ChronicleLogReader {
    private static final SimpleDateFormat tsFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
    private final ChronicleQueue cq;

    /**
     * Create reader with default wire type
     *
     * @param path the path to Chronicle Logs storage
     */
    public ChronicleLogReader(
            @NotNull String path) {
        this(path, WireType.BINARY_LIGHT);
    }

    /**
     * @param path     the path to Chronicle Logs storage
     * @param wireType Chronicle wire type. Must match the wire type specified in corresponding Chronicle Logger
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
     * Decode logs
     *
     * @param processor user-provided processor called for each log message
     * @param waitForIt whether to wait for more data or stop after EOF reached
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
                Throwable th = wire.hasMore() ? wire.read("throwable").throwable(false) : null;
                List<Object> argsL = new ArrayList<>();
                if (wire.hasMore()) {
                    wire.read("args").sequence(argsL, (l, vi) -> {
                        while (vi.hasNextSequenceItem()) {
                            l.add(vi.object(Object.class));
                        }
                    });
                }
                Object[] args = argsL.toArray(new Object[argsL.size()]);
                processor.process(timestamp, level, threadName, loggerName, message, th, args);
            }
        }
    }
}
