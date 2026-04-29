/*
 * Copyright 2013-2026 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.logger;

import java.io.Closeable;

/**
 * Provides a minimal API for writing structured log events to a Chronicle.
 * Implementations are responsible for persisting the supplied fields and may
 * be shared between threads.
 */
public interface ChronicleLogWriter extends Closeable {

    /**
     * Records a log event without additional arguments.
     *
     * @param level      severity of the event
     * @param timestamp  epoch time in milliseconds
     * @param threadName name of the calling thread
     * @param loggerName name of the logger
     * @param message    formatted message text
     */
    void write(
            ChronicleLogLevel level,
            long timestamp,
            String threadName,
            String loggerName,
            String message);

    /**
     * Records a log event with optional throwable and argument array.
     * The arguments are written in sequence and may be used by the reader
     * for deferred formatting.
     *
     * @param level      severity of the event
     * @param timestamp  epoch time in milliseconds
     * @param threadName name of the calling thread
     * @param loggerName name of the logger
     * @param message    formatted message text
     * @param throwable  optional stack trace to record, may be {@code null}
     * @param args       optional argument objects, may be empty
     */
    void write(
            ChronicleLogLevel level,
            long timestamp,
            String threadName,
            String loggerName,
            String message,
            Throwable throwable,
            Object... args);

}
