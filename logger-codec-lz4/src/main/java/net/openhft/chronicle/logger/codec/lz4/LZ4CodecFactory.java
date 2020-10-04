package net.openhft.chronicle.logger.codec.lz4;

import net.jpountz.lz4.LZ4Factory;
import net.openhft.chronicle.logger.codec.Codec;
import net.openhft.chronicle.logger.codec.CodecFactory;
import net.openhft.chronicle.logger.codec.CodecRegistry;

public class LZ4CodecFactory implements CodecFactory {
    @Override
    public Codec find(CodecRegistry registry, String encodingName) {
        if (LZ4Codec.NAME.equalsIgnoreCase(encodingName)) {
            return new LZ4Codec(LZ4Factory.fastestInstance());
        } else {
            return null;
        }
    }
}
