package net.openhft.chronicle.logger.codec;

public interface CodecFactory extends AutoCloseable {
    Codec find(CodecRegistry registry, String encodingName);

    @Override
    default void close() {

    }
}
