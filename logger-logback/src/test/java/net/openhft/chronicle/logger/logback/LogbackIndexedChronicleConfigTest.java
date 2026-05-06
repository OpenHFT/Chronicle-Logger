/*
 * Copyright 2013-2026 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.logger.logback;

import ch.qos.logback.classic.spi.ILoggingEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class LogbackIndexedChronicleConfigTest extends LogbackTestBase {

    @BeforeEach
    void setup() {
        System.setProperty(
                "logback.configurationFile",
                System.getProperty("resources.path") + "/logback-chronicle-config.xml"
        );
    }

    @Test
    void testBinaryIndexedChronicleAppenderConfig() {
        final String loggerName = "config-binary-chronicle";
        final String appenderName = "CONFIG-BINARY-CHRONICLE";

        final ch.qos.logback.classic.Logger logger = getLoggerContext().getLogger(loggerName);
        assertNotNull(logger);

        final ch.qos.logback.core.Appender<ILoggingEvent> appender = logger.getAppender(appenderName);
        assertNotNull(appender);
        assertTrue(appender instanceof ChronicleAppender);

        ChronicleAppender ba = (ChronicleAppender) appender;
        assertEquals(128, ba.getChronicleConfig().getBlockSize());
    }
}
