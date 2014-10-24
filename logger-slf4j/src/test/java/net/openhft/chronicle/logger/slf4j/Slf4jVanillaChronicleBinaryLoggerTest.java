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
public class Slf4jVanillaChronicleBinaryLoggerTest extends Slf4jTestBase {

    // *************************************************************************
    //
    // *************************************************************************

    @Before
    public void setUp() {
        System.setProperty(
            "slf4j.chronicle.properties",
            System.getProperty("slf4j.chronicle.vanilla.binary.properties"));

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

        assertEquals(cl.getLevel(), ChronicleLogLevel.DEBUG);
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

        ChronicleLogEvent evt = ChronicleLogHelper.decodeBinary(tailer);
        assertNotNull(evt);
        assertEquals(evt.getVersion(), ChronicleLog.VERSION);
        assertEquals(evt.getType(), ChronicleLog.Type.SLF4J);
        assertTrue(timestamp <= evt.getTimeStamp());
        assertEquals(ChronicleLogLevel.DEBUG,evt.getLevel());
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
        ChronicleLogEvent evt       = null;

        for(int[] vals : new int[][] {  { 1 } , {1, 2} , {1, 2, 3}}) {
            assertTrue(tailer.nextIndex());

            evt = ChronicleLogHelper.decodeBinary(tailer);
            assertNotNull(evt);
            assertEquals(evt.getVersion(), ChronicleLog.VERSION);
            assertEquals(evt.getType(), ChronicleLog.Type.SLF4J);
            assertTrue(evt.getTimeStamp() >= timestamp);
            assertEquals(ChronicleLogLevel.INFO, evt.getLevel());
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
