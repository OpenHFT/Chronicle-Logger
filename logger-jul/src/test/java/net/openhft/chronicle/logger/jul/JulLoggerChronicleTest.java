/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.logger.jul;

import net.openhft.chronicle.core.Jvm;
import net.openhft.chronicle.core.io.IOTools;
import net.openhft.chronicle.logger.ChronicleLogLevel;
import net.openhft.chronicle.logger.DefaultChronicleLogWriter;
import net.openhft.chronicle.queue.ChronicleQueue;
import net.openhft.chronicle.wire.DocumentContext;
import net.openhft.chronicle.wire.Wire;
import net.openhft.chronicle.wire.WireType;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import static java.lang.System.currentTimeMillis;
import static org.junit.jupiter.api.Assertions.*;

public class JulLoggerChronicleTest extends JulLoggerTestBase {

    private static void testChronicleConfiguration(
            final String loggerId,
            final Class<? extends ChronicleLogger> expectedLoggerType,
            final Level level,
            final WireType wireType) {

        Logger logger = Logger.getLogger(loggerId);

        assertNotNull(logger, "Logger.getLogger should return non-null instance for " + loggerId);
        ChronicleLogger chronicleLogger = assertInstanceOf(ChronicleLogger.class, logger,
                "Logger '" + loggerId + "' should be instance of ChronicleLogger");
        if (!(logger instanceof ChronicleLogger.Null))
            assertEquals(expectedLoggerType, logger.getClass(),
                    "Logger '" + loggerId + "' should have concrete type " + expectedLoggerType.getSimpleName());
        assertEquals(loggerId, logger.getName(),
                "Logger '" + loggerId + "' getName() should return configured logger name");
        DefaultChronicleLogWriter writer = assertInstanceOf(DefaultChronicleLogWriter.class, chronicleLogger.writer(),
                "ChronicleLogger '" + loggerId + "' writer should be DefaultChronicleLogWriter instance");
        assertEquals(level, logger.getLevel(),
                "Logger '" + loggerId + "' should have configured level " + level);
        assertEquals(wireType, writer.getWireType(),
                "Logger '" + loggerId + "' writer should use configured wire type " + wireType);
    }

    @NotNull
    private static ChronicleQueue getChronicleQueue(String testId) {
        return ChronicleQueue.singleBuilder(basePath(testId)).build();

    }

    @BeforeEach
    public void setUp() throws IOException {
        setupLogger(getClass());
        Files.createDirectories(Paths.get(basePath()));
    }

    @AfterEach
    public void tearDown() {
        IOTools.deleteDirWithFiles(basePath());
    }

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

        final String threadId = "thread-" + Jvm.currentThreadId();

        Thread.currentThread().setName(threadId);

        for (ChronicleLogLevel level : LOG_LEVELS) {
            log(logger, level, "level is {0}", level);
        }

