package net.openhft.chronicle.logger.codec;

import net.openhft.chronicle.bytes.Bytes;

import java.nio.ByteBuffer;

public class IdentityCodec implements Codec {
    @Override
    public int decompress(ByteBuffer src, ByteBuffer dst) {
        dst.put(src);
        dst.flip(); // turn to read mode
        return dst.limit();
    }

    @Override
    public int compress(ByteBuffer src, ByteBuffer dst) throws CodecException {
        dst.put(src);
        dst.flip();
        return dst.limit();
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
