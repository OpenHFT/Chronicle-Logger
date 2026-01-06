/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.logger.log4j1;

import net.openhft.chronicle.core.io.IOTools;
import net.openhft.chronicle.logger.ChronicleLogLevel;
import net.openhft.chronicle.queue.ChronicleQueue;
import net.openhft.chronicle.wire.DocumentContext;
import net.openhft.chronicle.wire.Wire;
import net.openhft.chronicle.wire.WireType;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static java.lang.System.currentTimeMillis;
import static org.junit.jupiter.api.Assertions.*;

// CPD-OFF - duplicated log verification with Slf4jBridgeChronicleLogTest is intentional
public class Log4j1ChronicleLogTest extends Log4j1TestBase {

    @NotNull
    private static ChronicleQueue getChronicleQueue(String testId, WireType wt) {
        return ChronicleQueue.singleBuilder(basePath(testId)).wireType(wt).build();
    }

    @AfterEach
    public void tearDown() {
        IOTools.deleteDirWithFiles(rootPath());
    }

    @Test
    public void testBinaryAppender() throws IOException {
        final String testId = "chronicle";
        final String threadId = testId + "-th";
        final Logger logger = Logger.getLogger(testId);
        Path dir = Paths.get(basePath(testId));
        IOTools.deleteDirWithFiles(dir.toFile());
        Files.createDirectories(dir);
        Thread.currentThread().setName(threadId);

        for (ChronicleLogLevel level : LOG_LEVELS) {
            log(logger, level, "level is " + level);
        }

        try (final ChronicleQueue cq = getChronicleQueue(testId, WireType.BINARY_LIGHT)) {
            net.openhft.chronicle.queue.ExcerptTailer tailer = cq.createTailer();
            for (ChronicleLogLevel level : LOG_LEVELS) {
                try (DocumentContext dc = tailer.readingDocument()) {
                    Wire wire = dc.wire();
                    assertNotNull(wire, () -> "Expected Chronicle Queue to contain log entry for " + level + " but tailer returned null");
                    assertTrue(wire.read("ts").int64() <= currentTimeMillis(), () -> "Timestamp for " + level + " log entry should not be in the future");
                    assertEquals(level, wire.read("level").asEnum(ChronicleLogLevel.class), () -> "Expected log level " + level + " in Chronicle Queue entry");
                    assertEquals(threadId, wire.read("threadName").text(), () -> "Expected thread name '" + threadId + "' for " + level + " log entry");
                    assertEquals(testId, wire.read("loggerName").text(), () -> "Expected logger name '" + testId + "' for " + level + " log entry");
                    assertEquals("level is " + level, wire.read("message").text(), () -> "Expected message 'level is " + level + "' in Chronicle Queue entry");
                    assertFalse(wire.hasMore(), () -> "Chronicle Queue entry for " + level + " should not contain additional unexpected fields");
                }
            }
            try (DocumentContext dc = tailer.readingDocument()) {
                if (dc.wire() != null)
                    fail("Binary appender Chronicle Queue should contain exactly " + LOG_LEVELS.length + " log entries but found additional entry: " + dc.wire());
            }

            logger.debug("Throwable test 1", new UnsupportedOperationException());
            logger.debug("Throwable test 2", new UnsupportedOperationException("Exception message"));

            try (DocumentContext dc = tailer.readingDocument()) {
                Wire wire = dc.wire();
                assertNotNull(wire, "Expected binary appender Chronicle Queue to contain first throwable test log entry but tailer returned null");
                assertTrue(wire.read("ts").int64() <= currentTimeMillis(), "Binary appender first throwable test log timestamp should not be in the future");
                assertEquals(ChronicleLogLevel.DEBUG, wire.read("level").asEnum(ChronicleLogLevel.class), "Expected DEBUG level for binary appender first throwable test log entry");
                assertEquals(threadId, wire.read("threadName").text(), "Expected thread name '" + threadId + "' for binary appender first throwable test log entry");
                assertEquals(testId, wire.read("loggerName").text(), "Expected logger name '" + testId + "' for binary appender first throwable test log entry");
                assertEquals("Throwable test 1", wire.read("message").text(), "Expected message 'Throwable test 1' in binary appender first throwable test log entry");
                assertTrue(wire.hasMore(), "Expected binary appender first throwable test log entry to contain throwable field");
                Throwable throwable = wire.read("throwable").throwable(false);
                assertInstanceOf(UnsupportedOperationException.class, throwable, "Expected UnsupportedOperationException in binary appender first throwable test log entry");
                assertFalse(wire.hasMore(), "Binary appender first throwable test log entry should not contain additional unexpected fields after throwable");
            }

            try (DocumentContext dc = tailer.readingDocument()) {
                Wire wire = dc.wire();
                assertNotNull(wire, "Expected binary appender Chronicle Queue to contain second throwable test log entry but tailer returned null");
                assertTrue(wire.read("ts").int64() <= currentTimeMillis(), "Binary appender second throwable test log timestamp should not be in the future");
                assertEquals(ChronicleLogLevel.DEBUG, wire.read("level").asEnum(ChronicleLogLevel.class), "Expected DEBUG level for binary appender second throwable test log entry");
                assertEquals(threadId, wire.read("threadName").text(), "Expected thread name '" + threadId + "' for binary appender second throwable test log entry");
                assertEquals(testId, wire.read("loggerName").text(), "Expected logger name '" + testId + "' for binary appender second throwable test log entry");
                assertEquals("Throwable test 2", wire.read("message").text(), "Expected message 'Throwable test 2' in binary appender second throwable test log entry");
                assertTrue(wire.hasMore(), "Expected binary appender second throwable test log entry to contain throwable field");
                Throwable throwable = wire.read("throwable").throwable(false);
                assertInstanceOf(UnsupportedOperationException.class, throwable, "Expected UnsupportedOperationException in binary appender second throwable test log entry");
                assertEquals("Exception message", throwable.getMessage(), "Expected exception message 'Exception message' in binary appender second throwable test exception");
                assertFalse(wire.hasMore(), "Binary appender second throwable test log entry should not contain additional unexpected fields after throwable");
            }

            try (DocumentContext dc = tailer.readingDocument()) {
                Wire wire = dc.wire();
                assertNull(wire, "Binary appender Chronicle Queue should not contain any additional log entries after throwable tests");
            }
        }
        IOTools.deleteDirWithFiles(basePath(testId));
    }

