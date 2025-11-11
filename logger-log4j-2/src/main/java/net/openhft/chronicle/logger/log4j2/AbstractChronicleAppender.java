/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.logger.log4j2;

import net.openhft.chronicle.logger.ChronicleLogLevel;
import net.openhft.chronicle.logger.ChronicleLogWriter;
import net.openhft.chronicle.logger.LogAppenderConfig;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

/**
 * Base Log4j2 appender that writes events to a Chronicle queue.
 *
 * <p>The appender keeps the queue {@code path} and optional wire type
 * supplied via plugin attributes. These values are used when the writer is
 * created and must be set before the appender is started.</p>
 *
 * <p>Typical subclasses define the plugin attributes {@code name},
 * {@code path}, {@code wireType} and an optional {@code chronicleCfg}
 * element.</p>
 *
 * <p>Subclasses implement {@link #createWriter()} to provide the concrete
 * {@link ChronicleLogWriter}. The {@link ChronicleCfg} plugin can be used by
 * subclasses to expose extra queue configuration attributes.</p>
 */
public abstract class AbstractChronicleAppender extends AbstractAppender {

    private String path;
    private String wireType;

    private ChronicleLogWriter writer;

    AbstractChronicleAppender(String name, Filter filter, String path, String wireType) {
        super(name, filter, null, true, null);

        this.path = path;
        this.wireType = wireType;
        this.writer = null;
    }

    // *************************************************************************
    // Custom logging options
    // *************************************************************************

    static ChronicleLogLevel toChronicleLogLevel(final Level level) {
        if (level.intLevel() == Level.DEBUG.intLevel()) {
            return ChronicleLogLevel.DEBUG;

        } else if (level.intLevel() == Level.TRACE.intLevel()) {
            return ChronicleLogLevel.TRACE;

        } else if (level.intLevel() == Level.INFO.intLevel()) {
            return ChronicleLogLevel.INFO;

        } else if (level.intLevel() == Level.WARN.intLevel()) {
            return ChronicleLogLevel.WARN;

        } else if (level.intLevel() == Level.ERROR.intLevel()) {
            return ChronicleLogLevel.ERROR;
        }

        throw new IllegalArgumentException(level.intLevel() + " not a valid level value");
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

    // *************************************************************************
    // Chronicle implementation
    // *************************************************************************

    public void setWireType(String wireType) {
        this.wireType = wireType;
    }

    /**
     * Builds the {@link ChronicleLogWriter} for this appender.
     *
     * @return writer bound to the configured path and wire type
     * @throws IOException if the Chronicle queue cannot be opened
     */
    protected abstract ChronicleLogWriter createWriter() throws IOException;

    // *************************************************************************
    //
    // *************************************************************************

    protected abstract void doAppend(@NotNull final LogEvent event, @NotNull final ChronicleLogWriter writer);

    @Override
    public void start() {
        if (getPath() == null) {
            LOGGER.error("Appender " + getName() + " has configuration errors and is not started!");

        } else {
            try {
                this.writer = createWriter();
            } catch (IOException e) {
                this.writer = null;
                LOGGER.error("Appender " + getName() + " " + e.getMessage());
            }

            super.start();
        }
    }

    @Override
    public void stop() {
        if (this.writer != null) {
            try {
                this.writer.close();
            } catch (IOException e) {
                LOGGER.error("Appender " + getName() + " " + e.getMessage());
            }
        }

        super.stop();
    }

    // *************************************************************************
    //
    // *************************************************************************

    @Override
    public void append(final LogEvent event) {
        if (this.writer != null) {
            doAppend(event, writer);
        }
    }

    // *************************************************************************
    //
    // *************************************************************************

    /**
     * Plugin helper used by subclasses to expose queue options.
     *
     * <p>The following attributes are supported:</p>
     * <ul>
     *     <li>{@code blockSize} - Chronicle queue block size</li>
     *     <li>{@code bufferCapacity} - memory buffer size</li>
     *     <li>{@code rollCycle} - queue roll cycle name</li>
     * </ul>
     */
    @Plugin(
            name = "chronicleCfg",
            category = "Core")
    public static final class ChronicleCfg extends LogAppenderConfig {

        ChronicleCfg() {
        }

        /**
         * Builds a configuration from plugin attributes.
         */
        @PluginFactory
        public static ChronicleCfg create(
                @PluginAttribute("blockSize") final String blockSize,
                @PluginAttribute("bufferCapacity") final String bufferCapacity,
                @PluginAttribute("rollCycle") final String rollCycle) {

            final ChronicleCfg cfg = new ChronicleCfg();
            if (blockSize != null)
                cfg.setProperty("blockSize", blockSize);
            if (bufferCapacity != null)
                cfg.setProperty("bufferCapacity", bufferCapacity);
            if (rollCycle != null)
                cfg.setProperty("rollCycle", rollCycle);

            return cfg;
        }
    }
}
