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
package net.openhft.chronicle.logger.log4j1;

import net.openhft.chronicle.bytes.Bytes;
import net.openhft.chronicle.core.io.IOTools;
import net.openhft.chronicle.logger.EntryProcessor;
import net.openhft.chronicle.logger.LogAppenderConfig;
import net.openhft.chronicle.logger.codec.Codec;
import net.openhft.chronicle.logger.entry.EntryReader;
import net.openhft.chronicle.logger.DefaultEntryProcessor;
import net.openhft.chronicle.logger.codec.CodecRegistry;
import net.openhft.chronicle.logger.entry.Entry;
import net.openhft.chronicle.queue.ChronicleQueue;
import net.openhft.chronicle.queue.ExcerptTailer;
import net.openhft.chronicle.wire.DocumentContext;
import net.openhft.chronicle.wire.Wire;
import net.openhft.chronicle.wire.WireType;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;

import static org.apache.log4j.Level.*;
import static org.junit.Assert.*;

public class Log4j1ChronicleLogTest extends Log4j1TestBase {

    static final Level[] levels = { FATAL, ERROR, WARN, INFO, DEBUG, TRACE };

    private static ChronicleQueue getChronicleQueue(LogAppenderConfig config, Path path) {
        return config.build(path);
    }

    @After
    public void tearDown() {
        IOTools.deleteDirWithFiles(rootPath());
    }

    @Test
    public void testBinaryAppender() throws IOException {
        final String testId = "chronicle";
        final String threadId = testId + "-th";
        final Logger logger = LogManager.getLogger(testId);
        Path path = Paths.get(basePath(testId));
        Files.createDirectories(path);
        Thread.currentThread().setName(threadId);

        for (Level level: levels) {
            log(logger, level, "level is " + level, null);
        }
        LogAppenderConfig config = LogAppenderConfig.parse(path);
        try (final ChronicleQueue cq = getChronicleQueue(config, path)) {
            Path parent = Paths.get(cq.fileAbsolutePath()).getParent();
            CodecRegistry registry = CodecRegistry.builder().withDefaults(parent).build();
            Codec codec = registry.find(config.contentEncoding);
            EntryProcessor<String> processor = new DefaultEntryProcessor(codec);
            EntryReader entryReader = new EntryReader();

            ExcerptTailer tailer = cq.createTailer();
            for (Level level: levels) {
                Bytes<ByteBuffer> bytes = Bytes.elasticHeapByteBuffer();
                tailer.readBytes(bytes);
                Entry entry = entryReader.read(bytes);
                assertTrue(entry.timestamp().epochSecond() <= Instant.now().getEpochSecond());
                assertEquals(level.toInt(), entry.level());
                assertEquals(threadId, entry.threadName());
                assertEquals(testId, entry.loggerName());
                assertEquals("level is " + level.toString() + " ", processor.apply(entry));
            }

            try (DocumentContext dc = tailer.readingDocument()) {
                Wire wire = dc.wire();
                assertNull(wire);
            }
        }
        IOTools.deleteDirWithFiles(basePath(testId));
    }

    @Test
    @Ignore
    public void testJsonAppender() throws IOException {
        final String testId = "json-chronicle";
        final String threadId = testId + "-th";
        final Logger logger = LogManager.getLogger(testId);
        Path path = Paths.get(basePath(testId));
        Files.createDirectories(path);
        Thread.currentThread().setName(threadId);

        for (Level level: levels) {
            log(logger, level, "level is " + level, null);
        }

        LogAppenderConfig config = LogAppenderConfig.parse(path);
        try (final ChronicleQueue cq = getChronicleQueue(config, path)) {
            Path parent = Paths.get(cq.fileAbsolutePath()).getParent();
            CodecRegistry registry = CodecRegistry.builder().withDefaults(path).build();
            Codec codec = registry.find(config.contentEncoding);
            EntryProcessor<String> processor = new DefaultEntryProcessor(codec);

            ExcerptTailer tailer = cq.createTailer();
            EntryReader entryReader = new EntryReader();

            for (Level level: levels) {
                Bytes<ByteBuffer> bytes = Bytes.elasticHeapByteBuffer();
                tailer.readBytes(bytes);
                Entry entry = entryReader.read(bytes);
                assertTrue(entry.timestamp().epochSecond() <= Instant.now().getEpochSecond());
                assertEquals(level.toInt(), entry.level());
                assertEquals(threadId, entry.threadName());
                assertEquals(testId, entry.loggerName());
                assertEquals("level is " + level.toString() + " ", processor.apply(entry));
            }
            try (DocumentContext dc = tailer.readingDocument()) {
                Wire wire = dc.wire();
                assertNull(wire);
            }

            logger.debug("Throwable test 1", new UnsupportedOperationException());
            logger.debug("Throwable test 2", new UnsupportedOperationException("Exception message"));

            {
                Bytes<ByteBuffer> bytes = Bytes.elasticHeapByteBuffer();
                tailer.readBytes(bytes);
                Entry entry = entryReader.read(bytes);
                assertTrue(entry.timestamp().epochSecond() <= Instant.now().getEpochSecond());
                assertEquals(DEBUG.toInt(), entry.level());
                assertEquals(threadId, entry.threadName());
                assertEquals(testId, entry.loggerName());
                assertEquals("Throwable test 1", processor.apply(entry));
            }

            {
                Bytes<ByteBuffer> bytes = Bytes.elasticHeapByteBuffer();
                tailer.readBytes(bytes);
                Entry entry = entryReader.read(bytes);
                assertTrue(entry.timestamp().epochSecond() <= Instant.now().getEpochSecond());
                assertEquals(DEBUG.toInt(), entry.level());
                assertEquals(threadId, entry.threadName());
                assertEquals(testId, entry.loggerName());

                assertEquals("Throwable test 2", processor.apply(entry));
            }

            try (DocumentContext dc = tailer.readingDocument()) {
                Wire wire = dc.wire();
                assertNull(wire);
            }
        }
        IOTools.deleteDirWithFiles(basePath(testId));
    }
}