    @Test
    public void testJsonAppender() {
        final String testId = "json-chronicle";
        final String threadId = testId + "-th";
        final Logger logger = Logger.getLogger(testId);

        Thread.currentThread().setName(threadId);

        for (ChronicleLogLevel level : LOG_LEVELS) {
            log(logger, level, "level is " + level);
        }

        try (final ChronicleQueue cq = getChronicleQueue(testId, WireType.BINARY_LIGHT)) {
            net.openhft.chronicle.queue.ExcerptTailer tailer = cq.createTailer();
            for (ChronicleLogLevel level : LOG_LEVELS) {
                try (DocumentContext dc = tailer.readingDocument()) {
                    Wire wire = dc.wire();
                    assertNotNull(wire, () -> "Expected Chronicle Queue to contain log entry for " + level + " but tailer returned null");
                    assertTrue(wire.read("ts").int64() <= currentTimeMillis(), () -> "Timestamp for " + level + " log entry should not be in the future");
                    assertEquals(level, wire.read("level").asEnum(ChronicleLogLevel.class), () -> "Expected log level " + level + " in Chronicle Queue entry");
                    assertEquals(threadId, wire.read("threadName").text(), () -> "Expected thread name '" + threadId + "' for " + level + " log entry");
                    assertEquals(testId, wire.read("loggerName").text(), () -> "Expected logger name '" + testId + "' for " + level + " log entry");
                    assertEquals("level is " + level, wire.read("message").text(), () -> "Expected message 'level is " + level + "' in Chronicle Queue entry");
                    assertFalse(wire.hasMore(), () -> "Chronicle Queue entry for " + level + " should not contain additional unexpected fields");
                }
            }
            try (DocumentContext dc = tailer.readingDocument()) {
                Wire wire = dc.wire();
                assertNull(wire, "JSON appender Chronicle Queue should contain exactly " + LOG_LEVELS.length + " log entries, no more entries expected");
            }

            logger.debug("Throwable test 1", new UnsupportedOperationException());
            logger.debug("Throwable test 2", new UnsupportedOperationException("Exception message"));

            try (DocumentContext dc = tailer.readingDocument()) {
                Wire wire = dc.wire();
                assertNotNull(wire, "Expected JSON appender Chronicle Queue to contain first throwable test log entry but tailer returned null");
                assertTrue(wire.read("ts").int64() <= currentTimeMillis(), "JSON appender first throwable test log timestamp should not be in the future");
                assertEquals(ChronicleLogLevel.DEBUG, wire.read("level").asEnum(ChronicleLogLevel.class), "Expected DEBUG level for JSON appender first throwable test log entry");
                assertEquals(threadId, wire.read("threadName").text(), "Expected thread name '" + threadId + "' for JSON appender first throwable test log entry");
                assertEquals(testId, wire.read("loggerName").text(), "Expected logger name '" + testId + "' for JSON appender first throwable test log entry");
                assertEquals("Throwable test 1", wire.read("message").text(), "Expected message 'Throwable test 1' in JSON appender first throwable test log entry");
                assertTrue(wire.hasMore(), "Expected JSON appender first throwable test log entry to contain throwable field");
                Throwable throwable = wire.read("throwable").throwable(false);
                assertInstanceOf(UnsupportedOperationException.class, throwable, "Expected UnsupportedOperationException in JSON appender first throwable test log entry");
                assertFalse(wire.hasMore(), "JSON appender first throwable test log entry should not contain additional unexpected fields after throwable");
            }

            try (DocumentContext dc = tailer.readingDocument()) {
                Wire wire = dc.wire();
                assertNotNull(wire, "Expected JSON appender Chronicle Queue to contain second throwable test log entry but tailer returned null");
                assertTrue(wire.read("ts").int64() <= currentTimeMillis(), "JSON appender second throwable test log timestamp should not be in the future");
                assertEquals(ChronicleLogLevel.DEBUG, wire.read("level").asEnum(ChronicleLogLevel.class), "Expected DEBUG level for JSON appender second throwable test log entry");
                assertEquals(threadId, wire.read("threadName").text(), "Expected thread name '" + threadId + "' for JSON appender second throwable test log entry");
                assertEquals(testId, wire.read("loggerName").text(), "Expected logger name '" + testId + "' for JSON appender second throwable test log entry");
                assertEquals("Throwable test 2", wire.read("message").text(), "Expected message 'Throwable test 2' in JSON appender second throwable test log entry");
                assertTrue(wire.hasMore(), "Expected JSON appender second throwable test log entry to contain throwable field");
                Throwable throwable = wire.read("throwable").throwable(false);
                assertInstanceOf(UnsupportedOperationException.class, throwable, "Expected UnsupportedOperationException in JSON appender second throwable test log entry");
                assertEquals("Exception message", throwable.getMessage(), "Expected exception message 'Exception message' in JSON appender second throwable test exception");
                assertFalse(wire.hasMore(), "JSON appender second throwable test log entry should not contain additional unexpected fields after throwable");
            }

            try (DocumentContext dc = tailer.readingDocument()) {
                Wire wire = dc.wire();
                assertNull(wire, "JSON appender Chronicle Queue should not contain any additional log entries after throwable tests");
            }
        }
        IOTools.deleteDirWithFiles(basePath(testId));
    }
}
