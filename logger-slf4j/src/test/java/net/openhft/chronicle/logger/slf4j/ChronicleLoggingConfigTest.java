/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.logger.slf4j;

import net.openhft.chronicle.logger.ChronicleLogConfig;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ChronicleLoggingConfigTest {
    @Test
    public void testLoadClasspathIndexed() {
        System.setProperty("chronicle.logger.properties", "chronicle.logger.properties");
        assertLoadsValidConfig();
    }

    private void assertLoadsValidConfig() {
        ChronicleLogConfig config = ChronicleLogConfig.load();
        assertNotNull(config, "unable to load config");
        assertNotNull(config.getAppenderConfig(), "is not a valid config");
    }
}
