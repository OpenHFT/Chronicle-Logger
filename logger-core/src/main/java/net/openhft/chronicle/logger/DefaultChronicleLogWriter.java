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
import net.openhft.chronicle.queue.ChronicleQueue;
import net.openhft.chronicle.wire.DocumentContext;
import net.openhft.chronicle.wire.Wire;
import net.openhft.chronicle.wire.WireType;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;
import java.time.ZoneOffset;

/**
 * A log writer that defers encoding to the logging framework in question.
 *
 *  <ul>
 *      <li>Timestamp written as zoned date time in UTC</li>
 *      <li>Level written out as raw integer (log4j2 can have custom levels)</li>
 *      <li>Uses encoded bytes (much safer than having throwable/args encoded directly)</li>
 *      <li>Optional content type as HTTP content type (bytes are known SMILE/CBOR/ION/JSON+UTF8)</li>
 *      <li>Optional encoding as HTTP content encoding header (zstd/br/gzip) etc</li>
 *  </ul>
 */
public class DefaultChronicleLogWriter implements ChronicleLogWriter {

    private static final ThreadLocal<Boolean> REENTRANCY_FLAG = ThreadLocal.withInitial(() -> false);

    private final ChronicleQueue cq;

    public DefaultChronicleLogWriter(@NotNull ChronicleQueue cq) {
        this.cq = cq;
    }

    @Override
    public void close() {
        cq.close();
    }

    @Override
    public void write(@NotNull Instant timestamp, int level, String threadName, @NotNull String loggerName, @NotNull BytesStore entry) {
        write(timestamp, level, threadName, loggerName, entry, null, null);
    }

    @Override
    public void write(
            final Instant timestamp,
            final int level,
            final String threadName,
            final String loggerName,
            final BytesStore entry,
            final String contentType,
            final String contentEncoding) {
        if (REENTRANCY_FLAG.get()) {
            System.out.printf("%s|%s|%s|%s|%s%n",
                    timestamp.toString(),
                    level,
                    threadName,
                    loggerName,
                    entry.toDebugString());
            return;
        }
        REENTRANCY_FLAG.set(true);
        try (final DocumentContext dc = cq.acquireAppender().writingDocument()) {
            Wire wire = dc.wire();
            assert wire != null;
            wire
                    .write("instant").zonedDateTime(timestamp.atZone(ZoneOffset.UTC)) // there is no "instant" mapping
                    .write("level").int32(level) // log4j2 can have custom levels
                    .write("threadName").text(threadName)
                    .write("loggerName").text(loggerName)
                    .write("entry").bytes(entry);
                    if (contentType != null) {
                        wire.write("type").text(contentType); // HTTP content-type header,
                    }
                    if (contentEncoding != null) {
                        wire.write("encoding").text(contentEncoding);
                    }
        } finally {
            REENTRANCY_FLAG.set(false);
        }
    }

    public WireType getWireType() {
        return cq.wireType();
    }

}
