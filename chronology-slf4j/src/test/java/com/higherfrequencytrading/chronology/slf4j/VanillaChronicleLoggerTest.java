package com.higherfrequencytrading.chronology.slf4j;

import com.higherfrequencytrading.chronology.Chronology;
import com.higherfrequencytrading.chronology.ChronologyLogLevel;
import net.openhft.chronicle.Chronicle;
import net.openhft.chronicle.ExcerptTailer;
import net.openhft.chronicle.VanillaChronicle;
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
 * TODO: add test case for text-logegrs
 */
public class VanillaChronicleLoggerTest extends ChronicleTestBase {

    // *************************************************************************
    //
    // *************************************************************************

    @Before
    public void setUp() {
        System.setProperty(
            "slf4j.chronicle.properties",
            System.getProperty("slf4j.chronicle.vanilla.properties"));

        getChronicleLoggerFactory().relaod();
        getChronicleLoggerFactory().warmup();
    }

    @After
    public void tearDown() {
        getChronicleLoggerFactory().shutdown();

        IOTools.deleteDir(basePath(ChronicleLoggingConfig.TYPE_VANILLA));
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
        Logger l1 = LoggerFactory.getLogger(VanillaChronicleLoggerTest.class);
        Logger l2 = LoggerFactory.getLogger(VanillaChronicleLoggerTest.class);
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
        assertEquals(cl1.getName(), VanillaChronicleLoggerTest.class.getName());
        assertTrue(cl1.getWriter().getChronicle() instanceof VanillaChronicle);

        ChronicleLogger cl2 = (ChronicleLogger) l2;
        assertEquals(cl2.getLevel(), ChronicleLoggingHelper.LOG_LEVEL_DEBUG);
        assertEquals(cl2.getName(), VanillaChronicleLoggerTest.class.getName());
        assertTrue(cl2.getWriter().getChronicle() instanceof VanillaChronicle);

        ChronicleLogger cl3 = (ChronicleLogger) l3;
        assertEquals(cl3.getLevel(), ChronicleLoggingHelper.LOG_LEVEL_INFO);
        assertTrue(cl3.getWriter().getChronicle() instanceof VanillaChronicle);
        assertEquals(cl3.getName(), "Logger1");

        ChronicleLogger cl4 = (ChronicleLogger) l4;
        assertEquals(cl4.getLevel(), ChronicleLoggingHelper.LOG_LEVEL_DEBUG);
        assertTrue(cl4.getWriter().getChronicle() instanceof VanillaChronicle);
        assertEquals(cl4.getName(), "readwrite");
    }

    // *************************************************************************
    //
    // *************************************************************************

    @Test
    public void testBiaryLogging() throws IOException {
        String theradName = "th-test-logging_1";
        String loggerName = "readwrite";
        long   timestamp  = System.currentTimeMillis();

        Thread.currentThread().setName(theradName);

        Logger l = LoggerFactory.getLogger("readwrite");
        l.trace("trace");
        l.debug("debug");
        l.info("info");
        l.warn("warn");
        l.error("error");

        Chronicle reader = new VanillaChronicle(basePath(ChronicleLoggingConfig.TYPE_VANILLA, loggerName));
        ExcerptTailer tailer = reader.createTailer();

        // debug
        assertTrue(tailer.nextIndex());
        assertEquals(Chronology.VERSION, tailer.readByte());
        assertEquals(Chronology.TYPE_SLF4J, tailer.readByte());
        assertTrue(timestamp <= tailer.readLong());
        assertEquals(ChronologyLogLevel.DEBUG.levelInt, tailer.readInt());
        assertEquals(theradName, tailer.readUTF());
        assertEquals(loggerName, tailer.readUTF());
        assertEquals("debug", tailer.readUTF());

        // info
        assertTrue(tailer.nextIndex());
        assertEquals(Chronology.VERSION, tailer.readByte());
        assertEquals(Chronology.TYPE_SLF4J, tailer.readByte());
        assertTrue(timestamp <= tailer.readLong());
        assertEquals(ChronologyLogLevel.INFO.levelInt, tailer.readInt());
        assertEquals(theradName, tailer.readUTF());
        assertEquals(loggerName, tailer.readUTF());
        assertEquals("info", tailer.readUTF());

        // warn
        assertTrue(tailer.nextIndex());
        assertEquals(Chronology.VERSION, tailer.readByte());
        assertEquals(Chronology.TYPE_SLF4J, tailer.readByte());
        assertTrue(timestamp <= tailer.readLong());
        assertEquals(ChronologyLogLevel.WARN.levelInt, tailer.readInt());
        assertEquals(theradName, tailer.readUTF());
        assertEquals(loggerName, tailer.readUTF());
        assertEquals("warn", tailer.readUTF());

        // error
        assertTrue(tailer.nextIndex());
        assertEquals(Chronology.VERSION, tailer.readByte());
        assertEquals(Chronology.TYPE_SLF4J, tailer.readByte());
        assertTrue(timestamp <= tailer.readLong());
        assertEquals(ChronologyLogLevel.ERROR.levelInt, tailer.readInt());
        assertEquals(theradName, tailer.readUTF());
        assertEquals(loggerName, tailer.readUTF());
        assertEquals("error", tailer.readUTF());

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

        Chronicle reader = new VanillaChronicle(basePath(ChronicleLoggingConfig.TYPE_VANILLA, "text_1"));
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
