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

import net.openhft.chronicle.bytes.BytesStore;
import net.openhft.chronicle.core.io.IOTools;
import net.openhft.chronicle.logger.codec.CodecRegistry;
import net.openhft.chronicle.queue.ChronicleQueue;
import net.openhft.chronicle.queue.ExcerptTailer;
import net.openhft.chronicle.wire.DocumentContext;
import net.openhft.chronicle.wire.Wire;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.junit.Assert.*;

public class DefaultChronicleLogWriterTest {

    @After
    public void cleanup() {
        IOTools.deleteDirWithFiles(basePath());
    }

    @Before
    public void setUp() throws Exception {
        Files.createDirectories(Paths.get(basePath()));
    }

    @Test
    public void testWrite() {
        try (final ChronicleQueue cq = ChronicleQueue.singleBuilder(basePath()).build()) {
            try (CodecRegistry codecRegistry = CodecRegistry.builder().withDefaults(basePath()).build()) {
                ChronicleLogWriter lw = new DefaultChronicleLogWriter(codecRegistry, cq);
                lw.write(Instant.now(),
                        10,
                        Thread.currentThread().getName(),
                        this.getClass().getCanonicalName(),
                        "Test message".getBytes(StandardCharsets.UTF_8)
                );

                lw.write(Instant.now(),
                        50,
                        Thread.currentThread().getName(),
                        this.getClass().getCanonicalName(),
                        "Test debug message".getBytes(StandardCharsets.UTF_8)
                );
            }
        }

        try (final ChronicleQueue cq = ChronicleQueue.singleBuilder(basePath()).build()) {
            ExcerptTailer tailer = cq.createTailer();
            try (DocumentContext dc = tailer.readingDocument()) {
                Wire wire = dc.wire();
                assertNotNull(wire);
                assertTrue(wire.read("instant").zonedDateTime().isBefore(Instant.now().atZone(ZoneOffset.UTC)));
                assertEquals(10, wire.read("level").int32());
                assertEquals(Thread.currentThread().getName(), wire.read("threadName").text());
                assertEquals(this.getClass().getCanonicalName(), wire.read("loggerName").text());
                assertEquals("Test message", wire.read("entry").text());
                assertNull(wire.read("type").text());
                assertNull(wire.read("encoding").text());
                assertFalse(wire.hasMore());
            }

            try (DocumentContext dc = tailer.readingDocument()) {
                Wire wire = dc.wire();
                assertNotNull(wire);
                assertTrue(wire.read("instant").zonedDateTime().isBefore(Instant.now().atZone(ZoneOffset.UTC)));
                assertEquals(50, wire.read("level").int32());
                assertEquals(Thread.currentThread().getName(), wire.read("threadName").text());
                assertEquals(this.getClass().getCanonicalName(), wire.read("loggerName").text());
                assertEquals("Test debug message", wire.read("entry").text());
                assertNull(wire.read("type").text());
                assertNull(wire.read("encoding").text());
                assertFalse(wire.hasMore());
            }
            try (DocumentContext dc = tailer.readingDocument()) {
                Wire wire = dc.wire();
                assertNull(wire);
            }
        }
    }

    private String basePath() {
        String path = System.getProperty("java.io.tmpdir");
        String sep = System.getProperty("file.separator");

        if (!path.endsWith(sep)) {
            path += sep;
        }

        return path + "chronicle-logger" + sep;
    }
}
