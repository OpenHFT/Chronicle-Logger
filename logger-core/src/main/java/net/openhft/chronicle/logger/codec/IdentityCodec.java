package net.openhft.chronicle.logger.codec;

import net.openhft.chronicle.bytes.Bytes;

import java.nio.ByteBuffer;

public class IdentityCodec implements Codec {
    public static final String NAME = "identity";

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public int compress(Bytes<ByteBuffer> sourceBytes, Bytes<ByteBuffer> destBytes) throws CodecException {
        destBytes.write(sourceBytes);
        return (int) destBytes.readLimit();
    }

    @Override
    public int decompress(Bytes<ByteBuffer> sourceBytes, Bytes<ByteBuffer> destBytes) throws CodecException {
        destBytes.write(sourceBytes);
        return (int) destBytes.readLimit();
    }

    @Override
    public long compressBounds(int length) {
        return length;
    }

    @Override
    public long uncompressedSize(ByteBuffer buf) {
        return buf.limit();
    }
}
