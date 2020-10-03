package net.openhft.chronicle.logger.codec.zstd;

import com.github.luben.zstd.*;
import net.openhft.chronicle.bytes.Bytes;
import net.openhft.chronicle.logger.codec.Codec;
import net.openhft.chronicle.logger.codec.CodecException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;

/**
 * ZStandard compression.
 */
public class ZStandardCodec implements Codec, AutoCloseable {

    private ZStandardDictCodec zstdDictCodec;
    private ZStandardTrainerCodec zstdTrainerCodec;

    public ZStandardCodec(byte[] dictionary, int level) {
        this.zstdDictCodec = new ZStandardDictCodec(dictionary, level);
    }

    public ZStandardCodec(int sampleSize, int dictSize, int level, Function<ZstdDictTrainer, CompletionStage<byte[]>> trainingHook) {
        this(sampleSize, dictSize, Duration.ofSeconds(0), level, trainingHook);
    }

    public ZStandardCodec(int sampleSize, int dictSize, Duration trainingSleep, int level, Function<ZstdDictTrainer, CompletionStage<byte[]>> trainingHook) {
        this.zstdTrainerCodec = new ZStandardTrainerCodec(sampleSize, dictSize, trainingSleep, level, trainingHook);
    }

    @Override
    public String getName() {
        return ZStandardCodecFactory.NAME;
    }

    @Override
    public int compress(Bytes<ByteBuffer> sourceBytes, Bytes<ByteBuffer> dstBytes) throws CodecException {
        ByteBuffer src = sourceBytes.underlyingObject();
        src.position((int) sourceBytes.readPosition());
        src.limit((int) sourceBytes.readLimit());

        dstBytes.ensureCapacity(compressBounds(sourceBytes.length()));
        ByteBuffer dst = dstBytes.underlyingObject();
        dst.clear();

        int actualSize = compress(src, dst);
        dst.flip();

        dstBytes.readPosition(dst.position());
        dstBytes.readLimit(dst.limit());
        assert dstBytes.length() > 0;

        return actualSize;
    }

    @Override
    public int decompress(Bytes<ByteBuffer> sourceBytes, Bytes<ByteBuffer> dstBytes) throws CodecException {
        ByteBuffer src = sourceBytes.underlyingObject();
        src.position((int) sourceBytes.readPosition());
        src.limit((int) sourceBytes.readLimit());

        dstBytes.ensureCapacity(uncompressedSize(src));
        ByteBuffer dst = dstBytes.underlyingObject();
        dst.clear();

        int retvalue = decompress(src, dst);
        dst.flip();
        dstBytes.readPosition(dst.position());
        dstBytes.readLimit(dst.limit());

        return retvalue;
    }

    protected int compress(ByteBuffer original, ByteBuffer compressed) {
        if (zstdDictCodec != null) {
            return zstdDictCodec.compress(original, compressed);
        } else {
            return zstdTrainerCodec.compress(original, compressed);
        }
    }

    protected int decompress(ByteBuffer compressed, ByteBuffer decompressed) {
        Objects.requireNonNull(compressed);
        Objects.requireNonNull(decompressed);

        if (zstdDictCodec != null) {
            return zstdDictCodec.decompress(compressed, decompressed);
        } else {
            return Zstd.decompress(decompressed, compressed);
        }
    }

    @Override
    public void close() {
        if (zstdDictCodec != null) {
            zstdDictCodec.close();
        }
        if (zstdTrainerCodec != null) {
            zstdTrainerCodec.close();
        }
    }

    @Override
    public long compressBounds(int length) {
        return zstdCompressBounds(length);
    }

    @Override
    public long uncompressedSize(ByteBuffer buf) {
        return zstdUncompressedSize(buf);
    }

    private static long zstdCompressBounds(int length) {
        return Zstd.compressBound(length);
    }

    private static long zstdUncompressedSize(ByteBuffer buf) {
        return Zstd.decompressedSize(buf);
    }

    /**
     * The zstd trainer codec uses zstd without compression, but also trains the
     * message set using a dictionary.  When the trainer has enough sample messages,
     * the hook is called.
     */
    class ZStandardTrainerCodec implements Codec {
        private final Logger logger = LoggerFactory.getLogger(getClass());

        private final Function<ZstdDictTrainer, CompletionStage<byte[]>> trainingHook;
        private final Instant trainingDeadline;
        private final int compressionLevel;

        private final ZstdDictTrainer trainer;
        private final AtomicBoolean trained = new AtomicBoolean(false);

