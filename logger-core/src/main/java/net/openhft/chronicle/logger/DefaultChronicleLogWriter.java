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
import net.openhft.chronicle.queue.ExcerptAppender;
import org.jetbrains.annotations.NotNull;

import java.nio.ByteBuffer;

/**
 * A log writer that runs a compression operation based on the content encoding.
 *
 * This class is not thread safe and should only be run inside a single thread.
 */
public class DefaultChronicleLogWriter implements ChronicleLogWriter, AutoCloseable {

    private final EntryWriter entryWriter;
    private final FlatBufferBuilder builder;

    private final Bytes<ByteBuffer> entryBytes;
    private final ExcerptAppender excerptAppender;

    public DefaultChronicleLogWriter(@NotNull ChronicleQueue cq) {
        this.excerptAppender = cq.acquireAppender();
        this.entryWriter = new EntryWriter();

        // Direct byte buffer makes access to memory mapped file faster?
        this.entryBytes = Bytes.elasticByteBuffer(1024);
        // XXX Would this be faster if the builder could use entryBytes bytebuffer directly?
        this.builder = new FlatBufferBuilder(1024);
    }

    @Override
    public void write(
            final long epochSecond,
            final int nanos,
            final int level,
            final String loggerName,
            final String threadName,
            final Bytes<ByteBuffer> content) {
        try {
            //System.out.println("write: " + content.toHexString());
            ByteBuffer entryBuffer = entryWriter.write(builder,
                    epochSecond,
                    nanos,
                    level,
                    loggerName,
                    threadName,
                    content);
            entryBytes.writeSome(entryBuffer);
            excerptAppender.writeBytes(entryBytes);
        } finally {
            entryBytes.clear();
            builder.clear();
        }
    }

    @Override
    public void close() {
        excerptAppender.close();
        this.entryBytes.releaseLast();
    }
}
