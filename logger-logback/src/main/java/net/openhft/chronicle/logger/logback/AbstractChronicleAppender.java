/*
 * Copyright 2013-2026 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.logger.logback;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.filter.Filter;
import ch.qos.logback.core.spi.ContextAwareBase;
import ch.qos.logback.core.spi.FilterAttachableImpl;
import ch.qos.logback.core.spi.FilterReply;
import net.openhft.chronicle.logger.ChronicleLogLevel;
import net.openhft.chronicle.logger.ChronicleLogWriter;

import java.io.IOException;
import java.util.List;

/**
 * Base appender that writes Logback events to Chronicle queues.
 * Sub-classes provide the concrete queue configuration.
 */
public abstract class AbstractChronicleAppender
        extends ContextAwareBase
        implements Appender<ILoggingEvent> {

    private final FilterAttachableImpl<ILoggingEvent> filterAttachable;

    /**
     * Writer used to publish events to the Chronicle queue.
     * Created in {@link #start()} via {@link #createWriter()}.
     */
    protected ChronicleLogWriter writer;
    private String name;
    private boolean started;
    private String path;
    private String wireType;

    protected AbstractChronicleAppender() {
        this.filterAttachable = new FilterAttachableImpl<>();
        this.name = null;
        this.started = false;
        this.path = null;
        this.wireType = null;
        this.writer = null;
    }

    // Custom logging options
    public static ChronicleLogLevel toChronicleLogLevel(final Level level) {
        switch (level.levelInt) {
            case Level.DEBUG_INT:
                return ChronicleLogLevel.DEBUG;
            case Level.TRACE_INT:
                return ChronicleLogLevel.TRACE;
            case Level.INFO_INT:
                return ChronicleLogLevel.INFO;
            case Level.WARN_INT:
                return ChronicleLogLevel.WARN;
            case Level.ERROR_INT:
                return ChronicleLogLevel.ERROR;
            default:
                throw new IllegalArgumentException(level.levelInt + " not a valid level value");
        }
    }

    public String getPath() {
        return this.path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getWireType() {
        return wireType;
    }

    // Chronicle implementation
    public void setWireType(String wireType) {
        this.wireType = wireType;
    }

    /**
     * Creates the Chronicle writer used to store events.
     *
     * @return a log writer bound to the configured queue
     * @throws IOException if the writer cannot be created
     */
    protected abstract ChronicleLogWriter createWriter() throws IOException;

    /**
     * Logs a single event using the supplied writer.
     *
     * @param event  the event to log
     * @param writer the target writer
     */
    protected abstract void doAppend(final ILoggingEvent event, final ChronicleLogWriter writer);

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public boolean isStarted() {
        return started;
    }

    @Override
    public void addFilter(Filter<ILoggingEvent> newFilter) {
        this.filterAttachable.addFilter(newFilter);
    }

    @Override
    public void clearAllFilters() {
        this.filterAttachable.clearAllFilters();
    }

    @Override
    public List<Filter<ILoggingEvent>> getCopyOfAttachedFiltersList() {
        return this.filterAttachable.getCopyOfAttachedFiltersList();
    }

    @Override
    public FilterReply getFilterChainDecision(ILoggingEvent event) {
        return this.filterAttachable.getFilterChainDecision(event);
    }

    @Override
    public void start() {
        if (getPath() == null) {
            addError("Appender " + getName() + " has configuration errors and is not started!");

        } else {
            try {
                this.writer = createWriter();
                this.started = true;
            } catch (IOException e) {
                this.writer = null;
                addError("Appender " + getName() + " " + e.getMessage());
            }
        }
    }

    @Override
    public void stop() {
        if (this.writer != null) {
            try {
                this.writer.close();
            } catch (IOException e) {
                addError("Appender " + getName() + " " + e.getMessage());
            }
        }

        this.started = false;
    }

    @Override
    public void doAppend(final ILoggingEvent event) {
        if (getFilterChainDecision(event) != FilterReply.DENY) {
            doAppend(event, writer);
        }
    }
}
