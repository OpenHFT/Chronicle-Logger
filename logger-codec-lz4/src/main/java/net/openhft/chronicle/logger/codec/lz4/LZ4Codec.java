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
    public int compress(Bytes<ByteBuffer> sourceBytes, Bytes<ByteBuffer> destBytes) throws CodecException {
        ByteBuffer src = sourceBytes.underlyingObject();
        src.position((int) sourceBytes.readPosition());
        src.limit((int) sourceBytes.readLimit());

        destBytes.ensureCapacity(sourceBytes.readLimit());
        ByteBuffer dst = destBytes.underlyingObject();
        dst.clear();

        compressor.compress(src, dst);
        dst.flip();
        destBytes.readPosition(dst.position());
        destBytes.readLimit(dst.limit());

        return (int) destBytes.readLimit();
    }

    @Override
    public int decompress(Bytes<ByteBuffer> sourceBytes, Bytes<ByteBuffer> destBytes) throws CodecException {
        ByteBuffer src = sourceBytes.underlyingObject();

        int srcLimit = (int) sourceBytes.readLimit();
        src.position((int) sourceBytes.readPosition());
        src.limit(srcLimit);

        destBytes.ensureCapacity(sourceBytes.readLimit() * 3);
        ByteBuffer dst = destBytes.underlyingObject();
        dst.clear();

        // safe compressor doesn't require knowing the exact uncompressedSize.
        int actual = decompressor.decompress(src, src.position(), srcLimit, dst, dst.position(), dst.remaining());
        dst.flip();
        destBytes.readPosition(dst.position());
        destBytes.readLimit(actual);
        return actual;
    }

    @Override
    public long compressBounds(int length) {
        return compressor.maxCompressedLength(length);
    }

    @Override
    public long uncompressedSize(ByteBuffer buf) {
        return UNSUPPORTED_SIZE;
    }
}
