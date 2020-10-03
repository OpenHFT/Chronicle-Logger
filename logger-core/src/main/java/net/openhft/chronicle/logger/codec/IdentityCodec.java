package net.openhft.chronicle.logger.codec;

import net.openhft.chronicle.bytes.Bytes;

import java.nio.ByteBuffer;

public class IdentityCodec implements Codec {

    @Override
    public int compress(Bytes<ByteBuffer> src, Bytes<ByteBuffer> dst) throws CodecException {
        dst.write(src);
        return (int) dst.readLimit();
    }

    @Override
    public int decompress(Bytes<ByteBuffer> src, Bytes<ByteBuffer> dst) throws CodecException {
        dst.write(src);
        return (int) dst.readLimit();
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
