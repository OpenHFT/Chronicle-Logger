/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.logger.logback;

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
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static java.lang.System.currentTimeMillis;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Validates that the Logback binary appender records each field in the
 * chronicle queue and handles exceptions correctly.
 */
public class LogbackChronicleBinaryAppenderTest extends LogbackTestBase {
    @NotNull
    private static ChronicleQueue getChronicleQueue(String testId) {
        return ChronicleQueue.singleBuilder(basePath(testId)).build();
    }

    @BeforeEach
    public void setup() {
        System.setProperty(
                "logback.configurationFile",
                System.getProperty("resources.path")
                        + "/logback-chronicle-binary-appender.xml");
    }

    @AfterEach
    public void tearDown() {
        IOTools.deleteDirWithFiles(rootPath());
    }

    /**
     * Writes log events and confirms that each queue entry contains the
     * expected fields and throwable data.
     *
     * @throws IOException if the queue directory cannot be created
     */
    @Test
    public void testBinaryAppender() throws IOException {
        final String testId = "binary-chronicle";
        final String threadId = testId + "-th";

        final Logger logger = LoggerFactory.getLogger(testId);
        Path dir = Paths.get(basePath(testId));
        IOTools.deleteDirWithFiles(dir.toFile());
        Files.createDirectories(dir);

        Thread.currentThread().setName(threadId);

        for (ChronicleLogLevel level : LOG_LEVELS) {
            log(logger, level, "level is {}", level);
        }

        try (final ChronicleQueue cq = getChronicleQueue(testId);
            net.openhft.chronicle.queue.ExcerptTailer tailer = cq.createTailer()) {
            for (ChronicleLogLevel level : LOG_LEVELS) {
                try (DocumentContext dc = tailer.readingDocument()) {
                    Wire wire = dc.wire();
                    assertNotNull(wire, () -> "Expected wire document to exist for log entry at level=" + level + ", but found null");
                    assertTrue(wire.read("ts").int64() <= currentTimeMillis(),
                            () -> "Expected timestamp field to be less than or equal to current time for level=" + level);
                    assertEquals(level, wire.read("level").asEnum(ChronicleLogLevel.class),
                            () -> "Expected level field to match " + level + " in serialized log entry");
                    assertEquals(threadId, wire.read("threadName").text(),
                            () -> "Expected threadName field to be '" + threadId + "' for level=" + level);
                    assertEquals(testId, wire.read("loggerName").text(),
                            () -> "Expected loggerName field to be '" + testId + "' for level=" + level);
                    assertEquals("level is {}", wire.read("message").text(),
                            () -> "Expected message field to contain template 'level is {}' for level=" + level);
                    assertTrue(wire.hasMore(),
                            () -> "Expected wire to have more fields (args sequence) after message for level=" + level);
                    List<Object> args = new ArrayList<>();
                    assertTrue(wire.hasMore(),
                            () -> "Expected wire to have args field before reading sequence for level=" + level);
                    wire.read("args").sequence(args, (l, vi) -> {
                        while (vi.hasNextSequenceItem()) {
                            l.add(vi.object(Object.class));
                        }
                    });
                    assertEquals(level, args.iterator().next(),
                            () -> "Expected first argument in args sequence to be " + level + ", but got " + args.iterator().next());
                    assertFalse(wire.hasMore(),
                            () -> "Expected no additional fields after args sequence for level=" + level + ", but wire has more data");
                }
            }
            try (DocumentContext dc = tailer.readingDocument()) {
                if (dc.wire() != null)
                    fail("Expected no more log entries after processing all log levels, but found unexpected entry: " + dc.wire().toString());
            }

            logger.debug("Throwable test 1", new UnsupportedOperationException());
            logger.debug("Throwable test 2", new UnsupportedOperationException("Exception message"));

            try (DocumentContext dc = tailer.readingDocument()) {
                Wire wire = dc.wire();
                assertNotNull(wire, "Expected wire document to exist for throwable test 1, but found null");
                assertTrue(wire.read("ts").int64() <= currentTimeMillis(),
                        "Expected timestamp field to be less than or equal to current time for throwable test 1");
                assertEquals(ChronicleLogLevel.DEBUG, wire.read("level").asEnum(ChronicleLogLevel.class),
                        "Expected level field to be DEBUG for throwable test 1");
                assertEquals(threadId, wire.read("threadName").text(),
                        "Expected threadName field to be '" + threadId + "' for throwable test 1");
                assertEquals(testId, wire.read("loggerName").text(),
                        "Expected loggerName field to be '" + testId + "' for throwable test 1");
                assertEquals("Throwable test 1", wire.read("message").text(),
                        "Expected message field to be 'Throwable test 1'");
                assertTrue(wire.hasMore(),
                        "Expected wire to have more fields (throwable) after message for throwable test 1");
                Throwable throwable = wire.read("throwable").throwable(false);
                assertInstanceOf(UnsupportedOperationException.class, throwable,
                        "Expected throwable to be instance of UnsupportedOperationException for throwable test 1");
                assertFalse(wire.hasMore(),
                        "Expected no additional fields after throwable for throwable test 1, but wire has more data");
            }

            try (DocumentContext dc = tailer.readingDocument()) {
                Wire wire = dc.wire();
                assertNotNull(wire, "Expected wire document to exist for throwable test 2, but found null");
                assertTrue(wire.read("ts").int64() <= currentTimeMillis(),
                        "Expected timestamp field to be less than or equal to current time for throwable test 2");
                assertEquals(ChronicleLogLevel.DEBUG, wire.read("level").asEnum(ChronicleLogLevel.class),
                        "Expected level field to be DEBUG for throwable test 2");
                assertEquals(threadId, wire.read("threadName").text(),
                        "Expected threadName field to be '" + threadId + "' for throwable test 2");
                assertEquals(testId, wire.read("loggerName").text(),
                        "Expected loggerName field to be '" + testId + "' for throwable test 2");
                assertEquals("Throwable test 2", wire.read("message").text(),
                        "Expected message field to be 'Throwable test 2'");
                assertTrue(wire.hasMore(),
                        "Expected wire to have more fields (throwable) after message for throwable test 2");
                Throwable throwable = wire.read("throwable").throwable(false);
                assertInstanceOf(UnsupportedOperationException.class, throwable,
                        "Expected throwable to be instance of UnsupportedOperationException for throwable test 2");
                assertEquals("Exception message", throwable.getMessage(),
                        "Expected throwable message to be 'Exception message' for throwable test 2");
                assertFalse(wire.hasMore(),
                        "Expected no additional fields after throwable for throwable test 2, but wire has more data");
            }

            try (DocumentContext dc = tailer.readingDocument()) {
                Wire wire = dc.wire();
                assertNull(wire, "Expected no more log entries after throwable tests, but found unexpected wire document");
            }
        }
        IOTools.deleteDirWithFiles(basePath(testId));
    }
}
