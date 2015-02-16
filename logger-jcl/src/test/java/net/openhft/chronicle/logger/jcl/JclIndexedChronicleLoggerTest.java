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

package net.openhft.chronicle.logger.jcl;

import net.openhft.chronicle.Chronicle;
import net.openhft.chronicle.ExcerptTailer;
import net.openhft.chronicle.IndexedChronicle;
import net.openhft.chronicle.logger.*;
import net.openhft.lang.io.IOTools;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.*;

@Ignore
public class JclIndexedChronicleLoggerTest extends JclTestBase {

    @Before
    public void setUp() {
        System.setProperty(
            "chronicle.logger.properties",
            "chronicle.logger.indexed.properties"
        );
    }

    @After
    public void tearDown() {
        LogFactory.getFactory().release();
        IOTools.deleteDir(basePath(ChronicleLogConfig.TYPE_INDEXED));
    }

    // *************************************************************************
    //
    // *************************************************************************

    @Test
    public void testLoggerFactory() {
        assertEquals(
            ChronicleLoggerFactory.class,
            LogFactory.getFactory().getClass());
    }

    // *************************************************************************
    //
    // *************************************************************************

    @Test
    public void testIndexedLogger() {
        Log l1 = LogFactory.getLog("jcl-indexed-chronicle");
        Log l2 = LogFactory.getLog("jcl-indexed-chronicle");
        Log l3 = LogFactory.getLog("logger_1");
        Log l4 = LogFactory.getLog("readwrite");

        assertNotNull(l1);
        assertEquals(l1.getClass(), ChronicleLogger.class);

        assertNotNull(l2);
        assertEquals(l2.getClass(), ChronicleLogger.class);

        assertNotNull(l3);
        assertEquals(l3.getClass(), ChronicleLogger.class);

        assertNotNull(l4);
        assertEquals(l4.getClass(), ChronicleLogger.class);

        assertEquals(l1, l2);
        assertNotEquals(l1, l3);
        assertNotEquals(l3, l4);
        assertNotEquals(l1, l4);

        ChronicleLogger cl1 = (ChronicleLogger) l1;

        assertEquals(cl1.level(), ChronicleLogLevel.DEBUG);
        assertEquals(cl1.name(), "jcl-indexed-chronicle");
        assertTrue(cl1.appender().getChronicle() instanceof IndexedChronicle);
        assertTrue(cl1.appender() instanceof ChronicleLogWriters.SynchronizedWriter);

        ChronicleLogger cl2 = (ChronicleLogger) l2;
        assertEquals(cl2.level(), ChronicleLogLevel.DEBUG);
        assertEquals(cl2.name(), "jcl-indexed-chronicle");
        assertTrue(cl2.appender().getChronicle() instanceof IndexedChronicle);
        assertTrue(cl2.appender() instanceof ChronicleLogWriters.SynchronizedWriter);

        ChronicleLogger cl3 = (ChronicleLogger) l3;
        assertEquals(cl3.level(), ChronicleLogLevel.INFO);
        assertEquals(cl3.name(), "logger_1");
        assertTrue(cl3.appender().getChronicle() instanceof IndexedChronicle);
        assertTrue(cl3.appender() instanceof ChronicleLogWriters.SynchronizedWriter);

        ChronicleLogger cl4 = (ChronicleLogger) l4;
        assertEquals(cl4.level(), ChronicleLogLevel.DEBUG);
        assertEquals(cl4.name(), "readwrite");
        assertTrue(cl4.appender().getChronicle() instanceof IndexedChronicle);
        assertTrue(cl4.appender() instanceof ChronicleLogWriters.SynchronizedWriter);
    }

