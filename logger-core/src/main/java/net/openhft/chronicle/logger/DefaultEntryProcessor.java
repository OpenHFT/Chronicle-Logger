package net.openhft.chronicle.logger;

import net.openhft.chronicle.bytes.Bytes;
import net.openhft.chronicle.logger.codec.Codec;
import net.openhft.chronicle.logger.entry.Entry;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * Turns a log entry into a String with the appropriate, passing it through a decompression
 * codec depending on the event's encoding.
 */
public class DefaultEntryProcessor implements EntryProcessor<String>, AutoCloseable {

    private final Bytes<ByteBuffer> sourceBytes;
    private final Bytes<ByteBuffer> destBytes;
    private final Codec codec;
    private final Charset charset;

    public DefaultEntryProcessor(Codec codec) {
        this(codec, StandardCharsets.UTF_8);
    }

    public DefaultEntryProcessor(Codec codec, Charset charset) {
        this.sourceBytes = Bytes.elasticByteBuffer(1024);
        this.destBytes = Bytes.elasticByteBuffer(1024);
        this.codec = codec;
        this.charset = charset;
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
        // XXX fix this so not as wasteful, use contentInByteBuffer
        // https://openhft.github.io/Chronicle-Bytes/apidocs/net/openhft/chronicle/bytes/Bytes.html#wrapForRead-byte:A-
        try {
            ByteBuffer byteBuffer = e.contentAsByteBuffer();
            sourceBytes.writeSome(byteBuffer);
            //System.out.println("read: " + sourceBytes.toHexString());
            int actualSize = codec.decompress(sourceBytes, destBytes);
            byte[] actualArray;
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
