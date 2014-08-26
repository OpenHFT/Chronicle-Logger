/*
 * Copyright 2014 Higher Frequency Trading
 * <p/>
 * http://www.higherfrequencytrading.com
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.openhft.chronicle.logger.logback;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import net.openhft.chronicle.Chronicle;
import net.openhft.chronicle.ExcerptTailer;
import net.openhft.chronicle.logger.ChronicleLog;
import net.openhft.chronicle.logger.ChronicleLogEvent;
import net.openhft.chronicle.logger.ChronicleLogHelper;
import net.openhft.chronicle.logger.ChronicleLogLevel;
import net.openhft.chronicle.tools.ChronicleTools;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class LogbackIndexedChronicleTest extends LogbackTestBase {

    // *************************************************************************
    //
    // *************************************************************************

    @Test
    public void testBinaryIndexedChronicleAppenderConfig() throws IOException {
        final LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();

        final ch.qos.logback.classic.Logger logger = context.getLogger("config-binary-indexed-chronicle");
        assertNotNull(logger);

        final Appender<ILoggingEvent> appender =logger.getAppender("CONFIG-BINARY-INDEXED-CHRONICLE");
        assertNotNull(appender);
        assertTrue(appender instanceof BinaryIndexedChronicleAppender);

        BinaryIndexedChronicleAppender ba = (BinaryIndexedChronicleAppender)appender;
        assertEquals(128, ba.getChronicleConfig().getIndexFileCapacity());
    }

    @Test
    public void testTextIndexedChronicleAppenderConfig() throws IOException {
        final LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();

        final ch.qos.logback.classic.Logger logger = context.getLogger("config-text-indexed-chronicle");
        assertNotNull(logger);

        final Appender<ILoggingEvent> appender =logger.getAppender("CONFIG-TEXT-INDEXED-CHRONICLE");
        assertNotNull(appender);
        assertTrue(appender instanceof TextIndexedChronicleAppender);

        TextIndexedChronicleAppender ba = (TextIndexedChronicleAppender)appender;
        assertEquals(128, ba.getChronicleConfig().getIndexFileCapacity());
    }

    // *************************************************************************
    // BINARY
    // *************************************************************************

    @Test
    public void testBinaryAppender1() throws IOException {
        final String testId = "binary-indexed-chronicle";
        final String threadId  = testId + "-th";
        final long   timestamp = System.currentTimeMillis();
        final Logger logger    = LoggerFactory.getLogger(testId);

        Thread.currentThread().setName(threadId);

        for(ChronicleLogLevel level : LOG_LEVELS) {
            log(logger,level,"level is {}",level);
        }

        Chronicle          chronicle = getIndexedChronicle(testId);
        ExcerptTailer      tailer    = chronicle.createTailer().toStart();
        ChronicleLogEvent evt       = null;

        for(ChronicleLogLevel level : LOG_LEVELS) {
            assertTrue(tailer.nextIndex());

            evt = ChronicleLogHelper.decodeBinary(tailer);
            assertNotNull(evt);
            assertEquals(evt.getVersion(), ChronicleLog.VERSION);
            assertEquals(evt.getType(), ChronicleLog.Type.LOGBACK);
            assertTrue(evt.getTimeStamp() >= timestamp);
            assertEquals(level,evt.getLevel());
            assertEquals(threadId, evt.getThreadName());
            assertEquals(testId, evt.getLoggerName());
            assertEquals("level is {}", evt.getMessage());
            assertNotNull(evt.getArgumentArray());
            assertEquals(1, evt.getArgumentArray().length);
            assertEquals(level , evt.getArgumentArray()[0]);

            tailer.finish();
        }

        logger.debug("Throwable test",new UnsupportedOperationException());
        logger.debug("Throwable test",new UnsupportedOperationException("Exception message"));

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

        ChronicleTools.deleteOnExit(basePath(testId));
    }

    @Test
    public void testBinaryAppender2() throws IOException {
        final String testId = "binary-indexed-chronicle-fmt";
        final String threadId  = testId + "-th";
        final long   timestamp = System.currentTimeMillis();
        final Logger logger    = LoggerFactory.getLogger(testId);

        Thread.currentThread().setName(threadId);

        for(ChronicleLogLevel level : LOG_LEVELS) {
            log(logger,level,"level is {}",level);
        }

        Chronicle          chronicle = getIndexedChronicle(testId);
        ExcerptTailer      tailer    = chronicle.createTailer().toStart();
        ChronicleLogEvent evt       = null;

        for(ChronicleLogLevel level : LOG_LEVELS) {
            assertTrue(tailer.nextIndex());

            evt = ChronicleLogHelper.decodeBinary(tailer);
            assertNotNull(evt);
            assertEquals(evt.getVersion(), ChronicleLog.VERSION);
            assertEquals(evt.getType(), ChronicleLog.Type.LOGBACK);
            assertTrue(evt.getTimeStamp() >= timestamp);
            assertEquals(level,evt.getLevel());
            assertEquals(threadId, evt.getThreadName());
            assertEquals(testId, evt.getLoggerName());
            assertEquals("level is " + level, evt.getMessage());
            assertNotNull(evt.getArgumentArray());
            assertEquals(0, evt.getArgumentArray().length);

            tailer.finish();
        }

        logger.debug("Throwable test",new UnsupportedOperationException());
        logger.debug("Throwable test",new UnsupportedOperationException("Exception message"));

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

        ChronicleTools.deleteOnExit(basePath(testId));
    }

    @Test
    public void testTextAppender1() throws IOException {
        final String testId    = "text-indexed-chronicle";
        final String threadId  = testId + "-th";
        final Logger logger    = LoggerFactory.getLogger(testId);

        Thread.currentThread().setName(threadId);

        for(ChronicleLogLevel level : LOG_LEVELS) {
            log(logger,level,"level is {}",level);
        }

        Chronicle          chronicle = getIndexedChronicle(testId);
        ExcerptTailer      tailer    = chronicle.createTailer().toStart();
        ChronicleLogEvent evt       = null;

        for(ChronicleLogLevel level : LOG_LEVELS) {
            assertTrue(tailer.nextIndex());

            evt = ChronicleLogHelper.decodeText(tailer);
            assertNotNull(evt);
            assertEquals(level,evt.getLevel());
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
        evt = ChronicleLogHelper.decodeText(tailer);
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
        evt = ChronicleLogHelper.decodeText(tailer);assertNotNull(evt);
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

        ChronicleTools.deleteOnExit(basePath(testId));
    }
}
