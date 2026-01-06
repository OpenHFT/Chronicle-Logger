/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.logger.logback;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import net.openhft.chronicle.core.OS;
import net.openhft.chronicle.core.util.Time;
import net.openhft.chronicle.logger.LogAppenderConfig;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class LogbackChronicleProgrammaticConfigTest extends LogbackTestBase {

    @Test
    public void testConfig() {
        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
        context.reset();

        ChronicleAppender appender = new ChronicleAppender();
        appender.setPath(OS.getTarget() + "/clog" + Time.uniqueId());
        appender.setChronicleConfig(new LogAppenderConfig());
        appender.setContext(context);
        appender.start();
        assertTrue(appender.isStarted(), "appender started");

        Logger logger = context.getLogger(Logger.ROOT_LOGGER_NAME);
        logger.addAppender(appender);

        assertDoesNotThrow(() -> logger.info("Hello World"), "log through configured appender");
    }
}
