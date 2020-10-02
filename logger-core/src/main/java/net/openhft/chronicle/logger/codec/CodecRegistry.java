package net.openhft.chronicle.logger.codec;

import net.openhft.chronicle.bytes.Bytes;

import java.io.Closeable;
import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

// XXX this should use a ServiceLoader map
public class CodecRegistry implements Closeable {

    public static final String IDENTITY = "identity";
    public static final String ZSTANDARD = "zstd";

    private final HashMap<String, Codec> codecMap;

    CodecRegistry() {
        codecMap = new HashMap<>();
        codecMap.put(IDENTITY, new IdentityCodec());
    }

    public void addCodec(String key, Codec codec) {
        codecMap.put(key, codec);
    }

    public Codec find(String encoding) throws CodecException {
        if (encoding == null) {
            throw new IllegalArgumentException("Null encoding!");
        }
        Codec codec = codecMap.get(encoding);
        if (codec == null) {
            throw new CodecException("No codec found for encoding " + encoding);
        }
        return codec;
    }

    @Override
    public void close() {
        for (Map.Entry<String, Codec> entry : codecMap.entrySet()) {
            Codec codec = entry.getValue();
            codec.close();
        }
    }

    public static class IdentityCodec implements Codec {

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

    public static class Builder {
        private final CodecRegistry registry;
        private Duration initialDelay = Duration.ZERO;

        Builder() {
            registry = new CodecRegistry();
        }

        public Builder withCodec(String key, Codec codec) {
            registry.addCodec(key, codec);
            return this;
        }

        public CodecRegistry build() {
            return registry;
        }

        public Builder withDefaults(String path) {
            return withDefaults(Paths.get(path));
        }

        public Builder withDefaults(Path path) {
            ZStandardCodec codec = ZStandardCodec.builder()
                    .withInitialDelay(initialDelay)
                    .withDefaults(path)
                    .build();
            return withCodec(ZSTANDARD, codec);
        }

        public Builder withInitialDelay(Duration initialDelay) {
            this.initialDelay = initialDelay;
            return this;
        }
    }

    public static Builder builder() {
        return new Builder();
    }
}
