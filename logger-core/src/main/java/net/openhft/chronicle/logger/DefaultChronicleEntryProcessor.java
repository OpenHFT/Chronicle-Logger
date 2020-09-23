package net.openhft.chronicle.logger;

import net.openhft.chronicle.bytes.Bytes;
import net.openhft.chronicle.bytes.BytesStore;
import net.openhft.chronicle.logger.codec.Codec;
import net.openhft.chronicle.logger.codec.CodecRegistry;
import net.openhft.chronicle.logger.entry.Entry;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * Turns a log entry into a String with the appropriate, passing it through a decompression
 * codec depending on the event's encoding.
 */
public class DefaultChronicleEntryProcessor implements ChronicleEntryProcessor<String> {

    private final CodecRegistry codecRegistry;

    public DefaultChronicleEntryProcessor(CodecRegistry registry) {
        this.codecRegistry = registry;
    }

    @Override
    public String apply(Entry e) {
        try {
            return decode(e);
        } catch (UnsupportedEncodingException ex) {
            return ex.getMessage();
        }
    }

    protected String decode(Entry e) throws UnsupportedEncodingException {
        Bytes<ByteBuffer> bytes = Bytes.wrapForRead(e.contentAsByteBuffer());
        byte[] decoded = decompress(e.contentEncoding(), bytes.toByteArray());
        Charset charset = getCharset(e.contentType());
        return new String(decoded, charset);
    }

    protected byte[] decompress(String encoding, byte[] bytes) {
        Codec codec = codecRegistry.find(encoding);
        return codec.decompress(bytes);
    }

    protected Charset getCharset(String contentType) {
        if (contentType != null) {
            for (String param : contentType.replace(" ", "").split(";")) {
                if (param.startsWith("charset=")) {
                    String charset = param.split("=", 2)[1];
                    return Charset.forName(charset);
                }
            }
        }
        return StandardCharsets.UTF_8;
    }

}
