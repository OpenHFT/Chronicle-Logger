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
 * limitations under the License.
 */

package net.openhft.chronicle.logger.jul;

import net.openhft.chronicle.logger.ChronicleLogWriter;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import static org.junit.Assert.*;

public class JulApiTestBase {

    // *************************************************************************
    //
    // *************************************************************************

    protected static String rootPath() {
        String path = System.getProperty("java.io.tmpdir");
        String sep  = System.getProperty("file.separator");

        if(!path.endsWith(sep)) {
            path += sep;
        }

        return path + "chronicle-jul-api";
    }

    protected static String basePath(String type) {
        return rootPath()
                + System.getProperty("file.separator")
                + type;
    }


    protected static void setupLogger(Class<?> testName) {
        setupLogger(testName.getSimpleName());
    }

    protected static void setupLogger(String id) {
        System.setProperty(
            "java.util.logging.manager",
            ChronicleLoggerManager.class.getName());
        System.setProperty(
            "sun.util.logging.disableCallerCheck",
            "false");
        System.setProperty(
            "chronicle.logger.properties",
            id.endsWith(".properties") ? id : id + ".properties");

        LogManager.getLogManager().reset();
    }

    // *************************************************************************
    //
    // *************************************************************************

    protected void testChronicleConfiguration(
        final Class<?> testType,
        final String loggerId,
        final Class<? extends ChronicleLogger> expectedLoggerType,
        final Class<? extends ChronicleLogWriter> expectedWriterType,
        final Level level) throws IOException {

        setupLogger(testType);
        Logger logger = Logger.getLogger(loggerId);

        assertNotNull(logger);
        assertTrue(logger instanceof ChronicleLogger);
        assertEquals(expectedLoggerType, logger.getClass());
        assertEquals(loggerId, logger.getName());
        assertNotNull(((ChronicleLogger) logger).writer());
        assertEquals(expectedWriterType, ((ChronicleLogger)logger).writer().getClass());
        assertEquals(level, logger.getLevel());
    }
}
