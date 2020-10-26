/*
 * Copyright 2014-2020 chronicle.software
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

import net.openhft.chronicle.core.io.IOTools;
import net.openhft.chronicle.queue.ChronicleQueue;
import net.openhft.chronicle.queue.ExcerptTailer;
import net.openhft.chronicle.wire.DocumentContext;
import net.openhft.chronicle.wire.Wire;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static java.lang.System.currentTimeMillis;
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
            ChronicleLogWriter lw = new DefaultChronicleLogWriter(cq);
            lw.write(
                    ChronicleLogLevel.ERROR,
                    currentTimeMillis(),
                    Thread.currentThread().getName(),
                    this.getClass().getCanonicalName(),
                    "Test message",
                    new Exception("Test exception"),
                    10,
                    12.1
            );

            lw.write(
                    ChronicleLogLevel.DEBUG,
                    currentTimeMillis(),
                    Thread.currentThread().getName(),
                    this.getClass().getCanonicalName(),
                    "Test debug message"
            );

        }

        try (final ChronicleQueue cq = ChronicleQueue.singleBuilder(basePath()).build()) {
            ExcerptTailer tailer = cq.createTailer();
            try (DocumentContext dc = tailer.readingDocument()) {
                Wire wire = dc.wire();
                assertNotNull(wire);
                assertTrue(wire.read("ts").int64() <= currentTimeMillis());
                assertEquals(ChronicleLogLevel.ERROR, wire.read("level").asEnum(ChronicleLogLevel.class));
                assertEquals(Thread.currentThread().getName(), wire.read("threadName").text());
                assertEquals(this.getClass().getCanonicalName(), wire.read("loggerName").text());
                assertEquals("Test message", wire.read("message").text());
                assertEquals("Test exception", wire.read("throwable").throwable(false).getMessage());
                List<Object> args = new ArrayList<>();
                assertTrue(wire.hasMore());
                wire.read("args").sequence(args, (l, vi) -> {
                    while (vi.hasNextSequenceItem()) {
                        l.add(vi.object(Object.class));
                    }
                });
                assertArrayEquals(new Object[]{10, 12.1}, args.toArray(new Object[args.size()]));
            }

            try (DocumentContext dc = tailer.readingDocument()) {
                Wire wire = dc.wire();
                assertNotNull(wire);
                assertTrue(wire.read("ts").int64() <= currentTimeMillis());
                assertEquals(ChronicleLogLevel.DEBUG, wire.read("level").asEnum(ChronicleLogLevel.class));
                assertEquals(Thread.currentThread().getName(), wire.read("threadName").text());
                assertEquals(this.getClass().getCanonicalName(), wire.read("loggerName").text());
                assertEquals("Test debug message", wire.read("message").text());
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
