package net.openhft.chronicle.logger.codec;

import net.openhft.chronicle.bytes.Bytes;

import java.nio.ByteBuffer;

public interface Codec extends AutoCloseable {

    String getName();

    int compress(Bytes<ByteBuffer> src, Bytes<ByteBuffer> dst) throws CodecException;

    int decompress(Bytes<ByteBuffer> src, Bytes<ByteBuffer> dst) throws CodecException;

    default void close() {
    }

    long compressBounds(int length);

    long uncompressedSize(ByteBuffer buf);
}
