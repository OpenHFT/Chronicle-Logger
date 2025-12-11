/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.logger.log4j1;

import net.openhft.chronicle.logger.ChronicleLogLevel;
import net.openhft.chronicle.logger.ChronicleLogWriter;
import org.apache.log4j.Appender;
import org.apache.log4j.Layout;
import org.apache.log4j.Level;
import org.apache.log4j.helpers.LogLog;
import org.apache.log4j.helpers.OnlyOnceErrorHandler;
import org.apache.log4j.spi.*;

import java.io.IOException;

/**
 * Base Log4j 1.x appender for Chronicle.
 * <p>
 * The class manages filter handling and delegates the actual write
 * operation to a {@link ChronicleLogWriter} created by
 * {@link #createWriter()}.
 */
public abstract class AbstractChronicleAppender implements Appender, OptionHandler {

    /**
     * Chronicle writer that records events.
     */
    protected ChronicleLogWriter writer;
    /** First filter in the chain. */
    private Filter filter;
    /** Appender name used in Log4j configuration. */
    private String name;
    /** Handler for errors during logging. */
    private ErrorHandler errorHandler;
    /** Queue directory. */
    private String path;
    /** Wire type for queue encoding. */
    private String wireType;

    /**
     * Creates an appender without an attached writer.
     */
    protected AbstractChronicleAppender() {
        this.path = null;
        this.writer = null;
        this.name = null;
        this.errorHandler = new OnlyOnceErrorHandler();
    }

    // Custom logging options

    /**
     * Converts a Log4j level to the corresponding Chronicle level.
     *
     * @param level Log4j level
     * @return Chronicle log level
     */
    public static ChronicleLogLevel toChronicleLogLevel(final Level level) {
        switch (level.toInt()) {
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
                throw new IllegalArgumentException(level.toInt() + " not a valid level value");
        }
    }

    /**
     * Initialises the writer once the configuration is complete.
     * The writer is only created when a path has been set.
     */
    @Override
    public void activateOptions() {
        if (path != null) {
            this.writer = createWriter();
        } else {
            LogLog.warn("path option not set for appender [" + name + "].");
        }
    }

    @Override
    public void addFilter(Filter newFilter) {
        if (filter == null) {
            filter = newFilter;

        } else {
            filter.setNext(newFilter);
        }
    }

    // Custom logging options
    @Override
    public void clearFilters() {
        filter = null;
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
     * Sets the Chronicle queue path.
     *
     * @param path queue directory
     */
    public void setPath(String path) {
        this.path = path;
    }

    /**
     * Returns the configured wire type name.
     *
     * @return wire type name or {@code null} if defaulting
     */
    public String getWireType() {
        return wireType;
    }

    /**
     * Sets the wire type name to use when building queues.
     *
     * @param wireType wire type name
     */
    public void setWireType(String wireType) {
        this.wireType = wireType;
    }

    /**
     * Ensures the writer is closed when the object is collected.
     */
    @SuppressWarnings({"deprecation", "removal"})
    @Override
    protected void finalize() {
        // An appender might be closed then garbage collected. There is no
        // point in closing twice.
        if (this.writer == null) {
            LogLog.debug("Finalizing appender named [" + name + "].");
            close();
        }
    }

    @Override
    public ErrorHandler getErrorHandler() {
        return this.errorHandler;
    }

    @Override
    public synchronized void setErrorHandler(ErrorHandler eh) {
        if (eh == null) {
            // We do not throw exception here since the cause is probably a
            // bad cfg file.
            LogLog.warn("You have tried to set a null error-handler.");

        } else {
            this.errorHandler = eh;
        }
    }

    @Override
    public Filter getFilter() {
        return this.filter;
    }

    @Override
    public Layout getLayout() {
        return null;
    }

    @Override
    public void setLayout(Layout layout) {
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    // Chronicle implementation
    /**
     * Writes the event to the Chronicle queue after filter evaluation.
     */
    @Override
    public void doAppend(LoggingEvent event) {
        if (this.writer != null) {
            for (Filter f = this.filter; f != null; f = f.getNext()) {
                switch (f.decide(event)) {
                    case Filter.DENY:
                        return;
                    case Filter.ACCEPT:
                        f = null;
                        break;
                    default:
                        break;
                }
            }

            Throwable throwable = null;
            ThrowableInformation ti = event.getThrowableInformation();
            if (ti != null) {
                throwable = ti.getThrowable();
            }

            writer.write(
                    toChronicleLogLevel(event.getLevel()),
                    event.getTimeStamp(),
                    event.getThreadName(),
                    event.getLoggerName(),
                    event.getMessage().toString(),
                    throwable
            );

        } else {
            LogLog.error("Attempted to append to closed appender named [" + name + "].");
        }
    }

    /**
     * Creates the {@link ChronicleLogWriter} used by this appender.
     *
     * @return the writer instance
     */
    protected abstract ChronicleLogWriter createWriter();

    /**
     * Closes the writer when the appender is stopped.
     */
    @Override
    public void close() {
        if (this.writer != null) {
            try {
                this.writer.close();
            } catch (IOException e) {
                LogLog.warn("Failed to close the writer", e);
            }
        }
    }

    @Override
    public boolean requiresLayout() {
        return false;
    }
}
