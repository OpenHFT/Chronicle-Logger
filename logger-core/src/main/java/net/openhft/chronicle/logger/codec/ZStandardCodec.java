package net.openhft.chronicle.logger.codec;

import com.github.luben.zstd.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.function.Supplier;

import static java.nio.file.StandardOpenOption.CREATE_NEW;

/**
 * ZStandard compression.
 */
public class ZStandardCodec implements Codec, AutoCloseable {

    private ZStandardDictCodec zstdDictCodec;
    private ZStandardTrainerCodec zstdTrainerCodec;

    // Dictionary size can be up to 10 MeB in bytes.  This is the output from training.
    public static final int DEFAULT_DICT_SIZE = 10485760;

    // The sample size is the sum of the individual samples,
    // i.e. if you have 1000 messages that are all 26 bytes each,
    // then the sample size is 26000 bytes.
    public static final int DEFAULT_SAMPLE_SIZE = 100_000 * 1024;

    public ZStandardCodec(byte[] dictionary, int level) {
        this.zstdDictCodec = new ZStandardDictCodec(dictionary, level);
    }

    public ZStandardCodec(int sampleSize, int dictSize, int level, Function<ZstdDictTrainer, CompletionStage<byte[]>> trainingHook) {
        this(sampleSize, dictSize, Duration.ofSeconds(0), level, trainingHook);
    }

