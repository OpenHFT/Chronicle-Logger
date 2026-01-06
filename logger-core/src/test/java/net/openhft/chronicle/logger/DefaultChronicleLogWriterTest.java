/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.logger;

import net.openhft.chronicle.core.OS;
import net.openhft.chronicle.core.io.IOTools;
import net.openhft.chronicle.core.util.Time;
import net.openhft.chronicle.queue.ChronicleQueue;
import net.openhft.chronicle.queue.ExcerptTailer;
import net.openhft.chronicle.wire.DocumentContext;
import net.openhft.chronicle.wire.Wire;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static java.lang.System.currentTimeMillis;
import static org.junit.jupiter.api.Assertions.*;

public class DefaultChronicleLogWriterTest {

    String baseBath;

    @AfterEach
    public void cleanup() {
        IOTools.deleteDirWithFiles(this.baseBath);
    }

    @BeforeEach
    public void setUp() throws Exception {
        Path path = Paths.get(OS.getTarget(), "chronicle-logger-" + Time.uniqueId());
        Files.createDirectories(path);
        this.baseBath = path.toString();
    }

    @Test
    public void testWrite() {
        try (final ChronicleQueue cq = ChronicleQueue.singleBuilder(this.baseBath).build()) {
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

        try (final ChronicleQueue cq = ChronicleQueue.singleBuilder(this.baseBath).build()) {
            ExcerptTailer tailer = cq.createTailer();
            try (DocumentContext dc = tailer.readingDocument()) {
                Wire wire = dc.wire();
                assertNotNull(wire, "first log entry should be deserialisable from the queue");
                assertTrue(wire.read("ts").int64() <= currentTimeMillis(), "error log timestamp should not be in the future");
                assertEquals(ChronicleLogLevel.ERROR, wire.read("level").asEnum(ChronicleLogLevel.class), "error log level should be preserved during serialisation");
                assertEquals(Thread.currentThread().getName(), wire.read("threadName").text(), "error log should capture the thread name that wrote it");
                assertEquals(this.getClass().getCanonicalName(), wire.read("loggerName").text(), "error log should capture the logger class name");
                assertEquals("Test message", wire.read("message").text(), "error log message should be preserved exactly");
                assertEquals("Test exception", wire.read("throwable").throwable(false).getMessage(), "exception message should be preserved in the serialised throwable");
                List<Object> args = new ArrayList<>();
                assertTrue(wire.hasMore(), "error log should contain the args field when arguments are provided");
                wire.read("args").sequence(args, (l, vi) -> {
                    while (vi.hasNextSequenceItem()) {
                        l.add(vi.object(Object.class));
                    }
                });
                assertArrayEquals(new Object[]{10, 12.1}, args.toArray(new Object[0]), "error log arguments should be preserved with correct types and values");
            }

            try (DocumentContext dc = tailer.readingDocument()) {
                Wire wire = dc.wire();
                assertNotNull(wire, "second log entry should be deserialisable from the queue");
                assertTrue(wire.read("ts").int64() <= currentTimeMillis(), "debug log timestamp should not be in the future");
                assertEquals(ChronicleLogLevel.DEBUG, wire.read("level").asEnum(ChronicleLogLevel.class), "debug log level should be preserved during serialisation");
                assertEquals(Thread.currentThread().getName(), wire.read("threadName").text(), "debug log should capture the thread name that wrote it");
                assertEquals(this.getClass().getCanonicalName(), wire.read("loggerName").text(), "debug log should capture the logger class name");
                assertEquals("Test debug message", wire.read("message").text(), "debug log message should be preserved exactly");
                assertFalse(wire.hasMore(), "debug log without throwable or args should have no additional fields");
            }
            try (DocumentContext dc = tailer.readingDocument()) {
                Wire wire = dc.wire();
                assertNull(wire, "tailer should find no more entries after reading all written logs");
            }
        }
    }
}
