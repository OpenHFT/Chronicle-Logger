/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.logger.jcl;

import net.openhft.chronicle.core.io.IOTools;
import net.openhft.chronicle.logger.ChronicleLogLevel;
import net.openhft.chronicle.logger.DefaultChronicleLogWriter;
import net.openhft.chronicle.queue.ChronicleQueue;
import net.openhft.chronicle.wire.DocumentContext;
import net.openhft.chronicle.wire.Wire;
import net.openhft.chronicle.wire.WireType;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static java.lang.System.currentTimeMillis;
import static org.junit.jupiter.api.Assertions.*;

public class JclChronicleLoggerTest extends JclTestBase {

    @NotNull
    private static ChronicleQueue getChronicleQueue(String testId) {
        return ChronicleQueue.singleBuilder(basePath(testId)).build();
    }

    @BeforeEach
    public void setUp() throws IOException {
        System.setProperty(
                "chronicle.logger.properties",
                "chronicle.logger.properties"
        );
        Files.createDirectories(Paths.get(basePath()));
    }

    @AfterEach
    public void tearDown() {
        LogFactory.getFactory().release();
        IOTools.deleteDirWithFiles(basePath());
    }

    @Test
    public void testLoggerFactory() {
        assertEquals(
                ChronicleLoggerFactory.class,
                LogFactory.getFactory().getClass(),
                "should return ChronicleLoggerFactory as the JCL logger factory implementation");
    }

    @Test
    public void testLogger() {
        Log l1 = LogFactory.getLog("jcl-chronicle");
        Log l2 = LogFactory.getLog("jcl-chronicle");
        Log l3 = LogFactory.getLog("logger_1");

        assertNotNull(l1, "should create logger instance for 'jcl-chronicle' name");
        assertInstanceOf(ChronicleLogger.class, l1, "should return ChronicleLogger implementation for 'jcl-chronicle' logger");

        assertNotNull(l2, "should create logger instance for second request to 'jcl-chronicle' name");
        assertInstanceOf(ChronicleLogger.class, l2, "should return ChronicleLogger implementation for second 'jcl-chronicle' logger");

        assertNotNull(l3, "should create logger instance for 'logger_1' name");
        assertInstanceOf(ChronicleLogger.class, l3, "should return ChronicleLogger implementation for 'logger_1' logger");

        Log l4 = LogFactory.getLog("readwrite");

        assertNotNull(l4, "should create logger instance for 'readwrite' name");
        assertInstanceOf(ChronicleLogger.class, l4, "should return ChronicleLogger implementation for 'readwrite' logger");

        assertEquals(l1, l2, "should return same cached logger instance for identical 'jcl-chronicle' name");
        assertNotEquals(l1, l3, "should return different logger instances for 'jcl-chronicle' and 'logger_1' names");
        assertNotEquals(l3, l4, "should return different logger instances for 'logger_1' and 'readwrite' names");
        assertNotEquals(l1, l4, "should return different logger instances for 'jcl-chronicle' and 'readwrite' names");

        ChronicleLogger cl1 = (ChronicleLogger) l1;

        assertEquals(ChronicleLogLevel.DEBUG, cl1.level(), "should configure 'jcl-chronicle' logger with DEBUG level from properties");
        assertEquals("jcl-chronicle", cl1.name(), "should preserve 'jcl-chronicle' as the logger name");
        DefaultChronicleLogWriter cl1Writer = assertInstanceOf(DefaultChronicleLogWriter.class, cl1.writer(), "should use DefaultChronicleLogWriter for 'jcl-chronicle' logger");
        assertEquals(WireType.BINARY_LIGHT, cl1Writer.getWireType(), "should configure BINARY_LIGHT wire type for 'jcl-chronicle' logger from properties");

        ChronicleLogger cl2 = (ChronicleLogger) l2;
        assertEquals(ChronicleLogLevel.DEBUG, cl2.level(), "should configure cached 'jcl-chronicle' logger with same DEBUG level");
        assertEquals("jcl-chronicle", cl2.name(), "should preserve 'jcl-chronicle' name in cached logger instance");
        DefaultChronicleLogWriter cl2Writer = assertInstanceOf(DefaultChronicleLogWriter.class, cl2.writer(), "should use same DefaultChronicleLogWriter type for cached 'jcl-chronicle' logger");
        assertEquals(WireType.BINARY_LIGHT, cl2Writer.getWireType(), "should maintain BINARY_LIGHT wire type in cached 'jcl-chronicle' logger");

        ChronicleLogger cl3 = (ChronicleLogger) l3;
        assertEquals(ChronicleLogLevel.INFO, cl3.level(), "should configure 'logger_1' logger with INFO level from properties");
        assertEquals("logger_1", cl3.name(), "should preserve 'logger_1' as the logger name");
        DefaultChronicleLogWriter cl3Writer = assertInstanceOf(DefaultChronicleLogWriter.class, cl3.writer(), "should use DefaultChronicleLogWriter for 'logger_1' logger");
        assertEquals(WireType.JSON, cl3Writer.getWireType(), "should configure JSON wire type for 'logger_1' logger from properties");

        ChronicleLogger cl4 = (ChronicleLogger) l4;
        assertEquals(ChronicleLogLevel.DEBUG, cl4.level(), "should configure 'readwrite' logger with DEBUG level from properties");
        assertEquals("readwrite", cl4.name(), "should preserve 'readwrite' as the logger name");
        DefaultChronicleLogWriter cl4Writer = assertInstanceOf(DefaultChronicleLogWriter.class, cl4.writer(), "should use DefaultChronicleLogWriter for 'readwrite' logger");
        assertEquals(WireType.BINARY_LIGHT, cl4Writer.getWireType(), "should configure BINARY_LIGHT wire type for 'readwrite' logger from properties");
    }

