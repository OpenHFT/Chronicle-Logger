/*
 * Copyright 2013-2026 chronicle.software; SPDX-License-Identifier: Apache-2.0
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

class JclChronicleLoggerTest extends JclTestBase {

    @NotNull
    private static ChronicleQueue getChronicleQueue(String testId) {
        return ChronicleQueue.singleBuilder(basePath(testId)).build();
    }

    @BeforeEach
    void setUp() throws IOException {
        System.setProperty(
                "chronicle.logger.properties",
                "chronicle.logger.properties"
        );
        Files.createDirectories(Paths.get(basePath()));
    }

    @AfterEach
    void tearDown() {
        LogFactory.getFactory().release();
        IOTools.deleteDirWithFiles(basePath());
    }

    @Test
    void testLoggerFactory() {
        assertSame(ChronicleLoggerFactory.class, LogFactory.getFactory().getClass());
    }

    @Test
    void testLogger() {
        Log l1 = LogFactory.getLog("jcl-chronicle");
        Log l2 = LogFactory.getLog("jcl-chronicle");
        Log l3 = LogFactory.getLog("logger_1");

        assertNotNull(l1);
        assertSame(ChronicleLogger.class, l1.getClass());

        assertNotNull(l2);
        assertSame(ChronicleLogger.class, l2.getClass());

        assertNotNull(l3);
        assertSame(ChronicleLogger.class, l3.getClass());

        Log l4 = LogFactory.getLog("readwrite");

        assertNotNull(l4);
        assertSame(ChronicleLogger.class, l4.getClass());

        assertEquals(l1, l2);
        assertNotEquals(l1, l3);
        assertNotEquals(l3, l4);
        assertNotEquals(l1, l4);

        ChronicleLogger cl1 = (ChronicleLogger) l1;

        assertEquals(ChronicleLogLevel.DEBUG, cl1.level());
        assertEquals("jcl-chronicle", cl1.name());
        assertTrue(cl1.writer() instanceof DefaultChronicleLogWriter);
        assertEquals(WireType.BINARY_LIGHT, ((DefaultChronicleLogWriter) cl1.writer()).getWireType());

        ChronicleLogger cl2 = (ChronicleLogger) l2;
        assertEquals(ChronicleLogLevel.DEBUG, cl2.level());
        assertEquals("jcl-chronicle", cl2.name());
        assertTrue(cl2.writer() instanceof DefaultChronicleLogWriter);
        assertEquals(WireType.BINARY_LIGHT, ((DefaultChronicleLogWriter) cl2.writer()).getWireType());

        ChronicleLogger cl3 = (ChronicleLogger) l3;
        assertEquals(ChronicleLogLevel.INFO, cl3.level());
        assertEquals("logger_1", cl3.name());
        assertTrue(cl3.writer() instanceof DefaultChronicleLogWriter);
        assertEquals(WireType.JSON, ((DefaultChronicleLogWriter) cl3.writer()).getWireType());

        ChronicleLogger cl4 = (ChronicleLogger) l4;
        assertEquals(ChronicleLogLevel.DEBUG, cl4.level());
        assertEquals("readwrite", cl4.name());
        assertTrue(cl4.writer() instanceof DefaultChronicleLogWriter);
        assertEquals(WireType.BINARY_LIGHT, ((DefaultChronicleLogWriter) cl4.writer()).getWireType());
    }

    @Test
    void testLogging() throws IOException {
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
                        assertNotNull(wire, "log not found for " + level);
                        assertTrue(wire.read("ts").int64() <= currentTimeMillis());
                        assertEquals(level, wire.read("level").asEnum(ChronicleLogLevel.class));
                        assertEquals(threadId, wire.read("threadName").text());
                        assertEquals(testId, wire.read("loggerName").text());
                        assertEquals("level is " + level, wire.read("message").text());
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
        }

        IOTools.deleteDirWithFiles(basePath(testId));
    }
}
