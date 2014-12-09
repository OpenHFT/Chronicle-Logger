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

package net.openhft.chronicle.logger.slf4j;

import net.openhft.chronicle.Chronicle;
import net.openhft.chronicle.ExcerptTailer;
import net.openhft.chronicle.VanillaChronicle;
import net.openhft.chronicle.logger.ChronicleLog;
import net.openhft.chronicle.logger.ChronicleLogConfig;
import net.openhft.chronicle.logger.ChronicleLogEvent;
import net.openhft.chronicle.logger.ChronicleLogHelper;
import net.openhft.chronicle.logger.ChronicleLogLevel;
import net.openhft.lang.io.IOTools;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.impl.StaticLoggerBinder;

import java.io.IOException;

import static org.junit.Assert.*;

/**
 * TODO: add test case for text-logegrs
 */
public class Slf4jVanillaChronicleLoggerTest extends Slf4jTestBase {

    // *************************************************************************
    //
    // *************************************************************************

    @Before
    public void setUp() {
        System.setProperty(
            "chronicle.logger.properties",
            System.getProperty("chronicle.logger.vanilla.properties"));

        getChronicleLoggerFactory().reload();
    }

    @After
    public void tearDown() {
        getChronicleLoggerFactory().shutdown();

        IOTools.deleteDir(basePath(ChronicleLogConfig.TYPE_VANILLA));
    }

    // *************************************************************************
    //
    // *************************************************************************

    @Test
    public void testLoggerFactory() {
        assertEquals(
            StaticLoggerBinder.getSingleton().getLoggerFactory().getClass(),
            ChronicleLoggerFactory.class);
    }

    @Test
    public void testLogger() {
        Logger l1 = LoggerFactory.getLogger(Slf4jVanillaChronicleLoggerTest.class);
        Logger l2 = LoggerFactory.getLogger(Slf4jVanillaChronicleLoggerTest.class);
        Logger l3 = LoggerFactory.getLogger("logger_1");
        Logger l4 = LoggerFactory.getLogger("readwrite");

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

        assertEquals(cl1.getLevel(), ChronicleLogLevel.DEBUG);
        assertEquals(cl1.getName(), Slf4jVanillaChronicleLoggerTest.class.getName());
        assertTrue(cl1.getWriter().getChronicle() instanceof VanillaChronicle);

        ChronicleLogger cl2 = (ChronicleLogger) l2;
        assertEquals(cl2.getLevel(), ChronicleLogLevel.DEBUG);
        assertEquals(cl2.getName(), Slf4jVanillaChronicleLoggerTest.class.getName());
        assertTrue(cl2.getWriter().getChronicle() instanceof VanillaChronicle);

        ChronicleLogger cl3 = (ChronicleLogger) l3;
        assertEquals(cl3.getLevel(), ChronicleLogLevel.INFO);
        assertTrue(cl3.getWriter().getChronicle() instanceof VanillaChronicle);
        assertEquals(cl3.getName(), "logger_1");

        ChronicleLogger cl4 = (ChronicleLogger) l4;
        assertEquals(cl4.getLevel(), ChronicleLogLevel.DEBUG);
        assertTrue(cl4.getWriter().getChronicle() instanceof VanillaChronicle);
        assertEquals(cl4.getName(), "readwrite");
    }

    // *************************************************************************
    //
    // *************************************************************************

    @Test
    public void testLogging() throws IOException {
        final String testId    = "readwrite";
        final String threadId  = testId + "-th";
        final long   timestamp = System.currentTimeMillis();
        final Logger logger    = LoggerFactory.getLogger(testId);

        IOTools.deleteDir(basePath(ChronicleLogConfig.TYPE_VANILLA,testId));
        Thread.currentThread().setName(threadId);

        for(ChronicleLogLevel level : LOG_LEVELS) {
            log(logger,level,"level is {}",level);
        }

        Chronicle         chronicle = getVanillaChronicle(ChronicleLogConfig.TYPE_VANILLA,testId);
        ExcerptTailer     tailer    = chronicle.createTailer().toStart();
        ChronicleLogEvent evt       = null;

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
                assertEquals("level is " + level, evt.getMessage());
                assertNotNull(evt.getArgumentArray());
                assertEquals(0, evt.getArgumentArray().length);

                tailer.finish();
            }
        }

        logger.debug("Throwable test",new UnsupportedOperationException());
        logger.debug("Throwable test",new UnsupportedOperationException("Exception message"));

        assertTrue(tailer.nextIndex());
        evt = ChronicleLogHelper.decodeBinary(tailer);
        assertEquals("Throwable test",evt.getMessage());
        assertNotNull(evt.getThrowable());
        assertTrue(evt.getThrowable() instanceof UnsupportedOperationException);
        assertEquals(UnsupportedOperationException.class.getName(),evt.getThrowable().getMessage());

        assertTrue(tailer.nextIndex());
        evt = ChronicleLogHelper.decodeBinary(tailer);
        assertEquals("Throwable test",evt.getMessage());
        assertNotNull(evt.getThrowable());
        assertTrue(evt.getThrowable() instanceof UnsupportedOperationException);
        assertEquals(UnsupportedOperationException.class.getName() + ": Exception message",evt.getThrowable().getMessage());


        tailer.close();
        chronicle.close();

        IOTools.deleteDir(basePath(ChronicleLogConfig.TYPE_VANILLA,testId));
    }

    @Test
    public void testTextLogging() throws IOException {
        final String testId    = "text_1";
        final String threadId  = testId + "-th";
        final Logger logger    = LoggerFactory.getLogger(testId);

        IOTools.deleteDir(basePath(ChronicleLogConfig.TYPE_VANILLA,testId));
        Thread.currentThread().setName(threadId);

        for(ChronicleLogLevel level : LOG_LEVELS) {
            log(logger,level,"level is {}",level);
        }

        Chronicle          chronicle = getVanillaChronicle(ChronicleLogConfig.TYPE_VANILLA,testId);
        ExcerptTailer      tailer    = chronicle.createTailer().toStart();
        ChronicleLogEvent evt       = null;

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

        IOTools.deleteDir(basePath(ChronicleLogConfig.TYPE_VANILLA,testId));

    }
}