        try (final ChronicleQueue cq = getChronicleQueue(testId)) {
            net.openhft.chronicle.queue.ExcerptTailer tailer = cq.createTailer();
            for (ChronicleLogLevel level : LOG_LEVELS) {
                try (DocumentContext dc = tailer.readingDocument()) {
                    Wire wire = dc.wire();
                    assertNotNull(wire, () -> "Chronicle Queue should contain log entry for " + level + " level");
                    assertTrue(wire.read("ts").int64() <= currentTimeMillis(),
                            () -> "Log entry timestamp for " + level + " should not be in future");
                    assertEquals(level, wire.read("level").asEnum(ChronicleLogLevel.class),
                            () -> "Serialised log level should match logged " + level);
                    assertEquals(threadId, wire.read("threadName").text(),
                            () -> "Log entry for " + level + " should capture current thread name '" + threadId + "'");
                    assertEquals(testId, wire.read("loggerName").text(),
                            () -> "Log entry for " + level + " should record logger name '" + testId + "'");
                    assertEquals("level is {0}", wire.read("message").text(),
                            () -> "Log entry for " + level + " should preserve parameterised message template");
                    assertTrue(wire.hasMore(), () -> "Log entry for " + level + " should contain message arguments");
                    List<Object> args = new ArrayList<>();
                    assertTrue(wire.hasMore(), () -> "Log entry for " + level + " should have 'args' field before reading");
                    wire.read("args").sequence(args, (l, vi) -> {
                        while (vi.hasNextSequenceItem()) {
                            l.add(vi.object(Object.class));
                        }
                    });
                    assertEquals(level, args.iterator().next(),
                            () -> "First message argument for " + level + " log should be level enum value");
                    assertFalse(wire.hasMore(),
                            () -> "Log entry for " + level + " should not have unexpected trailing fields");
                }
            }
            try (DocumentContext dc = tailer.readingDocument()) {
                Wire wire = dc.wire();
                assertNull(wire, "Chronicle Queue should be exhausted after reading all logged entries");
            }

            logger.log(Level.FINE, "Throwable test 1", new UnsupportedOperationException());
            logger.log(Level.FINE, "Throwable test 2", new UnsupportedOperationException("Exception message"));

            try (DocumentContext dc = tailer.readingDocument()) {
                Wire wire = dc.wire();
                assertNotNull(wire, "Chronicle Queue should contain first log entry with UnsupportedOperationException");
                assertTrue(wire.read("ts").int64() <= currentTimeMillis(),
                        "First throwable log entry timestamp should not be in future");
                assertEquals(ChronicleLogLevel.DEBUG, wire.read("level").asEnum(ChronicleLogLevel.class),
                        "JUL Level.FINE should map to ChronicleLogLevel.DEBUG for throwable log");
                assertEquals(threadId, wire.read("threadName").text(),
                        "First throwable log entry should capture current thread name '" + threadId + "'");
                assertEquals(testId, wire.read("loggerName").text(),
                        "First throwable log entry should record logger name '" + testId + "'");
                assertEquals("Throwable test 1", wire.read("message").text(),
                        "First throwable log entry should preserve message 'Throwable test 1'");
                assertTrue(wire.hasMore(), "Log entry with throwable should contain 'throwable' field");
                Throwable throwable = wire.read("throwable").throwable(false);
                assertInstanceOf(UnsupportedOperationException.class, throwable,
                        "Deserialised throwable should be UnsupportedOperationException for first test");
                assertFalse(wire.hasMore(),
                        "First throwable log entry should not have unexpected trailing fields");
            }

            try (DocumentContext dc = tailer.readingDocument()) {
                Wire wire = dc.wire();
                assertNotNull(wire, "Chronicle Queue should contain second log entry with UnsupportedOperationException");
                assertTrue(wire.read("ts").int64() <= currentTimeMillis(),
                        "Second throwable log entry timestamp should not be in future");
                assertEquals(ChronicleLogLevel.DEBUG, wire.read("level").asEnum(ChronicleLogLevel.class),
                        "JUL Level.FINE should map to ChronicleLogLevel.DEBUG for second throwable log");
                assertEquals(threadId, wire.read("threadName").text(),
                        "Second throwable log entry should capture current thread name '" + threadId + "'");
                assertEquals(testId, wire.read("loggerName").text(),
                        "Second throwable log entry should record logger name '" + testId + "'");
                assertEquals("Throwable test 2", wire.read("message").text(),
                        "Second throwable log entry should preserve message 'Throwable test 2'");
                assertTrue(wire.hasMore(), "Second log entry with throwable should contain 'throwable' field");
                Throwable throwable = wire.read("throwable").throwable(false);
                assertInstanceOf(UnsupportedOperationException.class, throwable,
                        "Deserialised throwable should be UnsupportedOperationException for second test");
                assertEquals("Exception message", throwable.getMessage(),
                        "Deserialised exception should preserve original exception message 'Exception message'");
                assertFalse(wire.hasMore(),
                        "Second throwable log entry should not have unexpected trailing fields");
            }

            try (DocumentContext dc = tailer.readingDocument()) {
                Wire wire = dc.wire();
                assertNull(wire, "Chronicle Queue should be exhausted after reading all throwable log entries");
            }
        }

        IOTools.deleteDirWithFiles(basePath(testId));
    }
}
