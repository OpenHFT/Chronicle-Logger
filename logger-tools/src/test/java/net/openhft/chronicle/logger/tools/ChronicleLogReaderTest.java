/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.logger.tools;

import net.openhft.chronicle.core.io.IOTools;
import net.openhft.chronicle.wire.WireType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class ChronicleLogReaderTest {
    @BeforeEach
    public void setup() {
        URL configUrl = ChronicleLogReaderTest.class.getResource("/logback-chronicle-binary-appender.xml");
        assertNotNull(configUrl, "logback test configuration");
        System.setProperty("logback.configurationFile", configUrl.toExternalForm());
    }

    @Test
    public void readTest() {
        final Logger logger = LoggerFactory.getLogger("binary-chronicle");
        assertNotNull(logger, "logger instance should be created by LoggerFactory");

        String queuePath = Paths.get(System.getProperty("java.io.tmpdir"), "chronicle-logback", "binary-chronicle").toString();
        IOTools.deleteDirWithFiles(queuePath);

        try {
            logger.info("test {} {} {}", 1, 100L, 100.123D);
            logger.info("test {} {} {}", 2, 100L, 100.123D);
            logger.info("test {} {} {}", 3, 100L, 100.123D);

            List<String> messages = new ArrayList<>();
            List<Object[]> args = new ArrayList<>();
            ChronicleLogReader reader = new ChronicleLogReader(queuePath, WireType.BINARY_LIGHT);
            reader.processLogs((timestamp, level, threadName, loggerName, message, throwable, arguments) -> {
                messages.add(message);
                args.add(arguments);
            }, false);

            assertEquals(3, messages.size(), "log entries read");
            assertEquals("test {} {} {}", messages.get(0), "message template");
            assertArrayEquals(new Object[]{1, 100L, 100.123D}, args.get(0), "first log entry arguments should match expected values");
            assertArrayEquals(new Object[]{2, 100L, 100.123D}, args.get(1), "second log entry arguments should match expected values");
            assertArrayEquals(new Object[]{3, 100L, 100.123D}, args.get(2), "third log entry arguments should match expected values");
        } finally {
            IOTools.deleteDirWithFiles(queuePath);
        }
    }
}
