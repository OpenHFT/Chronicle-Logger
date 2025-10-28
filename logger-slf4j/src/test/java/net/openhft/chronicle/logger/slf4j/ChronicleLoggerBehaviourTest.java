package net.openhft.chronicle.logger.slf4j;

import net.openhft.chronicle.logger.ChronicleLogLevel;
import net.openhft.chronicle.logger.ChronicleLogWriter;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Verifies that {@link ChronicleLogger} filters levels correctly and forwards
 * arguments plus throwables with the expected shape.
 */
public class ChronicleLoggerBehaviourTest {

    private String originalThreadName;

    @Before
    public void captureThreadName() {
        originalThreadName = Thread.currentThread().getName();
        Thread.currentThread().setName("slf4j-behaviour-test");
    }

    @After
    public void restoreThreadName() {
        Thread.currentThread().setName(originalThreadName);
    }

    @Test
    public void filtersLevelsAndPreservesArguments() {
        RecordingWriter writer = new RecordingWriter();
        ChronicleLogger logger = new ChronicleLogger(writer, "behaviour-logger", ChronicleLogLevel.INFO);

        assertFalse("trace should be disabled at INFO threshold", logger.isTraceEnabled());
        assertFalse("debug should be disabled at INFO threshold", logger.isDebugEnabled());
        assertTrue(logger.isInfoEnabled());
        assertTrue(logger.isWarnEnabled());
        assertTrue(logger.isErrorEnabled());

        // below threshold: no events
        logger.trace("trace skipped");
        logger.debug("debug skipped");
        assertEquals(0, writer.events.size());

        logger.info("info message");
        logger.info("info one arg {}", 42);
        logger.info("info two args {} {}", "lhs", "rhs");
        IllegalStateException warningThrowable = new IllegalStateException("warn");
        logger.warn("warn with throwable", warningThrowable);
        logger.warn("warn contextual {}", "ctx", new IllegalArgumentException("warn-ctx"));
        logger.error("error varargs {} {}", 1, 2);
        logger.error("error arg and throwable {}", "payload", new RuntimeException("kaboom"));

        List<LoggedEvent> events = writer.events;
        assertEquals("seven events expected", 7, events.size());

        LoggedEvent first = events.get(0);
        assertEquals(ChronicleLogLevel.INFO, first.level);
        assertEquals("behaviour-logger", first.loggerName);
        assertEquals("slf4j-behaviour-test", first.threadName);
        assertEquals("info message", first.message);
        assertNull(first.throwable);
        assertEquals(0, first.args.length);

        LoggedEvent second = events.get(1);
        assertEquals("info one arg {}", second.message);
        assertArrayEquals(new Object[]{42}, second.args);
        assertNull(second.throwable);

        LoggedEvent third = events.get(2);
        assertEquals("info two args {} {}", third.message);
        assertArrayEquals(new Object[]{"lhs", "rhs"}, third.args);

        LoggedEvent fourth = events.get(3);
        assertEquals(ChronicleLogLevel.WARN, fourth.level);
        assertEquals("warn with throwable", fourth.message);
        assertEquals(warningThrowable, fourth.throwable);
        assertEquals(0, fourth.args.length);

        LoggedEvent fifth = events.get(4);
        assertEquals("warn contextual {}", fifth.message);
        assertArrayEquals(new Object[]{"ctx"}, fifth.args);
        assertTrue(fifth.throwable instanceof IllegalArgumentException);
        assertEquals("warn-ctx", fifth.throwable.getMessage());

        LoggedEvent sixth = events.get(5);
        assertEquals(ChronicleLogLevel.ERROR, sixth.level);
        assertEquals("error varargs {} {}", sixth.message);
        assertArrayEquals(new Object[]{1, 2}, sixth.args);
        assertNull(sixth.throwable);

        LoggedEvent seventh = events.get(6);
        assertEquals("error arg and throwable {}", seventh.message);
        assertArrayEquals(new Object[]{"payload"}, seventh.args);
        assertTrue(seventh.throwable instanceof RuntimeException);
        assertEquals("kaboom", seventh.throwable.getMessage());
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
