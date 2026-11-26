/*
 * Copyright 2013-2026 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.logger.log4j2;

import net.openhft.chronicle.core.io.BackgroundResourceReleaser;
import net.openhft.chronicle.core.io.IOTools;
import net.openhft.chronicle.logger.ChronicleLogLevel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.ConfigurationFactory;
import org.apache.logging.log4j.core.config.DefaultConfiguration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class Log4j2TestBase {

    static final ChronicleLogLevel[] LOG_LEVELS = ChronicleLogLevel.values();

    private String originalThreadName;
    private LoggerContext context;

    @BeforeEach
    void setUpFixture() throws IOException, URISyntaxException {
        originalThreadName = Thread.currentThread().getName();
        context = (LoggerContext) LogManager.getContext(false);
        releaseAndDeleteFixture();
        Files.createDirectories(Paths.get(rootPath()));
        java.net.URL configuration = getClass().getClassLoader().getResource("log4j2.xml");
        assertNotNull(configuration, "log4j2.xml not found on classpath");
        context.setConfiguration(ConfigurationFactory.getInstance()
                .getConfiguration(context, "fixture", configuration.toURI()));
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
        // Explicitly invoke the appender's stop() before the context uses timed shutdown.
        for (Appender appender : context.getConfiguration().getAppenders().values())
            appender.stop();
        // Keep the context registered so subsequent logger lookups use this fixture.
        context.setConfiguration(new DefaultConfiguration());
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

        return path + "chronicle-log4j2";
    }

    static String basePath(String type) {
        return rootPath()
                + File.separator
                + type;
    }

    static void log(Logger logger, ChronicleLogLevel level, String fmt, Object... args) {
        switch (level) {
            case TRACE:
                logger.trace(fmt, args);
                break;

            case DEBUG:
                logger.debug(fmt, args);
                break;

            case INFO:
                logger.info(fmt, args);
                break;

            case WARN:
                logger.warn(fmt, args);
                break;

            case ERROR:
                logger.error(fmt, args);
                break;
            default:
                throw new UnsupportedOperationException();
        }
    }

    Appender getAppender(String name) {
        return context.getConfiguration().getAppender(name);
    }
}
