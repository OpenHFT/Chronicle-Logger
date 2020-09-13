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

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;
import net.openhft.chronicle.core.io.IOTools;
import net.openhft.chronicle.queue.ChronicleQueue;
import net.openhft.chronicle.wire.DocumentContext;
import net.openhft.chronicle.wire.Wire;
import org.jetbrains.annotations.NotNull;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.ZoneOffset;

import static ch.qos.logback.classic.Level.*;
import static org.junit.Assert.*;

public class LogbackChronicleBinaryAppenderTest extends LogbackTestBase {

    @Override
    String getResource() {
        return "/logback-chronicle-binary-appender.xml";
    }

    @Test
    public void testBinaryAppender() throws IOException {
        final String testId = "binary-chronicle";
        final String threadId = testId + "-th";

        final Logger logger = getLoggerContext().getLogger(testId);
        Files.createDirectories(Paths.get(basePath(testId)));

        Thread.currentThread().setName(threadId);

        Level[] levels = { ERROR, WARN, INFO, DEBUG, TRACE };
        for (Level level : levels) {
            log(logger, level, "level is {}", level);
        }

        try (final ChronicleQueue cq = getChronicleQueue(testId)) {
            net.openhft.chronicle.queue.ExcerptTailer tailer = cq.createTailer();
            for (Level level: levels) {
                try (DocumentContext dc = tailer.readingDocument()) {
                    Wire wire = dc.wire();
                    assertNotNull("log not found for " + level, wire);
                    assertTrue(wire.read("instant").zonedDateTime().isBefore(Instant.now().atZone(ZoneOffset.UTC)));
                    assertEquals(level.toInt(), wire.read("level").int32());
                    assertEquals(threadId, wire.read("threadName").text());
                    assertEquals(testId, wire.read("loggerName").text());
                    assertEquals("level is " + level.levelStr, wire.read("entry").text());
                    assertNull(wire.read("type").text());
                    assertNull(wire.read("encoding").text());
                    assertFalse(wire.hasMore());
                }
            }
            try (DocumentContext dc = tailer.readingDocument()) {
                Wire wire = dc.wire();
                assertNull(wire);
            }

            logger.debug("Throwable test 1", new UnsupportedOperationException());
            logger.debug("Throwable test 2", new UnsupportedOperationException("Exception message"));

            try (DocumentContext dc = tailer.readingDocument()) {
                Wire wire = dc.wire();
                assertNotNull(wire);
                assertTrue(wire.read("instant").zonedDateTime().isBefore(Instant.now().atZone(ZoneOffset.UTC)));
                assertEquals(Level.DEBUG.toInt(), wire.read("level").int32());
                assertEquals(threadId, wire.read("threadName").text());
                assertEquals(testId, wire.read("loggerName").text());
                String entry = wire.read("entry").text();
                assertTrue(entry.startsWith("Throwable test 1"));
                assertTrue(entry.contains("UnsupportedOperationException"));
                // java.lang.UnsupportedOperationException: null
                assertNull(wire.read("type").text());
                assertNull(wire.read("encoding").text());
                assertFalse(wire.hasMore());
            }

            try (DocumentContext dc = tailer.readingDocument()) {
                Wire wire = dc.wire();
                assertNotNull(wire);
                assertTrue(wire.read("instant").zonedDateTime().isBefore(Instant.now().atZone(ZoneOffset.UTC)));
                assertEquals(Level.DEBUG.toInt(), wire.read("level").int32());
                assertEquals(threadId, wire.read("threadName").text());
                assertEquals(testId, wire.read("loggerName").text());
                String entry = wire.read("entry").text();
                assertTrue(entry.startsWith("Throwable test 2"));
                assertTrue(entry.contains("UnsupportedOperationException: Exception message"));
                assertNull(wire.read("type").text());
                assertNull(wire.read("encoding").text());
                assertFalse(wire.hasMore());
            }

            try (DocumentContext dc = tailer.readingDocument()) {
                Wire wire = dc.wire();
                assertNull(wire);
            }
        }
        IOTools.deleteDirWithFiles(basePath(testId));
    }
}
