/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.logger.logback;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.ThrowableProxy;
import ch.qos.logback.core.joran.spi.DefaultClass;
import net.openhft.chronicle.logger.ChronicleLogWriter;
import net.openhft.chronicle.logger.DefaultChronicleLogWriter;
import net.openhft.chronicle.logger.LogAppenderConfig;

import java.io.IOException;

/**
 * Appender that forwards Logback events to Chronicle storage.
 * <p>
 * Caller data and the mapped diagnostic context (MDC) may be recorded when
 * writing events. Both options are enabled by default and can be toggled via
 * {@link #setIncludeCallerData(boolean)} and
 * {@link #setIncludeMappedDiagnosticContext(boolean)}. Chronicle specific
 * parameters are provided through {@link #setChronicleConfig(LogAppenderConfig)}.
 */
public class ChronicleAppender extends AbstractChronicleAppender {

    private boolean includeCallerData;
    private boolean includeMDC;

    private LogAppenderConfig config;

    /**
     * Creates an appender with default Chronicle configuration and caller/MDC capture enabled.
     */
    public ChronicleAppender() {
        super();

        this.includeCallerData = true;
        this.includeMDC = true;
        this.config = new LogAppenderConfig();
    }

    /**
     * Exposes the configuration used when initialising the Chronicle writer.
     *
     * @return the current writer configuration
     */
    public LogAppenderConfig getChronicleConfig() {
        return this.config;
    }

    /**
     * Sets the Chronicle writer configuration.
     *
     * @param config writer configuration
     */
    @DefaultClass(LogAppenderConfig.class)
    public void setChronicleConfig(final LogAppenderConfig config) {
        this.config = config;
    }

    /**
     * Builds the concrete Chronicle writer for this appender using the configured path and wire type.
     */
    @Override
    protected ChronicleLogWriter createWriter() {
        return new DefaultChronicleLogWriter(this.config.build(this.getPath(), getWireType()));
    }

    // Custom logging options

    /**
     * Whether caller data is written to the Chronicle log.
     *
     * @return {@code true} if caller data is included
     */
    public boolean isIncludeCallerData() {
        return this.includeCallerData;
    }

    /**
     * Enables or disables writing caller data.
     *
     * @param logCallerData {@code true} to include caller data
     */
    public void setIncludeCallerData(boolean logCallerData) {
        this.includeCallerData = logCallerData;
    }

    /**
     * Whether MDC entries are written to the Chronicle log.
     *
     * @return {@code true} if MDC is included
     */
    public boolean isIncludeMappedDiagnosticContext() {
        return this.includeMDC;
    }

    /**
     * Enables or disables writing MDC.
     *
     * @param logMDC {@code true} to include MDC data
     */
    public void setIncludeMappedDiagnosticContext(boolean logMDC) {
        this.includeMDC = logMDC;
    }

    /**
     * Writes the supplied event to the Chronicle writer.
     * The throwable is extracted from the {@link ThrowableProxy}
     * so that its type is preserved in the log.
     *
     * @param event  the event to log
     * @param writer the target writer
     */
    @Override
    public void doAppend(final ILoggingEvent event, final ChronicleLogWriter writer) {
        final ThrowableProxy tp = (ThrowableProxy) event.getThrowableProxy();

        writer.write(
                toChronicleLogLevel(event.getLevel()),
                event.getTimeStamp(),
                event.getThreadName(),
                event.getLoggerName(),
                event.getMessage(),
                tp != null ? tp.getThrowable() : null,
                event.getArgumentArray()
        );
    }
}
