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

package net.openhft.chronicle.logger.logback;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;

public class LogbackVanillaChronicleConfigTest extends LogbackTestBase {

    @Before
    public void setup() {
        System.setProperty(
                "logback.configurationFile",
                System.getProperty("resources.path") + "/logback-vanilla-chronicle-config.xml"
        );
    }

    // *************************************************************************
    //
    // *************************************************************************

    @Test
    public void testBinaryVanillaChronicleAppenderConfig() throws IOException {
        final String loggerName = "config-binary-vanilla-chronicle";
        final String appenderName = "CONFIG-BINARY-VANILLA-CHRONICLE";

        try {
            final ch.qos.logback.classic.Logger logger = getLoggerContext().getLogger(loggerName);
            assertNotNull(logger);

            final Appender<ILoggingEvent> appender = logger.getAppender(appenderName);
            assertNotNull(appender);
            assertTrue(appender instanceof BinaryVanillaChronicleAppender);

            BinaryVanillaChronicleAppender ba = (BinaryVanillaChronicleAppender)appender;
            assertEquals(128, ba.getChronicleConfig().getDataCacheCapacity());
            assertEquals(256, ba.getChronicleConfig().getDataBlockSize());
            assertFalse(ba.getChronicleConfig().isUseCompressedObjectSerializer());
        } finally {
        }
    }

    @Test
    public void testTextVanillaChronicleAppenderConfig() throws IOException {
        final String loggerName = "config-text-vanilla-chronicle";
        final String appenderName = "CONFIG-TEXT-VANILLA-CHRONICLE";

        try {
            final ch.qos.logback.classic.Logger logger = getLoggerContext().getLogger(loggerName);
            assertNotNull(logger);

            final Appender<ILoggingEvent> appender =logger.getAppender(appenderName);
            assertNotNull(appender);
            assertTrue(appender instanceof TextVanillaChronicleAppender);

            TextVanillaChronicleAppender ba = (TextVanillaChronicleAppender)appender;
            assertEquals(128, ba.getChronicleConfig().getDataCacheCapacity());
            assertEquals(256, ba.getChronicleConfig().getDataBlockSize());
            assertFalse(ba.getChronicleConfig().isUseCompressedObjectSerializer());

            assertNotNull(ba.getDateFormat());
        } finally {
        }
    }
}
