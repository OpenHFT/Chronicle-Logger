/*
 * Copyright 2013-2026 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.logger.slf4j2;

import net.openhft.chronicle.core.io.IOTools;
import net.openhft.chronicle.logger.ChronicleLogLevel;
import net.openhft.chronicle.logger.DefaultChronicleLogWriter;
import net.openhft.chronicle.queue.ChronicleQueue;
import net.openhft.chronicle.wire.DocumentContext;
import net.openhft.chronicle.wire.Wire;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static java.lang.System.currentTimeMillis;
import static org.junit.jupiter.api.Assertions.*;

class Slf4jChronicleLoggerTest extends Slf4jTestBase {

    @NotNull
    private static ChronicleQueue getChronicleQueue(String testId) {
        return ChronicleQueue.singleBuilder(basePath(testId)).build();
    }

    @BeforeEach
    void setUp() {
        System.setProperty(
                "chronicle.logger.properties",
                "chronicle.logger.properties"
        );

        getChronicleLoggerFactory().reload();
    }

    @AfterEach
    void tearDown() {

        IOTools.deleteDirWithFiles(basePath());
    }

    @Test
    void testLoggerFactory() {
        assertSame(getChronicleLoggerFactory().getClass(), ChronicleLoggerFactory.class);
    }

    @Test
    void testLogger() {
        Logger l1 = LoggerFactory.getLogger("slf4j-chronicle");
        Logger l2 = LoggerFactory.getLogger("slf4j-chronicle");
        Logger l3 = LoggerFactory.getLogger("logger_1");

        assertNotNull(l1);
        assertTrue(l1 instanceof ChronicleLogger);

        assertNotNull(l2);
        assertTrue(l2 instanceof ChronicleLogger);

        assertNotNull(l3);
        assertTrue(l3 instanceof ChronicleLogger);

        Logger l4 = LoggerFactory.getLogger("readwrite");

        assertNotNull(l4);
        assertTrue(l4 instanceof ChronicleLogger);

        assertEquals(l1, l2);
        assertNotEquals(l1, l3);
        assertNotEquals(l3, l4);
        assertNotEquals(l1, l4);

        ChronicleLogger cl1 = (ChronicleLogger) l1;

        assertEquals(ChronicleLogLevel.DEBUG, cl1.getLevel());
        assertEquals("slf4j-chronicle", cl1.getName());
        assertTrue(cl1.getWriter() instanceof DefaultChronicleLogWriter);

        ChronicleLogger cl2 = (ChronicleLogger) l2;
        assertEquals(ChronicleLogLevel.DEBUG, cl2.getLevel());
        assertEquals("slf4j-chronicle", cl2.getName());
        assertTrue(cl2.getWriter() instanceof DefaultChronicleLogWriter);

        ChronicleLogger cl3 = (ChronicleLogger) l3;
        assertEquals(ChronicleLogLevel.INFO, cl3.getLevel());
        assertTrue(cl3.getWriter() instanceof DefaultChronicleLogWriter);
        assertEquals("logger_1", cl3.getName());

        ChronicleLogger cl4 = (ChronicleLogger) l4;
        assertEquals(ChronicleLogLevel.DEBUG, cl4.getLevel());
        assertTrue(cl4.getWriter() instanceof DefaultChronicleLogWriter);
        assertEquals("readwrite", cl4.getName());
    }

    @Test
    void testLogging() throws IOException {
        final String testId = "readwrite";
        final String threadId = testId + "-th";
        final Logger logger = LoggerFactory.getLogger(testId);

        IOTools.deleteDirWithFiles(basePath(testId));
        Files.createDirectories(Paths.get(basePath(testId)));

        Thread.currentThread().setName(threadId);

        for (ChronicleLogLevel level : LOG_LEVELS) {
            log(logger, level, "level is {}", level);
        }

        try (final ChronicleQueue cq = getChronicleQueue(testId)) {
            net.openhft.chronicle.queue.ExcerptTailer tailer = cq.createTailer();
            for (ChronicleLogLevel level : LOG_LEVELS) {
                // logger configured to debug
                if (level.isHigherOrEqualTo(ChronicleLogLevel.DEBUG)) {
                    try (DocumentContext dc = tailer.readingDocument()) {
                        Wire wire = dc.wire();
                        assertNotNull(wire, "log not found for " + level);
                        assertTrue(wire.read("ts").int64() <= currentTimeMillis());
                        assertEquals(level, wire.read("level").asEnum(ChronicleLogLevel.class));
                        assertEquals(threadId, wire.read("threadName").text());
                        assertEquals(testId, wire.read("loggerName").text());
                        assertEquals("level is {}", wire.read("message").text());
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

            //noinspection LoggingPlaceholderCountMatchesArgumentCount
            logger.warn("Test object", new Object());

            try (DocumentContext dc = tailer.readingDocument()) {
                Wire wire = dc.wire();
                assertNotNull(wire);
                assertTrue(wire.read("ts").int64() <= currentTimeMillis());
                assertEquals(ChronicleLogLevel.WARN, wire.read("level").asEnum(ChronicleLogLevel.class));
                assertEquals(threadId, wire.read("threadName").text());
                assertEquals(testId, wire.read("loggerName").text());
                assertEquals("Test object", wire.read("message").text());
                assertTrue(wire.hasMore());
                List<Object> args = new ArrayList<>();
                assertTrue(wire.hasMore());
                wire.read("args").sequence(args, (l, vi) -> {
                    while (vi.hasNextSequenceItem()) {
                        l.add(vi.object(Object.class));
                    }
                });
                assertTrue(((String) args.iterator().next()).contains("java.lang.Object@"));
                assertFalse(wire.hasMore());
            }
        }
    }
}
