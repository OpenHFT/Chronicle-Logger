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
package net.openhft.chronicle.logger.log4j2;

import net.openhft.chronicle.core.OS;
import net.openhft.chronicle.core.io.IOTools;
import net.openhft.chronicle.queue.ChronicleQueue;
import net.openhft.chronicle.wire.DocumentContext;
import net.openhft.chronicle.wire.Wire;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.junit.After;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

import static org.junit.Assert.*;

public class Log4j2BinaryTest extends Log4j2TestBase {

    @NotNull
    private static ChronicleQueue getChronicleQueue(String testId) {
        return ChronicleQueue.singleBuilder(basePath(testId)).build();
    }

    @After
    public void tearDown() {
        IOTools.deleteDirWithFiles(rootPath());
    }

    // *************************************************************************
    //
    // *************************************************************************

    @Test
    public void testConfig() {
        // needs to be initialised before trying to get the appender, otherwise we end up in a loop
        final Logger logger = LogManager.getLogger(OS.class);
        final String appenderName = "CONF-CHRONICLE";

        final org.apache.logging.log4j.core.Appender appender = getAppender(appenderName);

        assertNotNull(appender);
        assertEquals(appenderName, appender.getName());
        assertTrue(appender instanceof ChronicleAppender);

        final ChronicleAppender ba = (ChronicleAppender) appender;
        assertEquals(128, ba.getChronicleConfig().getBlockSize());
        assertEquals(256, ba.getChronicleConfig().getBufferCapacity());
    }

    @Test
    public void testIndexedAppender() throws IOException {
        final String testId = "chronicle";
        final String threadId = testId + "-th";
        final Logger logger = LogManager.getLogger(testId);

        Thread.currentThread().setName(threadId);
        Files.createDirectories(Paths.get(basePath(testId)));

        Level[] values = { Level.TRACE, Level.DEBUG, Level.INFO, Level.WARN, Level.ERROR, Level.FATAL };
        for (Level level : values) {
            log(logger, level, "level is {}", level);
        }

        try (final ChronicleQueue cq = getChronicleQueue(testId)) {
            net.openhft.chronicle.queue.ExcerptTailer tailer = cq.createTailer();
            for (Level level : values) {
                try (DocumentContext dc = tailer.readingDocument()) {
                    Wire wire = dc.wire();
                    ZonedDateTime now = Instant.now().atZone(ZoneOffset.UTC);
                    ZonedDateTime instant = wire.read("instant").zonedDateTime();
                    assertTrue(instant.isBefore(now));
                    assertEquals(level.intLevel(), wire.read("level").int32());
                    assertEquals(threadId, wire.read("threadName").text());
                    assertEquals(testId, wire.read("loggerName").text());
                    assertEquals(level + " level is " + level, wire.read("entry").text());
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
                ZonedDateTime instant = wire.read("instant").zonedDateTime();
                assertTrue(instant.isBefore(now));
                assertEquals(Level.DEBUG.intLevel(), wire.read("level").int32());
                assertEquals(threadId, wire.read("threadName").text());
                assertEquals(testId, wire.read("loggerName").text());
                assertTrue(wire.read("entry").text().startsWith("DEBUG Throwable test 1 java.lang.UnsupportedOperationException: null"));
            }

            try (DocumentContext dc = tailer.readingDocument()) {
                Wire wire = dc.wire();
                assertNotNull(wire);
                ZonedDateTime now = Instant.now().atZone(ZoneOffset.UTC);
                ZonedDateTime instant = wire.read("instant").zonedDateTime();
                assertTrue(instant.isBefore(now));
                assertEquals(Level.DEBUG.intLevel(), wire.read("level").int32());
                assertEquals(threadId, wire.read("threadName").text());
                assertEquals(testId, wire.read("loggerName").text());
                assertTrue(wire.read("entry").text().startsWith("DEBUG Throwable test 2 java.lang.UnsupportedOperationException: Exception message"));
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
