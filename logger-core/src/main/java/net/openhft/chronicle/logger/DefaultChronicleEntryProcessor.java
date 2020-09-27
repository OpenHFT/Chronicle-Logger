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
public class DefaultChronicleEntryProcessor implements ChronicleEntryProcessor<String>, AutoCloseable {

    private final CodecRegistry codecRegistry;
    private final Bytes<ByteBuffer> sourceBytes;
    private final Bytes<ByteBuffer> destBytes;

    public DefaultChronicleEntryProcessor(CodecRegistry registry) {
        this.codecRegistry = registry;
        this.sourceBytes = Bytes.elasticByteBuffer(1024);
        this.destBytes = Bytes.elasticByteBuffer(1024);
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
        // XXX fix this so not as wasteful
        // https://openhft.github.io/Chronicle-Bytes/apidocs/net/openhft/chronicle/bytes/Bytes.html#wrapForRead-byte:A-
        try {
            sourceBytes.writeSome(e.contentAsByteBuffer());
            Codec codec = codecRegistry.find(e.contentEncoding());
            byte[] actualArray;
            if (codec != null) {
                ByteBuffer src = sourceBytes.underlyingObject();
                src.position(0);
                src.limit((int) sourceBytes.readLimit());
                long maxBounds = codec.compressBounds(src.limit());
                destBytes.ensureCapacity(maxBounds);
                ByteBuffer dst = destBytes.underlyingObject();
                dst.position(0);
                dst.limit((int) maxBounds);
                int actualSize = codec.decompress(src, dst);
                destBytes.readLimit(actualSize);

                actualArray = new byte[actualSize];
                destBytes.read(actualArray);
            } else {
                actualArray = new byte[(int) sourceBytes.readRemaining()];
                sourceBytes.copyTo(actualArray);
            }

            Charset charset = getCharset(e.contentType());
            return new String(actualArray, charset);
        } finally {
            sourceBytes.clear();
            destBytes.clear();
        }
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

    @Override
    public void close() {
        sourceBytes.releaseLast();
        destBytes.releaseLast();
    }
}
