/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.logger.log4j2;

import net.openhft.chronicle.logger.ChronicleLogWriter;
import net.openhft.chronicle.logger.DefaultChronicleLogWriter;
import net.openhft.chronicle.logger.LogAppenderConfig;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginElement;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

/**
 * Log4j 2 appender that writes events to Chronicle Queue.
 *
 * <p>The class is registered as a plugin named {@code Chronicle} so it can be
 * referenced from a Log4j 2 configuration file. The nested
 * {@code chronicleCfg} element allows Chronicle Queue settings such as block
 * size and roll cycle to be supplied.</p>
 */

@Plugin(
        name = "Chronicle",
        category = "Core",
        elementType = "appender",
        printObject = true)
public class ChronicleAppender extends AbstractChronicleAppender {

    private final ChronicleCfg config;

    public ChronicleAppender(final String name, final Filter filter, final String path, final String wireType, final ChronicleCfg config) {
        super(name, filter, path, wireType);

        this.config = config != null ? config : new ChronicleCfg();
    }

    /**
     * Factory used by the plugin framework to create the appender.
     *
     * @param name mandatory appender name
     * @param path Chronicle Queue path
     * @param wireType optional queue wire type
     * @param chronicleConfig optional queue configuration
     * @param filter optional filter
     * @return the configured appender or {@code null} when name or path are missing
     */
    @PluginFactory
    public static ChronicleAppender createAppender(
            @PluginAttribute("name") final String name,
            @PluginAttribute("path") final String path,
            @PluginAttribute("wireType") final String wireType,
            @PluginElement("chronicleCfg") final ChronicleCfg chronicleConfig,
            @PluginElement("filter") final Filter filter) {
        if (name == null) {
            LOGGER.error("No name provided for ChronicleAppender");
            return null;
        }

        if (path == null) {
            LOGGER.error("No path provided for ChronicleAppender");
            return null;
        }

        return new ChronicleAppender(name, filter, path, wireType, chronicleConfig);
    }

    /**
     * Writes the event to the Chronicle writer.
     */
    @Override
    public void doAppend(@NotNull final LogEvent event, @NotNull final ChronicleLogWriter writer) {
        writer.write(
                toChronicleLogLevel(event.getLevel()),
                event.getTimeMillis(),
                event.getThreadName(),
                event.getLoggerName(),
                event.getMessage().getFormattedMessage(),
                event.getThrown()
        );
    }

    @Override
    protected ChronicleLogWriter createWriter() {
        return new DefaultChronicleLogWriter(config.build(getPath(), getWireType()));
    }

    /**
     * Returns the Chronicle configuration backing this appender.
     *
     * @return the configuration instance
     */
    protected LogAppenderConfig getChronicleConfig() {
        return this.config;
    }
}
