/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.logger.slf4j;

import net.openhft.chronicle.core.io.IOTools;
import net.openhft.chronicle.logger.ChronicleLogLevel;
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

public class Slf4jChronicleLoggerTest extends Slf4jTestBase {

    @NotNull
    private static ChronicleQueue getChronicleQueue(String testId) {
        return ChronicleQueue.singleBuilder(basePath(testId)).build();
    }

    @BeforeEach
    public void setUp() {
        System.setProperty(
                "chronicle.logger.properties",
                "chronicle.logger.properties"
        );

        reloadChronicleLoggerFactory();
    }

    @AfterEach
    public void tearDown() {

        IOTools.deleteDirWithFiles(basePath());
    }

    @Test
    public void testLoggerFactory() {
        Object factory = getChronicleLoggerFactory();
        // Check that we got a ChronicleLoggerFactory (either slf4j or slf4j2 variant)
        String className = factory.getClass().getSimpleName();
        assertEquals("ChronicleLoggerFactory", className, "factory simple name");
    }

    @Test
    public void testLogger() {
        Logger l1 = LoggerFactory.getLogger("slf4j-chronicle");
        Logger l2 = LoggerFactory.getLogger("slf4j-chronicle");
        Logger l3 = LoggerFactory.getLogger("logger_1");

        assertNotNull(l1, "l1 logger");
        assertEquals("ChronicleLogger", l1.getClass().getSimpleName(), "l1 implementation: " + l1.getClass());

        assertNotNull(l2, "l2 logger");
        assertEquals("ChronicleLogger", l2.getClass().getSimpleName(), "l2 implementation: " + l2.getClass());

        assertNotNull(l3, "l3 logger");
        assertEquals("ChronicleLogger", l3.getClass().getSimpleName(), "l3 implementation: " + l3.getClass());

        Logger l4 = LoggerFactory.getLogger("readwrite");

        assertNotNull(l4, "l4 logger");
        assertEquals("ChronicleLogger", l4.getClass().getSimpleName(), "l4 implementation: " + l4.getClass());

        assertEquals(l1, l2, "same logger name should return cached instance");
        assertNotEquals(l1, l3, "different logger name should return different instance");
        assertNotEquals(l3, l4, "different logger name should return different instance");
        assertNotEquals(l1, l4, "different logger name should return different instance");

        // Note: Detailed assertions on Chronicle-specific methods (getLevel, getWriter, etc.)
        // are skipped here because they have different visibility in SLF4J 1.x vs 2.x.
        // The testLogging() method provides comprehensive verification of logging behavior.

        // Verify that loggers are enabled at appropriate levels via SLF4J API
        assertTrue(l1.isDebugEnabled(), "l1 debug enabled");
        assertTrue(l2.isDebugEnabled(), "l2 debug enabled");
        assertTrue(l3.isInfoEnabled(), "l3 info enabled");
        assertTrue(l4.isDebugEnabled(), "l4 debug enabled");

        // Verify logger names via SLF4J API
        assertEquals("slf4j-chronicle", l1.getName(), "l1 name");
        assertEquals("slf4j-chronicle", l2.getName(), "l2 name");
        assertEquals("logger_1", l3.getName(), "l3 name");
        assertEquals("readwrite", l4.getName(), "l4 name");
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
                        assertNotNull(wire, () -> "should have log entry in queue for level=" + level);
                        assertTrue(wire.read("ts").int64() <= currentTimeMillis(), () -> "log timestamp should not be in the future for level=" + level);
                        assertEquals(level, wire.read("level").asEnum(ChronicleLogLevel.class), () -> "serialized level should match logged level=" + level);
                        assertEquals(threadId, wire.read("threadName").text(), () -> "should capture thread name from logging context for level=" + level);
                        assertEquals(testId, wire.read("loggerName").text(), () -> "should capture logger name from logging context for level=" + level);
                        assertEquals("level is {}", wire.read("message").text(), () -> "should preserve original message template for level=" + level);
                        assertTrue(wire.hasMore(), () -> "should have arguments field when template contains placeholders for level=" + level);
                        List<Object> args = new ArrayList<>();
                        assertTrue(wire.hasMore(), () -> "should serialize placeholder arguments for level=" + level);
                        wire.read("args").sequence(args, (l, vi) -> {
                            while (vi.hasNextSequenceItem()) {
                                l.add(vi.object(Object.class));
                            }
                        });
                        assertEquals(level, args.iterator().next(), () -> "first argument should match logged parameter for level=" + level);
                        assertFalse(wire.hasMore(), () -> "should not have unexpected fields after standard log entry for level=" + level);
                    }
                }
            }
            try (DocumentContext dc = tailer.readingDocument()) {
                Wire wire = dc.wire();
                assertNull(wire, "queue should be fully consumed after reading all logged entries");
            }

            logger.debug("Throwable test 1", new UnsupportedOperationException());
            logger.debug("Throwable test 2", new UnsupportedOperationException("Exception message"));

            try (DocumentContext dc = tailer.readingDocument()) {
                Wire wire = dc.wire();
                assertNotNull(wire, "should serialize log entry with throwable parameter");
                assertTrue(wire.read("ts").int64() <= currentTimeMillis(), "log timestamp should not be in the future when exception logged");
                assertEquals(ChronicleLogLevel.DEBUG, wire.read("level").asEnum(ChronicleLogLevel.class), "should preserve debug level for exception logging");
                assertEquals(threadId, wire.read("threadName").text(), "should capture thread name when logging exception");
                assertEquals(testId, wire.read("loggerName").text(), "should capture logger name when logging exception");
                assertEquals("Throwable test 1", wire.read("message").text(), "should preserve message text when exception provided");
                assertTrue(wire.hasMore(), "should include throwable field when exception logged");
                Throwable throwable = wire.read("throwable").throwable(false);
                assertInstanceOf(UnsupportedOperationException.class, throwable, "should preserve exception type during serialization");
                assertFalse(wire.hasMore(), "should not have unexpected fields after exception log entry");
            }

            try (DocumentContext dc = tailer.readingDocument()) {
                Wire wire = dc.wire();
                assertNotNull(wire, "should serialize second exception log entry");
                assertTrue(wire.read("ts").int64() <= currentTimeMillis(), "log timestamp should not be in the future for second exception");
                assertEquals(ChronicleLogLevel.DEBUG, wire.read("level").asEnum(ChronicleLogLevel.class), "should preserve debug level for second exception");
                assertEquals(threadId, wire.read("threadName").text(), "should capture thread name for second exception");
                assertEquals(testId, wire.read("loggerName").text(), "should capture logger name for second exception");
                assertEquals("Throwable test 2", wire.read("message").text(), "should preserve distinct message for second exception");
                assertTrue(wire.hasMore(), "should include throwable field for second exception");
                Throwable throwable = wire.read("throwable").throwable(false);
                assertInstanceOf(UnsupportedOperationException.class, throwable, "should preserve exception type for second exception");
                assertEquals("Exception message", throwable.getMessage(), "should preserve exception message during serialization");
                assertFalse(wire.hasMore(), "should not have unexpected fields after second exception entry");
            }

            try (DocumentContext dc = tailer.readingDocument()) {
                Wire wire = dc.wire();
                assertNull(wire, "queue should be consumed after reading exception entries");
            }

            //noinspection LoggingPlaceholderCountMatchesArgumentCount
            logger.warn("Test object {}", new Object());

            try (DocumentContext dc = tailer.readingDocument()) {
                Wire wire = dc.wire();
                assertNotNull(wire, "should serialize warn-level log entry with object parameter");
                assertTrue(wire.read("ts").int64() <= currentTimeMillis(), "log timestamp should not be in the future for warn level");
                assertEquals(ChronicleLogLevel.WARN, wire.read("level").asEnum(ChronicleLogLevel.class), "should serialize warn level correctly");
                assertEquals(threadId, wire.read("threadName").text(), "should capture thread name for warn-level entry");
                assertEquals(testId, wire.read("loggerName").text(), "should capture logger name for warn-level entry");
                assertEquals("Test object {}", wire.read("message").text(), "should preserve message template with object placeholder");
                assertTrue(wire.hasMore(), "should include arguments when object parameter provided");
                List<Object> args = new ArrayList<>();
                assertTrue(wire.hasMore(), "should serialize object parameter as argument");
                wire.read("args").sequence(args, (l, vi) -> {
                    while (vi.hasNextSequenceItem()) {
                        l.add(vi.object(Object.class));
                    }
                });
                assertTrue(((String) args.iterator().next()).contains("java.lang.Object@"), "should serialize object using toString format");
                assertFalse(wire.hasMore(), "should not have unexpected fields after object parameter entry");
            }
        }
    }
}
