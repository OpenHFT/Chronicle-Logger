/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.logger;

import net.openhft.chronicle.queue.ChronicleQueue;
import net.openhft.chronicle.queue.ExcerptAppender;
import net.openhft.chronicle.wire.DocumentContext;
import net.openhft.chronicle.wire.Wire;
import net.openhft.chronicle.wire.WireType;
import org.jetbrains.annotations.NotNull;

import java.text.SimpleDateFormat;

/**
 * Writes log entries to a {@link ChronicleQueue}.
 * Each log event is serialised to the queue using the configured wire type.
 */
public class DefaultChronicleLogWriter implements ChronicleLogWriter {

    private static final ThreadLocal<Boolean> REENTRANCY_FLAG = ThreadLocal.withInitial(() -> false);
    private static final ThreadLocal<SimpleDateFormat> tsFormatter = ThreadLocal.withInitial(() -> new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS"));

    private final ChronicleQueue cq;

    /**
     * Creates a writer that appends log entries to the supplied queue.
     *
     * @param cq the target queue, not {@code null}
     */
    public DefaultChronicleLogWriter(@NotNull ChronicleQueue cq) {
        this.cq = cq;
    }

    @Override
    public void close() {
        cq.close();
    }

    /**
     * Records a log event without a throwable or arguments.
     *
     * @param level      the severity level
     * @param timestamp  epoch time in milliseconds
     * @param threadName name of the emitting thread
     * @param loggerName name of the logger
     * @param message    formatted message text
     */
    @Override
    public void write(
            final ChronicleLogLevel level,
            final long timestamp,
            final String threadName,
            final String loggerName,
            final String message) {
        write(level, timestamp, threadName, loggerName, message, null);
    }

    /**
     * Records a log event with optional throwable and arguments.
     *
     * @param level      the severity level
     * @param timestamp  epoch time in milliseconds
     * @param threadName name of the emitting thread
     * @param loggerName name of the logger
     * @param message    formatted message text
     * @param throwable  associated exception, may be {@code null}
     * @param args       application-specific values to serialise
     */
    @Override
    public void write(
            final ChronicleLogLevel level,
            final long timestamp,
            final String threadName,
            final String loggerName,
            final String message,
            final Throwable throwable,
            final Object... args) {
        if (REENTRANCY_FLAG.get()) {
            if (throwable == null) {
                System.out.printf("%s|%s|%s|%s|%s%n",
                        tsFormatter.get().format(timestamp),
                        level.toString(),
                        threadName,
                        loggerName,
                        message);

            } else {
                System.out.printf("%s|%s|%s|%s|%s|%s%n",
                        tsFormatter.get().format(timestamp),
                        level.toString(),
                        threadName,
                        loggerName,
                        message,
                        throwable);
            }
            return;
        }
        REENTRANCY_FLAG.set(true);
        try (ExcerptAppender appender = cq.createAppender();
             final DocumentContext dc = appender.writingDocument()) {
            Wire wire = dc.wire();
            assert wire != null;
            wire
                    .write("ts").int64(timestamp)
                    .write("level").asEnum(level)
                    .write("threadName").text(threadName)
                    .write("loggerName").text(loggerName)
                    .write("message").text(message);

            if (throwable != null) {
                wire.write("throwable").throwable(throwable);
            }

            if (args != null && args.length > 0) {
                wire.write("args").sequence(vo -> {
                    for (Object o : args)
                        try {
                            vo.object(o);
                        } catch (IllegalArgumentException unsupported) {
                            vo.text(o.toString());
                        }
                });
            }
        } finally {
            REENTRANCY_FLAG.set(false);
        }
    }

    public WireType getWireType() {
        return cq.wireType();
    }
}
