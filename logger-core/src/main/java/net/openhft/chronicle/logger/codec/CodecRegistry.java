package net.openhft.chronicle.logger.codec;

import java.io.Closeable;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class CodecRegistry implements Closeable {

    public static final String IDENTITY_ENCODING = "identity";

    private final HashMap<String, Codec> codecMap;

    private static final Codec IDENTITY = new IdentityCodec();

    CodecRegistry() {
        codecMap = new HashMap<>();
    }

    public void addCodec(String key, Codec codec) {
        codecMap.put(key, codec);
    }

    public Codec find(String encoding) throws CodecException {
        if (encoding == null || IDENTITY_ENCODING.equalsIgnoreCase(encoding)) return IDENTITY;
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

    public static class Builder {
        private final CodecRegistry registry;

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
            ZStandardCodec codec = ZStandardCodec.builder().withDefaults(path).build();
            return withCodec("zstd", codec);
        }
    }

    public static Builder builder() {
        return new Builder();
    }
}
