/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
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

    /**
     * Creates an appender with no writer configured.
     */
    protected AbstractChronicleAppender() {
        this.filterAttachable = new FilterAttachableImpl<>();
        this.name = null;
        this.started = false;
        this.path = null;
        this.wireType = null;
        this.writer = null;
    }

    // Custom logging options

    /**
     * Converts a Logback {@link Level} to the corresponding Chronicle level.
     *
     * @param level Logback level
     * @return Chronicle log level
     */
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

    /**
     * Returns the queue path configured for this appender.
     *
     * @return queue path
     */
    public String getPath() {
        return this.path;
    }

    /**
     * Sets the queue directory to write to.
     *
     * @param path queue directory
     */
    public void setPath(String path) {
        this.path = path;
    }

    /**
     * Returns the configured wire type name.
     *
     * @return wire type name or {@code null} for default
     */
    public String getWireType() {
        return wireType;
    }

    // Chronicle implementation

    /**
     * Sets the wire type name for new queues.
     *
     * @param wireType wire type name
     */
    public void setWireType(String wireType) {
        this.wireType = wireType;
    }

    /**
     * Creates the Chronicle writer used to store events.
     *
     * @return a log writer bound to the configured queue
     */
    protected abstract ChronicleLogWriter createWriter();

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
            this.writer = createWriter();
            this.started = true;
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
