package net.openhft.chronicle.logger.jul;

import net.openhft.chronicle.core.io.IOTools;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.LogManager;

import static org.junit.Assert.assertNotNull;

/**
 * Ensures {@link ChronicleHandler} surfaces failures when the configured path
 * refers to an invalid location.
 */
public class ChronicleHandlerFailureTest extends JulHandlerTestBase {

    private Path tempDir;
    private String previousPath;

    @Before
    public void setUp() throws IOException {
        tempDir = Files.createTempDirectory("chronicle-handler-failure");
        previousPath = System.getProperty("chronicle.handler.invalid");
    }

    @After
    public void tearDown() {
        LogManager.getLogManager().reset();
        if (previousPath == null) {
            System.clearProperty("chronicle.handler.invalid");
        } else {
            System.setProperty("chronicle.handler.invalid", previousPath);
        }
        IOTools.deleteDirWithFiles(tempDir.toString());
    }

    @Test
    public void throwsWhenPathPointsToFile() throws IOException {
        Path pathAsFile = Files.createTempFile(tempDir, "existing-queue", ".txt");
        System.setProperty("chronicle.handler.invalid", pathAsFile.toString());
        setupLogManager("chronicle-handler-failure");

        RuntimeException failure = null;
        try {
            new ChronicleHandler();
        } catch (IOException ioException) {
            failure = new RuntimeException(ioException);
        } catch (RuntimeException runtimeException) {
            failure = runtimeException;
        }
        assertNotNull("ChronicleHandler should not start when the queue path is invalid", failure);
    }
}
