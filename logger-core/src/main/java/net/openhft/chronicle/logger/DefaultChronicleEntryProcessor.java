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

    private final Bytes<ByteBuffer> sourceBytes;
    private final Bytes<ByteBuffer> destBytes;
    private final Codec codec;
    private final Charset charset = StandardCharsets.UTF_8;

    public DefaultChronicleEntryProcessor(Codec codec) {
        this.sourceBytes = Bytes.elasticByteBuffer(1024);
        this.destBytes = Bytes.elasticByteBuffer(1024);
        this.codec = codec;
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
            byte[] actualArray;
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
            return new String(actualArray, charset);
        } finally {
            sourceBytes.clear();
            destBytes.clear();
        }
    }

    @Override
    public void close() {
        sourceBytes.releaseLast();
        destBytes.releaseLast();
    }
}
