package net.openhft.chronicle.logger.codec.lz4;

import net.jpountz.lz4.*;
import net.openhft.chronicle.bytes.Bytes;
import net.openhft.chronicle.logger.codec.Codec;
import net.openhft.chronicle.logger.codec.CodecException;

import java.nio.ByteBuffer;

public class LZ4Codec implements Codec {

    public static final String NAME = "lz4";
    private final LZ4Compressor compressor;
    private final LZ4SafeDecompressor decompressor;

    LZ4Codec(LZ4Factory factory) {
        this.compressor = factory.fastCompressor();
        this.decompressor = factory.safeDecompressor();
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public int compress(Bytes<ByteBuffer> sourceBytes, Bytes<ByteBuffer> dstBytes) throws CodecException {
        ByteBuffer src = sourceBytes.underlyingObject();
        src.position((int) sourceBytes.readPosition());
        src.limit((int) sourceBytes.readLimit());

        dstBytes.ensureCapacity(uncompressedSize(src));
        ByteBuffer dst = dstBytes.underlyingObject();
        dst.clear();

        compressor.compress(src, dst);
        dst.flip();
        dstBytes.readPosition(dst.position());
        dstBytes.readLimit(dst.limit());

        return (int) dstBytes.readLimit();
    }

    @Override
    public int decompress(Bytes<ByteBuffer> sourceBytes, Bytes<ByteBuffer> dstBytes) throws CodecException {
        ByteBuffer src = sourceBytes.underlyingObject();
        src.position((int) sourceBytes.readPosition());
        src.limit((int) sourceBytes.readLimit());

        dstBytes.ensureCapacity(uncompressedSize(src));
        ByteBuffer dst = dstBytes.underlyingObject();
        dst.clear();

        // safe compressor doesn't require knowing the exact uncompressedSize.
        int actual = decompressor.decompress(src, 0, src.limit(), dst, 0, dst.capacity());
        dst.flip();
        dstBytes.readPosition(dst.position());
        dstBytes.readLimit(actual);
        return actual;
    }

    @Override
    public long compressBounds(int length) {
        return compressor.maxCompressedLength(length);
    }

    @Override
    public long uncompressedSize(ByteBuffer buf) {
        return -1;
    }
}
