/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.logger.slf4j;

import net.openhft.chronicle.core.io.IOTools;
import net.openhft.chronicle.logger.ChronicleLogLevel;
import net.openhft.chronicle.logger.DefaultChronicleLogWriter;
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
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static java.lang.System.currentTimeMillis;
import static org.junit.Assert.*;

public class Slf4jChronicleLoggerTest extends Slf4jTestBase {

    @NotNull
    private static ChronicleQueue getChronicleQueue(String testId) {
        return ChronicleQueue.singleBuilder(basePath(testId)).build();
    }

    @Before
    public void setUp() throws Exception {
        System.setProperty(
                "chronicle.logger.properties",
                "chronicle.logger.properties"
        );

        // Call reload() via reflection (works with both slf4j and slf4j2 factory)
        Object factory = getChronicleLoggerFactory();
        factory.getClass().getMethod("reload").invoke(factory);
    }

    @After
    public void tearDown() {

        IOTools.deleteDirWithFiles(basePath());
    }

    @Test
    public void testLoggerFactory() {
        Object factory = getChronicleLoggerFactory();
        // Check that we got a ChronicleLoggerFactory (either slf4j or slf4j2 variant)
        String className = factory.getClass().getSimpleName();
        assertEquals("ChronicleLoggerFactory", className);
    }

    @Test
    public void testLogger() throws Exception {
        Logger l1 = LoggerFactory.getLogger("slf4j-chronicle");
        Logger l2 = LoggerFactory.getLogger("slf4j-chronicle");
        Logger l3 = LoggerFactory.getLogger("logger_1");

        assertNotNull(l1);
        assertTrue("Expected ChronicleLogger but got " + l1.getClass(),
                   l1.getClass().getSimpleName().equals("ChronicleLogger"));

        assertNotNull(l2);
        assertTrue("Expected ChronicleLogger but got " + l2.getClass(),
                   l2.getClass().getSimpleName().equals("ChronicleLogger"));

        assertNotNull(l3);
        assertTrue("Expected ChronicleLogger but got " + l3.getClass(),
                   l3.getClass().getSimpleName().equals("ChronicleLogger"));

        Logger l4 = LoggerFactory.getLogger("readwrite");

        assertNotNull(l4);
        assertTrue("Expected ChronicleLogger but got " + l4.getClass(),
                   l4.getClass().getSimpleName().equals("ChronicleLogger"));

        assertEquals(l1, l2);
        assertNotEquals(l1, l3);
        assertNotEquals(l3, l4);
        assertNotEquals(l1, l4);

        // Note: Detailed assertions on Chronicle-specific methods (getLevel, getWriter, etc.)
        // are skipped here because they have different visibility in SLF4J 1.x vs 2.x.
        // The testLogging() method provides comprehensive verification of logging behavior.

        // Verify that loggers are enabled at appropriate levels via SLF4J API
        assertTrue("L1 should have debug enabled", l1.isDebugEnabled());
        assertTrue("L2 should have debug enabled", l2.isDebugEnabled());
        assertTrue("L3 should have info enabled", l3.isInfoEnabled());
        assertTrue("L4 should have debug enabled", l4.isDebugEnabled());

        // Verify logger names via SLF4J API
        assertEquals("slf4j-chronicle", l1.getName());
        assertEquals("slf4j-chronicle", l2.getName());
        assertEquals("logger_1", l3.getName());
        assertEquals("readwrite", l4.getName());
    }

    @Test
    public void testLogging() throws IOException {
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
                        assertNotNull("log not found for " + level, wire);
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
