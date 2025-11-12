/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.logger.jul;

import net.openhft.chronicle.logger.ChronicleLogWriter;

import java.io.IOException;
import java.util.logging.Filter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;

/**
 * Base implementation for Chronicle JUL handlers.
 *
 * <p>The class holds the Chronicle queue {@code path} and the
 * {@link ChronicleLogWriter} that forwards log records. Subclasses are
 * responsible for creating the writer and implementing {@link #doPublish}.</p>
 *
 * <p>The level and filter setters are final so that configuration cannot
 * be altered once the handler is in use.</p>
 */
abstract class AbstractChronicleHandler extends Handler {

    /**
     * Filesystem location of the Chronicle queue.
     */
    private String path;

    /**
     * Destination writer used to emit log events.
     */
    private ChronicleLogWriter writer;

    protected AbstractChronicleHandler() {
        this.path = null;
        this.writer = null;
    }

    @Override
    public void flush() {
    }

    @Override
    public void close() throws SecurityException {
        if (this.writer != null) {
            try {
                this.writer.close();
            } catch (IOException e) {
                // Ignore
            }
        }
    }

    @Override
    public void publish(final LogRecord record) {
        if ((writer != null) && isLoggable(record)) {
            doPublish(record, this.writer);
        }
    }

    protected abstract void doPublish(final LogRecord record, final ChronicleLogWriter writer);

    protected final void setWriter(ChronicleLogWriter appender) {
        this.writer = appender;
    }

    @Override
    public final void setFilter(Filter newFilter) {
        super.setFilter(newFilter);
    }

    @Override
    public final synchronized void setLevel(Level newLevel) {
        super.setLevel(newLevel);
    }
}