        public ZStandardTrainerCodec(int sampleSize,
                                     int dictSize,
                                     Duration trainingSleep,
                                     int compressionLevel,
                                     Function<ZstdDictTrainer, CompletionStage<byte[]>> trainingHook) throws CodecException {
            this.trainer = new ZstdDictTrainer(sampleSize, dictSize);
            this.trainingDeadline = Instant.now().plus(trainingSleep);
            this.trainingHook = trainingHook;
            this.compressionLevel = compressionLevel;
            logger.info("Dictionary training enabled, will begin training in {} after {}", trainingSleep, trainingDeadline);
        }

        public boolean isTraining() {
            return Instant.now().isAfter(trainingDeadline);
        }

        @Override
        public String getName() {
            return ZStandardCodec.this.getName();
        }

        public int compress(Bytes<ByteBuffer> sourceBytes, Bytes<ByteBuffer> destBytes) {
            // Set up underlying source bytebuffer
            ByteBuffer src = sourceBytes.underlyingObject();
            src.position(0);
            src.limit((int) sourceBytes.readLimit());

            // Set up underlying dest bytebuffer
            long maxBounds = compressBounds(src.limit());
            destBytes.ensureCapacity(maxBounds);
            ByteBuffer dst = destBytes.underlyingObject();
            dst.position(0);
            dst.limit((int) maxBounds);

            int actualSize = compress(src, dst);

            // Set the dest buffer to the actual size.
            destBytes.readLimit(actualSize);
            return actualSize;
        }

        @Override
        public int decompress(Bytes<ByteBuffer> src, Bytes<ByteBuffer> dst) throws CodecException {

            return decompress(src.underlyingObject(), dst.underlyingObject());
        }

        protected int compress(ByteBuffer src, ByteBuffer dst) throws CodecException {
            if (!trained.get() && isTraining()) {
                byte[] byteArray = new byte[src.limit()];
                src.get(byteArray);
                src.rewind();
                if (!this.trainer.addSample(byteArray)) {
                    if (!trained.getAndSet(true)) {
                        trainingHook.apply(trainer).thenAccept(dictBytes -> {
                            // Not thread safe but this is fine?
                            logger.info("Successfully trained with {} bytes", dictBytes.length);
                            ZStandardCodec.this.zstdDictCodec = new ZStandardDictCodec(dictBytes, compressionLevel);
                        });
                    }
                }
            }
            return Zstd.compress(dst, src);
        }

        protected int decompress(ByteBuffer src, ByteBuffer dst) throws CodecException {
            return Zstd.decompress(dst, src);
        }

        @Override
        public long compressBounds(int length) {
            return zstdCompressBounds(length);
        }

        @Override
        public long uncompressedSize(ByteBuffer buf) {
            return zstdUncompressedSize(buf);
        }
    }

    /**
     * ZStandard using dictionary compression.
     */
    static class ZStandardDictCodec implements Codec, AutoCloseable {

        private final Logger logger = LoggerFactory.getLogger(getClass());

        private final ZstdDictCompress zstdDictCompress;
        private final ZstdDictDecompress zstdDictDecompress;

        public ZStandardDictCodec(byte[] dictBytes, int level) throws CodecException {
            this.zstdDictCompress = new ZstdDictCompress(dictBytes, level);
            this.zstdDictDecompress = new ZstdDictDecompress(dictBytes);
            logger.info("Using zstandard dictionary with length {}, level {}", dictBytes.length, level);
        }
        @Override
        public long compressBounds(int length) {
            return zstdCompressBounds(length);
        }

        @Override
        public long uncompressedSize(ByteBuffer buf) {
            return zstdUncompressedSize(buf);
        }

        @Override
        public String getName() {
            return ZStandardCodecFactory.NAME;
        }

        @Override
        public int compress(Bytes<ByteBuffer> src, Bytes<ByteBuffer> dst) throws CodecException {
            return compress(src.underlyingObject(), dst.underlyingObject());
        }

        @Override
        public int decompress(Bytes<ByteBuffer> src, Bytes<ByteBuffer> dst) throws CodecException {
            return decompress(src.underlyingObject(), dst.underlyingObject());
        }

        protected int compress(ByteBuffer src, ByteBuffer dst) throws CodecException {
            try {
                return Zstd.compress(dst, src, zstdDictCompress);
            } catch (ZstdException e) {
                throw new CodecException(e);
            }
        }

        protected int decompress(ByteBuffer src, ByteBuffer dst) throws CodecException {
            try {
                return Zstd.decompress(dst, src, zstdDictDecompress);
            } catch (ZstdException e) {
                throw new CodecException(e);
            }
        }

        @Override
        public void close() {
            zstdDictCompress.close();
            zstdDictDecompress.close();
        }
    }

}
