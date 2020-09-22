package net.openhft.chronicle.logger.codec;

// XXX should make this use Bytes and work garbage-free
public interface Codec extends AutoCloseable {
    byte[] decompress(byte[] bytes) throws CodecException;

    byte[] compress(byte[] bytes) throws CodecException;

    default void close() {
    }
}
