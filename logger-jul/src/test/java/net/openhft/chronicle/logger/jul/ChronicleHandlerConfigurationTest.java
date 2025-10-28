package net.openhft.chronicle.logger.jul;

import net.openhft.chronicle.core.io.IOTools;
import net.openhft.chronicle.logger.ChronicleLogLevel;
import net.openhft.chronicle.logger.LogAppenderConfig;
import net.openhft.chronicle.logger.jul.support.AllowAllFilter;
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
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.LogRecord;

import static java.lang.System.currentTimeMillis;
import static org.junit.Assert.*;

/**
 * Exercises {@link ChronicleHandlerConfig} property resolution and verifies
 * that {@link ChronicleHandler} writes expected entries to a Chronicle Queue.
 */
public class ChronicleHandlerConfigurationTest extends JulHandlerTestBase {

    private Path tempDir;
    private String previousBasePath;

    @Before
    public void setUp() throws IOException {
        tempDir = Files.createTempDirectory("chronicle-handler-config");
        previousBasePath = System.getProperty("chronicle.handler.base");
        System.setProperty("chronicle.handler.base", tempDir.toString());
        setupLogManager("chronicle-handler-config");
    }

    @After
    public void tearDown() {
        LogManager.getLogManager().reset();
        if (previousBasePath == null) {
            System.clearProperty("chronicle.handler.base");
        } else {
            System.setProperty("chronicle.handler.base", previousBasePath);
        }
        IOTools.deleteDirWithFiles(tempDir.toString());
    }

    @Test
    public void loadsConfigurationAndPersistsLogEntries() throws Exception {
        ChronicleHandlerConfig config = new ChronicleHandlerConfig(ChronicleHandler.class);

        String expectedPath = tempDir.resolve("config-test").toAbsolutePath().normalize().toString();
        assertEquals("path placeholder should resolve", expectedPath, config.getString("path", null));
        assertEquals(Level.FINE, config.getLevel("level", Level.INFO));
        assertTrue("boolean value of 1 should map to true", config.getBoolean("enabled", false));
        assertEquals("TEXT", config.getString("wireType", "BINARY_LIGHT"));
        assertTrue(config.getFilter("filter", null) instanceof AllowAllFilter);

        LogAppenderConfig appenderConfig = config.getAppenderConfig();
        assertEquals(32, appenderConfig.getBlockSize());
        assertEquals(64L, appenderConfig.getBufferCapacity());
        assertEquals("FAST_DAILY", appenderConfig.getRollCycle());

        ChronicleHandler handler = new ChronicleHandler();
        assertTrue(handler.getFilter() instanceof AllowAllFilter);

        handler.publish(record(Level.CONFIG, "config message {0}", new Object[]{7}, null));
        handler.publish(record(Level.WARNING, "warning message", new Object[0], new IllegalStateException("broken")));
        handler.close();

        Path queueDir = tempDir.resolve("config-test");
        try (ChronicleQueue queue = ChronicleQueue.singleBuilder(queueDir.toString()).wireType(WireType.BINARY_LIGHT).build()) {
            ExcerptTailer tailer = queue.createTailer();

            try (DocumentContext dc = tailer.readingDocument()) {
                Wire wire = dc.wire();
                assertNotNull("first log entry missing", wire);
                assertTrue(wire.read("ts").int64() <= currentTimeMillis());
                assertEquals(ChronicleLogLevel.DEBUG, wire.read("level").asEnum(ChronicleLogLevel.class));
                String threadName = wire.read("threadName").text();
                assertTrue("thread name should start with thread-", threadName.startsWith("thread-"));
                assertEquals("config-logger", wire.read("loggerName").text());
                assertEquals("config message {0}", wire.read("message").text());
                assertTrue("args expected", wire.hasMore());
                List<Object> args = new ArrayList<>();
                wire.read("args").sequence(args, (list, valueIn) -> {
                    while (valueIn.hasNextSequenceItem()) {
                        list.add(valueIn.object(Object.class));
                    }
                });
                assertEquals(1, args.size());
                assertEquals(7, ((Number) args.get(0)).intValue());
                assertFalse("no throwable for config event", wire.hasMore());
            }

            try (DocumentContext dc = tailer.readingDocument()) {
                Wire wire = dc.wire();
                assertNotNull("second log entry missing", wire);
                assertTrue(wire.read("ts").int64() <= currentTimeMillis());
                assertEquals(ChronicleLogLevel.WARN, wire.read("level").asEnum(ChronicleLogLevel.class));
                assertEquals("config-logger", wire.read("loggerName").text());
                assertEquals("warning message", wire.read("message").text());
                assertTrue("throwable expected", wire.hasMore());
                Throwable throwable = wire.read("throwable").throwable(false);
                assertTrue(throwable instanceof IllegalStateException);
                assertEquals("broken", throwable.getMessage());
                assertFalse("no args on warning log", wire.hasMore());
            }

            try (DocumentContext dc = tailer.readingDocument()) {
                assertNull("queue should only contain two events", dc.wire());
            }
        }
    }

    private static LogRecord record(Level level, String message, Object[] args, Throwable throwable) {
        LogRecord record = new LogRecord(level, message);
        record.setLoggerName("config-logger");
        record.setParameters(args);
        record.setThrown(throwable);
        return record;
    }
}
