package com.higherfrequencytrading.chronology.logback;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import net.openhft.chronicle.ExcerptTailer;
import net.openhft.chronicle.VanillaChronicle;
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

public class LogbackVanillaChronicleTest extends ChronicleTestBase {

    // *************************************************************************
    //
    // *************************************************************************

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
        IOTools.deleteDir(rootPath());
    }

    // *************************************************************************
    //
    // *************************************************************************

    @Test
    public void testAppender() throws IOException {
        final String testId    = "vanilla-chronicle";
        final String threadId  = testId + "-th";
        final long   timestamp = System.currentTimeMillis();

        Thread.currentThread().setName(threadId);

        Logger l = LoggerFactory.getLogger(testId);

        l.trace(Level.TRACE.levelStr);
        l.debug(Level.DEBUG.levelStr);
        l.info(Level.INFO.levelStr);
        l.warn(Level.WARN.levelStr);
        l.error(Level.ERROR.levelStr);

        VanillaChronicle chronicle = getVanillaChronicle(testId);
        ExcerptTailer    tailer    = chronicle.createTailer().toStart();

        for(Level level : LOG_LEVELS) {
            assertTrue(tailer.nextIndex());
            ILoggingEvent evt = ChronicleAppenderHelper.read(tailer);
            assertNotNull(evt);
            assertTrue(evt.getTimeStamp() >= timestamp);
            assertEquals(level.levelStr,evt.getMessage());
            assertEquals(threadId,evt.getThreadName());
            assertNotNull(evt.getArgumentArray());
            assertEquals(0, evt.getArgumentArray().length);
            assertNotNull(evt.getCallerData());
            assertTrue(evt.getCallerData().length > 0);
            assertNull(evt.getThrowableProxy());

            tailer.finish();
        }

        tailer.close();
        chronicle.close();
    }


    @Test
    public void testAppenderReduced() throws IOException {
        final String testId    = "vanilla-chronicle-reduced";
        final String threadId  = testId + "-th";
        final long   timestamp = System.currentTimeMillis();

        Thread.currentThread().setName(threadId);

        Logger l = LoggerFactory.getLogger(testId);

        l.trace(Level.TRACE.levelStr);
        l.debug(Level.DEBUG.levelStr);
        l.info(Level.INFO.levelStr);
        l.warn(Level.WARN.levelStr);
        l.error(Level.ERROR.levelStr);

        VanillaChronicle chronicle = getVanillaChronicle(testId);
        ExcerptTailer    tailer    = chronicle.createTailer().toStart();

        for(Level level : LOG_LEVELS) {
            assertTrue(tailer.nextIndex());
            ILoggingEvent evt = ChronicleAppenderHelper.read(tailer);
            assertNotNull(evt);
            assertTrue(evt.getTimeStamp() >= timestamp);
            assertEquals(level.levelStr,evt.getMessage());
            assertEquals(threadId,evt.getThreadName());
            assertNotNull(evt.getArgumentArray());
            assertEquals(0, evt.getArgumentArray().length);
            assertNotNull(evt.getCallerData());
            assertEquals(0, evt.getCallerData().length);
            assertNull(evt.getThrowableProxy());

            tailer.finish();
        }

        tailer.close();
        chronicle.close();
    }
}
