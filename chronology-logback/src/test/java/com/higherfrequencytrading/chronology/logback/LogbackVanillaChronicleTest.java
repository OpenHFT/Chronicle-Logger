package com.higherfrequencytrading.chronology.logback;

import ch.qos.logback.classic.Level;
import com.higherfrequencytrading.chronology.Chronology;
import com.higherfrequencytrading.chronology.ChronologyLogEvent;
import net.openhft.chronicle.ExcerptTailer;
import net.openhft.chronicle.VanillaChronicle;
import net.openhft.lang.io.IOTools;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import static org.junit.Assert.*;

public class LogbackVanillaChronicleTest extends ChronologyTestBase {

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
            ChronologyLogEvent evt = ChronicleAppenderHelper.read(tailer);
            assertNotNull(evt);
            assertEquals(evt.getVersion(), Chronology.VERSION);
            assertEquals(evt.getType(), Chronology.TYPE_LOGBACK);
            assertTrue(evt.getTimeStamp() >= timestamp);
            assertEquals(level.levelStr,evt.getMessage());
            assertEquals(threadId, evt.getThreadName());
            assertNotNull(evt.getArgumentArray());
            assertEquals(0, evt.getArgumentArray().length);

            tailer.finish();
        }

        tailer.close();
        chronicle.close();
    }
}
