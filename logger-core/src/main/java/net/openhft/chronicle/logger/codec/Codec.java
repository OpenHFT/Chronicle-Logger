package net.openhft.chronicle.logger.codec;

import net.openhft.chronicle.bytes.Bytes;

import java.nio.ByteBuffer;

/**
 * A compressor/decompressor pair.
 */
public interface Codec extends AutoCloseable {

    long UNSUPPORTED_SIZE = -1;

    /**
     * @return The key that is used in the codec registry.
     */
    String getName();

    /**
     * Compresses content from sourceBytes bytes to destBytes bytes.
     *
     * @param sourceBytes the bytes containing uncompressed data.
     * @param destBytes the bytes that will contain compressed data
     * @return the size of the compressed data.
     * @throws CodecException
     */
    int compress(Bytes<ByteBuffer> sourceBytes, Bytes<ByteBuffer> destBytes) throws CodecException;

    /**
     * Decompresses content from sourceBytes bytes to destBytes bytes.
     *
     * @param sourceBytes the bytes containing compressed data
     * @param destBytes the bytes that will contain uncompressed data
     * @return the actual size of the decompression
     * @throws CodecException
     */
    int decompress(Bytes<ByteBuffer> sourceBytes, Bytes<ByteBuffer> destBytes) throws CodecException;

    default void close() {
    }

    /**
     * The bounds of a compression.
     *
     * @param length
     * @return the maximum size of a compressed buffer.
     */
    long compressBounds(int length);

    /**
     * Returns the uncompressed size of the buffer, returns -1 if the codec does not support it.
     *
     * @param buf
     * @return the uncompressed size of the buffer, returns -1 if the codec does not support it.
     */
    long uncompressedSize(ByteBuffer buf);
}
