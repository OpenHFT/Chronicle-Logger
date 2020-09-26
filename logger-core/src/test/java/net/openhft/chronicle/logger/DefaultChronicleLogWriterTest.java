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
package net.openhft.chronicle.logger;

import net.openhft.chronicle.bytes.Bytes;
import net.openhft.chronicle.core.io.IOTools;
import net.openhft.chronicle.logger.codec.CodecRegistry;
import net.openhft.chronicle.logger.entry.Entry;
import net.openhft.chronicle.logger.entry.EntryReader;
import net.openhft.chronicle.queue.ChronicleQueue;
import net.openhft.chronicle.queue.ExcerptTailer;
import net.openhft.chronicle.wire.DocumentContext;
import net.openhft.chronicle.wire.Wire;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;

import static org.junit.Assert.*;

public class DefaultChronicleLogWriterTest {

    private Path path;

    @After
    public void cleanup() {
        IOTools.deleteDirWithFiles(this.path.toFile());
    }

    @Before
    public void setUp() throws Exception {
        this.path = Files.createTempDirectory(basePath());
    }

    @Test
    public void testWrite() {
        try (final ChronicleQueue cq = ChronicleQueue.singleBuilder(path).build()) {
            try (CodecRegistry codecRegistry = CodecRegistry.builder().withDefaults(path).build()) {
                ChronicleLogWriter lw = new DefaultChronicleLogWriter(codecRegistry, cq);

                Instant now = Instant.now();
                lw.write(now.getEpochSecond(),
                        now.getNano(),
                        10,
                        this.getClass().getCanonicalName(),
                        Thread.currentThread().getName(),
                        "Test message".getBytes(StandardCharsets.UTF_8)
                );

                Instant n2 = Instant.now();
                lw.write(n2.getEpochSecond(),
                        n2.getNano(),
                        50,
                        this.getClass().getCanonicalName(),
                        Thread.currentThread().getName(),
                        "Test debug message".getBytes(StandardCharsets.UTF_8)
                );
            }
        }

        try (final ChronicleQueue cq = ChronicleQueue.singleBuilder(path).build()) {
            ExcerptTailer tailer = cq.createTailer();

            // XXX this is awkward, should just have EntryTailer
            Bytes<ByteBuffer> bytes = Bytes.elasticHeapByteBuffer();
            tailer.readBytes(bytes);
            EntryReader entryReader = new EntryReader();
            Entry entry = entryReader.read(bytes);
            assertTrue(entry.timestamp().epochSecond() <= Instant.now().getEpochSecond());
            assertTrue(entry.timestamp().nanoAdjust() > 0);
            assertEquals(10, entry.level());
            assertEquals(Thread.currentThread().getName(), entry.threadName());
            assertEquals(this.getClass().getCanonicalName(), entry.loggerName());
            assertEquals("Test message", Bytes.wrapForRead(entry.contentAsByteBuffer()).to8bitString());
            assertNull(entry.contentType());
            assertNull(entry.contentEncoding());

            bytes.clear();
            tailer.readBytes(bytes);
            Entry entry2 = entryReader.read(bytes);
            assertTrue(entry2.timestamp().epochSecond() <= Instant.now().getEpochSecond());
            assertEquals(50, entry2.level());
            assertEquals(Thread.currentThread().getName(), entry2.threadName());
            assertEquals(this.getClass().getCanonicalName(), entry2.loggerName());
            assertEquals("Test debug message", Bytes.wrapForRead(entry2.contentAsByteBuffer()).to8bitString());
            assertNull(entry2.contentType());
            assertNull(entry2.contentEncoding());

            try (DocumentContext dc = tailer.readingDocument()) {
                Wire wire = dc.wire();
                assertNull(wire);
            }
        }
    }

    private String basePath() {
        return "chronicle-logger";
    }
}
