/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.logger.log4j2;

import net.openhft.chronicle.core.OS;
import net.openhft.chronicle.core.io.IOTools;
import net.openhft.chronicle.logger.ChronicleLogLevel;
import net.openhft.chronicle.queue.ChronicleQueue;
import net.openhft.chronicle.wire.DocumentContext;
import net.openhft.chronicle.wire.Wire;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static java.lang.System.currentTimeMillis;
import static org.junit.jupiter.api.Assertions.*;

public class Log4j2BinaryTest extends Log4j2TestBase {

    @NotNull
    private static ChronicleQueue getChronicleQueue(String testId) {
        return ChronicleQueue.singleBuilder(basePath(testId)).build();
    }

    @AfterEach
    public void tearDown() {
        IOTools.deleteDirWithFiles(rootPath());
    }

    @Test
    public void testConfig() {
        // needs to be initialised before trying to get the appender, otherwise we end up in a loop
        final Logger logger = LogManager.getLogger(OS.class);
        final String appenderName = "CONF-CHRONICLE";

        final org.apache.logging.log4j.core.Appender appender = getAppender(appenderName);

        assertNotNull(appender, "appender should be registered in log4j configuration");
        assertEquals(appenderName, appender.getName(), "appender should have configured name from xml");
        ChronicleAppender ba = assertInstanceOf(ChronicleAppender.class, appender, "appender should be chronicle implementation not generic log4j type");

        assertEquals(128, ba.getChronicleConfig().getBlockSize(), "chronicle queue should use custom block size from configuration");
        assertEquals(256, ba.getChronicleConfig().getBufferCapacity(), "chronicle queue should use custom buffer capacity from configuration");
    }

    @Test
    public void testIndexedAppender() throws IOException {
        final String testId = "chronicle";
        final String threadId = testId + "-th";
        final Logger logger = LogManager.getLogger(testId);

        Thread.currentThread().setName(threadId);
        Path dir = Paths.get(basePath(testId));
        IOTools.deleteDirWithFiles(dir.toFile());
        Files.createDirectories(dir);

        for (ChronicleLogLevel level : LOG_LEVELS) {
            log(logger, level, "level is {}", level);
        }

        try (final ChronicleQueue cq = getChronicleQueue(testId)) {
            net.openhft.chronicle.queue.ExcerptTailer tailer = cq.createTailer();
            for (ChronicleLogLevel level : LOG_LEVELS) {
                try (DocumentContext dc = tailer.readingDocument()) {
                    Wire wire = dc.wire();
                    assertNotNull(wire, () -> "chronicle queue should contain serialized log entry for " + level + " level");
                    assertTrue(wire.read("ts").int64() <= currentTimeMillis(), () -> "log timestamp should not be in the future for " + level + " entry");
                    assertEquals(level, wire.read("level").asEnum(ChronicleLogLevel.class), () -> "serialized level field should match logged level " + level);
                    assertEquals(threadId, wire.read("threadName").text(), () -> "serialized thread name should match logging thread for " + level + " entry");
                    assertEquals(testId, wire.read("loggerName").text(), () -> "serialized logger name should match configured logger for " + level + " entry");
                    assertEquals("level is " + level, wire.read("message").text(), () -> "serialized message should match logged message for " + level);
                    assertFalse(wire.hasMore(), () -> "log entry should not contain unexpected trailing fields for " + level);
                }
            }
            try (DocumentContext dc = tailer.readingDocument()) {
                Wire wire = dc.wire();
                assertNull(wire, "chronicle queue should be exhausted after reading all logged level entries");
            }

            logger.debug("Throwable test 1", new UnsupportedOperationException());
            logger.debug("Throwable test 2", new UnsupportedOperationException("Exception message"));

            try (DocumentContext dc = tailer.readingDocument()) {
                Wire wire = dc.wire();
                assertNotNull(wire, "chronicle queue should contain first exception log entry");
                assertTrue(wire.read("ts").int64() <= currentTimeMillis(), "exception log timestamp should not be in the future");
                assertEquals(ChronicleLogLevel.DEBUG, wire.read("level").asEnum(ChronicleLogLevel.class), "exception log should preserve debug level");
                assertEquals(threadId, wire.read("threadName").text(), "exception log should capture logging thread name");
                assertEquals(testId, wire.read("loggerName").text(), "exception log should preserve logger name");
                assertEquals("Throwable test 1", wire.read("message").text(), "exception log should contain message without exception detail");
                assertTrue(wire.hasMore(), "exception log entry should include serialized throwable field");
                Throwable throwable = wire.read("throwable").throwable(false);
                assertInstanceOf(UnsupportedOperationException.class, throwable, "serialized throwable should preserve exception type");
                assertFalse(wire.hasMore(), "exception log entry should not contain unexpected trailing fields");
            }

            try (DocumentContext dc = tailer.readingDocument()) {
                Wire wire = dc.wire();
                assertNotNull(wire, "chronicle queue should contain second exception log entry");
                assertTrue(wire.read("ts").int64() <= currentTimeMillis(), "second exception log timestamp should not be in the future");
                assertEquals(ChronicleLogLevel.DEBUG, wire.read("level").asEnum(ChronicleLogLevel.class), "second exception log should preserve debug level");
                assertEquals(threadId, wire.read("threadName").text(), "second exception log should capture logging thread name");
                assertEquals(testId, wire.read("loggerName").text(), "second exception log should preserve logger name");
                assertEquals("Throwable test 2", wire.read("message").text(), "second exception log should contain message without exception detail");
                assertTrue(wire.hasMore(), "second exception log entry should include serialized throwable field");
                Throwable throwable = wire.read("throwable").throwable(false);
                assertInstanceOf(UnsupportedOperationException.class, throwable, "serialized throwable should preserve exception type");
                assertEquals("Exception message", throwable.getMessage(), "deserialized exception should preserve original exception message");
                assertFalse(wire.hasMore(), "second exception log entry should not contain unexpected trailing fields");
            }

            try (DocumentContext dc = tailer.readingDocument()) {
                Wire wire = dc.wire();
                assertNull(wire, "chronicle queue should be exhausted after reading all exception log entries");
            }
        }
        IOTools.deleteDirWithFiles(basePath(testId));
    }
}
