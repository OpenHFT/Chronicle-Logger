package net.openhft.chronicle.logger.jul;

import net.openhft.chronicle.core.io.IOTools;
import net.openhft.chronicle.logger.ChronicleLogManager;
import net.openhft.chronicle.logger.ChronicleLogWriter;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Enumeration;
import java.util.Properties;
import java.util.logging.Logger;

import static org.junit.Assert.*;

/**
 * Covers caching and fallback behaviour for {@link ChronicleLoggerManager}.
 */
public class ChronicleLoggerManagerTest {

    private String previousConfig;
    private Path tempDir;
    private Path configFile;
    private ChronicleLoggerManager manager;
    private ChronicleLogManager logManager;

    @Before
    public void setUp() throws IOException {
        logManager = ChronicleLogManager.getInstance();
        logManager.clear();
        previousConfig = System.getProperty("chronicle.logger.properties");
        tempDir = Files.createTempDirectory("chronicle-jul-manager");
        configFile = tempDir.resolve("chronicle.logger.properties");
        manager = new ChronicleLoggerManager();
    }

    @After
    public void tearDown() {
        manager.reset();
        logManager.clear();
        if (previousConfig == null) {
            System.clearProperty("chronicle.logger.properties");
        } else {
            System.setProperty("chronicle.logger.properties", previousConfig);
        }
        IOTools.deleteDirWithFiles(tempDir.toString());
    }

    @Test
    public void returnsCachedLoggerInstances() throws Exception {
        Path root = Files.createDirectories(tempDir.resolve("root"));
        Path app = Files.createDirectories(tempDir.resolve("cached-app"));
        writeConfig(root, app);

        Logger first = manager.getLogger("app");
        Logger second = manager.getLogger("app");
        assertSame("logger should be cached", first, second);
        assertTrue(first instanceof ChronicleLogger);

        Enumeration<String> names = manager.getLoggerNames();
        assertTrue(names.hasMoreElements());
        assertEquals("app", names.nextElement());
    }

    @Test
    public void resetClearsCachedLoggers() throws Exception {
        Path root = Files.createDirectories(tempDir.resolve("root"));
        Path app = Files.createDirectories(tempDir.resolve("reset-app"));
        writeConfig(root, app);

        Logger logger = manager.getLogger("app");
        assertTrue(logger instanceof ChronicleLogger);

        manager.reset();
        assertFalse(manager.getLoggerNames().hasMoreElements());
    }

    @Test
    public void returnsNullLoggerWhenWriterCreationFails() throws Exception {
        Properties props = new Properties();
        props.setProperty("chronicle.logger.root.level", "info");
        writeConfig(props);

        Logger logger = manager.getLogger("missing");
        assertSame(ChronicleLogger.Null.INSTANCE, logger);
    }

    private void writeConfig(Path rootPath, Path loggerPath) throws IOException {
        Properties props = new Properties();
        props.setProperty("chronicle.logger.root.path", rootPath.toString());
        props.setProperty("chronicle.logger.root.level", "debug");
        props.setProperty("chronicle.logger.app.path", loggerPath.toString());
        props.setProperty("chronicle.logger.app.level", "info");
        writeConfig(props);
    }

    private void writeConfig(Properties props) throws IOException {
        try (Writer out = Files.newBufferedWriter(configFile, StandardCharsets.ISO_8859_1)) {
            props.store(out, "chronicle-jul-manager-test");
        }
        System.setProperty("chronicle.logger.properties", configFile.toString());
        logManager.reload();
    }
}
