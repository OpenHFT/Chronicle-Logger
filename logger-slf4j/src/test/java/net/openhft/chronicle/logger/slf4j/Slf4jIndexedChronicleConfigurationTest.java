/*
 * Copyright 2014 Higher Frequency Trading
 *
 * http://www.higherfrequencytrading.com
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

import net.openhft.chronicle.ChronicleQueueBuilder;
import net.openhft.chronicle.logger.ChronicleLog;
import net.openhft.chronicle.logger.ChronicleLogConfig;
import net.openhft.chronicle.logger.ChronicleLogLevel;
import org.junit.Test;

import java.io.File;
import java.util.Properties;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class Slf4jIndexedChronicleConfigurationTest extends Slf4jTestBase {

    @Test
    public void testLoadProperties() {
        final String cfgPath = "chronicle.logger.indexed.properties";
        final ChronicleLogConfig cfg = ChronicleLogConfig.load(cfgPath);

        assertEquals(
            new File(basePath(ChronicleLogConfig.TYPE_INDEXED, "root")),
            new File(cfg.getString(ChronicleLogConfig.KEY_PATH)));
        assertEquals(
            ChronicleLogConfig.TYPE_INDEXED,
            cfg.getString(ChronicleLogConfig.KEY_TYPE));
        assertEquals(
            ChronicleLogConfig.BINARY_MODE_FORMATTED,
            cfg.getString(ChronicleLogConfig.KEY_BINARY_MODE));
        assertEquals(
            ChronicleLogLevel.DEBUG.toString(),
            cfg.getString(ChronicleLogConfig.KEY_LEVEL).toUpperCase());
        assertEquals(
            ChronicleLog.STR_FALSE,
            cfg.getString(ChronicleLogConfig.KEY_SHORTNAME));
        assertEquals(
            ChronicleLog.STR_FALSE,
            cfg.getString(ChronicleLogConfig.KEY_APPEND));
        assertEquals(
            new File(basePath(ChronicleLogConfig.TYPE_INDEXED, "logger_1")),
            new File(cfg.getString("logger_1", ChronicleLogConfig.KEY_PATH)));
        assertEquals(
            ChronicleLogLevel.INFO.toString(),
            cfg.getString("logger_1", ChronicleLogConfig.KEY_LEVEL).toUpperCase());
        assertEquals(
            new File(basePath(ChronicleLogConfig.TYPE_INDEXED, "readwrite")),
            new File(cfg.getString("readwrite", ChronicleLogConfig.KEY_PATH)));
        assertEquals(
            ChronicleLogLevel.DEBUG.toString(),
            cfg.getString("readwrite", ChronicleLogConfig.KEY_LEVEL).toUpperCase());
    }

    @Test
    public void testLoadConfig() {
        final Properties properties = new Properties();
        properties.setProperty("chronicle.logger.root.type","indexed");
        properties.setProperty("chronicle.logger.root.cfg.dataBlockSize","256");
        properties.setProperty("chronicle.logger.root.cfg.synchronous","true");

        final ChronicleLogConfig clc = ChronicleLogConfig.load(properties);
        assertNotNull(clc.getIndexedChronicleConfig());
        assertNull(clc.getVanillaChronicleConfig());

        final ChronicleQueueBuilder.IndexedChronicleQueueBuilder defaultCfg =
            ChronicleQueueBuilder.indexed((File) null);

        assertEquals(256, clc.getIndexedChronicleConfig().getDataBlockSize());
        assertTrue(clc.getIndexedChronicleConfig().isSynchronous());
        assertEquals(defaultCfg.cacheLineSize(), clc.getIndexedChronicleConfig().getCacheLineSize());
    }
}