    @Test
    public void testLogging() throws IOException {
        final String testId = "readwrite";
        final String threadId = testId + "-th";
        final Log logger = LogFactory.getLog(testId);

        IOTools.deleteDirWithFiles(basePath(testId));
        Files.createDirectories(Paths.get(basePath(testId)));
        Thread.currentThread().setName(threadId);

        for (ChronicleLogLevel level : LOG_LEVELS) {
            log(logger, level, "level is " + level);
        }

        try (final ChronicleQueue cq = getChronicleQueue(testId)) {
            net.openhft.chronicle.queue.ExcerptTailer tailer = cq.createTailer();
            for (ChronicleLogLevel level : LOG_LEVELS) {
                // logger configured to debug
                if (level.isHigherOrEqualTo(ChronicleLogLevel.DEBUG)) {
                    try (DocumentContext dc = tailer.readingDocument()) {
                        Wire wire = dc.wire();
                        assertNotNull(wire, () -> "should write log entry to Chronicle Queue for " + level + " level");
                        assertTrue(wire.read("ts").int64() <= currentTimeMillis(), () -> "should write current timestamp in log entry for " + level + " level");
                        assertEquals(level, wire.read("level").asEnum(ChronicleLogLevel.class), () -> "should write correct log level field as " + level + " in Chronicle Queue");
                        assertEquals(threadId, wire.read("threadName").text(), () -> "should capture thread name '" + threadId + "' in log entry for " + level + " level");
                        assertEquals(testId, wire.read("loggerName").text(), () -> "should write logger name '" + testId + "' in log entry for " + level + " level");
                        assertEquals("level is " + level, wire.read("message").text(), () -> "should preserve log message text in Chronicle Queue for " + level + " level");
                        assertFalse(wire.hasMore(), () -> "should not write unexpected trailing fields in log entry for " + level + " level");
                    }
                }
            }
            try (DocumentContext dc = tailer.readingDocument()) {
                Wire wire = dc.wire();
                assertNull(wire, "should have no more log entries after reading all expected log levels");
            }

            logger.debug("Throwable test 1", new UnsupportedOperationException());
            logger.debug("Throwable test 2", new UnsupportedOperationException("Exception message"));

            try (DocumentContext dc = tailer.readingDocument()) {
                Wire wire = dc.wire();
                assertNotNull(wire, "should write log entry with throwable to Chronicle Queue");
                assertTrue(wire.read("ts").int64() <= currentTimeMillis(), "should write current timestamp for log entry with throwable");
                assertEquals(ChronicleLogLevel.DEBUG, wire.read("level").asEnum(ChronicleLogLevel.class), "should write DEBUG level for log entry with throwable");
                assertEquals(threadId, wire.read("threadName").text(), "should capture thread name in log entry with throwable");
                assertEquals(testId, wire.read("loggerName").text(), "should write logger name in log entry with throwable");
                assertEquals("Throwable test 1", wire.read("message").text(), "should preserve log message 'Throwable test 1' when logging with exception");
                assertTrue(wire.hasMore(), "should include throwable field after message in log entry");
                Throwable throwable = wire.read("throwable").throwable(false);
                assertInstanceOf(UnsupportedOperationException.class, throwable, "should serialize UnsupportedOperationException as throwable field");
                assertFalse(wire.hasMore(), "should not write unexpected trailing fields after throwable in log entry");
            }

            try (DocumentContext dc = tailer.readingDocument()) {
                Wire wire = dc.wire();
                assertNotNull(wire, "should write second log entry with throwable to Chronicle Queue");
                assertTrue(wire.read("ts").int64() <= currentTimeMillis(), "should write current timestamp for second log entry with throwable");
                assertEquals(ChronicleLogLevel.DEBUG, wire.read("level").asEnum(ChronicleLogLevel.class), "should write DEBUG level for second log entry with throwable");
                assertEquals(threadId, wire.read("threadName").text(), "should capture thread name in second log entry with throwable");
                assertEquals(testId, wire.read("loggerName").text(), "should write logger name in second log entry with throwable");
                assertEquals("Throwable test 2", wire.read("message").text(), "should preserve log message 'Throwable test 2' when logging with exception");
                assertTrue(wire.hasMore(), "should include throwable field after message in second log entry");
                Throwable throwable = wire.read("throwable").throwable(false);
                assertInstanceOf(UnsupportedOperationException.class, throwable, "should serialize UnsupportedOperationException as throwable field in second entry");
                assertEquals("Exception message", throwable.getMessage(), "should preserve exception message 'Exception message' in serialized throwable");
                assertFalse(wire.hasMore(), "should not write unexpected trailing fields after throwable in second log entry");
            }

            try (DocumentContext dc = tailer.readingDocument()) {
                Wire wire = dc.wire();
                assertNull(wire, "should have no more log entries after reading all throwable test entries");
            }
        }

        IOTools.deleteDirWithFiles(basePath(testId));
    }
}
