/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.logger.slf4j2;

import net.openhft.chronicle.logger.ChronicleLogConfig;
import net.openhft.chronicle.logger.ChronicleLogLevel;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;

class Slf4jChronicleConfigurationTest extends Slf4jTestBase {

    @Test
    void testLoadProperties() {
        final String cfgPath = "chronicle.logger.properties";
        final ChronicleLogConfig cfg = ChronicleLogConfig.load(cfgPath);

        assertNotNull(cfg);

        assertEquals(
                new File(basePath("root")),
                new File(cfg.getString(ChronicleLogConfig.KEY_PATH)));
        assertEquals(
                ChronicleLogLevel.DEBUG.toString(),
                cfg.getString(ChronicleLogConfig.KEY_LEVEL).toUpperCase());
        assertEquals("false", cfg.getString(ChronicleLogConfig.KEY_APPEND));
        assertEquals(
                new File(basePath("logger_1")),
                new File(cfg.getString("logger_1", ChronicleLogConfig.KEY_PATH)));
        assertEquals(
                ChronicleLogLevel.INFO.toString(),
                cfg.getString("logger_1", ChronicleLogConfig.KEY_LEVEL).toUpperCase());
        assertEquals(
                ChronicleLogLevel.DEBUG.toString(),
                cfg.getString("readwrite", ChronicleLogConfig.KEY_LEVEL).toUpperCase());
    }

    @Test
    void testLoadConfig() {
        final Properties properties = new Properties();
        properties.setProperty("chronicle.logger.root.cfg.blockSize", "256");

        final ChronicleLogConfig clc = ChronicleLogConfig.load(properties);
        assertNotNull(clc.getAppenderConfig());

        assertEquals(256, clc.getAppenderConfig().getBlockSize());
    }
}
