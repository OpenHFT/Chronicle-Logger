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

import net.openhft.chronicle.Chronicle;
import net.openhft.chronicle.ChronicleQueueBuilder;
import net.openhft.chronicle.ExcerptTailer;
import net.openhft.chronicle.logger.*;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import static org.junit.Assert.*;

public class JulLoggerTestBase extends JulTestBase {

    // *************************************************************************
    //
    // *************************************************************************

    protected static String basePath(String type) {
        String path = System.getProperty("java.io.tmpdir");
        String sep  = System.getProperty("file.separator");

        if(!path.endsWith(sep)) {
            path += sep;
        }

        return path
             + "chronicle-jul-api"
             + System.getProperty("file.separator")
             + type
             + System.getProperty("file.separator")
             + new SimpleDateFormat("yyyyMMdd").format(new Date());
    }

    protected static String basePath(String type, String loggerName) {
        return basePath(type)
             + System.getProperty("file.separator")
             + loggerName;
    }

    protected static String indexedBasePath(String loggerName) {
        return basePath(ChronicleLogConfig.TYPE_INDEXED)
             + System.getProperty("file.separator")
             + loggerName;
    }

    protected static String vanillaBasePath(String loggerName) {
        return basePath(ChronicleLogConfig.TYPE_VANILLA)
             + System.getProperty("file.separator")
             + loggerName;
    }

    protected Chronicle getIndexedChronicle(String id) throws IOException {
        return ChronicleQueueBuilder.indexed(basePath(ChronicleLogConfig.TYPE_INDEXED, id)).build();
    }

    protected Chronicle getIndexedChronicle(String type, String id) throws IOException {
        return ChronicleQueueBuilder.indexed(basePath(type, id)).build();
    }

    protected Chronicle getVanillaChronicle(String id) throws IOException {
        return ChronicleQueueBuilder.vanilla(basePath(ChronicleLogConfig.TYPE_VANILLA, id)).build();
    }

    protected Chronicle getVanillaChronicle(String type, String id) throws IOException {
        return ChronicleQueueBuilder.vanilla(basePath(type, id)).build();
    }

    // *************************************************************************
    //
    // *************************************************************************

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
        final String loggerId,
        final Class<? extends ChronicleLogger> expectedLoggerType,
        final Class<? extends ChronicleLogWriter> expectedWriterType,
        final Level level) throws IOException {

        Logger logger = Logger.getLogger(loggerId);

        assertNotNull(logger);
        assertTrue(logger instanceof ChronicleLogger);
        assertEquals(expectedLoggerType, logger.getClass());
        assertEquals(loggerId, logger.getName());
        assertNotNull(((ChronicleLogger) logger).writer());
        assertEquals(expectedWriterType, ((ChronicleLogger)logger).writer().getClass());
        assertEquals(level, logger.getLevel());
    }

    protected void testBinaryAppender(
            final String testId, final Logger logger, final Chronicle chronicle) throws IOException {

        final String threadId = "thread-" + Thread.currentThread().getId();
        final long timestamp = System.currentTimeMillis();

        Thread.currentThread().setName(threadId);

        for(ChronicleLogLevel level : LOG_LEVELS) {
            log(logger,level,"level is {0}",level);
        }

        ExcerptTailer tailer = chronicle.createTailer().toStart();
        ChronicleLogEvent evt = null;

        for(ChronicleLogLevel level : LOG_LEVELS) {
            assertTrue(tailer.nextIndex());

            evt = ChronicleLogHelper.decodeBinary(tailer);
            assertNotNull(evt);
            assertEquals(evt.getVersion(), ChronicleLog.VERSION);
            assertTrue(evt.getTimeStamp() >= timestamp);
            assertEquals(level, evt.getLevel());
            assertEquals(threadId, evt.getThreadName());
            assertEquals(testId, evt.getLoggerName());
            assertEquals("level is {0}", evt.getMessage());
            assertNotNull(evt.getArgumentArray());
            assertEquals(1, evt.getArgumentArray().length);
            assertEquals(level , evt.getArgumentArray()[0]);
            assertNull(evt.getThrowable());

            tailer.finish();
        }

        logger.log(Level.FINE, "Throwable test 1", new UnsupportedOperationException());
        logger.log(Level.FINE, "Throwable test 2", new UnsupportedOperationException("Exception message"));

        assertTrue(tailer.nextIndex());
        evt = ChronicleLogHelper.decodeBinary(tailer);
        assertEquals("Throwable test 1", evt.getMessage());
        assertNotNull(evt.getThrowable());
        assertTrue(evt.getThrowable() instanceof UnsupportedOperationException);
        assertNull(evt.getThrowable().getMessage());

        assertTrue(tailer.nextIndex());
        evt = ChronicleLogHelper.decodeBinary(tailer);
        assertEquals("Throwable test 2",evt.getMessage());
        assertNotNull(evt.getThrowable());
        assertTrue(evt.getThrowable() instanceof UnsupportedOperationException);
        assertEquals("Exception message",evt.getThrowable().getMessage());

        tailer.close();

        chronicle.close();
        chronicle.clear();
    }

    protected void testTextAppender(
            final String testId, final Logger logger, final Chronicle chronicle) throws IOException {

        final String threadId = "thread-" + Thread.currentThread().getId();
        Thread.currentThread().setName(threadId);

        for(ChronicleLogLevel level : LOG_LEVELS) {
            log(logger,level,"level is {0}",level);
        }

        ExcerptTailer tailer = chronicle.createTailer().toStart();
        ChronicleLogEvent evt = null;

        for(ChronicleLogLevel level : LOG_LEVELS) {
            assertTrue(tailer.nextIndex());

            evt = ChronicleLogHelper.decodeText(tailer);
            assertNotNull(evt);
            assertEquals(level,evt.getLevel());
            assertEquals(threadId, evt.getThreadName());
            assertEquals(testId, evt.getLoggerName());
            assertEquals("level is " + level, evt.getMessage());
            assertNotNull(evt.getArgumentArray());
            assertEquals(0, evt.getArgumentArray().length);

            tailer.finish();
        }

        logger.log(Level.FINE, "Throwable test", new UnsupportedOperationException());
        logger.log(Level.FINE, "Throwable test", new UnsupportedOperationException("Exception message"));

        assertTrue(tailer.nextIndex());
        evt = ChronicleLogHelper.decodeText(tailer);
        assertNotNull(evt);
        assertEquals(threadId, evt.getThreadName());
        assertEquals(testId, evt.getLoggerName());
        assertTrue(evt.getMessage().contains("Throwable test"));
        assertTrue(evt.getMessage().contains(UnsupportedOperationException.class.getName()));
        assertTrue(evt.getMessage().contains(this.getClass().getName()));
        assertNotNull(evt.getArgumentArray());
        assertEquals(0, evt.getArgumentArray().length);
        assertNull(evt.getThrowable());

        assertTrue(tailer.nextIndex());
        evt = ChronicleLogHelper.decodeText(tailer);assertNotNull(evt);
        assertEquals(threadId, evt.getThreadName());
        assertEquals(testId, evt.getLoggerName());
        assertTrue(evt.getMessage().contains("Throwable test"));
        assertTrue(evt.getMessage().contains("Exception message"));
        assertTrue(evt.getMessage().contains(UnsupportedOperationException.class.getName()));
        assertTrue(evt.getMessage().contains(this.getClass().getName()));
        assertNotNull(evt.getArgumentArray());
        assertEquals(0, evt.getArgumentArray().length);
        assertNull(evt.getThrowable());

        tailer.close();

        chronicle.close();
        chronicle.clear();
    }
}
