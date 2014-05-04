package com.higherfrequencytrading.chronology.log4j1;

import com.higherfrequencytrading.chronology.Chronology;
import com.higherfrequencytrading.chronology.ChronologyLogEvent;
import com.higherfrequencytrading.chronology.ChronologyLogHelper;
import com.higherfrequencytrading.chronology.ChronologyLogLevel;
import net.openhft.chronicle.Chronicle;
import net.openhft.chronicle.ExcerptTailer;
import net.openhft.lang.io.IOTools;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class Log4j1IndexedChronicleTest extends Log4j1TestBase {

    // *************************************************************************
    //
    // *************************************************************************

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    // *************************************************************************
    // BINARY
    // *************************************************************************

    @Test
    public void testBinaryAppender1() throws IOException {
        final String testId    = "binary-indexed-chronicle";
        final String threadId  = testId + "-th";
        final long   timestamp = System.currentTimeMillis();
        final Logger logger    = LoggerFactory.getLogger(testId);

        IOTools.deleteDir(basePath(testId));
        Thread.currentThread().setName(threadId);

        for(ChronologyLogLevel level : LOG_LEVELS) {
            log(logger,level,"level is {}",level.levelStr);
        }

        Chronicle          chronicle = getIndexedChronicle(testId);
        ExcerptTailer      tailer    = chronicle.createTailer().toStart();
        ChronologyLogEvent evt       = null;

        for(ChronologyLogLevel level : LOG_LEVELS) {
            assertTrue(tailer.nextIndex());

            evt = ChronologyLogHelper.decodeBinary(tailer);
            assertNotNull(evt);
            assertEquals(evt.getVersion(), Chronology.VERSION);
            assertEquals(evt.getType(), Chronology.TYPE_LOG4J_1);
            assertTrue(evt.getTimeStamp() >= timestamp);
            assertEquals(level,evt.getLevel());
            assertEquals(threadId, evt.getThreadName());
            assertEquals(testId, evt.getLoggerName());
            assertEquals("level is " + level.levelStr, evt.getMessage());
            assertNotNull(evt.getArgumentArray());
            assertEquals(0, evt.getArgumentArray().length);

            tailer.finish();
        }

        logger.debug("Throwable test",new UnsupportedOperationException());
        logger.debug("Throwable test",new UnsupportedOperationException("Exception message"));

        assertTrue(tailer.nextIndex());
        evt = ChronologyLogHelper.decodeBinary(tailer);
        assertEquals("Throwable test",evt.getMessage());
        assertNotNull(evt.getThrowable());
        assertTrue(evt.getThrowable() instanceof UnsupportedOperationException);
        assertEquals(UnsupportedOperationException.class.getName(),evt.getThrowable().getMessage());

        assertTrue(tailer.nextIndex());
        evt = ChronologyLogHelper.decodeBinary(tailer);
        assertEquals("Throwable test",evt.getMessage());
        assertNotNull(evt.getThrowable());
        assertTrue(evt.getThrowable() instanceof UnsupportedOperationException);
        assertEquals(UnsupportedOperationException.class.getName() + ": Exception message",evt.getThrowable().getMessage());


        tailer.close();
        chronicle.close();

        IOTools.deleteDir(basePath(testId));
    }

    @Test
    public void testTextAppender1() throws IOException {
        final String testId    = "text-indexed-chronicle";
        final String threadId  = testId + "-th";
        final Logger logger    = LoggerFactory.getLogger(testId);

        IOTools.deleteDir(basePath(testId));
        Thread.currentThread().setName(threadId);

        for(ChronologyLogLevel level : LOG_LEVELS) {
            log(logger,level,"level is {}",level.levelStr);
        }

        Chronicle          chronicle = getIndexedChronicle(testId);
        ExcerptTailer      tailer    = chronicle.createTailer().toStart();
        ChronologyLogEvent evt       = null;

        for(ChronologyLogLevel level : LOG_LEVELS) {
            assertTrue(tailer.nextIndex());

            evt = ChronologyLogHelper.decodeText(tailer);
            assertNotNull(evt);
            assertEquals(level,evt.getLevel());
            assertEquals(threadId, evt.getThreadName());
            assertEquals(testId, evt.getLoggerName());
            assertEquals("level is " + level.levelStr, evt.getMessage());
            assertNotNull(evt.getArgumentArray());
            assertEquals(0, evt.getArgumentArray().length);

            tailer.finish();
        }

        logger.debug("Throwable test",new UnsupportedOperationException());
        logger.debug("Throwable test",new UnsupportedOperationException("Exception message"));

        assertTrue(tailer.nextIndex());
        evt = ChronologyLogHelper.decodeText(tailer);
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
        evt = ChronologyLogHelper.decodeText(tailer);assertNotNull(evt);
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

        IOTools.deleteDir(basePath(testId));
    }
}
