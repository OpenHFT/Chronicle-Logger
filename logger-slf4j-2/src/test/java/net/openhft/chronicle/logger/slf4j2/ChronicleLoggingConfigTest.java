/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.logger.slf4j2;

import net.openhft.chronicle.logger.ChronicleLogConfig;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;

public class ChronicleLoggingConfigTest {
    @Test
    public void testLoadClasspathIndexed() {
        System.setProperty("chronicle.logger.properties", "chronicle.logger.properties");
        assertLoadsValidConfig();
    }

    private void assertLoadsValidConfig() {
        ChronicleLogConfig config = ChronicleLogConfig.load();
        assertNotNull("unable to load config", config);
        assertNotNull("is not a valid config", config.getAppenderConfig());
    }
}
