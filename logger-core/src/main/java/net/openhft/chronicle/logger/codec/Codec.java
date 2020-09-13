package net.openhft.chronicle.logger.codec;

public interface Codec extends AutoCloseable {
    byte[] decompress(byte[] bytes) throws CodecException;

    byte[] compress(byte[] bytes) throws CodecException;

    default void close() {
    }
}
