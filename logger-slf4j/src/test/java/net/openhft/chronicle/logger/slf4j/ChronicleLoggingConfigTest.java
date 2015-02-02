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

import net.openhft.chronicle.logger.ChronicleLogConfig;

import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.*;

public class ChronicleLoggingConfigTest {
    @Ignore
    @Test
    public void testLoadFileVanilla() throws Exception {
        System.setProperty("chronicle.logger.properties", System.getProperty("chronicle.logger.vanilla.properties"));
        assertLoadsValidVanillaConfig();
    }

    @Test
    public void testLoadClasspathVanilla() throws Exception {
        System.setProperty("chronicle.logger.properties", "chronicle.logger.vanilla.properties");
        assertLoadsValidVanillaConfig();
    }

    private void assertLoadsValidVanillaConfig() {
        ChronicleLogConfig config = ChronicleLogConfig.load();
        assertNotNull("unable to load config", config);
        assertNotNull("is not a vanilla config", config.getVanillaChronicleConfig());
        assertEquals(ChronicleLogConfig.FORMAT_BINARY, config.getString(ChronicleLogConfig.KEY_FORMAT));
    }

    @Ignore
    @Test
    public void testLoadFileIndexed() throws Exception {
        System.setProperty("chronicle.logger.properties", System.getProperty("chronicle.logger.indexed.properties"));
        assertLoadsValidIndexedConfig();
    }

    @Test
    public void testLoadClasspathIndexed() throws Exception {
        System.setProperty("chronicle.logger.properties", "chronicle.logger.indexed.properties");
        assertLoadsValidIndexedConfig();
    }

    private void assertLoadsValidIndexedConfig() {
        ChronicleLogConfig config = ChronicleLogConfig.load();
        assertNotNull("unable to load config", config);
        assertNotNull("is not a indexed config", config.getIndexedChronicleConfig());
        assertEquals(ChronicleLogConfig.FORMAT_BINARY, config.getString(ChronicleLogConfig.KEY_FORMAT));
    }
}
