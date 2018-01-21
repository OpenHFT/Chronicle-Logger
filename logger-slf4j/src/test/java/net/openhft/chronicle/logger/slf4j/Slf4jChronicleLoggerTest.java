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
package net.openhft.chronicle.logger.slf4j;

import net.openhft.chronicle.logger.ChronicleLogLevel;
import net.openhft.chronicle.logger.DefaultChronicleLogWriter;
import net.openhft.chronicle.queue.ChronicleQueue;
import net.openhft.chronicle.queue.ChronicleQueueBuilder;
import net.openhft.chronicle.queue.impl.single.SingleChronicleQueue;
import net.openhft.chronicle.wire.DocumentContext;
import net.openhft.chronicle.wire.Wire;
import net.openhft.lang.io.IOTools;
import org.jetbrains.annotations.NotNull;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.impl.StaticLoggerBinder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static java.lang.System.currentTimeMillis;
import static org.junit.Assert.*;

public class Slf4jChronicleLoggerTest extends Slf4jTestBase {

    // *************************************************************************
    //
    // *************************************************************************

    @Before
    public void setUp() {
        System.setProperty(
                "chronicle.logger.properties",
                "chronicle.logger.properties"
        );

        getChronicleLoggerFactory().reload();
    }

    @After
    public void tearDown() {

        IOTools.deleteDir(basePath());
    }

    // *************************************************************************
    //
    // *************************************************************************

    @Test
    public void testLoggerFactory() {
        assertEquals(
                StaticLoggerBinder.getSingleton().getLoggerFactory().getClass(),
                ChronicleLoggerFactory.class);
    }

    @Test
    public void testLogger() {
        Logger l1 = LoggerFactory.getLogger("slf4j-chronicle");
        Logger l2 = LoggerFactory.getLogger("slf4j-chronicle");
        Logger l3 = LoggerFactory.getLogger("logger_1");
        Logger l4 = LoggerFactory.getLogger("readwrite");

        assertNotNull(l1);
        assertTrue(l1 instanceof ChronicleLogger);

        assertNotNull(l2);
        assertTrue(l2 instanceof ChronicleLogger);

        assertNotNull(l3);
        assertTrue(l3 instanceof ChronicleLogger);

        assertNotNull(l4);
        assertTrue(l4 instanceof ChronicleLogger);

        assertEquals(l1, l2);
        assertNotEquals(l1, l3);
        assertNotEquals(l3, l4);
        assertNotEquals(l1, l4);

        ChronicleLogger cl1 = (ChronicleLogger) l1;

        assertEquals(cl1.getLevel(), ChronicleLogLevel.DEBUG);
        assertEquals(cl1.getName(), "slf4j-chronicle");
        assertTrue(cl1.getWriter() instanceof DefaultChronicleLogWriter);

        ChronicleLogger cl2 = (ChronicleLogger) l2;
        assertEquals(cl2.getLevel(), ChronicleLogLevel.DEBUG);
        assertEquals(cl2.getName(), "slf4j-chronicle");
        assertTrue(cl2.getWriter() instanceof DefaultChronicleLogWriter);

        ChronicleLogger cl3 = (ChronicleLogger) l3;
        assertEquals(cl3.getLevel(), ChronicleLogLevel.INFO);
        assertTrue(cl3.getWriter() instanceof DefaultChronicleLogWriter);
        assertEquals(cl3.getName(), "logger_1");

        ChronicleLogger cl4 = (ChronicleLogger) l4;
        assertEquals(cl4.getLevel(), ChronicleLogLevel.DEBUG);
        assertTrue(cl4.getWriter() instanceof DefaultChronicleLogWriter);
        assertEquals(cl4.getName(), "readwrite");
    }

    // *************************************************************************
    //
    // *************************************************************************

    @Test
    public void testLogging() throws IOException {
        final String testId = "readwrite";
        final String threadId = testId + "-th";
        final Logger logger = LoggerFactory.getLogger(testId);

        IOTools.deleteDir(basePath(testId));

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
                        assertNotNull("log not found for " + level, wire);
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

    }

    @NotNull
    private static SingleChronicleQueue getChronicleQueue(String testId) {
        return ChronicleQueueBuilder.single(basePath(testId)).build();
    }
}
