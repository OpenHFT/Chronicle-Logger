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
import net.openhft.chronicle.logger.codec.Codec;
import net.openhft.chronicle.logger.codec.CodecRegistry;
import net.openhft.chronicle.logger.entry.EntryWriter;
import net.openhft.chronicle.queue.ChronicleQueue;
import org.jetbrains.annotations.NotNull;

import java.nio.ByteBuffer;

/**
 * A log writer that runs a compression operation based on the content encoding.
 */
public class DefaultChronicleLogWriter implements ChronicleLogWriter {

    private final EntryWriter entryWriter;
    private final FlatBufferBuilder builder;

    private final ChronicleQueue cq;
    private final CodecRegistry codecRegistry;

    private final Bytes<ByteBuffer> entryBytes;
    private final Bytes<ByteBuffer> destBytes;
    private final Bytes<ByteBuffer> sourceBytes;

    public DefaultChronicleLogWriter(@NotNull CodecRegistry codecRegistry, @NotNull ChronicleQueue cq) {
        this.cq = cq;
        this.entryWriter = new EntryWriter();
        this.entryBytes = Bytes.elasticByteBuffer(1024);
        this.sourceBytes = Bytes.elasticByteBuffer(1024);
        this.destBytes = Bytes.elasticByteBuffer(1024);

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
            entryBytes.writeSome(entryBuffer);
            cq.acquireAppender().writeBytes(entryBytes);
        } finally {
            entryBytes.clear();
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

            // Put the content bytes into buffer and flip for reading.
            sourceBytes.ensureCapacity(content.length);
            sourceBytes.write(content);
            ByteBuffer src = sourceBytes.underlyingObject();
            src.position(0);
            src.limit(content.length);

            // Put the compressed bytes into the dst byte buffer and flip reading.
            long compressBounds = codec.compressBounds(content.length);
            destBytes.ensureCapacity(compressBounds);
            ByteBuffer dst = destBytes.underlyingObject();
            dst.position(0);
            dst.limit((int) compressBounds);
            int actualSize = codec.compress(src, dst);
            dst.position(0);
            dst.limit(actualSize);

            ByteBuffer entryBuffer = entryWriter.write(builder,
                    epochSecond,
                    nanos,
                    level,
                    loggerName,
                    threadName,
                    dst,
                    contentType,
                    contentEncoding);
            entryBytes.writeSome(entryBuffer);
            cq.acquireAppender().writeBytes(entryBytes);
        } finally {
            entryBytes.clear();
            sourceBytes.clear();
            destBytes.clear();
            builder.clear();
        }
    }

    @Override
    public void close() {
        this.sourceBytes.releaseLast();
        this.destBytes.releaseLast();
        this.entryBytes.releaseLast();
    }
}
