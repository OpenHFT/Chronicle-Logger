package net.openhft.chronicle.logger.codec;

public interface CodecFactory {
    Codec find(CodecRegistry registry, String encodingName);
}
