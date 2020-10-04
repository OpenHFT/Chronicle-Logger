/*
 * Copyright 2014-2017 Chronicle Software
 *
 * http://www.chronicle.software
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

import net.openhft.chronicle.bytes.Bytes;
import net.openhft.chronicle.core.io.IOTools;
import net.openhft.chronicle.logger.EntryTransformer;
import net.openhft.chronicle.logger.DefaultEntryTransformer;
import net.openhft.chronicle.logger.codec.Codec;
import net.openhft.chronicle.logger.codec.CodecRegistry;
import net.openhft.chronicle.logger.entry.Entry;
import net.openhft.chronicle.logger.entry.EntryReader;
import net.openhft.chronicle.queue.ChronicleQueue;
import net.openhft.chronicle.wire.DocumentContext;
import net.openhft.chronicle.wire.Wire;
import org.junit.After;
import org.junit.Test;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.time.Instant;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import static java.util.logging.Level.*;
import static org.junit.Assert.*;

public class JulHandlerChronicleTest extends JulHandlerTestBase {

    private static ChronicleQueue getChronicleQueue(String testId) throws IOException {
        return ChronicleQueue.singleBuilder(basePath(testId)).build();
    }

    @After
    public void tearDown() {
        IOTools.deleteDirWithFiles(rootPath());
    }

    @Test
    public void testConfiguration() throws IOException {
        setupLogManager("binary-cfg");
        Logger logger = Logger.getLogger("binary-cfg");
        assertEquals(Level.INFO, logger.getLevel());
        assertFalse(logger.getUseParentHandlers());
        assertNull(logger.getFilter());
        assertNotNull(logger.getHandlers());
        assertEquals(1, logger.getHandlers().length);

        assertEquals(ChronicleHandler.class, logger.getHandlers()[0].getClass());
        ChronicleHandler chronicleHandler = (ChronicleHandler) logger.getHandlers()[0];
        assertNotNull(chronicleHandler.getFormatter());
        assertEquals(SimpleFormatter.class, chronicleHandler.getFormatter().getClass());
    }

    @Test
    public void testAppender() throws IOException {
        final String testId = "binary-chronicle";

        setupLogManager(testId);
        Logger logger = Logger.getLogger(testId);

        final String threadId = "thread-" + Thread.currentThread().getId();

        Level[] standardLevels = {
                SEVERE, WARNING, INFO, CONFIG, FINE, FINER, FINEST
        };

        for (Level level : standardLevels) {
            logger.log(level, "level is {0}", level);
        }
        EntryReader entryReader = new EntryReader();
        CodecRegistry registry = CodecRegistry.builder().withDefaults(basePath(testId)).build();
        Codec codec = registry.find("identity");
        EntryTransformer<String> processor = new DefaultEntryTransformer(codec);

        try (final ChronicleQueue cq = getChronicleQueue(testId)) {
            net.openhft.chronicle.queue.ExcerptTailer tailer = cq.createTailer();
            for (Level level : standardLevels) {
                Bytes<ByteBuffer> bytes = Bytes.elasticHeapByteBuffer();
                tailer.readBytes(bytes);
                Entry entry = entryReader.read(bytes);
                String content = processor.apply(entry);
                assertTrue(entry.timestamp().epochSecond() <= Instant.now().getEpochSecond());
                assertTrue(entry.timestamp().nanoAdjust() > 0);
                assertEquals(level.intValue(), entry.level());
                assertEquals(threadId, entry.threadName());
                assertEquals(testId, entry.loggerName());

                assertTrue(content.contains("level is " + level.getName()));
            }

            try (DocumentContext dc = tailer.readingDocument()) {
                Wire wire = dc.wire();
                assertNull(wire);
            }

            logger.log(Level.FINE, "Throwable test 1", new UnsupportedOperationException());
            logger.log(Level.FINE, "Throwable test 2", new UnsupportedOperationException("Exception message"));

            {
                Bytes<ByteBuffer> bytes = Bytes.elasticHeapByteBuffer();
                tailer.readBytes(bytes);
                Entry entry = entryReader.read(bytes);
                assertTrue(entry.timestamp().epochSecond() <= Instant.now().getEpochSecond());
                assertTrue(entry.timestamp().nanoAdjust() > 0);
                assertEquals(FINE.intValue(), entry.level());
                assertEquals(threadId, entry.threadName());
                assertEquals(testId, entry.loggerName());

                String content = processor.apply(entry);
                assertTrue(content.contains("FINE: Throwable test 1"));
                assertTrue(content.contains("java.lang.UnsupportedOperationException"));
            }

            {
                Bytes<ByteBuffer> bytes = Bytes.elasticHeapByteBuffer();
                tailer.readBytes(bytes);
                Entry entry = entryReader.read(bytes);
                assertTrue(entry.timestamp().epochSecond() <= Instant.now().getEpochSecond());
                assertTrue(entry.timestamp().nanoAdjust() > 0);
                assertEquals(FINE.intValue(), entry.level());
                assertEquals(threadId, entry.threadName());
                assertEquals(testId, entry.loggerName());

                String content = processor.apply(entry);
                assertTrue(content.contains("FINE: Throwable test 2"));
                assertTrue(content.contains("java.lang.UnsupportedOperationException: Exception message"));
            }

            try (DocumentContext dc = tailer.readingDocument()) {
                Wire wire = dc.wire();
                assertNull(wire);
            }
        }
    }

}
