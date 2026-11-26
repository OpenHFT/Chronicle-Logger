/*
 * Copyright 2013-2026 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.logger.log4j1;

import net.openhft.chronicle.core.io.BackgroundResourceReleaser;
import net.openhft.chronicle.core.io.IOTools;
import net.openhft.chronicle.logger.ChronicleLogLevel;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class Log4j1TestBase {

    static final ChronicleLogLevel[] LOG_LEVELS = ChronicleLogLevel.values();

    private String originalThreadName;

    @BeforeEach
    void setUpFixture() throws IOException {
        originalThreadName = Thread.currentThread().getName();
        releaseAndDeleteFixture();
        Files.createDirectories(Paths.get(rootPath()));
        java.net.URL configuration = getClass().getClassLoader().getResource("log4j.xml");
        assertNotNull(configuration, "log4j.xml not found on classpath");
        org.apache.log4j.xml.DOMConfigurator.configure(configuration);
    }

    @AfterEach
    void tearDownFixture() {
        try {
            releaseAndDeleteFixture();
        } finally {
            Thread.currentThread().setName(originalThreadName);
        }
    }

    private void releaseAndDeleteFixture() {
        // Close the configured appenders before removing their queue files.
        LogManager.resetConfiguration();
        BackgroundResourceReleaser.releasePendingResources();
        IOTools.deleteDirWithFiles(rootPath());
        assertFalse(Files.exists(Paths.get(rootPath())), "Logger fixture was not deleted");
    }

    static String rootPath() {
        String path = System.getProperty("java.io.tmpdir");
        String sep = FileSystems.getDefault().getSeparator();

        if (!path.endsWith(sep)) {
            path += sep;
        }

        return path + "chronicle-log4j1";
    }

    static String basePath(String type) {
        return rootPath()
                + File.separator
                + type;
    }

    static void log(Logger logger, ChronicleLogLevel level, String message) {
        switch (level) {
            case TRACE:
                logger.log(Level.TRACE, message);
                break;

            case DEBUG:
                logger.log(Level.DEBUG, message);
                break;

            case INFO:
                logger.log(Level.INFO, message);
                break;

            case WARN:
                logger.log(Level.WARN, message);
                break;

            case ERROR:
                logger.log(Level.ERROR, message);
                break;
            default:
                throw new UnsupportedOperationException();
        }
    }
}
