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
 * TODO: add test case for text-logegrs
 */
public class Slf4jIndexedChronicleBinaryLoggerTest extends Slf4jTestBase {

    // *************************************************************************
    //
    // *************************************************************************

    @Before
    public void setUp() {
        System.setProperty(
            "slf4j.chronicle.properties",
            System.getProperty("slf4j.chronicle.indexed.binary.properties"));

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
        Logger logger = LoggerFactory.getLogger(Slf4jIndexedChronicleBinaryLoggerTest.class);

        assertNotNull(logger);
        assertEquals(logger.getClass(), ChronicleLogger.class);

        ChronicleLogger cl = (ChronicleLogger) logger;

        assertEquals(cl.getLevel(), ChronicleLoggingHelper.LOG_LEVEL_DEBUG);
        assertEquals(cl.getName(), Slf4jIndexedChronicleBinaryLoggerTest.class.getName());
        assertTrue(cl.getWriter() instanceof ChronicleLogWriters.SynchronizedWriter);
        assertTrue(cl.getWriter().getChronicle() instanceof IndexedChronicle);
    }

    // *************************************************************************
    //
    // *************************************************************************

    @Test
    public void testLogging1() throws IOException {
        String theradName = "th-test-binary-logging";
        String loggerName = Slf4jIndexedChronicleBinaryLoggerTest.class.getName();
        long   timestamp  = System.currentTimeMillis();

        Thread.currentThread().setName(theradName);

        Logger l = LoggerFactory.getLogger(loggerName);
        l.debug("data {}, {}",
            new MySerializableData("a Serializable object"),
            new MyMarshallableData("a Marshallable object")
        );

        Chronicle reader = getIndexedChronicle(ChronicleLoggingConfig.TYPE_INDEXED, "root-binary");
        ExcerptTailer tailer = reader.createTailer();

        assertTrue(tailer.nextIndex());

        ChronologyLogEvent evt = ChronologyLogHelper.decodeBinary(tailer);
        assertNotNull(evt);
        assertEquals(evt.getVersion(), Chronology.VERSION);
        assertEquals(evt.getType(), Chronology.TYPE_SLF4J);
        assertTrue(timestamp <= evt.getTimeStamp());
        assertEquals(ChronologyLogLevel.DEBUG,evt.getLevel());
        assertEquals("data {}, {}",evt.getMessage());
        assertEquals(theradName, evt.getThreadName());
        assertNotNull(evt.getArgumentArray());
        assertEquals(2, evt.getArgumentArray().length);

        Object serializableObject = evt.getArgumentArray()[0];
        assertNotNull(serializableObject);
        assertTrue(serializableObject instanceof MySerializableData);
        assertEquals(serializableObject.toString(), "a Serializable object");

        Object marshallableObject = evt.getArgumentArray()[1];
        assertNotNull(marshallableObject);
        assertTrue(marshallableObject instanceof MyMarshallableData);
        assertEquals(marshallableObject.toString(), "a Marshallable object");

        tailer.close();
        reader.close();
    }
}
