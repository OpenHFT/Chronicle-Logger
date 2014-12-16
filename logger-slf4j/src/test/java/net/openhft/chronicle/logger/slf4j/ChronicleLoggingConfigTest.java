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

import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.*;

public class ChronicleLoggingConfigTest {
    @Test
    @Ignore("Fails in mvn run from IDEA")
    public void testLoadNoProperty() throws Exception {
        assertNull("config should not load if no system property set up", ChronicleLoggingConfig.load());
    }

    @Test
    public void testLoadFileVanilla() throws Exception {
        System.setProperty("slf4j.chronicle.properties", System.getProperty("slf4j.chronicle.vanilla.properties"));
        assertLoadsValidVanillaConfig();
    }

    @Test
    public void testLoadClasspathVanilla() throws Exception {
        System.setProperty("slf4j.chronicle.properties", "slf4j.chronicle.vanilla.properties");
        assertLoadsValidVanillaConfig();
    }

    private void assertLoadsValidVanillaConfig() {
        ChronicleLoggingConfig config = ChronicleLoggingConfig.load();
        assertNotNull("unable to load config", config);
        assertNotNull("is not a vanilla config", config.getVanillaChronicleConfig());
        assertEquals(ChronicleLoggingConfig.FORMAT_BINARY, config.getString(ChronicleLoggingConfig.KEY_FORMAT));
    }
}