/*
 * Copyright 2014-2020 chronicle.software
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
package net.openhft.chronicle.logger.jul;

import net.openhft.chronicle.core.io.IOTools;
import net.openhft.chronicle.logger.ChronicleLogLevel;
import net.openhft.chronicle.logger.DefaultChronicleLogWriter;
import net.openhft.chronicle.queue.ChronicleQueue;
import net.openhft.chronicle.wire.DocumentContext;
import net.openhft.chronicle.wire.Wire;
import net.openhft.chronicle.wire.WireType;
import org.jetbrains.annotations.NotNull;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import static java.lang.System.currentTimeMillis;
import static org.junit.Assert.*;

public class JulLoggerChronicleTest extends JulLoggerTestBase {

    private static void testChronicleConfiguration(
            final String loggerId,
            final Class<? extends ChronicleLogger> expectedLoggerType,
            final Level level,
            final WireType wireType) {

        Logger logger = Logger.getLogger(loggerId);

        assertNotNull(logger);
        assertTrue(logger instanceof ChronicleLogger);
        assertEquals(expectedLoggerType, logger.getClass());
        assertEquals(loggerId, logger.getName());
        assertNotNull(((ChronicleLogger) logger).writer());
        assertEquals(level, logger.getLevel());
        assertEquals(wireType, ((DefaultChronicleLogWriter) ((ChronicleLogger) logger).writer()).getWireType());
    }

    @NotNull
    private static ChronicleQueue getChronicleQueue(String testId) {
        return ChronicleQueue.singleBuilder(basePath(testId)).build();

    }

    // *************************************************************************
    //
    // *************************************************************************

    @Before
    public void setUp() throws IOException {
        setupLogger(getClass());
        Files.createDirectories(Paths.get(basePath()));
    }

    @After
    public void tearDown() {
        IOTools.deleteDirWithFiles(basePath());
    }

    // *************************************************************************
    //
    // *************************************************************************

    @Test
    public void testChronicleConfig() {
        testChronicleConfiguration(
                "logger",
                ChronicleLogger.class,
                Level.FINE,
                WireType.BINARY_LIGHT);
        testChronicleConfiguration(
                "logger_1",
                ChronicleLogger.class,
                Level.INFO,
                WireType.JSON);
        testChronicleConfiguration(
                "logger_bin",
                ChronicleLogger.class,
                Level.FINER,
                WireType.BINARY_LIGHT);
    }

    @Test
    public void testAppender() {
        final String testId = "logger_bin";

        Logger logger = Logger.getLogger(testId);

        final String threadId = "thread-" + Thread.currentThread().getId();

        Thread.currentThread().setName(threadId);

        for (ChronicleLogLevel level : LOG_LEVELS) {
            log(logger, level, "level is {0}", level);
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
                    assertEquals("level is {0}", wire.read("message").text());
                    assertTrue(wire.hasMore());
                    List<Object> args = new ArrayList<>();
                    assertTrue(wire.hasMore());
                    wire.read("args").sequence(args, (l, vi) -> {
                        while (vi.hasNextSequenceItem()) {
                            l.add(vi.object(Object.class));
                        }
                    });
                    assertEquals(level, args.iterator().next());
                    assertFalse(wire.hasMore());
                }
            }
            try (DocumentContext dc = tailer.readingDocument()) {
                Wire wire = dc.wire();
                assertNull(wire);
            }

            logger.log(Level.FINE, "Throwable test 1", new UnsupportedOperationException());
            logger.log(Level.FINE, "Throwable test 2", new UnsupportedOperationException("Exception message"));

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
