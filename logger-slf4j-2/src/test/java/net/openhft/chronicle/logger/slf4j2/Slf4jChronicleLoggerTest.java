/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
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

        getChronicleLoggerFactory().reload();
    }

    @AfterEach
    public void tearDown() {

        IOTools.deleteDirWithFiles(basePath());
    }

    @Test
    public void testLoggerFactory() {
        assertEquals(
                ChronicleLoggerFactory.class,
                getChronicleLoggerFactory().getClass(),
                "SLF4J should return ChronicleLoggerFactory implementation for Chronicle Logger binding");
    }

    @Test
    public void testLogger() {
        Logger l1 = LoggerFactory.getLogger("slf4j-chronicle");
        Logger l2 = LoggerFactory.getLogger("slf4j-chronicle");
        Logger l3 = LoggerFactory.getLogger("logger_1");

        assertNotNull(l1, "Logger for 'slf4j-chronicle' should not be null");
        assertInstanceOf(ChronicleLogger.class, l1, "Logger for 'slf4j-chronicle' should be ChronicleLogger implementation");

        assertNotNull(l2, "Second logger instance for 'slf4j-chronicle' should not be null");
        assertInstanceOf(ChronicleLogger.class, l2, "Second logger instance for 'slf4j-chronicle' should be ChronicleLogger implementation");

        assertNotNull(l3, "Logger for 'logger_1' should not be null");
        assertInstanceOf(ChronicleLogger.class, l3, "Logger for 'logger_1' should be ChronicleLogger implementation");

        Logger l4 = LoggerFactory.getLogger("readwrite");

        assertNotNull(l4, "Logger for 'readwrite' should not be null");
        assertInstanceOf(ChronicleLogger.class, l4, "Logger for 'readwrite' should be ChronicleLogger implementation");

        assertEquals(l1, l2, "Multiple getLogger calls with same name 'slf4j-chronicle' should return cached instance");
        assertNotEquals(l1, l3, "Loggers with different names 'slf4j-chronicle' and 'logger_1' should be different instances");
        assertNotEquals(l3, l4, "Loggers with different names 'logger_1' and 'readwrite' should be different instances");
        assertNotEquals(l1, l4, "Loggers with different names 'slf4j-chronicle' and 'readwrite' should be different instances");

        ChronicleLogger cl1 = (ChronicleLogger) l1;

        assertEquals(ChronicleLogLevel.DEBUG, cl1.getLevel(), "ChronicleLogger 'slf4j-chronicle' should have DEBUG level configured");
        assertEquals("slf4j-chronicle", cl1.getName(), "ChronicleLogger name should match requested logger name 'slf4j-chronicle'");
        assertInstanceOf(DefaultChronicleLogWriter.class, cl1.getWriter(), "ChronicleLogger 'slf4j-chronicle' should use DefaultChronicleLogWriter");

        ChronicleLogger cl2 = (ChronicleLogger) l2;
        assertEquals(ChronicleLogLevel.DEBUG, cl2.getLevel(), "Cached ChronicleLogger 'slf4j-chronicle' should maintain DEBUG level");
        assertEquals("slf4j-chronicle", cl2.getName(), "Cached ChronicleLogger should maintain original name 'slf4j-chronicle'");
        assertInstanceOf(DefaultChronicleLogWriter.class, cl2.getWriter(), "Cached ChronicleLogger 'slf4j-chronicle' should maintain DefaultChronicleLogWriter");

        ChronicleLogger cl3 = (ChronicleLogger) l3;
        assertEquals(ChronicleLogLevel.INFO, cl3.getLevel(), "ChronicleLogger 'logger_1' should have INFO level configured");
        assertInstanceOf(DefaultChronicleLogWriter.class, cl3.getWriter(), "ChronicleLogger 'logger_1' should use DefaultChronicleLogWriter");
        assertEquals("logger_1", cl3.getName(), "ChronicleLogger name should match requested logger name 'logger_1'");

        ChronicleLogger cl4 = (ChronicleLogger) l4;
        assertEquals(ChronicleLogLevel.DEBUG, cl4.getLevel(), "ChronicleLogger 'readwrite' should have DEBUG level configured");
        assertInstanceOf(DefaultChronicleLogWriter.class, cl4.getWriter(), "ChronicleLogger 'readwrite' should use DefaultChronicleLogWriter");
        assertEquals("readwrite", cl4.getName(), "ChronicleLogger name should match requested logger name 'readwrite'");
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
                        assertNotNull(wire, () -> "Chronicle Queue should contain serialised log entry for " + level + " level");
                        assertTrue(wire.read("ts").int64() <= currentTimeMillis(), () -> "Timestamp in log entry should be valid (not future) for " + level + " level");
                        assertEquals(level, wire.read("level").asEnum(ChronicleLogLevel.class), () -> "Serialised log level should match logged " + level + " level");
                        assertEquals(threadId, wire.read("threadName").text(), () -> "Thread name in serialised log should match '" + threadId + "' for " + level + " level");
                        assertEquals(testId, wire.read("loggerName").text(), () -> "Logger name in serialised log should match '" + testId + "' for " + level + " level");
                        assertEquals("level is {}", wire.read("message").text(), () -> "Message template in serialised log should be 'level is {}' for " + level + " level");
                        assertTrue(wire.hasMore(), () -> "Serialised log should contain args section for parameterised message at " + level + " level");
                        List<Object> args = new ArrayList<>();
                        assertTrue(wire.hasMore(), () -> "Args field should be present in serialised log for " + level + " level");
                        wire.read("args").sequence(args, (l, vi) -> {
                            while (vi.hasNextSequenceItem()) {
                                l.add(vi.object(Object.class));
                            }
                        });
                        assertEquals(level, args.iterator().next(), () -> "First argument in serialised log should be " + level + " value");
                        assertFalse(wire.hasMore(), () -> "Serialised log should not have unexpected trailing fields for " + level + " level");
                    }
                }
            }
            try (DocumentContext dc = tailer.readingDocument()) {
                Wire wire = dc.wire();
                assertNull(wire, "Chronicle Queue tailer should return null after all log levels have been read");
            }

            logger.debug("Throwable test 1", new UnsupportedOperationException());
            logger.debug("Throwable test 2", new UnsupportedOperationException("Exception message"));

            try (DocumentContext dc = tailer.readingDocument()) {
                Wire wire = dc.wire();
                assertNotNull(wire, "Chronicle Queue should contain serialised log entry for throwable without exception message");
                assertTrue(wire.read("ts").int64() <= currentTimeMillis(), "Timestamp for throwable log entry should be valid (not future)");
                assertEquals(ChronicleLogLevel.DEBUG, wire.read("level").asEnum(ChronicleLogLevel.class), "Log level for throwable entry should be DEBUG");
                assertEquals(threadId, wire.read("threadName").text(), "Thread name for throwable entry should match '" + threadId + "'");
                assertEquals(testId, wire.read("loggerName").text(), "Logger name for throwable entry should match '" + testId + "'");
                assertEquals("Throwable test 1", wire.read("message").text(), "Message text for throwable entry should be 'Throwable test 1'");
                assertTrue(wire.hasMore(), "Serialised log entry should contain throwable field when exception is logged");
                Throwable throwable = wire.read("throwable").throwable(false);
                assertInstanceOf(UnsupportedOperationException.class, throwable, "Deserialised throwable should be UnsupportedOperationException");
                assertFalse(wire.hasMore(), "Throwable log entry should not have unexpected trailing fields");
            }

            try (DocumentContext dc = tailer.readingDocument()) {
                Wire wire = dc.wire();
                assertNotNull(wire, "Chronicle Queue should contain serialised log entry for throwable with exception message");
                assertTrue(wire.read("ts").int64() <= currentTimeMillis(), "Timestamp for throwable with message should be valid (not future)");
                assertEquals(ChronicleLogLevel.DEBUG, wire.read("level").asEnum(ChronicleLogLevel.class), "Log level for throwable with message should be DEBUG");
                assertEquals(threadId, wire.read("threadName").text(), "Thread name for throwable with message should match '" + threadId + "'");
                assertEquals(testId, wire.read("loggerName").text(), "Logger name for throwable with message should match '" + testId + "'");
                assertEquals("Throwable test 2", wire.read("message").text(), "Message text for throwable with message should be 'Throwable test 2'");
                assertTrue(wire.hasMore(), "Serialised log entry with exception message should contain throwable field");
                Throwable throwable = wire.read("throwable").throwable(false);
                assertInstanceOf(UnsupportedOperationException.class, throwable, "Deserialised throwable with message should be UnsupportedOperationException");
                assertEquals("Exception message", throwable.getMessage(), "Deserialised throwable should preserve original exception message 'Exception message'");
                assertFalse(wire.hasMore(), "Throwable with message log entry should not have unexpected trailing fields");
            }

            try (DocumentContext dc = tailer.readingDocument()) {
                Wire wire = dc.wire();
                assertNull(wire, "Chronicle Queue tailer should return null after all throwable test entries have been read");
            }

            //noinspection LoggingPlaceholderCountMatchesArgumentCount
            logger.warn("Test object {}", new Object());

            try (DocumentContext dc = tailer.readingDocument()) {
                Wire wire = dc.wire();
                assertNotNull(wire, "Chronicle Queue should contain serialised WARN log entry with Object argument");
                assertTrue(wire.read("ts").int64() <= currentTimeMillis(), "Timestamp for WARN object log entry should be valid (not future)");
                assertEquals(ChronicleLogLevel.WARN, wire.read("level").asEnum(ChronicleLogLevel.class), "Log level for object test should be WARN");
                assertEquals(threadId, wire.read("threadName").text(), "Thread name for WARN object entry should match '" + threadId + "'");
                assertEquals(testId, wire.read("loggerName").text(), "Logger name for WARN object entry should match '" + testId + "'");
                assertEquals("Test object {}", wire.read("message").text(), "Message template for object test should be 'Test object {}'");
                assertTrue(wire.hasMore(), "Serialised WARN log with Object argument should contain args section");
                List<Object> args = new ArrayList<>();
                assertTrue(wire.hasMore(), "Args field should be present for Object parameter in WARN log");
                wire.read("args").sequence(args, (l, vi) -> {
                    while (vi.hasNextSequenceItem()) {
                        l.add(vi.object(Object.class));
                    }
                });
                assertTrue(((String) args.iterator().next()).contains("java.lang.Object@"), "Object argument should be serialised as toString containing 'java.lang.Object@'");
                assertFalse(wire.hasMore(), "WARN object log entry should not have unexpected trailing fields");
            }
        }
    }
}
