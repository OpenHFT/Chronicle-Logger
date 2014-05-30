package com.higherfrequencytrading.chronology.slf4j;

import com.higherfrequencytrading.chronology.Chronology;
import com.higherfrequencytrading.chronology.ChronologyLogEvent;
import com.higherfrequencytrading.chronology.ChronologyLogHelper;
import com.higherfrequencytrading.chronology.ChronologyLogLevel;
import net.openhft.chronicle.Chronicle;
import net.openhft.chronicle.ExcerptTailer;
import net.openhft.chronicle.IndexedChronicle;
import net.openhft.lang.io.IOTools;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.impl.StaticLoggerBinder;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 *
 */
public class Slf4jIndexedChronicleLoggerTest extends Slf4jTestBase {

    // *************************************************************************
    //
    // *************************************************************************

    @Before
    public void setUp() {
        System.setProperty(
            "slf4j.chronology.properties",
            System.getProperty("slf4j.chronology.indexed.properties")
        );

        getChronicleLoggerFactory().relaod();
    }

    @After
    public void tearDown() {
        getChronicleLoggerFactory().shutdown();

        IOTools.deleteDir(basePath(ChronicleLoggingConfig.TYPE_INDEXED));
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

        assertEquals(cl1.getLevel(), ChronologyLogLevel.DEBUG);
        assertEquals(cl1.getName(), Slf4jVanillaChronicleLoggerTest.class.getName());
        assertTrue(cl1.getWriter().getChronicle() instanceof IndexedChronicle);
        assertTrue(cl1.getWriter() instanceof ChronicleLogAppenders.SynchronizedWriter);

        ChronicleLogger cl2 = (ChronicleLogger) l2;
        assertEquals(cl2.getLevel(), ChronologyLogLevel.DEBUG);
        assertEquals(cl2.getName(), Slf4jVanillaChronicleLoggerTest.class.getName());
        assertTrue(cl2.getWriter().getChronicle() instanceof IndexedChronicle);
        assertTrue(cl2.getWriter() instanceof ChronicleLogAppenders.SynchronizedWriter);

        ChronicleLogger cl3 = (ChronicleLogger) l3;
        assertEquals(cl3.getLevel(), ChronologyLogLevel.INFO);
        assertTrue(cl3.getWriter().getChronicle() instanceof IndexedChronicle);
        assertTrue(cl3.getWriter() instanceof ChronicleLogAppenders.SynchronizedWriter);
        assertEquals(cl3.getName(), "logger_1");

        ChronicleLogger cl4 = (ChronicleLogger) l4;
        assertEquals(cl4.getLevel(), ChronologyLogLevel.DEBUG);
        assertTrue(cl4.getWriter().getChronicle() instanceof IndexedChronicle);
        assertTrue(cl4.getWriter() instanceof ChronicleLogAppenders.SynchronizedWriter);
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

        IOTools.deleteDir(basePath(ChronicleLoggingConfig.TYPE_INDEXED,testId));
        Thread.currentThread().setName(threadId);

        for(ChronologyLogLevel level : LOG_LEVELS) {
            log(logger,level,"level is {}",level);
        }

        Chronicle          chronicle = getIndexedChronicle(ChronicleLoggingConfig.TYPE_INDEXED,testId);
        ExcerptTailer      tailer    = chronicle.createTailer().toStart();
        ChronologyLogEvent evt       = null;

        for(ChronologyLogLevel level : LOG_LEVELS) {
            if(level != ChronologyLogLevel.TRACE) {
                assertTrue(tailer.nextIndex());

                evt = ChronologyLogHelper.decodeBinary(tailer);
                assertNotNull(evt);
                assertEquals(evt.getVersion(), Chronology.VERSION);
                assertEquals(evt.getType(), Chronology.Type.SLF4J);
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

        IOTools.deleteDir(basePath(ChronicleLoggingConfig.TYPE_INDEXED,testId));
    }

    @Test
    public void testTextLogging() throws IOException {
        final String testId    = "text_1";
        final String threadId  = testId + "-th";
        final Logger logger    = LoggerFactory.getLogger(testId);

        IOTools.deleteDir(basePath(ChronicleLoggingConfig.TYPE_INDEXED,testId));
        Thread.currentThread().setName(threadId);

        for(ChronologyLogLevel level : LOG_LEVELS) {
            log(logger,level,"level is {}",level);
        }

        Chronicle          chronicle = getIndexedChronicle(ChronicleLoggingConfig.TYPE_INDEXED,testId);
        ExcerptTailer      tailer    = chronicle.createTailer().toStart();
        ChronologyLogEvent evt       = null;

        for(ChronologyLogLevel level : LOG_LEVELS) {
            assertTrue(tailer.nextIndex());

            evt = ChronologyLogHelper.decodeText(tailer);
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

        IOTools.deleteDir(basePath(ChronicleLoggingConfig.TYPE_INDEXED,testId));

    }
}
