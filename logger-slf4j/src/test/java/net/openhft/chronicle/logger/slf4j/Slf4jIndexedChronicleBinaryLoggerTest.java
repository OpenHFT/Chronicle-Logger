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
import net.openhft.chronicle.IndexedChronicle;
import net.openhft.chronicle.logger.ChronicleLog;
import net.openhft.chronicle.logger.ChronicleLogWriters;
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
public class Slf4jIndexedChronicleBinaryLoggerTest extends Slf4jTestBase {

    // *************************************************************************
    //
    // *************************************************************************

    @Before
    public void setUp() {
        System.setProperty(
            "chronicle.logger.properties",
            "chronicle.logger.indexed.binary.properties");

        getChronicleLoggerFactory().reload();
    }

    @After
    public void tearDown() {
        getChronicleLoggerFactory().shutdown();

        IOTools.deleteDir(basePath(ChronicleLogConfig.TYPE_INDEXED));
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
        Logger logger = LoggerFactory.getLogger("slf4j-indexed-binary-logger");

        assertNotNull(logger);
        assertTrue(logger instanceof ChronicleLogger);

        ChronicleLogger cl = (ChronicleLogger) logger;

        assertEquals(ChronicleLogLevel.DEBUG, cl.getLevel());
        assertEquals("slf4j-indexed-binary-logger", cl.getName());
        assertTrue(cl.getWriter() instanceof ChronicleLogWriters.SynchronizedWriter);
        assertTrue(cl.getWriter().getChronicle() instanceof IndexedChronicle);
    }

    // *************************************************************************
    //
    // *************************************************************************

    @Test
    public void testLogging1() throws IOException {
        final String threadName = "th-test-binary-logging-1";
        final String loggerName = "logging_1";
        final long   timestamp  = System.currentTimeMillis();

        IOTools.deleteDir(indexedBasePath(loggerName));
        Thread.currentThread().setName(threadName);

        final Logger l = LoggerFactory.getLogger(loggerName);
        l.debug("data {}, {}",
            new MyMarshallableData("a Marshallable object 1"),
            new MyMarshallableData("a Marshallable object 2")
        );

        Chronicle reader = getIndexedChronicle( loggerName);
        ExcerptTailer tailer = reader.createTailer();

        assertTrue(tailer.nextIndex());

        ChronicleLogEvent evt = ChronicleLogHelper.decodeBinary(tailer);
        assertNotNull(evt);
        assertEquals(evt.getVersion(), ChronicleLog.VERSION);
        assertTrue(timestamp <= evt.getTimeStamp());
        assertEquals(ChronicleLogLevel.DEBUG,evt.getLevel());
        assertEquals("data {}, {}",evt.getMessage());
        assertEquals(threadName, evt.getThreadName());
        assertNotNull(evt.getArgumentArray());
        assertEquals(2, evt.getArgumentArray().length);

        Object marshallableObject1 = evt.getArgumentArray()[0];
        assertNotNull(marshallableObject1);
        assertTrue(marshallableObject1 instanceof MyMarshallableData);
        assertEquals(marshallableObject1.toString(), "a Marshallable object 1");

        Object marshallableObject2 = evt.getArgumentArray()[1];
        assertNotNull(marshallableObject2);
        assertTrue(marshallableObject2 instanceof MyMarshallableData);
        assertEquals(marshallableObject2.toString(), "a Marshallable object 2");

        tailer.close();
        reader.close();

        IOTools.deleteDir(indexedBasePath(loggerName));
    }

    @Test
    public void testLogging2() throws IOException {
        final String threadName = "th-test-binary-logging-2";
        final String loggerName = "logging_2";
        final long   timestamp  = System.currentTimeMillis();

        IOTools.deleteDir(indexedBasePath(loggerName));
        Thread.currentThread().setName(threadName);

        final Logger logger = LoggerFactory.getLogger(loggerName);
        logger.info("args",1);
        logger.info("args",1,2);
        logger.info("args",1,2,3);

        Chronicle          chronicle = getIndexedChronicle(loggerName);
        ExcerptTailer      tailer    = chronicle.createTailer().toStart();
        ChronicleLogEvent evt       = null;

        for(int[] vals : new int[][] {  { 1 } , { 1, 2} , { 1, 2, 3} }) {
            assertTrue(tailer.nextIndex());

            evt = ChronicleLogHelper.decodeBinary(tailer);
            assertNotNull(evt);
            assertEquals(evt.getVersion(), ChronicleLog.VERSION);
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

        IOTools.deleteDir(indexedBasePath(loggerName));
    }
}
