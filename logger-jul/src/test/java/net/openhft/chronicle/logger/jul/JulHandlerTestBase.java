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
import net.openhft.chronicle.logger.ChronicleLog;
import net.openhft.chronicle.logger.ChronicleLogEvent;
import net.openhft.chronicle.logger.ChronicleLogHelper;
import net.openhft.chronicle.logger.ChronicleLogLevel;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class JulHandlerTestBase extends JulTestBase {

    // *************************************************************************
    //
    // *************************************************************************

    protected static String rootPath() {
        String path = System.getProperty("java.io.tmpdir");
        String sep  = System.getProperty("file.separator");

        if(!path.endsWith(sep)) {
            path += sep;
        }

        return path + "chronicle-jul";
    }

    protected static String basePath(String type) {
        return rootPath()
                + System.getProperty("file.separator")
                + type;
    }

    // *************************************************************************
    //
    // *************************************************************************

    /**
     * @param type
     * @return
     */
    protected Chronicle getIndexedChronicle(String type) throws IOException {
        return ChronicleQueueBuilder.indexed(basePath(type)).build();
    }

    /**
     * @param type
     * @return
     */
    protected Chronicle getVanillaChronicle(String type) throws IOException {
        return ChronicleQueueBuilder.vanilla(basePath(type)).build();
    }

    /**
     *
     * @param id
     * @throws IOException
     */
    protected void setupLogManager(String id) throws IOException {
        String cfgPath = System.getProperty("resources.path");
        File cfgFile = new File(cfgPath, id + ".properties");

        assertNotNull(cfgPath);
        assertTrue(cfgFile.exists());

        LogManager manager = LogManager.getLogManager();
        manager.reset();
        manager.readConfiguration(new FileInputStream(cfgFile));
    }

    // *************************************************************************
    //
    // *************************************************************************

    protected void testChronicleConfiguration(
            String testId, Class<? extends Handler> expectedHandlerType) throws IOException {

        setupLogManager(testId);
        Logger logger = Logger.getLogger(testId);

        assertEquals(Level.INFO, logger.getLevel());
        assertFalse(logger.getUseParentHandlers());
        assertNull(logger.getFilter());
        assertNotNull(logger.getHandlers());
        assertEquals(1, logger.getHandlers().length);

        assertTrue(logger.getHandlers()[0].getClass() == expectedHandlerType);
    }

    protected void testBinaryAppender(
            String testId, Chronicle chronicle) throws IOException {

        final String threadId = "thread-" + Thread.currentThread().getId();
        final long timestamp = System.currentTimeMillis();

        setupLogManager(testId);
        final Logger logger = Logger.getLogger(testId);

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
            String testId, Chronicle chronicle) throws IOException {

        final String threadId = "thread-" + Thread.currentThread().getId();

        setupLogManager(testId);
        final Logger logger = Logger.getLogger(testId);

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
