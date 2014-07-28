package com.higherfrequencytrading.chronology.slf4j;

import com.higherfrequencytrading.chronology.Chronology;
import com.higherfrequencytrading.chronology.ChronologyLogEvent;
import com.higherfrequencytrading.chronology.ChronologyLogHelper;
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * TODO: add test case for text-logegrs
 */
public class Slf4jVanillaChronicleBinaryLoggerTest extends Slf4jTestBase {

    // *************************************************************************
    //
    // *************************************************************************

    @Before
    public void setUp() {
        System.setProperty(
            "slf4j.chronology.properties",
            System.getProperty("slf4j.chronology.vanilla.binary.properties"));

        getChronicleLoggerFactory().relaod();
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
        Logger logger = LoggerFactory.getLogger(Slf4jVanillaChronicleBinaryLoggerTest.class);

        assertNotNull(logger);
        assertEquals(logger.getClass(), ChronicleLogger.class);

        ChronicleLogger cl = (ChronicleLogger) logger;

        assertEquals(cl.getLevel(), ChronologyLogLevel.DEBUG);
        assertEquals(cl.getName(), Slf4jVanillaChronicleBinaryLoggerTest.class.getName());
        assertTrue(cl.getWriter() instanceof ChronicleLogAppenders.BinaryWriter);
        assertTrue(cl.getWriter().getChronicle() instanceof VanillaChronicle);
    }

    // *************************************************************************
    //
    // *************************************************************************

    @Test
    public void testLogging1() throws IOException {
        final String theradName = "th-test-binary-logging-1";
        final String loggerName = "logging_1";
        final long   timestamp  = System.currentTimeMillis();

        IOTools.deleteDir(vanillaBasePath(loggerName));
        Thread.currentThread().setName(theradName);

        final Logger l = LoggerFactory.getLogger(loggerName);
        l.debug("data {}, {}",
            new MySerializableData("a Serializable object"),
            new MyMarshallableData("a Marshallable object")
        );

        Chronicle reader = getVanillaChronicle(loggerName);
        ExcerptTailer tailer = reader.createTailer();

        assertTrue(tailer.nextIndex());

        ChronologyLogEvent evt = ChronologyLogHelper.decodeBinary(tailer);
        assertNotNull(evt);
        assertEquals(evt.getVersion(), Chronology.VERSION);
        assertEquals(evt.getType(), Chronology.Type.SLF4J);
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

        IOTools.deleteDir(vanillaBasePath(loggerName));
    }

    @Test
    public void testLogging2() throws IOException {
        final String theradName = "th-test-binary-logging-2";
        final String loggerName = "logging_2";
        final long   timestamp  = System.currentTimeMillis();

        IOTools.deleteDir(vanillaBasePath(loggerName));
        Thread.currentThread().setName(theradName);

        final Logger logger = LoggerFactory.getLogger(loggerName);
        logger.info("args",1);
        logger.info("args",1,2);
        logger.info("args",1,2,3);

        Chronicle          chronicle = getVanillaChronicle(loggerName);
        ExcerptTailer      tailer    = chronicle.createTailer().toStart();
        ChronologyLogEvent evt       = null;

        for(int[] vals : new int[][] {  { 1 } , {1, 2} , {1, 2, 3}}) {
            assertTrue(tailer.nextIndex());

            evt = ChronologyLogHelper.decodeBinary(tailer);
            assertNotNull(evt);
            assertEquals(evt.getVersion(), Chronology.VERSION);
            assertEquals(evt.getType(), Chronology.Type.SLF4J);
            assertTrue(evt.getTimeStamp() >= timestamp);
            assertEquals(ChronologyLogLevel.INFO, evt.getLevel());
            assertEquals("args", evt.getMessage());
            assertNotNull(evt.getArgumentArray());
            assertEquals(vals.length, evt.getArgumentArray().length);

            for(int i=0;i<vals.length;i++) {
                assertEquals(vals[i], evt.getArgumentArray()[i]);
            }

            tailer.finish();
        }

        tailer.close();
        chronicle.close();

        IOTools.deleteDir(vanillaBasePath(loggerName));
    }
}
