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
import org.apache.log4j.xml.DOMConfigurator;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Files;
import java.nio.file.Paths;

import static java.lang.System.currentTimeMillis;
import static org.junit.jupiter.api.Assertions.*;

// CPD-OFF - mirrors Log4j1ChronicleLogTest to verify bridge behaviour
public class Slf4jBridgeChronicleLogTest extends Log4j1TestBase {

    @NotNull
    private static ChronicleQueue getChronicleQueue(String testId, WireType wt) {
        return ChronicleQueue.singleBuilder(basePath(testId)).wireType(wt).build();
    }

    @AfterEach
    public void tearDown() {
        IOTools.deleteDirWithFiles(rootPath());
    }

    @Test
    public void slf4jLogsReachChronicleAppender() throws Exception {
        final String testId = "chronicle";
        final String threadId = testId + "-th";
        final Logger logger = LoggerFactory.getLogger(testId);

        final java.net.URL cfg = ClassLoader.getSystemResource("log4j.xml");
        assertNotNull(cfg, "log4j.xml not found on classpath");
        DOMConfigurator.configure(cfg);
        Files.createDirectories(Paths.get(basePath(testId)));
        Thread.currentThread().setName(threadId);

        for (ChronicleLogLevel level : LOG_LEVELS) {
            logSlf4j(logger, level, "level is " + level);
        }

        try (final ChronicleQueue cq = getChronicleQueue(testId, WireType.BINARY_LIGHT)) {
            net.openhft.chronicle.queue.ExcerptTailer tailer = cq.createTailer();
            for (ChronicleLogLevel level : LOG_LEVELS) {
                try (DocumentContext dc = tailer.readingDocument()) {
                    Wire wire = dc.wire();
                    assertNotNull(wire, () -> "wire: log entry for level=" + level);
                    assertTrue(wire.read("ts").int64() <= currentTimeMillis(), () -> "wire: timestamp for level=" + level);
                    assertEquals(level, wire.read("level").asEnum(ChronicleLogLevel.class), () -> "wire: level field for level=" + level);
                    assertEquals(threadId, wire.read("threadName").text(), () -> "wire: threadName for level=" + level);
                    assertEquals(testId, wire.read("loggerName").text(), () -> "wire: loggerName for level=" + level);
                    assertEquals("level is " + level, wire.read("message").text(), () -> "wire: message for level=" + level);
                    assertFalse(wire.hasMore(), () -> "wire: trailing fields for level=" + level);
                }
            }
        }
    }

    private static void logSlf4j(Logger logger, ChronicleLogLevel level, String message) {
        switch (level) {
            case TRACE:
                logger.trace(message);
                break;
            case DEBUG:
                logger.debug(message);
                break;
            case INFO:
                logger.info(message);
                break;
            case WARN:
                logger.warn(message);
                break;
            case ERROR:
                logger.error(message);
                break;
            default:
                throw new UnsupportedOperationException();
        }
    }
}
