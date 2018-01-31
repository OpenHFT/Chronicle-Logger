/*
 * Copyright 2014-2017 Chronicle Software
 *
 * http://www.chronicle.software
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.openhft.chronicle.logger.jcl;

import net.openhft.chronicle.logger.ChronicleLogLevel;
import net.openhft.chronicle.logger.DefaultChronicleLogWriter;
import net.openhft.chronicle.queue.ChronicleQueue;
import net.openhft.chronicle.queue.ChronicleQueueBuilder;
import net.openhft.chronicle.queue.impl.single.SingleChronicleQueue;
import net.openhft.chronicle.wire.DocumentContext;
import net.openhft.chronicle.wire.Wire;
import net.openhft.chronicle.wire.WireType;
import net.openhft.lang.io.IOTools;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jetbrains.annotations.NotNull;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static java.lang.System.currentTimeMillis;
import static org.junit.Assert.*;

public class JclChronicleLoggerTest extends JclTestBase {

    @Before
    public void setUp() throws IOException {
        System.setProperty(
                "chronicle.logger.properties",
                "chronicle.logger.properties"
        );
        Files.createDirectories(Paths.get(basePath()));
    }

    @After
    public void tearDown() {
        LogFactory.getFactory().release();
        IOTools.deleteDir(basePath());
    }

    // *************************************************************************
    //
    // *************************************************************************

    @Test
    public void testLoggerFactory() {
        assertEquals(
                ChronicleLoggerFactory.class,
                LogFactory.getFactory().getClass());
    }

    // *************************************************************************
    //
    // *************************************************************************

    @Test
    public void testLogger() {
        Log l1 = LogFactory.getLog("jcl-chronicle");
        Log l2 = LogFactory.getLog("jcl-chronicle");
        Log l3 = LogFactory.getLog("logger_1");
        Log l4 = LogFactory.getLog("readwrite");

        assertNotNull(l1);
        assertEquals(ChronicleLogger.class, l1.getClass());

        assertNotNull(l2);
        assertEquals(ChronicleLogger.class, l2.getClass());

        assertNotNull(l3);
        assertEquals(ChronicleLogger.class, l3.getClass());

        assertNotNull(l4);
        assertEquals(ChronicleLogger.class, l4.getClass());

        assertEquals(l1, l2);
        assertNotEquals(l1, l3);
        assertNotEquals(l3, l4);
        assertNotEquals(l1, l4);

        ChronicleLogger cl1 = (ChronicleLogger) l1;

        assertEquals(cl1.level(), ChronicleLogLevel.DEBUG);
        assertEquals(cl1.name(), "jcl-chronicle");
        assertTrue(cl1.writer() instanceof DefaultChronicleLogWriter);
        assertEquals(WireType.BINARY_LIGHT, ((DefaultChronicleLogWriter) cl1.writer()).getWireType());

        ChronicleLogger cl2 = (ChronicleLogger) l2;
        assertEquals(cl2.level(), ChronicleLogLevel.DEBUG);
        assertEquals(cl2.name(), "jcl-chronicle");
        assertTrue(cl2.writer() instanceof DefaultChronicleLogWriter);
        assertEquals(WireType.BINARY_LIGHT, ((DefaultChronicleLogWriter) cl2.writer()).getWireType());

        ChronicleLogger cl3 = (ChronicleLogger) l3;
        assertEquals(cl3.level(), ChronicleLogLevel.INFO);
        assertEquals(cl3.name(), "logger_1");
        assertTrue(cl3.writer() instanceof DefaultChronicleLogWriter);
        assertEquals(WireType.JSON, ((DefaultChronicleLogWriter) cl3.writer()).getWireType());

        ChronicleLogger cl4 = (ChronicleLogger) l4;
        assertEquals(cl4.level(), ChronicleLogLevel.DEBUG);
        assertEquals(cl4.name(), "readwrite");
        assertTrue(cl4.writer() instanceof DefaultChronicleLogWriter);
        assertEquals(WireType.BINARY_LIGHT, ((DefaultChronicleLogWriter) cl4.writer()).getWireType());
    }

    @Test
    public void testLogging() throws IOException {
        final String testId = "readwrite";
        final String threadId = testId + "-th";
        final Log logger = LogFactory.getLog(testId);

        IOTools.deleteDir(basePath(testId));
        Files.createDirectories(Paths.get(basePath(testId)));
        Thread.currentThread().setName(threadId);

        for (ChronicleLogLevel level : LOG_LEVELS) {
            log(logger, level, "level is " + level.toString());
        }

        try (final ChronicleQueue cq = getChronicleQueue(testId)) {
            net.openhft.chronicle.queue.ExcerptTailer tailer = cq.createTailer();
            for (ChronicleLogLevel level : LOG_LEVELS) {
                // logger configured to debug
                if (level.isHigherOrEqualTo(ChronicleLogLevel.DEBUG)) {
                    try (DocumentContext dc = tailer.readingDocument()) {
                        Wire wire = dc.wire();
                        assertNotNull("log not found for " + level, wire);
                        assertTrue(wire.read("ts").int64() <= currentTimeMillis());
                        assertEquals(level, wire.read("level").asEnum(ChronicleLogLevel.class));
                        assertEquals(threadId, wire.read("threadName").text());
                        assertEquals(testId, wire.read("loggerName").text());
                        assertEquals("level is " + level.toString(), wire.read("message").text());
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


        IOTools.deleteDir(basePath(testId));
    }

    @NotNull
    private static SingleChronicleQueue getChronicleQueue(String testId) {
        return ChronicleQueueBuilder.single(basePath(testId)).build();
    }
}
