package net.openhft.chronicle.logger.codec;

public class IdentityCodec implements Codec {
    @Override
    public byte[] decompress(byte[] bytes) {
        return bytes;
    }

    @Override
    public byte[] compress(byte[] bytes) {
        return bytes;
    }
}
