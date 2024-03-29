/*
 * Copyright 2014-2020 chronicle.software
 *
 *       https://chronicle.software
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.openhft.chronicle.logger.logback;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import net.openhft.chronicle.core.OS;
import net.openhft.chronicle.core.util.Time;
import net.openhft.chronicle.logger.LogAppenderConfig;
import org.junit.Test;
import org.slf4j.LoggerFactory;

public class LogbackChronicleProgrammaticConfigTest extends LogbackTestBase {

    @Test
    public void testConfig() {
        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
        context.reset();

        ChronicleAppender appender = new ChronicleAppender();
        appender.setPath(OS.getTarget() + "/clog" + Time.uniqueId());
        appender.setChronicleConfig(new LogAppenderConfig());
        appender.setContext(context);
        appender.start();

        Logger logger = context.getLogger(Logger.ROOT_LOGGER_NAME);
        logger.addAppender(appender);

        logger.info("Hello World");
    }
}
