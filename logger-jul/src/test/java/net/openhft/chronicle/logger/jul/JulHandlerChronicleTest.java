/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.logger.jul;

import net.openhft.chronicle.core.Jvm;
import net.openhft.chronicle.core.io.IOTools;
import net.openhft.chronicle.logger.ChronicleLogLevel;
import net.openhft.chronicle.queue.ChronicleQueue;
import net.openhft.chronicle.threads.DiskSpaceMonitor;
import net.openhft.chronicle.wire.DocumentContext;
import net.openhft.chronicle.wire.Wire;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import static java.lang.System.currentTimeMillis;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests that {@link ChronicleHandler} writes JUL events to a Chronicle Queue.
 * <p>
 * The LogManager loads a properties file to register the handler. The global
 * {@link DiskSpaceMonitor} is closed before the tests run and the temporary
 * queue directory is removed after each test.
 */
public class JulHandlerChronicleTest extends JulHandlerTestBase {

    @NotNull
    private static ChronicleQueue getChronicleQueue(String testId) {
        return ChronicleQueue.singleBuilder(basePath(testId)).build();
    }

    @BeforeAll
    public static void beforeClass() {
        // DiskSpaceMonitor interferes with this test
        DiskSpaceMonitor.INSTANCE.close();
    }

    @AfterEach
    public void tearDown() {
        IOTools.deleteDirWithFiles(rootPath());
    }

    @Test
    public void testConfiguration() throws IOException {
        setupLogManager("binary-cfg");
        Logger logger = Logger.getLogger("binary-cfg");
        assertEquals(Level.INFO, logger.getLevel(), "Expected binary-cfg logger to be configured with INFO level");
        assertFalse(logger.getUseParentHandlers(), "Expected binary-cfg logger to not use parent handlers to avoid duplicate logging");
        assertNull(logger.getFilter(), "Expected binary-cfg logger to have no filter configured");
        assertNotNull(logger.getHandlers(), "Expected binary-cfg logger to have handlers array initialized");
        assertEquals(1, logger.getHandlers().length, "Expected binary-cfg logger to have exactly one handler configured");

        assertEquals(ChronicleHandler.class, logger.getHandlers()[0].getClass(), "Expected binary-cfg logger's handler to be ChronicleHandler instance");
    }

