package net.openhft.chronicle.logger.codec;

import com.github.luben.zstd.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ForkJoinPool;
import java.util.function.Function;

import static java.nio.file.StandardOpenOption.CREATE_NEW;

/**
 * ZStandard compression.
 */
public class ZStandardCodec implements Codec, AutoCloseable {

    private ZStandardDictCodec zstdDictCodec;
    private ZStandardTrainerCodec zstdTrainerCodec;

    // Dictionary size 10 MeB in bytes
    public static final int DEFAULT_DICT_SIZE = 10485760;

    // no idea what the sample length is, but we'll assume around 1K for JSON.
    public static final int DEFAULT_SAMPLE_SIZE = DEFAULT_DICT_SIZE / 1024;

    public ZStandardCodec(byte[] dictionary) {
        this.zstdDictCodec = (new ZStandardDictCodec(dictionary));
    }

    public ZStandardCodec(int sampleSize, int dictSize, Function<ZstdDictTrainer, CompletionStage<byte[]>> trainingHook) {
        this.zstdTrainerCodec = new ZStandardTrainerCodec(sampleSize, dictSize, trainingHook);
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    public byte[] decompress(byte[] bytes) {
        if (zstdDictCodec != null) {
            return zstdDictCodec.decompress(bytes);
        } else {
            return staticDecompress(bytes);
        }
    }

    @Override
    public byte[] compress(byte[] bytes) {
        if (zstdDictCodec != null) {
            return zstdDictCodec.compress(bytes);
        } else {
            return zstdTrainerCodec.compress(bytes);
        }
    }

    @Override
    public void close() {
        if (zstdDictCodec != null) {
            zstdDictCodec.close();
        }
    }

    /**
     * The zstd trainer codec uses zstd without compression, but also trains the
     * message set using a dictionary.  When the trainer has enough sample messages,
     * the hook is called.
     */
    class ZStandardTrainerCodec implements Codec {
        private final Function<ZstdDictTrainer, CompletionStage<byte[]>> trainingHook;
        private volatile boolean trained = false;

        private final ZstdDictTrainer trainer;

        public ZStandardTrainerCodec(int sampleSize, int dictSize, Function<ZstdDictTrainer, CompletionStage<byte[]>> trainingHook) throws CodecException {
            this.trainer = new ZstdDictTrainer(sampleSize, dictSize);
            this.trainingHook = trainingHook;
        }

        @Override
        public byte[] decompress(byte[] bytes) throws CodecException {
            return staticDecompress(bytes);
        }

        @Override
        public byte[] compress(byte[] bytes) throws CodecException {
            // Once it's trained, send it to the completion hook.
            if (!(trained || this.trainer.addSample(bytes))) {
                trained = true;
                trainingHook.apply(trainer).thenAccept(dictBytes -> {
                    // Not thread safe but this is fine
                    ZStandardCodec.this.zstdDictCodec = new ZStandardDictCodec(dictBytes);
                });
            }
            return staticCompress(bytes);
        }
    }

    /**
     * ZStandard using dictionary compression.
     */
    static class ZStandardDictCodec implements Codec, AutoCloseable {
        private final ZstdDictCompress zstdDictCompress;
        private final ZstdDictDecompress zstdDictDecompress;

        public ZStandardDictCodec(byte[] dictBytes) throws CodecException {
            this(dictBytes, 3);
        }

        public ZStandardDictCodec(byte[] dictBytes, int level) throws CodecException {
            this.zstdDictCompress = new ZstdDictCompress(dictBytes, level);
            this.zstdDictDecompress = new ZstdDictDecompress(dictBytes);
        }

        @Override
        public byte[] decompress(byte[] bytes) {
            int originalSize = (int) Zstd.decompressedSize(bytes);
            byte[] dst = new byte[originalSize];
            return Zstd.decompress(dst, zstdDictDecompress, originalSize);
        }

        @Override
        public byte[] compress(byte[] bytes) throws CodecException {
            try {
                int dstSize = (int) Zstd.compressBound(bytes.length);
                // XXX is this right?  Seems wasteful...
                byte[] dst = new byte[dstSize];
                long ret = Zstd.compress(dst, bytes, this.zstdDictCompress);
                if (Zstd.isError(ret)) {
                    String errorName = Zstd.getErrorName(ret);
                    throw new CodecException(errorName);
                }
                int actualSize = (int) ret;
                byte[] actual = new byte[actualSize];
                System.arraycopy(dst, 0, actual, 0, actualSize);
                return actual;
            } catch (ZstdException ze) {
                throw new CodecException(ze);
            }
        }

        @Override
        public void close() {
            zstdDictCompress.close();
            zstdDictDecompress.close();
        }
    }

    private static byte[] staticDecompress(byte[] bytes) {
        int maxSize = (int) Zstd.decompressedSize(bytes);
        return Zstd.decompress(bytes, maxSize);
    }

    private static byte[] staticCompress(byte[] bytes) {
        return Zstd.compress(bytes);
    }

    public static class Builder {
        private int sampleSize = ZStandardCodec.DEFAULT_SAMPLE_SIZE;
        private int dictSize = ZStandardCodec.DEFAULT_DICT_SIZE;
        private ZStandardCodec codec;

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

        /**
         * Adds training hook.  This calls withCodec.  You may call withSampleSize/withDictionarySize before this.
         *
         * @param trainingHook
         * @return
         */
        public Builder withTrainingHook(Function<ZstdDictTrainer, CompletionStage<byte[]>> trainingHook) {
            ZStandardCodec codecWithTrainer = new ZStandardCodec(sampleSize, dictSize, trainingHook);
            return this.withCodec(codecWithTrainer);
        }

        /**
         * Adds a dictionary.  This calls withCodec.
         *
         * @param dictBytes
         * @return
         */
        public Builder withDictionary(byte[] dictBytes) {
            ZStandardCodec codecWithDict = new ZStandardCodec(dictBytes);
            return this.withCodec(codecWithDict);
        }

        public Builder withDefaults(String path) {
            return withDefaults(Paths.get(path));
        }

        public Builder withDefaults(Path dictionaryPath) {
            try {
                byte[] dictBytes = ZStandardDictionary.readFromFile(dictionaryPath);
                return this.withDictionary(dictBytes);
            } catch (IOException e) {
                return this.withTrainingHook(ZStandardDictionary.writeToFile(dictionaryPath));
            }
        }

        private Builder withCodec(ZStandardCodec codec) {
            this.codec = codec;
            return this;
        }

        public ZStandardCodec build() {
            return this.codec;
        }
    }
}

class ZStandardDictionary {

    public static final String DEFAULT_DICT_FILENAME = "dictionary";

    public static Function<ZstdDictTrainer, CompletionStage<byte[]>> writeToFile(Path path) {
        // Run this as a future so it's off the current thread (we don't
        // care when it completes)
        return trainer -> CompletableFuture.supplyAsync(() -> {
            try {
                byte[] dictBytes = trainer.trainSamples();
                if (Files.isDirectory(path)) {
                    Path filename = path.resolve(DEFAULT_DICT_FILENAME);
                    Files.write(filename, dictBytes, CREATE_NEW);
                } else {
                    Files.write(path, dictBytes);
                }
                return dictBytes;
            } catch (IOException e) {
                throw new RuntimeException(e);
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
        if (! Files.exists(path)) {
            String msg = String.format("Dictionary %s does not exist!", path);
            throw new IOException(msg);
        }
        if (! Files.isReadable(path)) {
            String msg = String.format("Dictionary %s is not readable!", path);
            throw new IOException(msg);
        }
        if (Files.isDirectory(path)) {
            Path dictPath = path.resolve(DEFAULT_DICT_FILENAME);
            return Files.readAllBytes(dictPath);
        }
        return Files.readAllBytes(path);
    }

}
