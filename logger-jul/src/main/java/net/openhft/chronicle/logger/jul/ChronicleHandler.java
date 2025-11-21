/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.logger.jul;

import net.openhft.chronicle.logger.ChronicleLogWriter;
import net.openhft.chronicle.logger.DefaultChronicleLogWriter;
import net.openhft.chronicle.logger.LogAppenderConfig;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.LogRecord;

import static net.openhft.chronicle.logger.ChronicleLogConfig.KEY_WIRETYPE;

/**
 * Handler that writes log records to a Chronicle Queue.
 *
 * <p>Configuration is read via {@link ChronicleHandlerConfig} using the fully
 * qualified class name as a prefix. Supported properties include:
 * <ul>
 *   <li>{@code path} - queue location</li>
 *   <li>{@code level} - minimum level</li>
 *   <li>{@code filter} - {@link java.util.logging.Filter} implementation</li>
 *   <li>{@code cfg.*} - appender specific settings</li>
 *   <li>{@code wireType} - Chronicle wire format</li>
 * </ul>
 */
public class ChronicleHandler extends AbstractChronicleHandler {

    /**
     * Create the handler and initialise the writer from the LogManager.
     *
     */
    @SuppressWarnings("this-escape")
    public ChronicleHandler() {
        ChronicleHandlerConfig handlerCfg = new ChronicleHandlerConfig(getClass());
        String appenderPath = handlerCfg.getString("path", null);

        setPath(appenderPath);

        LogAppenderConfig appenderCfg = handlerCfg.getAppenderConfig();

        setLevel(handlerCfg.getLevel("level", Level.ALL));
        setFilter(handlerCfg.getFilter("filter", null));

        setWriter(new DefaultChronicleLogWriter(appenderCfg.build(
                appenderPath,
                handlerCfg.getStringProperty(KEY_WIRETYPE, "BINARY_LIGHT"))
        ));
    }

    /**
     * Forward the record to the Chronicle writer.
     *
     * @param record the JUL record
     * @param writer target writer
     */
    @SuppressWarnings("deprecation")
    @Override
    protected void doPublish(final LogRecord record, final ChronicleLogWriter writer) {
        writer.write(
                ChronicleHelper.getLogLevel(record),
                record.getMillis(),
                "thread-" + record.getThreadID(),
                record.getLoggerName(),
                record.getMessage(),
                record.getThrown(),
                record.getParameters());
    }
}
