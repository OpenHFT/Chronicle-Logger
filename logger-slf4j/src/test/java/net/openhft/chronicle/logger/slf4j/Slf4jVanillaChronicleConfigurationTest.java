/*
 * Copyright 2014 Higher Frequency Trading
 * <p/>
 * http://www.higherfrequencytrading.com
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.openhft.chronicle.logger.slf4j;

import net.openhft.chronicle.VanillaChronicleConfig;
import net.openhft.chronicle.logger.ChronicleLog;
import net.openhft.chronicle.logger.ChronicleLogLevel;
import org.junit.Test;

import java.io.File;
import java.util.Properties;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class Slf4jVanillaChronicleConfigurationTest extends Slf4jTestBase {

    @Test
    public void testLoadProperties() {
        final String cfgPath = System.getProperty("slf4j.chronicle.vanilla.properties");
        final ChronicleLoggingConfig cfg = ChronicleLoggingConfig.load(cfgPath);

        assertEquals(
            new File(basePath(ChronicleLoggingConfig.TYPE_VANILLA, "root")),
            new File(cfg.getString(ChronicleLoggingConfig.KEY_PATH)));
        assertEquals(
            ChronicleLoggingConfig.TYPE_VANILLA,
            cfg.getString(ChronicleLoggingConfig.KEY_TYPE));
        assertEquals(
            ChronicleLoggingConfig.BINARY_MODE_FORMATTED,
            cfg.getString(ChronicleLoggingConfig.KEY_BINARY_MODE));
        assertEquals(
            ChronicleLogLevel.DEBUG.toString(),
            cfg.getString(ChronicleLoggingConfig.KEY_LEVEL).toUpperCase());
        assertEquals(
            ChronicleLog.STR_FALSE,
            cfg.getString(ChronicleLoggingConfig.KEY_SHORTNAME));
        assertEquals(
            ChronicleLog.STR_FALSE,
            cfg.getString(ChronicleLoggingConfig.KEY_APPEND));
        assertEquals(
            new File(basePath(ChronicleLoggingConfig.TYPE_VANILLA, "logger_1")),
            new File(cfg.getString("logger_1", ChronicleLoggingConfig.KEY_PATH)));
        assertEquals(
            ChronicleLogLevel.INFO.toString(),
            cfg.getString("logger_1", ChronicleLoggingConfig.KEY_LEVEL).toUpperCase());
        assertEquals(
            new File(basePath(ChronicleLoggingConfig.TYPE_VANILLA, "readwrite")),
            new File(cfg.getString("readwrite", ChronicleLoggingConfig.KEY_PATH)));
        assertEquals(
            ChronicleLogLevel.DEBUG.toString(),
            cfg.getString("readwrite", ChronicleLoggingConfig.KEY_LEVEL).toUpperCase());
    }

    @Test
    public void testLoadConfig() {
        final Properties properties = new Properties();
        properties.setProperty("slf4j.chronicle.type","vanilla");
        properties.setProperty("slf4j.chronicle.cfg.dataCacheCapacity","128");
        properties.setProperty("slf4j.chronicle.cfg.indexCacheCapacity","256");
        properties.setProperty("slf4j.chronicle.cfg.synchronous","true");

        final ChronicleLoggingConfig clc = ChronicleLoggingConfig.load(properties);
        assertNull(clc.getIndexedChronicleConfig());
        assertNotNull(clc.getVanillaChronicleConfig());
        assertTrue(VanillaChronicleConfig.DEFAULT != clc.getVanillaChronicleConfig().cfg());

        final VanillaChronicleConfig cfg = clc.getVanillaChronicleConfig().cfg();
        assertEquals(128, cfg.dataCacheCapacity());
        assertEquals(256, cfg.indexCacheCapacity());
        assertTrue(cfg.synchronous());
        assertEquals(VanillaChronicleConfig.DEFAULT.defaultMessageSize(), cfg.defaultMessageSize());
        assertEquals(VanillaChronicleConfig.DEFAULT.cleanupOnClose(), cfg.cleanupOnClose());
    }
}
