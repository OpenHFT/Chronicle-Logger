package net.openhft.chronicle.logger.codec;

import java.io.Closeable;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.ServiceLoader;

public class CodecRegistry implements Closeable {


    private final Duration initialDelay;
    private final Path path;

    CodecRegistry(Path path, Duration initialDelay) {
        this.path = path;
        this.initialDelay = initialDelay;
    }

    public Codec find(String encoding) throws CodecException {
        if (encoding == null || encoding.isEmpty()) {
            throw new IllegalArgumentException("Null or empty encoding!");
        }

        if (IdentityCodec.NAME.equalsIgnoreCase(encoding)) {
            return new IdentityCodec();
        }

        ServiceLoader<CodecFactory> loader = ServiceLoader.load(CodecFactory.class);
        for (CodecFactory factory: loader) {
            Codec codec = factory.find(this, encoding);
            if (codec != null) {
                return codec;
            }
            break;
        }
        String msg = String.format("No codec found for encoding \"%s\"", encoding);
        throw new CodecException(msg);
    }

    @Override
    public void close() {
        ServiceLoader<CodecFactory> loader = ServiceLoader.load(CodecFactory.class);
        for (CodecFactory factory : loader) {
            factory.close();
        }
    }

    public Path getPath() {
        return path;
    }

    public Duration getInitialDelay() {
        return initialDelay;
    }

    public static class Builder {
        private Duration initialDelay = Duration.ZERO;
        private Path path;

        Builder() {
        }

        public Builder withDefaults(String path) {
            return withDefaults(Paths.get(path));
        }

        public Builder withDefaults(Path path) {
            this.path = path;
            return this;
        }

        public Builder withInitialDelay(Duration initialDelay) {
            this.initialDelay = initialDelay;
            return this;
        }

        public CodecRegistry build() {
            return new CodecRegistry(path, initialDelay);
        }
    }

    public static Builder builder() {
        return new Builder();
    }
}
