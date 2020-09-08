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
package net.openhft.chronicle.logger.log4j1;

import net.openhft.chronicle.core.io.IOTools;
import net.openhft.chronicle.queue.ChronicleQueue;
import net.openhft.chronicle.wire.DocumentContext;
import net.openhft.chronicle.wire.Wire;
import net.openhft.chronicle.wire.WireType;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

import static org.apache.log4j.Level.*;
import static org.junit.Assert.*;

public class Log4j1ChronicleLogTest extends Log4j1TestBase {

    static final Level[] levels = { FATAL, ERROR, WARN, INFO, DEBUG, TRACE };

    private static ChronicleQueue getChronicleQueue(String testId, WireType wt) {
        return ChronicleQueue.singleBuilder(basePath(testId)).wireType(wt).build();
    }

    @After
    public void tearDown() {
        IOTools.deleteDirWithFiles(rootPath());
    }

    @Test
    public void testBinaryAppender() throws IOException {
        final String testId = "chronicle";
        final String threadId = testId + "-th";
        final Logger logger = LogManager.getLogger(testId);
        Files.createDirectories(Paths.get(basePath(testId)));
        Thread.currentThread().setName(threadId);

        for (Level level: levels) {
            log(logger, level, "level is " + level, null);
        }

        try (final ChronicleQueue cq = getChronicleQueue(testId, WireType.BINARY_LIGHT)) {
            net.openhft.chronicle.queue.ExcerptTailer tailer = cq.createTailer();
            for (Level level: levels) {
                try (DocumentContext dc = tailer.readingDocument()) {
                    Wire wire = dc.wire();
                    assertNotNull("log not found for " + level, wire);
                    ZonedDateTime now = Instant.now().atZone(ZoneOffset.UTC);
                    assertTrue(wire.read("instant").zonedDateTime().isBefore(now));
                    assertEquals(level.toInt(), wire.read("level").int32());
                    assertEquals(threadId, wire.read("threadName").text());
                    assertEquals(testId, wire.read("loggerName").text());
                    assertTrue(wire.read("entry").text().contains("level is " + level));
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
                ZonedDateTime now = Instant.now().atZone(ZoneOffset.UTC);
                assertTrue(wire.read("instant").zonedDateTime().isBefore(now));
                assertEquals(DEBUG.toInt(), wire.read("level").int32());
                assertEquals(threadId, wire.read("threadName").text());
                assertEquals(testId, wire.read("loggerName").text());
                String entry = wire.read("entry").text();
                assertTrue(entry.contains("Throwable test 1 java.lang.UnsupportedOperationException"));
            }

            try (DocumentContext dc = tailer.readingDocument()) {
                Wire wire = dc.wire();
                assertNotNull(wire);
                ZonedDateTime now = Instant.now().atZone(ZoneOffset.UTC);
                assertTrue(wire.read("instant").zonedDateTime().isBefore(now));
                assertEquals(DEBUG.toInt(), wire.read("level").int32());
                assertEquals(threadId, wire.read("threadName").text());
                assertEquals(testId, wire.read("loggerName").text());
                String entry = wire.read("entry").text();
                assertTrue(entry.contains("Throwable test 2 java.lang.UnsupportedOperationException: Exception message\n"));
                assertFalse(wire.hasMore());
            }

            try (DocumentContext dc = tailer.readingDocument()) {
                Wire wire = dc.wire();
                assertNull(wire);
            }
        }
        IOTools.deleteDirWithFiles(basePath(testId));
    }

    @Test
    @Ignore
    public void testJsonAppender() throws IOException {
        final String testId = "json-chronicle";
        final String threadId = testId + "-th";
        final Logger logger = LogManager.getLogger(testId);

        Thread.currentThread().setName(threadId);

        for (Level level: levels) {
            log(logger, level, "level is " + level, null);
        }

        try (final ChronicleQueue cq = getChronicleQueue(testId, WireType.TEXT)) {
            net.openhft.chronicle.queue.ExcerptTailer tailer = cq.createTailer();
            for (Level level: levels) {
                try (DocumentContext dc = tailer.readingDocument()) {
                    Wire wire = dc.wire();
                    assertNotNull("log not found for " + level, wire);
                    ZonedDateTime now = Instant.now().atZone(ZoneOffset.UTC);
                    assertTrue(wire.read("instant").zonedDateTime().isBefore(now));
                    assertEquals(DEBUG.toInt(), wire.read("level").int32());
                    assertEquals(threadId, wire.read("threadName").text());
                    assertEquals(testId, wire.read("loggerName").text());
                    assertEquals("level is " + level, wire.read("entry").text());
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
                ZonedDateTime now = Instant.now().atZone(ZoneOffset.UTC);
                assertTrue(wire.read("instant").zonedDateTime().isBefore(now));
                assertEquals(DEBUG.toInt(), wire.read("level").int32());
                assertEquals(threadId, wire.read("threadName").text());
                assertEquals(testId, wire.read("loggerName").text());
                assertEquals("Throwable test 1", wire.read("entry").text());
                assertTrue(wire.hasMore());
                assertTrue(wire.read("throwable").throwable(false) instanceof UnsupportedOperationException);
                assertFalse(wire.hasMore());
            }

            try (DocumentContext dc = tailer.readingDocument()) {
                Wire wire = dc.wire();
                assertNotNull(wire);
                ZonedDateTime now = Instant.now().atZone(ZoneOffset.UTC);
                assertTrue(wire.read("instant").zonedDateTime().isBefore(now));
                assertEquals(DEBUG.toInt(), wire.read("level").int32());
                assertEquals(threadId, wire.read("threadName").text());
                assertEquals(testId, wire.read("loggerName").text());
                assertEquals("Throwable test 2", wire.read("entry").text());
                assertTrue(wire.hasMore());
                Throwable throwable = wire.read("throwable").throwable(false);
                assertTrue(throwable instanceof UnsupportedOperationException);
                assertEquals("Exception message", throwable.getMessage());
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
