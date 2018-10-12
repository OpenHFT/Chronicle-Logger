/*
 * Copyright 2014-2017 Chronicle Software
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
