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

import com.google.flatbuffers.FlatBufferBuilder;
import net.openhft.chronicle.bytes.Bytes;
import net.openhft.chronicle.bytes.BytesStore;
import net.openhft.chronicle.logger.codec.Codec;
import net.openhft.chronicle.logger.codec.CodecRegistry;
import net.openhft.chronicle.logger.codec.IdentityCodec;
import net.openhft.chronicle.logger.entry.EntryWriter;
import net.openhft.chronicle.queue.ChronicleQueue;
import net.openhft.chronicle.wire.DocumentContext;
import net.openhft.chronicle.wire.Wire;
import net.openhft.chronicle.wire.WireType;
import org.jetbrains.annotations.NotNull;

import java.nio.ByteBuffer;
import java.time.Instant;
import java.time.ZoneOffset;

/**
 * A log writer that runs a compression operation based on the content encoding.
 */
public class DefaultChronicleLogWriter implements ChronicleLogWriter {

    private final EntryWriter entryWriter;
    private final FlatBufferBuilder builder;
    private final Bytes<ByteBuffer> bytes;

    private final ChronicleQueue cq;
    private final CodecRegistry codecRegistry;

    public DefaultChronicleLogWriter(@NotNull CodecRegistry codecRegistry, @NotNull ChronicleQueue cq) {
        this.cq = cq;
        this.entryWriter = new EntryWriter();
        this.bytes = Bytes.elasticByteBuffer();
        // XXX can make this more efficient by using off-heap memory & working with Bytes?
        FlatBufferBuilder.ByteBufferFactory factory = FlatBufferBuilder.HeapByteBufferFactory.INSTANCE;
        this.builder = new FlatBufferBuilder(1024, factory);
        this.codecRegistry = codecRegistry;
    }

    @Override
    public void write(
            final long epochSecond,
            final int nanos,
            final int level,
            final String loggerName,
            final String threadName,
            final byte[] content) {
        try {
            // XXX use a Bytes here?
            ByteBuffer contentBuffer = ByteBuffer.wrap(content);
            ByteBuffer entryBuffer = entryWriter.write(builder,
                    epochSecond,
                    nanos,
                    level,
                    loggerName,
                    threadName,
                    contentBuffer);
            bytes.writeSome(entryBuffer);
            cq.acquireAppender().writeBytes(bytes);
        } finally {
            bytes.clear();
            builder.clear();
        }
    }

    @Override
    public void write(
            final long epochSecond,
            final int nanos,
            final int level,
            final String loggerName,
            final String threadName,
            final byte[] content,
            final String contentType,
            final String contentEncoding) {
        try {
            Codec codec = codecRegistry.find(contentEncoding);
            byte[] encoded = codec.compress(content);
            ByteBuffer contentBuffer = ByteBuffer.wrap(encoded);

            ByteBuffer entryBuffer = entryWriter.write(builder,
                    epochSecond,
                    nanos,
                    level,
                    loggerName,
                    threadName,
                    contentBuffer,
                    contentType,
                    contentEncoding);
            bytes.writeSome(entryBuffer);
            cq.acquireAppender().writeBytes(bytes);
        } finally {
            bytes.clear();
            builder.clear();
        }
    }

    @Override
    public void close() {
        this.bytes.releaseLast();
    }
}