    @Test
    public void testAppender() throws IOException {
        final String testId = "binary-chronicle";

        setupLogManager(testId);
        Logger logger = Logger.getLogger(testId);

        final String threadId = "thread-" + Jvm.currentThreadId();

        for (ChronicleLogLevel level : LOG_LEVELS) {
            log(logger, level, "level is {0}", level);
        }

        try (final ChronicleQueue cq = getChronicleQueue(testId)) {
            net.openhft.chronicle.queue.ExcerptTailer tailer = cq.createTailer();
            for (ChronicleLogLevel level : LOG_LEVELS) {
                try (DocumentContext dc = tailer.readingDocument()) {
                    Wire wire = dc.wire();
                    assertNotNull(wire, () -> "Expected Chronicle Queue to contain log entry for level=" + level);
                    assertTrue(wire.read("ts").int64() <= currentTimeMillis(), () -> "Expected timestamp field to be <= current time for level=" + level);
                    assertEquals(level, wire.read("level").asEnum(ChronicleLogLevel.class), () -> "Expected level field to match logged level=" + level);
                    assertEquals(threadId, wire.read("threadName").text(), () -> "Expected threadName field to match current thread for level=" + level);
                    assertEquals(testId, wire.read("loggerName").text(), () -> "Expected loggerName field to match logger name '" + testId + "' for level=" + level);
                    assertEquals("level is {0}", wire.read("message").text(), () -> "Expected message field to contain parameterized template for level=" + level);
                    assertTrue(wire.hasMore(), () -> "Expected wire to have message arguments field for level=" + level);
                    List<Object> args = new ArrayList<>();
                    assertTrue(wire.hasMore(), () -> "Expected wire to have args sequence before reading for level=" + level);
                    wire.read("args").sequence(args, (l, vi) -> {
                        while (vi.hasNextSequenceItem()) {
                            l.add(vi.object(Object.class));
                        }
                    });
                    assertEquals(level, args.iterator().next(), () -> "Expected first argument to equal logged level=" + level);
                    assertFalse(wire.hasMore(), () -> "Expected no additional fields after args for level=" + level);
                }
            }
            try (DocumentContext dc = tailer.readingDocument()) {
                Wire wire = dc.wire();
                assertNull(wire, "Expected no additional log entries in Chronicle Queue after reading all logged levels");
            }

            logger.log(Level.FINE, "Throwable test 1", new UnsupportedOperationException());
            logger.log(Level.FINE, "Throwable test 2", new UnsupportedOperationException("Exception message"));

            try (DocumentContext dc = tailer.readingDocument()) {
                Wire wire = dc.wire();
                assertNotNull(wire, "Expected Chronicle Queue to contain log entry for throwable test 1 (exception without message)");
                assertTrue(wire.read("ts").int64() <= currentTimeMillis(), "Expected timestamp field to be <= current time for throwable test 1");
                assertEquals(ChronicleLogLevel.DEBUG, wire.read("level").asEnum(ChronicleLogLevel.class), "Expected JUL Level.FINE to map to ChronicleLogLevel.DEBUG for throwable test 1");
                assertEquals(threadId, wire.read("threadName").text(), "Expected threadName field to match current thread for throwable test 1");
                assertEquals(testId, wire.read("loggerName").text(), "Expected loggerName field to match logger name '" + testId + "' for throwable test 1");
                assertEquals("Throwable test 1", wire.read("message").text(), "Expected message field to contain 'Throwable test 1' text");
                assertTrue(wire.hasMore(), "Expected wire to have throwable field after message for throwable test 1");
                Throwable throwable = wire.read("throwable").throwable(false);
                assertInstanceOf(UnsupportedOperationException.class, throwable, "Expected throwable field to deserialize as UnsupportedOperationException for throwable test 1");
                assertFalse(wire.hasMore(), "Expected no additional fields after throwable for throwable test 1");
            }

            try (DocumentContext dc = tailer.readingDocument()) {
                Wire wire = dc.wire();
                assertNotNull(wire, "Expected Chronicle Queue to contain log entry for throwable test 2 (exception with message)");
                assertTrue(wire.read("ts").int64() <= currentTimeMillis(), "Expected timestamp field to be <= current time for throwable test 2");
                assertEquals(ChronicleLogLevel.DEBUG, wire.read("level").asEnum(ChronicleLogLevel.class), "Expected JUL Level.FINE to map to ChronicleLogLevel.DEBUG for throwable test 2");
                assertEquals(threadId, wire.read("threadName").text(), "Expected threadName field to match current thread for throwable test 2");
                assertEquals(testId, wire.read("loggerName").text(), "Expected loggerName field to match logger name '" + testId + "' for throwable test 2");
                assertEquals("Throwable test 2", wire.read("message").text(), "Expected message field to contain 'Throwable test 2' text");
                assertTrue(wire.hasMore(), "Expected wire to have throwable field after message for throwable test 2");
                Throwable throwable = wire.read("throwable").throwable(false);
                assertInstanceOf(UnsupportedOperationException.class, throwable, "Expected throwable field to deserialize as UnsupportedOperationException for throwable test 2");
                assertEquals("Exception message", throwable.getMessage(), "Expected deserialized exception to preserve original message 'Exception message'");
                assertFalse(wire.hasMore(), "Expected no additional fields after throwable for throwable test 2");
            }

            try (DocumentContext dc = tailer.readingDocument()) {
                Wire wire = dc.wire();
                assertNull(wire, "Expected no additional log entries in Chronicle Queue after reading both throwable tests");
            }
        }

        IOTools.deleteDirWithFiles(basePath(testId));
    }
}
