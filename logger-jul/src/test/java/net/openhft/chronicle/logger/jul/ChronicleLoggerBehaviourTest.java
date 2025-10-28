package net.openhft.chronicle.logger.jul;

import net.openhft.chronicle.logger.ChronicleLogLevel;
import net.openhft.chronicle.logger.ChronicleLogWriter;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import static org.junit.Assert.*;

/**
 * Behavioural coverage for {@link ChronicleLogger}.
 */
public class ChronicleLoggerBehaviourTest {

    @Test
    public void forwardsLogCallsWhenLevelEnabled() {
        RecordingWriter writer = new RecordingWriter();
        ChronicleLogger logger = new ChronicleLogger(writer, "jul-behaviour", ChronicleLogLevel.INFO);

        assertFalse(logger.isLoggable(Level.FINE));
        assertTrue(logger.isLoggable(Level.INFO));

        logger.log(Level.INFO, "plain");
        logger.log(Level.INFO, "one {0}", 7);
        logger.log(Level.WARNING, "warn with throwable", new IllegalStateException("boom"));
        logger.logp(Level.SEVERE, "source", "method", "p message", new RuntimeException("p"));
        logger.info("info shortcut");
        logger.finer("suppressed finer");
        logger.warning("warning shortcut");

        assertEquals("expected six emitted events", 6, writer.events.size());

        LoggedEvent first = writer.events.get(0);
        assertEquals(ChronicleLogLevel.INFO, first.level);
        assertEquals("plain", first.message);
        assertEquals(0, first.args.length);
        assertNull(first.throwable);

        LoggedEvent second = writer.events.get(1);
        assertEquals("one {0}", second.message);
        assertArrayEquals(new Object[]{7}, second.args);
        assertNull(second.throwable);

        LoggedEvent third = writer.events.get(2);
        assertEquals(ChronicleLogLevel.WARN, third.level);
        assertEquals("warn with throwable", third.message);
        assertTrue(third.throwable instanceof IllegalStateException);
        assertEquals("boom", third.throwable.getMessage());

        LoggedEvent fourth = writer.events.get(3);
        assertEquals(ChronicleLogLevel.ERROR, fourth.level);
        assertEquals("p message", fourth.message);
        assertTrue(fourth.throwable instanceof RuntimeException);
        assertEquals("p", fourth.throwable.getMessage());

        LoggedEvent fifth = writer.events.get(4);
        assertEquals(ChronicleLogLevel.INFO, fifth.level);
        assertEquals("info shortcut", fifth.message);
        assertEquals(0, fifth.args.length);
        assertNull(fifth.throwable);

        LoggedEvent sixth = writer.events.get(5);
        assertEquals(ChronicleLogLevel.WARN, sixth.level);
        assertEquals("warning shortcut", sixth.message);
    }

    @Test
    public void appendLogRecordHonoursThreshold() {
        RecordingWriter writer = new RecordingWriter();
        ChronicleLogger logger = new ChronicleLogger(writer, "jul-record", ChronicleLogLevel.WARN);

        LogRecord below = new LogRecord(Level.CONFIG, "config");
        below.setParameters(new Object[]{"ignored"});
        logger.log(below);
        assertEquals(0, writer.events.size());

        LogRecord atLevel = new LogRecord(Level.SEVERE, "severe");
        atLevel.setParameters(new Object[]{"rhs"});
        atLevel.setThrown(new IllegalArgumentException("bad"));
        logger.log(atLevel);

        assertEquals(1, writer.events.size());
        LoggedEvent event = writer.events.get(0);
        assertEquals(ChronicleLogLevel.ERROR, event.level);
        assertEquals("severe", event.message);
        assertArrayEquals(new Object[]{"rhs"}, event.args);
        assertTrue(event.throwable instanceof IllegalArgumentException);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void setParentIsNotSupported() {
        ChronicleLogger logger = new ChronicleLogger(new NoopWriter(), "jul-parent", ChronicleLogLevel.INFO);
        logger.setParent(Logger.getGlobal());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void enteringThrows() {
        ChronicleLogger logger = new ChronicleLogger(new NoopWriter(), "jul-enter", ChronicleLogLevel.INFO);
        logger.entering("source", "method");
    }

    @Test
    public void nullLoggerIsSingletonAndNoOps() {
        ChronicleLogger nullLogger = ChronicleLogger.Null.INSTANCE;
        assertSame(nullLogger, ChronicleLogger.Null.INSTANCE);
        nullLogger.log(Level.INFO, "ignored");
        nullLogger.log(new LogRecord(Level.SEVERE, "ignored"));
        nullLogger.info("ignored");
    }

    private static final class RecordingWriter implements ChronicleLogWriter {
        private final List<LoggedEvent> events = new ArrayList<>();

        @Override
        public void write(ChronicleLogLevel level, long timestamp, String threadName, String loggerName, String message) {
            events.add(new LoggedEvent(level, threadName, loggerName, message, null, new Object[0]));
        }

        @Override
        public void write(ChronicleLogLevel level, long timestamp, String threadName, String loggerName, String message, Throwable throwable, Object... args) {
            Object[] cloned = args == null ? new Object[0] : Arrays.copyOf(args, args.length);
            events.add(new LoggedEvent(level, threadName, loggerName, message, throwable, cloned));
        }

        @Override
        public void close() {
            // no-op for tests
        }
    }

    private static final class NoopWriter implements ChronicleLogWriter {
        @Override
        public void write(ChronicleLogLevel level, long timestamp, String threadName, String loggerName, String message) {
        }

        @Override
        public void write(ChronicleLogLevel level, long timestamp, String threadName, String loggerName, String message, Throwable throwable, Object... args) {
        }

        @Override
        public void close() {
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
