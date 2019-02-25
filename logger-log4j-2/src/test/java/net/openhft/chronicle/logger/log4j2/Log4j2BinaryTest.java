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
import net.openhft.chronicle.logger.ChronicleLogLevel;
import net.openhft.chronicle.queue.ChronicleQueue;
import net.openhft.chronicle.wire.DocumentContext;
import net.openhft.chronicle.wire.Wire;
import org.jetbrains.annotations.NotNull;
import org.junit.After;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static java.lang.System.currentTimeMillis;
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
        final Logger logger = LoggerFactory.getLogger(OS.class);
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
        final Logger logger = LoggerFactory.getLogger(testId);

        Thread.currentThread().setName(threadId);
        Files.createDirectories(Paths.get(basePath(testId)));

        for (ChronicleLogLevel level : LOG_LEVELS) {
            log(logger, level, "level is {}", level);
        }

        try (final ChronicleQueue cq = getChronicleQueue(testId)) {
            net.openhft.chronicle.queue.ExcerptTailer tailer = cq.createTailer();
            for (ChronicleLogLevel level : LOG_LEVELS) {
                try (DocumentContext dc = tailer.readingDocument()) {
                    Wire wire = dc.wire();
                    assertNotNull("log not found for " + level, wire);
                    assertTrue(wire.read("ts").int64() <= currentTimeMillis());
                    assertEquals(level, wire.read("level").asEnum(ChronicleLogLevel.class));
                    assertEquals(threadId, wire.read("threadName").text());
                    assertEquals(testId, wire.read("loggerName").text());
                    assertEquals("level is " + level, wire.read("message").text());
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
                assertTrue(wire.read("ts").int64() <= currentTimeMillis());
                assertEquals(ChronicleLogLevel.DEBUG, wire.read("level").asEnum(ChronicleLogLevel.class));
                assertEquals(threadId, wire.read("threadName").text());
                assertEquals(testId, wire.read("loggerName").text());
                assertEquals("Throwable test 1", wire.read("message").text());
                assertTrue(wire.hasMore());
                assertTrue(wire.read("throwable").throwable(false) instanceof UnsupportedOperationException);
                assertFalse(wire.hasMore());
            }

            try (DocumentContext dc = tailer.readingDocument()) {
                Wire wire = dc.wire();
                assertNotNull(wire);
                assertTrue(wire.read("ts").int64() <= currentTimeMillis());
                assertEquals(ChronicleLogLevel.DEBUG, wire.read("level").asEnum(ChronicleLogLevel.class));
                assertEquals(threadId, wire.read("threadName").text());
                assertEquals(testId, wire.read("loggerName").text());
                assertEquals("Throwable test 2", wire.read("message").text());
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
