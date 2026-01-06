/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.logger.slf4j;

import net.openhft.chronicle.logger.ChronicleLogLevel;
import net.openhft.chronicle.logger.ChronicleLogWriter;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Verifies that {@link ChronicleLogger} filters levels correctly and forwards
 * arguments plus throwables with the expected shape.
 */
public class ChronicleLoggerBehaviourTest {

    private String originalThreadName;

    @BeforeEach
    public void captureThreadName() {
        originalThreadName = Thread.currentThread().getName();
        Thread.currentThread().setName("slf4j-behaviour-test");
    }

    @AfterEach
    public void restoreThreadName() {
        Thread.currentThread().setName(originalThreadName);
    }

    @Test
    public void filtersLevelsAndPreservesArguments() {
        RecordingWriter writer = new RecordingWriter();
        ChronicleLogger logger = new ChronicleLogger(writer, "behaviour-logger", ChronicleLogLevel.INFO);

        assertFalse(logger.isTraceEnabled(), "trace should be disabled at INFO threshold");
        assertFalse(logger.isDebugEnabled(), "debug should be disabled at INFO threshold");
        assertTrue(logger.isInfoEnabled(), "info should be enabled at INFO threshold");
        assertTrue(logger.isWarnEnabled(), "warn should be enabled at INFO threshold");
        assertTrue(logger.isErrorEnabled(), "error should be enabled at INFO threshold");

        // below threshold: no events
        logger.trace("trace skipped");
        logger.debug("debug skipped");
        assertEquals(0, writer.events.size(), "events below threshold");

        logger.info("info message");
        logger.info("info one arg {}", 42);
        logger.info("info two args {} {}", "lhs", "rhs");
        IllegalStateException warningThrowable = new IllegalStateException("warn");
        logger.warn("warn with throwable", warningThrowable);
        logger.warn("warn contextual {}", "ctx", new IllegalArgumentException("warn-ctx"));
        logger.error("error varargs {} {}", 1, 2);
        logger.error("error arg and throwable {}", "payload", new RuntimeException("kaboom"));

        List<LoggedEvent> events = writer.events;
        assertEquals(7, events.size(), "seven events expected");

        LoggedEvent first = events.get(0);
        assertEquals(ChronicleLogLevel.INFO, first.level, "event[0] level");
        assertEquals("behaviour-logger", first.loggerName, "event[0] loggerName");
        assertEquals("slf4j-behaviour-test", first.threadName, "event[0] threadName");
        assertEquals("info message", first.message, "event[0] message");
        assertNull(first.throwable, "event[0] throwable");
        assertEquals(0, first.args.length, "event[0] args");

        LoggedEvent second = events.get(1);
        assertEquals("info one arg {}", second.message, "event[1] message");
        assertArrayEquals(new Object[]{42}, second.args, "event[1] args");
        assertNull(second.throwable, "event[1] throwable");

        LoggedEvent third = events.get(2);
        assertEquals("info two args {} {}", third.message, "event[2] message");
        assertArrayEquals(new Object[]{"lhs", "rhs"}, third.args, "event[2] args");

        LoggedEvent fourth = events.get(3);
        assertEquals(ChronicleLogLevel.WARN, fourth.level, "event[3] level");
        assertEquals("warn with throwable", fourth.message, "event[3] message");
        assertEquals(warningThrowable, fourth.throwable, "event[3] throwable");
        assertEquals(0, fourth.args.length, "event[3] args");

        LoggedEvent fifth = events.get(4);
        assertEquals("warn contextual {}", fifth.message, "event[4] message");
        assertArrayEquals(new Object[]{"ctx"}, fifth.args, "event[4] args");
        assertInstanceOf(IllegalArgumentException.class, fifth.throwable, "event[4] throwable type");
        assertEquals("warn-ctx", fifth.throwable.getMessage(), "event[4] throwable message");

        LoggedEvent sixth = events.get(5);
        assertEquals(ChronicleLogLevel.ERROR, sixth.level, "event[5] level");
        assertEquals("error varargs {} {}", sixth.message, "event[5] message");
        assertArrayEquals(new Object[]{1, 2}, sixth.args, "event[5] args");
        assertNull(sixth.throwable, "event[5] throwable");

        LoggedEvent seventh = events.get(6);
        assertEquals("error arg and throwable {}", seventh.message, "event[6] message");
        assertArrayEquals(new Object[]{"payload"}, seventh.args, "event[6] args");
        assertInstanceOf(RuntimeException.class, seventh.throwable, "event[6] throwable type");
        assertEquals("kaboom", seventh.throwable.getMessage(), "event[6] throwable message");
    }

    private static final class RecordingWriter implements ChronicleLogWriter {
        private final List<LoggedEvent> events = new ArrayList<>();

        @Override
        public void write(ChronicleLogLevel level, long timestamp, String threadName, String loggerName, String message) {
            events.add(new LoggedEvent(level, threadName, loggerName, message, null, new Object[0]));
        }

        @Override
        public void write(ChronicleLogLevel level, long timestamp, String threadName, String loggerName, String message, Throwable throwable, Object... args) {
            Object[] safeArgs = args == null ? new Object[0] : Arrays.copyOf(args, args.length);
            events.add(new LoggedEvent(level, threadName, loggerName, message, throwable, safeArgs));
        }

        @Override
        public void close() {
            // no-op
        }
    }

    private static final class LoggedEvent {
        private final ChronicleLogLevel level;
        private final String threadName;
        private final String loggerName;
        private final String message;
        private final Throwable throwable;
        private final Object[] args;

        LoggedEvent(ChronicleLogLevel level,
                    String threadName,
                    String loggerName,
                    String message,
                    Throwable throwable,
                    Object[] args) {
            this.level = level;
            this.threadName = threadName;
            this.loggerName = loggerName;
            this.message = message;
            this.throwable = throwable;
            this.args = args;
        }
    }
}