    public ZStandardCodec(int sampleSize, int dictSize, Duration trainingSleep, int level, Function<ZstdDictTrainer, CompletionStage<byte[]>> trainingHook) {
        this.zstdTrainerCodec = new ZStandardTrainerCodec(sampleSize, dictSize, trainingSleep, level, trainingHook);
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    public int decompress(ByteBuffer compressed, ByteBuffer decompressed) {
        Objects.requireNonNull(compressed);
        Objects.requireNonNull(decompressed);

        if (zstdDictCodec != null) {
            return zstdDictCodec.decompress(compressed, decompressed);
        } else {
            return Zstd.decompress(decompressed, compressed);
        }
    }

    @Override
    public int compress(ByteBuffer original, ByteBuffer compressed) {
        if (zstdDictCodec != null) {
            return zstdDictCodec.compress(original, compressed);
        } else {
            return zstdTrainerCodec.compress(original, compressed);
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
        return zsdCompressBounds(length);
    }

    @Override
    public long uncompressedSize(ByteBuffer buf) {
        return zstdUncompressedSize(buf);
    }

    private static long zsdCompressBounds(int length) {
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
        public int compress(ByteBuffer src, ByteBuffer dst) throws CodecException {
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

        @Override
        public int decompress(ByteBuffer src, ByteBuffer dst) throws CodecException {
            return Zstd.decompress(dst, src);
        }

        @Override
        public long compressBounds(int length) {
            return zsdCompressBounds(length);
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
        public int compress(ByteBuffer src, ByteBuffer dst) throws CodecException {
            try {
                return Zstd.compress(dst, src, zstdDictCompress);
            } catch (ZstdException e) {
                throw new CodecException(e);
            }
        }

        @Override
        public int decompress(ByteBuffer src, ByteBuffer dst) throws CodecException {
            try {
                return Zstd.decompress(dst, src, zstdDictDecompress);
            } catch (ZstdException e) {
                throw new CodecException(e);
            }
        }

        @Override
        public long compressBounds(int length) {
            return zsdCompressBounds(length);
        }

        @Override
        public long uncompressedSize(ByteBuffer buf) {
            return zstdUncompressedSize(buf);
        }

        @Override
        public void close() {
            zstdDictCompress.close();
            zstdDictDecompress.close();
        }
    }

    public static class Builder {
        private int sampleSize = ZStandardCodec.DEFAULT_SAMPLE_SIZE;
        private int dictSize = ZStandardCodec.DEFAULT_DICT_SIZE;
        private Duration initialDelay = Duration.ZERO;
        private int compressionLevel = 3;
        private Supplier<ZStandardCodec> codecSupplier;

        Builder() {
        }

        public Builder withSampleSize(int sampleSize) {
            this.sampleSize = sampleSize;
            return this;
        }

        public Builder withDictionarySize(int dictSize) {
            this.dictSize = dictSize;
            return this;
        }

        public Builder withCompressionLevel(int level) {
            this.compressionLevel = level;
            return this;
        }

        /**
         * Adds training hook.  This calls withCodec.  You may call withSampleSize/withDictionarySize before this.
         *
         * @param trainingHook
         * @return
         */
        public Builder withTrainingHook(Function<ZstdDictTrainer, CompletionStage<byte[]>> trainingHook) {
            ZStandardCodec codecWithTrainer = new ZStandardCodec(sampleSize, dictSize, compressionLevel, trainingHook);
            return this.withCodec(codecWithTrainer);
        }

        /**
         * Adds a dictionary.  This calls withCodec.
         *
         * @param dictBytes
         * @return
         */
        public Builder withDictionary(byte[] dictBytes) {
            ZStandardCodec codecWithDict = new ZStandardCodec(dictBytes, compressionLevel);
            return this.withCodec(codecWithDict);
        }

        public Builder withDefaults(String path) {
            return withDefaults(Paths.get(path));
        }

        public Builder withInitialDelay(Duration initialDelay) {
            this.initialDelay = initialDelay;
            return this;
        }

        public Builder withDefaults(Path dictionaryPath) {
            this.codecSupplier = () -> {
                try {
                    byte[] dictBytes = ZStandardDictionary.readFromFile(dictionaryPath);
                    return new ZStandardCodec(dictBytes, compressionLevel);
                } catch (NoSuchFileException | FileNotFoundException e) {
                    Function<ZstdDictTrainer, CompletionStage<byte[]>> trainingHook =
                            ZStandardDictionary.writeToFile(dictionaryPath);
                    return new ZStandardCodec(sampleSize, dictSize, initialDelay, compressionLevel, trainingHook);
                } catch (IOException e) {
                    throw new CodecException(e);
                }
            };
            return this;
        }

        private Builder withCodec(ZStandardCodec codec) {
            this.codecSupplier = () -> codec;
            return this;
        }

        public ZStandardCodec build() throws CodecException {
            return this.codecSupplier.get();
        }
    }
}

class ZStandardDictionary {

    private static final Logger logger = LoggerFactory.getLogger(ZStandardDictionary.class);

    public static final String DEFAULT_DICT_FILENAME = "dictionary";

    public static Function<ZstdDictTrainer, CompletionStage<byte[]>> writeToFile(Path path) {
        // Run this as a future so it's off the current thread (we don't
        // care when it completes)
        return trainer -> CompletableFuture.supplyAsync(() -> {
            Path filename = Files.isDirectory(path) ? path.resolve(DEFAULT_DICT_FILENAME) : path;
            try {
                logger.info("Training zstd dictionary samples...");
                byte[] dictBytes = trainer.trainSamples();
                logger.info("Writing zstd dictionary of {} bytes to {}", dictBytes.length, filename);
                Files.write(filename, dictBytes, CREATE_NEW);
                return dictBytes;
            } catch (IOException e) {
                throw new CompletionException(new CodecException(e));
            } catch (ZstdException e) {
                // XXX throws ZstdException (should use a try / catch)
                // https://github.com/facebook/zstd/issues/1735
                // I think "Src size is incorrect" means that there's not enough unique info in the
                // messages to create a dictionary.  So keep going?  Or throw out the
                // trainer and start from scratch?
                throw new CompletionException(new CodecException("Cannot create dictionary: probably not enough unique data", e));
            }
        }, ForkJoinPool.commonPool());

    }

    /**
     * Utility for reading bytes from a file.
     *
     * @param path a path to a regular readable file
     * @return the bytes of the file.
     * @throws IOException if the bytes cannot be read.
     */
    public static byte[] readFromFile(Path path) throws IOException {
        if (!Files.exists(path)) {
            String msg = String.format("Dictionary %s does not exist!", path);
            throw new FileNotFoundException(msg);
        }
        if (Files.isDirectory(path)) {
            Path dictPath = path.resolve(DEFAULT_DICT_FILENAME);
            return Files.readAllBytes(dictPath);
        }
        return Files.readAllBytes(path);
    }
}
