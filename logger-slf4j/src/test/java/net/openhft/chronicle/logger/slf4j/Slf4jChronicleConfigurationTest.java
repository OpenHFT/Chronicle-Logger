/*
 * Copyright 2014-2020 chronicle.software
 *
 * http://www.chronicle.software
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.openhft.chronicle.logger.slf4j;

import net.openhft.chronicle.logger.ChronicleLogConfig;
import net.openhft.chronicle.logger.ChronicleLogLevel;
import org.junit.Test;

import java.io.File;
import java.util.Properties;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class Slf4jChronicleConfigurationTest extends Slf4jTestBase {

    @Test
    public void testLoadProperties() {
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
    public void testLoadConfig() {
        final Properties properties = new Properties();
        properties.setProperty("chronicle.logger.root.cfg.blockSize", "256");

        final ChronicleLogConfig clc = ChronicleLogConfig.load(properties);
        assertNotNull(clc.getAppenderConfig());

        assertEquals(256, clc.getAppenderConfig().getBlockSize());
    }
}
