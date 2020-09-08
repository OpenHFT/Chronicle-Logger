/*
 * Copyright 2014-2017 Higher Frequency Trading
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
package net.openhft.chronicle.logger.tools;

import net.openhft.chronicle.bytes.Bytes;
import net.openhft.chronicle.queue.ChronicleQueue;
import net.openhft.chronicle.queue.ExcerptTailer;
import net.openhft.chronicle.wire.DocumentContext;
import net.openhft.chronicle.wire.Wire;
import net.openhft.chronicle.wire.WireType;
import org.jetbrains.annotations.NotNull;

import java.nio.charset.StandardCharsets;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Generic tool allowing users to process Chronicle logs in their own way
 */
public class ChronicleLogReader {
    private final ChronicleQueue cq;

    /**
     * Create reader with default wire type
     *
     * @param path the path to Chronicle Logs storage
     */
    public ChronicleLogReader(
            @NotNull String path) {
        this(path, WireType.BINARY_LIGHT);
    }

    /**
     * @param path     the path to Chronicle Logs storage
     * @param wireType Chronicle wire type. Must match the wire type specified in corresponding Chronicle Logger
     */
    public ChronicleLogReader(
            @NotNull String path,
            @NotNull WireType wireType) {
        cq = ChronicleQueue.singleBuilder(path).wireType(wireType).build();
    }

    /**
     * Decode logs
     *
     * @param processor user-provided processor called for each log message
     * @param waitForIt whether to wait for more data or stop after EOF reached
     */
    public void processLogs(@NotNull ChronicleLogProcessor processor, boolean waitForIt) {
        ExcerptTailer tailer = cq.createTailer();
        for (; ; ) {
            try (DocumentContext dc = tailer.readingDocument()) {
                Wire wire = dc.wire();
                if (wire == null)
                    if (waitForIt) {
                        try {
                            Thread.sleep(50L);
                        } catch (InterruptedException ignored) {

                        }
                        continue;
                    } else {
                        break;
                    }

                ChronicleLogEvent logEvent = createLogEvent(wire);
                processor.process(logEvent);
            }
        }
    }

    private ChronicleLogEvent createLogEvent(Wire wire) {
        ZonedDateTime timestamp = wire.read("instant").zonedDateTime();
        int level = wire.read("level").int32();
        String threadName = wire.read("threadName").text();
        String loggerName = wire.read("loggerName").text();

        byte[] entry = wire.read("entry").bytes();

        String contentType = null;
        if (wire.hasMore()) {
            contentType = wire.read("type").text();
        }

        String encoding = null;
        if (wire.hasMore()) {
            encoding = wire.read("encoding").text();
        }

        return new ChronicleLogEvent(timestamp.toInstant(), level, threadName, loggerName, entry, contentType, encoding);
    }
}
