package com.higherfrequencytrading.chronology.slf4j;

import com.higherfrequencytrading.chronology.Chronology;
import com.higherfrequencytrading.chronology.ChronologyLogEvent;
import com.higherfrequencytrading.chronology.ChronologyLogHelper;
import com.higherfrequencytrading.chronology.ChronologyLogLevel;
import net.openhft.chronicle.Chronicle;
import net.openhft.chronicle.ExcerptTailer;
import net.openhft.chronicle.IndexedChronicle;
import com.higherfrequencytrading.chronology.slf4j.impl.ChronicleLogWriters;
import net.openhft.lang.io.IOTools;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.impl.StaticLoggerBinder;

import java.io.IOException;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;

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
            "slf4j.chronicle.properties",
            System.getProperty("slf4j.chronicle.indexed.properties")
        );

        getChronicleLoggerFactory().relaod();
        getChronicleLoggerFactory().warmup();
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
        Logger l3 = LoggerFactory.getLogger("Logger1");
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

        assertEquals(cl1.getLevel(), ChronicleLoggingHelper.LOG_LEVEL_DEBUG);
        assertEquals(cl1.getName(), Slf4jVanillaChronicleLoggerTest.class.getName());
        assertTrue(cl1.getWriter().getChronicle() instanceof IndexedChronicle);
        assertTrue(cl1.getWriter() instanceof ChronicleLogWriters.SynchronizedWriter);

        ChronicleLogger cl2 = (ChronicleLogger) l2;
        assertEquals(cl2.getLevel(), ChronicleLoggingHelper.LOG_LEVEL_DEBUG);
        assertEquals(cl2.getName(), Slf4jVanillaChronicleLoggerTest.class.getName());
        assertTrue(cl2.getWriter().getChronicle() instanceof IndexedChronicle);
        assertTrue(cl2.getWriter() instanceof ChronicleLogWriters.SynchronizedWriter);

        ChronicleLogger cl3 = (ChronicleLogger) l3;
        assertEquals(cl3.getLevel(), ChronicleLoggingHelper.LOG_LEVEL_INFO);
        assertTrue(cl3.getWriter().getChronicle() instanceof IndexedChronicle);
        assertTrue(cl3.getWriter() instanceof ChronicleLogWriters.SynchronizedWriter);
        assertEquals(cl3.getName(), "Logger1");

        ChronicleLogger cl4 = (ChronicleLogger) l4;
        assertEquals(cl4.getLevel(), ChronicleLoggingHelper.LOG_LEVEL_DEBUG);
        assertTrue(cl4.getWriter().getChronicle() instanceof IndexedChronicle);
        assertTrue(cl4.getWriter() instanceof ChronicleLogWriters.SynchronizedWriter);
        assertEquals(cl4.getName(), "readwrite");
    }

    // *************************************************************************
    //
    // *************************************************************************

    @Test
    public void testLogging() throws IOException {
        String theradName = "th-test-logging_1";
        String loggerName = "readwrite";
        long   timestamp  = System.currentTimeMillis();

        Thread.currentThread().setName(theradName);

        Logger l = LoggerFactory.getLogger(loggerName);
        l.trace(ChronologyLogLevel.TRACE.levelStr);
        l.debug(ChronologyLogLevel.DEBUG.levelStr);
        l.info(ChronologyLogLevel.INFO.levelStr);
        l.warn(ChronologyLogLevel.WARN.levelStr);
        l.error(ChronologyLogLevel.ERROR.levelStr);

        Chronicle reader = getIndexedChronicle(ChronicleLoggingConfig.TYPE_INDEXED, loggerName);
        ExcerptTailer tailer = reader.createTailer().toStart();
        ChronologyLogEvent evt = null;

        for(int i=1;i< LOG_LEVELS.length; i++) {
            assertTrue(tailer.nextIndex());

            evt = ChronologyLogHelper.decodeBinary(tailer);
            assertNotNull(evt);
            assertEquals(evt.getVersion(), Chronology.VERSION);
            assertEquals(evt.getType(), Chronology.TYPE_SLF4J);
            assertTrue(timestamp <= evt.getTimeStamp());
            assertEquals(LOG_LEVELS[i],evt.getLevel());
            assertEquals(LOG_LEVELS[i].levelStr,evt.getMessage());
            assertEquals(theradName, evt.getThreadName());
            assertNotNull(evt.getArgumentArray());
            assertEquals(0, evt.getArgumentArray().length);
        }

        assertFalse(tailer.nextIndex());

        tailer.close();
        reader.close();
    }

    @Test
    public void testTextLogging() throws IOException {
        Logger l = LoggerFactory.getLogger("Text1");
        l.trace("trace");
        l.debug("debug");
        l.info("info");
        l.warn("warn");
        l.error("error");

        Chronicle reader = new IndexedChronicle(basePath(ChronicleLoggingConfig.TYPE_INDEXED, "text_1"));
        ExcerptTailer tailer = reader.createTailer().toStart();

        assertTrue(tailer.nextIndex());
        assertTrue(tailer.readLine().contains("warn"));

        assertTrue(tailer.nextIndex());
        assertTrue(tailer.readLine().contains("error"));

        assertFalse(tailer.nextIndex());

        tailer.close();
        reader.close();
    }
}
