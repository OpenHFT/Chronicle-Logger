/*
 * Copyright 2015 Higher Frequency Trading
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

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.ConsoleAppender;
import net.openhft.chronicle.logger.LogAppenderConfig;
import org.junit.Test;
import org.slf4j.LoggerFactory;

public class LogbackChronicleProgrammaticConfigTest extends LogbackTestBase {

    @Test
    public void testConfig()  {
        LoggerContext context=(LoggerContext) LoggerFactory.getILoggerFactory();
        context.reset();

        BinaryChronicleAppender appender = new BinaryChronicleAppender();
        appender.setPath(System.getProperty("java.io.tmpdir") +  "/clog");
        appender.setChronicleConfig(new LogAppenderConfig());
        appender.setContext(context);
        appender.start();

        ConsoleAppender<ILoggingEvent> console = new ConsoleAppender<>();
        PatternLayoutEncoder encoder = new PatternLayoutEncoder();
        encoder.setPattern("%d %contextName [%t] %level %logger{36} - %msg%n");
        console.setEncoder(encoder);
        console.setContext(context);
        console.start();

        Logger logger = context.getLogger(Logger.ROOT_LOGGER_NAME);
        logger.addAppender(appender);

        // this is a must to prevent recursion inside Chronicle Queue trying to log to Chronicle Logger
        Logger hft = context.getLogger("net.openhft");
        hft.setLevel(Level.WARN);
        hft.addAppender(console);
        hft.setAdditive(false);
        logger.info("Hello World");
    }
}
