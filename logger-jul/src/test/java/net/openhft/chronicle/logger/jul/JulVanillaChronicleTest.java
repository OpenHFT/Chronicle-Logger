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

package net.openhft.chronicle.logger.jul;

import net.openhft.chronicle.Chronicle;
import net.openhft.chronicle.ExcerptTailer;
import net.openhft.chronicle.logger.ChronicleLog;
import net.openhft.chronicle.logger.ChronicleLogEvent;
import net.openhft.chronicle.logger.ChronicleLogHelper;
import net.openhft.chronicle.logger.ChronicleLogLevel;
import net.openhft.lang.io.IOTools;
import org.junit.Test;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.junit.Assert.*;

public class JulVanillaChronicleTest extends JulTestBase{

    @Test
    public void testBinaryVanillaChronicleConfiguration() throws IOException {
        final String testId = "binary-vanilla-cfg";
        setupLogManager(testId);

        final Logger logger = Logger.getLogger(testId);
        assertEquals(Level.INFO, logger.getLevel());
        assertFalse(logger.getUseParentHandlers());
        assertNull(logger.getFilter());
        assertNotNull(logger.getHandlers());
        assertEquals(1, logger.getHandlers().length);
        assertTrue(logger.getHandlers()[0] instanceof BinaryVanillaChronicleHandler);
    }

    // *************************************************************************
    // BINARY
    // *************************************************************************

    @Test
    public void testVanillaBinaryAppender() throws IOException {
        final String testId = "binary-vanilla-chronicle";
        final String threadId = "thread-" + Thread.currentThread().getId();
        final long timestamp = System.currentTimeMillis();

        IOTools.deleteDir(basePath(testId));
        setupLogManager(testId);

        final Logger logger = Logger.getLogger(testId);

        for(ChronicleLogLevel level : LOG_LEVELS) {
            log(logger,level,"level is {}",level);
        }

        Chronicle chronicle = getVanillaChronicle(testId);
        ExcerptTailer tailer = chronicle.createTailer().toStart();
        ChronicleLogEvent evt = null;

        for(ChronicleLogLevel level : LOG_LEVELS) {
            assertTrue(tailer.nextIndex());

            evt = ChronicleLogHelper.decodeBinary(tailer);
            assertNotNull(evt);
            assertEquals(evt.getVersion(), ChronicleLog.VERSION);
            assertTrue(evt.getTimeStamp() >= timestamp);
            assertEquals(level, evt.getLevel());
            assertEquals(threadId, evt.getThreadName());
            assertEquals(testId, evt.getLoggerName());
            assertEquals("level is {}", evt.getMessage());
            assertNotNull(evt.getArgumentArray());
            assertEquals(1, evt.getArgumentArray().length);
            assertEquals(level , evt.getArgumentArray()[0]);
            assertNull(evt.getThrowable());

            tailer.finish();
        }

        logger.log(Level.FINE, "Throwable test", new UnsupportedOperationException());
        logger.log(Level.FINE, "Throwable test", new UnsupportedOperationException("Exception message"));

        assertTrue(tailer.nextIndex());
        evt = ChronicleLogHelper.decodeBinary(tailer);
        assertEquals("Throwable test",evt.getMessage());
        assertNotNull(evt.getThrowable());
        assertTrue(evt.getThrowable() instanceof UnsupportedOperationException);
        assertEquals(UnsupportedOperationException.class.getName(),evt.getThrowable().getMessage());

        assertTrue(tailer.nextIndex());
        evt = ChronicleLogHelper.decodeBinary(tailer);
        assertEquals("Throwable test",evt.getMessage());
        assertNotNull(evt.getThrowable());
        assertTrue(evt.getThrowable() instanceof UnsupportedOperationException);
        assertEquals(UnsupportedOperationException.class.getName() + ": Exception message",evt.getThrowable().getMessage());

        tailer.close();
        chronicle.close();
        chronicle.clear();
    }
}
