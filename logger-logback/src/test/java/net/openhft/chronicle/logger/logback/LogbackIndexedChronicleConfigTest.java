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
package net.openhft.chronicle.logger.logback;

import ch.qos.logback.classic.spi.ILoggingEvent;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.*;

public class LogbackIndexedChronicleConfigTest extends LogbackTestBase {

    @Override
    String getResource() {
        return "/logback-chronicle-config.xml";
    }

    // *************************************************************************
    //
    // *************************************************************************

    @Test
    public void testBinaryIndexedChronicleAppenderConfig() throws IOException {
        final String loggerName = "config-binary-chronicle";
        final String appenderName = "CONFIG-BINARY-CHRONICLE";

        final ch.qos.logback.classic.Logger logger = getLoggerContext().getLogger(loggerName);
        assertNotNull(logger);

        final ch.qos.logback.core.Appender<ILoggingEvent> appender = logger.getAppender(appenderName);
        assertNotNull(appender);
        assertTrue(appender instanceof ChronicleAppender);

        ChronicleAppender ba = (ChronicleAppender) appender;
        assertEquals(128, ba.getChronicleConfig().getBlockSize());
    }
}
