package net.openhft.chronicle.logger.codec.zstd;

import com.github.luben.zstd.ZstdDictTrainer;
import net.openhft.chronicle.logger.codec.Codec;
import net.openhft.chronicle.logger.codec.CodecException;
import net.openhft.chronicle.logger.codec.CodecFactory;
import net.openhft.chronicle.logger.codec.CodecRegistry;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.time.Duration;
import java.util.concurrent.CompletionStage;
import java.util.function.Function;
import java.util.function.Supplier;

public class ZStandardCodecFactory implements CodecFactory {
    public static final String NAME = "zstd";

    private Codec codec;

    @Override
    public Codec find(CodecRegistry registry, String encodingName) {
        if (NAME.equalsIgnoreCase(encodingName)) {
            // XXX move this to a singleton holder
            if (codec == null) {
                codec = builder()
                        .withInitialDelay(registry.getInitialDelay())
                        .withDefaults(registry.getPath())
                        .build();
            }
            return codec;
        }
        return null;
    }

    @Override
    public void close() {
        if (codec != null) {
            codec.close();
        }
    }

    public static class Builder {

        // Dictionary size can be up to 10 MeB in bytes.  This is the output from training.
        public static final int DEFAULT_DICT_SIZE = 10485760;

        // The sample size is the sum of the individual samples,
        // i.e. if you have 1000 messages that are all 26 bytes each,
        // then the sample size is 26000 bytes.
        public static final int DEFAULT_SAMPLE_SIZE = 100_000 * 1024;

        private int sampleSize = DEFAULT_SAMPLE_SIZE;
        private int dictSize = DEFAULT_DICT_SIZE;
        private int compressionLevel = 3;

        private Duration initialDelay = Duration.ZERO;
        private Supplier<ZStandardCodec> codecSupplier;

        Builder() {
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

        public ZStandardCodec build() throws CodecException {
            return this.codecSupplier.get();
        }
    }

    public static Builder builder() {
        return new Builder();
    }
}
