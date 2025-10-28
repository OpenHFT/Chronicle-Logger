package net.openhft.chronicle.logger.tools;

import net.openhft.chronicle.core.io.IOTools;
import net.openhft.chronicle.logger.ChronicleLogLevel;
import net.openhft.chronicle.logger.ChronicleLogWriter;
import net.openhft.chronicle.logger.DefaultChronicleLogWriter;
import net.openhft.chronicle.queue.ChronicleQueue;
import net.openhft.chronicle.queue.ExcerptTailer;
import net.openhft.chronicle.wire.DocumentContext;
import net.openhft.chronicle.wire.Wire;
import net.openhft.chronicle.wire.WireType;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static java.lang.System.currentTimeMillis;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class ChronicleLogReaderTest {
    private Path queuePath;

    @Before
    public void setup() throws IOException {
        queuePath = Files.createTempDirectory("chronicle-log-reader");
    }

    @Test
    public void readTest() throws Exception {
        try (ChronicleQueue cq = ChronicleQueue.singleBuilder(queuePath).wireType(WireType.BINARY_LIGHT).build()) {
            ChronicleLogWriter writer = new DefaultChronicleLogWriter(cq);
            writer.write(
                    ChronicleLogLevel.INFO,
                    currentTimeMillis(),
                    "thread-1",
                    "binary-chronicle",
                    "test {} {} {}",
                    null,
                    1,
                    100L,
                    100.123D);
            writer.write(
                    ChronicleLogLevel.INFO,
                    currentTimeMillis(),
                    "thread-1",
                    "binary-chronicle",
                    "test {} {} {}",
                    null,
                    2,
                    100L,
                    100.123D);
            writer.write(
                    ChronicleLogLevel.INFO,
                    currentTimeMillis(),
                    "thread-1",
                    "binary-chronicle",
                    "test {} {} {}",
                    null,
                    3,
                    100L,
                    100.123D);
            writer.close();
        }

        // sanity check: queue contains the expected number of entries
        try (ChronicleQueue cq = ChronicleQueue.singleBuilder(queuePath).wireType(WireType.BINARY_LIGHT).build()) {
            ExcerptTailer tailer = cq.createTailer();
            int count = 0;
            while (true) {
                try (DocumentContext dc = tailer.readingDocument()) {
                    Wire wire = dc.wire();
                    if (wire == null) {
                        break;
                    }
                    count++;
                }
            }
            assertEquals(3, count);
        }

        ChronicleLogReader reader = new ChronicleLogReader(queuePath.toString(), WireType.BINARY_LIGHT);
        CapturingProcessor processor = new CapturingProcessor();
        reader.processLogs(processor, false);

        List<LogEvent> events = processor.events();
        assertEquals(3, events.size());
        for (int i = 0; i < events.size(); i++) {
            LogEvent event = events.get(i);
            assertEquals(net.openhft.chronicle.logger.ChronicleLogLevel.INFO, event.level());
            assertEquals("binary-chronicle", event.loggerName());
            assertEquals("thread-1", event.threadName());
            assertEquals("test {} {} {}", event.message());
            assertNull(event.throwable());
            assertEquals(3, event.args().length);
            assertEquals(i + 1, ((Number) event.args()[0]).intValue());
            assertEquals(100L, ((Number) event.args()[1]).longValue());
            assertEquals(100.123D, ((Number) event.args()[2]).doubleValue(), 0.0D);
        }
    }

    @After
    public void tearDown() {
        if (queuePath != null) {
            IOTools.deleteDirWithFiles(queuePath.toString());
        }
    }

    private static final class CapturingProcessor implements ChronicleLogProcessor {
        private final List<LogEvent> captures = new java.util.ArrayList<>();

        @Override
        public void process(long timestamp, net.openhft.chronicle.logger.ChronicleLogLevel level, String loggerName, String threadName, String message, Throwable throwable, Object[] args) {
            captures.add(new LogEvent(level, loggerName, threadName, message, throwable, args));
        }

        List<LogEvent> events() {
            return captures;
        }
    }

    private static final class LogEvent {
        private final net.openhft.chronicle.logger.ChronicleLogLevel level;
        private final String loggerName;
        private final String threadName;
        private final String message;
        private final Throwable throwable;
        private final Object[] args;

        LogEvent(net.openhft.chronicle.logger.ChronicleLogLevel level,
                 String loggerName,
                 String threadName,
                 String message,
                 Throwable throwable,
                 Object[] args) {
            this.level = level;
            this.loggerName = loggerName;
            this.threadName = threadName;
            this.message = message;
            this.throwable = throwable;
            this.args = args;
        }

        net.openhft.chronicle.logger.ChronicleLogLevel level() {
            return level;
        }

        String loggerName() {
            return loggerName;
        }

        String threadName() {
            return threadName;
        }

        String message() {
            return message;
        }

        Throwable throwable() {
            return throwable;
        }

        Object[] args() {
            return args;
        }
    }
}
