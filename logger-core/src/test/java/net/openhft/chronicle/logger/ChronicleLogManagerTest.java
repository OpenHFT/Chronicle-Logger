package net.openhft.chronicle.logger;

import net.openhft.chronicle.core.io.IOTools;
import net.openhft.chronicle.queue.ChronicleQueue;
import net.openhft.chronicle.queue.ExcerptTailer;
import net.openhft.chronicle.wire.DocumentContext;
import net.openhft.chronicle.wire.Wire;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

/**
 * Covers configuration loading and reload semantics for {@link ChronicleLogManager}.
 */
public class ChronicleLogManagerTest {

    private ChronicleLogManager manager;
    private String previousConfig;
    private Path tempDir;
    private Path configFile;

    @Before
    public void setUp() throws IOException {
        manager = ChronicleLogManager.getInstance();
        manager.clear();
        previousConfig = System.getProperty("chronicle.logger.properties");
        tempDir = Files.createTempDirectory("chronicle-logger-test");
        configFile = tempDir.resolve("chronicle.logger.properties");
    }

    @After
    public void tearDown() {
        manager.clear();
        if (previousConfig == null) {
            System.clearProperty("chronicle.logger.properties");
        } else {
            System.setProperty("chronicle.logger.properties", previousConfig);
        }
        manager.reload();
        IOTools.deleteDirWithFiles(tempDir.toString());
    }

    @Test
    public void loadsWriterFromExplicitConfiguration() throws Exception {
        Path rootPath = Files.createDirectories(tempDir.resolve("root"));
        Path loggerPath = Files.createDirectories(tempDir.resolve("app"));

        reloadWithConfig(rootPath, loggerPath);

        ChronicleLogWriter writer = manager.getWriter("app");
        writer.write(
                ChronicleLogLevel.INFO,
                System.currentTimeMillis(),
                "thread-one",
                "app",
                "first-message");
        writer.close();

        assertQueueContainsOnlyMessage(loggerPath, "first-message");
    }

    @Test
    public void reloadRecreatesWriterWhenPathChanges() throws Exception {
        Path rootPath = Files.createDirectories(tempDir.resolve("root"));
        Path firstPath = Files.createDirectories(tempDir.resolve("first"));
        Path secondPath = Files.createDirectories(tempDir.resolve("second"));

        reloadWithConfig(rootPath, firstPath);

        ChronicleLogWriter writer = manager.getWriter("app");
        writer.write(
                ChronicleLogLevel.INFO,
                System.currentTimeMillis(),
                "thread-A",
                "app",
                "first-message");
        writer.close();

        reloadWithConfig(rootPath, secondPath);

        ChronicleLogWriter newWriter = manager.getWriter("app");
        newWriter.write(
                ChronicleLogLevel.INFO,
                System.currentTimeMillis(),
                "thread-B",
                "app",
                "second-message");
        newWriter.close();

        assertQueueContainsOnlyMessage(secondPath, "second-message");
        assertQueueContainsOnlyMessage(firstPath, "first-message");
    }

    private void reloadWithConfig(Path rootPath, Path loggerPath) throws IOException {
        writeConfig(rootPath, loggerPath);
        System.setProperty("chronicle.logger.properties", configFile.toString());
        manager.reload();
    }

    private void writeConfig(Path rootPath, Path loggerPath) throws IOException {
        Files.createDirectories(rootPath);
        Files.createDirectories(loggerPath);

        Properties props = new Properties();
        props.setProperty("chronicle.logger.root.path", rootPath.toString());
        props.setProperty("chronicle.logger.root.level", "debug");
        props.setProperty("chronicle.logger.app.path", loggerPath.toString());
        props.setProperty("chronicle.logger.app.level", "info");

        try (Writer out = Files.newBufferedWriter(configFile, StandardCharsets.ISO_8859_1)) {
            props.store(out, "test configuration");
        }
    }

    private static void assertQueueContainsOnlyMessage(Path queuePath, String expectedMessage) {
        try (ChronicleQueue queue = ChronicleQueue.singleBuilder(queuePath.toString()).build()) {
            ExcerptTailer tailer = queue.createTailer();
            try (DocumentContext dc = tailer.readingDocument()) {
                Wire wire = dc.wire();
                assertNotNull("Queue entry missing for " + queuePath, wire);
                assertEquals(expectedMessage, wire.read("message").text());
            }

            try (DocumentContext dc = tailer.readingDocument()) {
                Wire wire = dc.wire();
                assertNull("Queue contains unexpected extra entries for " + queuePath, wire);
            }
        }
    }
}
