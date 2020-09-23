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

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.core.status.OnConsoleStatusListener;
import net.openhft.chronicle.bytes.BytesStore;
import net.openhft.chronicle.logger.ChronicleLogWriter;
import net.openhft.chronicle.logger.DefaultChronicleLogWriter;
import net.openhft.chronicle.logger.LogAppenderConfig;
import net.openhft.chronicle.queue.ChronicleQueue;
import net.openhft.chronicle.wire.Wire;
import net.openhft.chronicle.wire.WireType;
import org.junit.Test;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicReference;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.Assert.*;

public class LogbackChronicleProgrammaticConfigTest {

    @Test
    public void testConfig() {
        LoggerContext context = new LoggerContext();
        final AtomicReference<byte[]> expected = new AtomicReference<>();
        ChronicleLogWriter mockWriter = new StubWriter() {
            @Override
            public void write(Instant timestamp, int level, String threadName, String loggerName, byte[] entry, String contentType, String contentEncoding) {
                expected.set(entry);
            }
        };
        ChronicleAppender appender = new ChronicleAppender() {
            @Override
            protected ChronicleLogWriter createWriter() throws IOException {
                ChronicleQueue cq = this.config.build(this.getPath(), getWireType());
                return mockWriter;
            }
        };
        appender.setPath(System.getProperty("java.io.tmpdir") + "/clog");
        appender.setChronicleConfig(new LogAppenderConfig());
        appender.setContext(context);
        PatternLayoutEncoder encoder = new PatternLayoutEncoder();
        encoder.setContext(context);
        encoder.setPattern("%msg IN BED");
        encoder.start();
        appender.setEncoder(encoder);
        appender.start();

        Logger logger = context.getLogger(Logger.ROOT_LOGGER_NAME);
        logger.addAppender(appender);

        logger.info("Hello World");
        String actual = new String(expected.get(), UTF_8);
        assertEquals("Hello World IN BED", actual);
    }

    abstract static class StubWriter implements ChronicleLogWriter {

        @Override
        public void close() throws IOException {
        }

        public void write(
                final Instant timestamp,
                final int level,
                final String loggerName,
                final String threadName,
                final byte[] entry) {
            throw new UnsupportedOperationException("Do not want");
        }

        @Override
        public abstract void write(Instant timestamp,
                                   int level,
                                   String loggerName,
                                   String threadName,
                                   byte[] entry,
                                   String contentType,
                                   String contentEncoding);
    }
}
