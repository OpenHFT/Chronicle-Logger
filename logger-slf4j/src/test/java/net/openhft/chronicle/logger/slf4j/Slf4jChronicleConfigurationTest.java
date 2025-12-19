/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.logger.slf4j;

import net.openhft.chronicle.logger.ChronicleLogConfig;
import net.openhft.chronicle.logger.ChronicleLogLevel;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class Slf4jChronicleConfigurationTest extends Slf4jTestBase {

    @Test
    public void testLoadProperties() {
        final String cfgPath = "chronicle.logger.properties";
        final ChronicleLogConfig cfg = ChronicleLogConfig.load(cfgPath);

        assertNotNull(cfg, "load config from classpath");

        assertEquals(
                new File(basePath("root")),
                new File(cfg.getString(ChronicleLogConfig.KEY_PATH)),
                "root path");
        assertEquals(
                ChronicleLogLevel.DEBUG.toString(),
                cfg.getString(ChronicleLogConfig.KEY_LEVEL).toUpperCase(),
                "root level");
        assertEquals("false", cfg.getString(ChronicleLogConfig.KEY_APPEND), "root append");
        assertEquals(
                new File(basePath("logger_1")),
                new File(cfg.getString("logger_1", ChronicleLogConfig.KEY_PATH)),
                "logger_1 path");
        assertEquals(
                ChronicleLogLevel.INFO.toString(),
                cfg.getString("logger_1", ChronicleLogConfig.KEY_LEVEL).toUpperCase(),
                "logger_1 level");
        assertEquals(
                ChronicleLogLevel.DEBUG.toString(),
                cfg.getString("readwrite", ChronicleLogConfig.KEY_LEVEL).toUpperCase(),
                "readwrite level");
    }

    @Test
    public void testLoadConfig() {
        final Properties properties = new Properties();
        properties.setProperty("chronicle.logger.root.cfg.blockSize", "256");

        final ChronicleLogConfig clc = ChronicleLogConfig.load(properties);
        assertNotNull(clc.getAppenderConfig(), "config has appender config");

        assertEquals(256, clc.getAppenderConfig().getBlockSize(), "blockSize should match configured property value");
    }
}
