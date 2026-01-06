/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.logger.slf4j2;

import net.openhft.chronicle.logger.ChronicleLogConfig;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class ChronicleLoggingConfigTest {
    @Test
    public void testLoadClasspathIndexed() {
        System.setProperty("chronicle.logger.properties", "chronicle.logger.properties");
        ChronicleLogConfig config = ChronicleLogConfig.load();
        assertNotNull(config, "load config");
        assertNotNull(config.getAppenderConfig(), "config has appender config");
    }
}
