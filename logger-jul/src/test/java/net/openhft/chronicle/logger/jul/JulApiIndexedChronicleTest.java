/*
 * Copyright 2014 Higher Frequency Trading
 *
 * http://www.higherfrequencytrading.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 */
package net.openhft.chronicle.logger.jul;

import net.openhft.chronicle.logger.ChronicleLogConfig;
import net.openhft.chronicle.logger.ChronicleLogHelper;
import net.openhft.chronicle.logger.ChronicleLogWriters;
import net.openhft.chronicle.tools.ChronicleTools;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class JulApiIndexedChronicleTest extends JulApiTestBase {

    @Before
    public void setUp() {
        setupLogger(getClass());
    }

    @After
    public void tearDown() {
    }

    // *************************************************************************
    //
    // *************************************************************************

    @Test
    public void testIndexedChronicleConfiguration() throws IOException {
        testChronicleConfiguration(
            "logger",
            ChronicleLogger.Binary.class,
            ChronicleLogWriters.SynchronizedWriter.class,
            Level.FINE);
        testChronicleConfiguration(
            "logger_1",
            ChronicleLogger.Binary.class,
            ChronicleLogWriters.SynchronizedWriter.class,
            Level.INFO);
        testChronicleConfiguration(
            "logger_2",
            ChronicleLogger.Text.class,
            ChronicleLogWriters.SynchronizedWriter.class,
            Level.FINER);
        testChronicleConfiguration(
            "logger_bin",
            ChronicleLogger.Binary.class,
            ChronicleLogWriters.SynchronizedWriter.class,
            Level.FINER);
        testChronicleConfiguration(
            "logger_txt",
            ChronicleLogger.Text.class,
            ChronicleLogWriters.SynchronizedWriter.class,
            Level.FINER);
    }

    // *************************************************************************
    //
    // *************************************************************************

    @Test
    public void testIndexedBinaryAppender() throws IOException {
        final String testId = "logger_bin";
        final String basePath = basePath(ChronicleLogConfig.TYPE_INDEXED, testId);

        ChronicleTools.deleteOnExit(basePath);

        testBinaryAppender(
                testId,
                Logger.getLogger(testId),
                getIndexedChronicle(testId)
        );
    }

    @Test
    public void testIndexedTextAppender() throws IOException {
        final String testId = "logger_txt";
        final String basePath = basePath(ChronicleLogConfig.TYPE_INDEXED, testId);

        ChronicleTools.deleteOnExit(basePath);

        testTextAppender(
                testId,
                Logger.getLogger(testId),
                getIndexedChronicle(testId)
        );
    }
}
