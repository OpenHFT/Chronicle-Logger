package net.openhft.chronicle.logger.codec;

import java.nio.ByteBuffer;

public interface Codec extends AutoCloseable {
    int decompress(ByteBuffer src, ByteBuffer dst) throws CodecException;

    int compress(ByteBuffer src, ByteBuffer dst) throws CodecException;

    default void close() {
    }

    long compressBounds(int length);

    long uncompressedSize(ByteBuffer buf);
}
