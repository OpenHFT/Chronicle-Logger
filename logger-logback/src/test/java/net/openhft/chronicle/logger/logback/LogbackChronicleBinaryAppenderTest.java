/*
 * Copyright 2013-2026 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.logger.logback;

import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;
import net.openhft.chronicle.core.io.BackgroundResourceReleaser;
import net.openhft.chronicle.core.io.IOTools;
import net.openhft.chronicle.logger.ChronicleLogLevel;
import net.openhft.chronicle.queue.ChronicleQueue;
import net.openhft.chronicle.wire.DocumentContext;
import net.openhft.chronicle.wire.Wire;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.RepeatedTest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static java.lang.System.currentTimeMillis;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Validates that the Logback binary appender records each field in the
 * chronicle queue and handles exceptions correctly.
 */
class LogbackChronicleBinaryAppenderTest extends LogbackTestBase {
    @NotNull
    private static ChronicleQueue getChronicleQueue(String testId) {
        return ChronicleQueue.singleBuilder(basePath(testId)).build();
    }

    private String originalThreadName;
    private String originalConfiguration;

    @BeforeEach
    void setup() throws IOException, JoranException {
        originalThreadName = Thread.currentThread().getName();
        originalConfiguration = System.getProperty("logback.configurationFile");
        String configuration = System.getProperty("resources.path")
                + "/logback-chronicle-binary-appender.xml";
        System.setProperty("logback.configurationFile", configuration);
        releaseAndDeleteFixture();
        Files.createDirectories(Paths.get(rootPath()));
        JoranConfigurator configurator = new JoranConfigurator();
        configurator.setContext(getLoggerContext());
        configurator.doConfigure(configuration);
    }

    @AfterEach
    void tearDown() {
        try {
            releaseAndDeleteFixture();
        } finally {
            Thread.currentThread().setName(originalThreadName);
            if (originalConfiguration == null)
                System.clearProperty("logback.configurationFile");
            else
                System.setProperty("logback.configurationFile", originalConfiguration);
        }
    }

    private void releaseAndDeleteFixture() {
        // Reset closes appenders before their queue files are removed.
        getLoggerContext().reset();
        BackgroundResourceReleaser.releasePendingResources();
        IOTools.deleteDirWithFiles(rootPath());
        assertFalse(Files.exists(Paths.get(rootPath())), "Logger fixture was not deleted");
    }

    /**
     * Writes log events and confirms that each queue entry contains the
     * expected fields and throwable data.
     */
    @RepeatedTest(2)
    void testBinaryAppender() {
        final String testId = "binary-chronicle";
        final String threadId = testId + "-th";

        final Logger logger = LoggerFactory.getLogger(testId);

        Thread.currentThread().setName(threadId);

        for (ChronicleLogLevel level : LOG_LEVELS) {
            log(logger, level, "level is {}", level);
        }

        try (final ChronicleQueue cq = getChronicleQueue(testId);
             net.openhft.chronicle.queue.ExcerptTailer tailer = cq.createTailer()) {
            for (ChronicleLogLevel level : LOG_LEVELS) {
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
            try (DocumentContext dc = tailer.readingDocument()) {
                if (dc.wire() != null)
                    fail("No more log entries expected was found: " + dc.wire().toString());
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
        }
    }
}
