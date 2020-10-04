package net.openhft.chronicle.logger.codec;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.ServiceLoader;

/**
 * A codec registry that can return specific codecs.
 *
 * Codecs are loaded using the CodecFactory using ServiceLoader.
 *
 * There are two implementations provided, "zstd" and "lz4".  You can
 * add your own fairly easily using those two as examples, for example
 * you could add <a href="https://github.com/Blosc/JBlosc">blosc</a> as
 * a "faster than memcpy" compression algorithm.
 */
public class CodecRegistry {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final Duration initialDelay;
    private final Path path;

    CodecRegistry(Path path, Duration initialDelay) {
        this.path = path;
        this.initialDelay = initialDelay;
    }

    public Codec find(String encoding) throws CodecException {
        return find(encoding, Thread.currentThread().getContextClassLoader());
    }

    public Codec find(String encoding, ClassLoader classLoader) throws CodecException {
        if (encoding == null || encoding.isEmpty()) {
            throw new IllegalArgumentException("Null or empty encoding!");
        }

        if (IdentityCodec.NAME.equalsIgnoreCase(encoding)) {
            return new IdentityCodec();
        }

        ServiceLoader<CodecFactory> loader = ServiceLoader.load(CodecFactory.class, classLoader);
        if (! loader.iterator().hasNext()) {
            String msg = "No codec factories found!  Please add library dependencies to classpath!";
            throw new CodecException(msg);
        }

        for (CodecFactory factory: loader) {
            logger.debug("Using factory {} to look for encoding {}", factory, encoding);
            Codec codec = factory.find(this, encoding);
            if (codec != null) {
                return codec;
            }
        }
        String msg = String.format("No codec found for encoding \"%s\"", encoding);
        throw new CodecException(msg);
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