    @Test
    public void testIndexedBinaryLogging() throws IOException {
        final String testId    = "readwrite";
        final String threadId  = testId + "-th";
        final long   timestamp = System.currentTimeMillis();
        final Log    logger    = LogFactory.getLog(testId);

        IOTools.deleteDir(basePath(ChronicleLogConfig.TYPE_INDEXED, testId));
        Thread.currentThread().setName(threadId);

        for(ChronicleLogLevel level : LOG_LEVELS) {
            log(logger,level,"level is " + level.toString());
        }

        final Chronicle chronicle = getIndexedChronicle(ChronicleLogConfig.TYPE_INDEXED,testId);
        final ExcerptTailer tailer = chronicle.createTailer().toStart();

        ChronicleLogEvent evt = null;
        for(ChronicleLogLevel level : LOG_LEVELS) {
            if(level != ChronicleLogLevel.TRACE) {
                assertTrue(tailer.nextIndex());

                evt = ChronicleLogHelper.decodeBinary(tailer);
                assertNotNull(evt);
                assertEquals(evt.getVersion(), ChronicleLog.VERSION);
                assertTrue(evt.getTimeStamp() >= timestamp);
                assertEquals(level, evt.getLevel());
                assertEquals(threadId, evt.getThreadName());
                assertEquals(testId, evt.getLoggerName());
                assertEquals("level is " + level.toString(), evt.getMessage());
                assertNotNull(evt.getArgumentArray());
                assertEquals(0, evt.getArgumentArray().length);

                tailer.finish();
            }
        }

        logger.debug("Throwable test 1",new UnsupportedOperationException());
        logger.debug("Throwable test 2",new UnsupportedOperationException("Exception message"));

        assertTrue(tailer.nextIndex());
        evt = ChronicleLogHelper.decodeBinary(tailer);
        assertEquals("Throwable test 1",evt.getMessage());
        assertNotNull(evt.getThrowable());
        assertTrue(evt.getThrowable() instanceof UnsupportedOperationException);
        assertEquals(UnsupportedOperationException.class.getName(),evt.getThrowable().getMessage());

        assertTrue(tailer.nextIndex());
        evt = ChronicleLogHelper.decodeBinary(tailer);
        assertEquals("Throwable test 2",evt.getMessage());
        assertNotNull(evt.getThrowable());
        assertTrue(evt.getThrowable() instanceof UnsupportedOperationException);
        assertEquals(UnsupportedOperationException.class.getName() + ": Exception message",evt.getThrowable().getMessage());

        tailer.close();
        chronicle.close();

        IOTools.deleteDir(basePath(ChronicleLogConfig.TYPE_INDEXED,testId));
    }

    @Test
    public void testIndexedTextLogging() throws IOException {
        final String testId   = "text_1";
        final String threadId = testId + "-th";
        final Log    logger   = LogFactory.getLog(testId);

        IOTools.deleteDir(basePath(ChronicleLogConfig.TYPE_INDEXED,testId));
        Thread.currentThread().setName(threadId);

        for(ChronicleLogLevel level : LOG_LEVELS) {
            log(logger,level,"level is " + level.toString());
        }

        final Chronicle chronicle = getIndexedChronicle(ChronicleLogConfig.TYPE_INDEXED,testId);
        final ExcerptTailer tailer = chronicle.createTailer().toStart();

        ChronicleLogEvent evt = null;
        for(ChronicleLogLevel level : LOG_LEVELS) {
            assertTrue(tailer.nextIndex());

            evt = ChronicleLogHelper.decodeText(tailer);
            assertNotNull(evt);
            assertEquals(level, evt.getLevel());
            assertEquals(threadId, evt.getThreadName());
            assertEquals(testId, evt.getLoggerName());
            assertEquals("level is " + level, evt.getMessage());
            assertNotNull(evt.getArgumentArray());
            assertEquals(0, evt.getArgumentArray().length);
            assertNull(evt.getThrowable());

            tailer.finish();
        }

        logger.debug("Throwable test",new UnsupportedOperationException());
        logger.debug("Throwable test",new UnsupportedOperationException("Exception message"));

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

        IOTools.deleteDir(basePath(ChronicleLogConfig.TYPE_INDEXED,testId));
    }
}
