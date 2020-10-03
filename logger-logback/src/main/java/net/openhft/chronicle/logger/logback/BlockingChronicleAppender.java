package net.openhft.chronicle.logger.logback;

import ch.qos.logback.classic.spi.ILoggingEvent;
import net.openhft.chronicle.bytes.Bytes;
import net.openhft.chronicle.logger.entry.EntryHelpers;

import java.nio.ByteBuffer;
import java.util.concurrent.locks.ReentrantLock;

/**
 * A chronicle appender that blocks and is thread-safe.
 *
 * This is useful for when you would rather block than lose logging messages, as
 * async appenders tend to drop messages from the queue under load.
 */
public class BlockingChronicleAppender extends ChronicleAppenderBase {

    private final Bytes<ByteBuffer> sourceBytes = Bytes.elasticByteBuffer(1024);
    private final Bytes<ByteBuffer> destBytes = Bytes.elasticByteBuffer(1024);

    /**
     * All synchronization in this class is done via the lock object.
     */
    protected final ReentrantLock lock = new ReentrantLock(false);

    @Override
    public void append(final ILoggingEvent event) {
        long epochMillis = event.getTimeStamp();
        EntryHelpers helpers = EntryHelpers.instance();
        long second = helpers.epochSecondFromMillis(epochMillis);
        int nanos = helpers.nanosFromMillis(epochMillis);
        try {
            lock.lock();
            byte[] content = encoder.encode(event);
            sourceBytes.write(content);
            codec.compress(sourceBytes, destBytes);
            writer.write(
                    second,
                    nanos,
                    event.getLevel().toInt(),
                    event.getLoggerName(),
                    event.getThreadName(),
                    destBytes
            );
        } finally {
            sourceBytes.clear();
            destBytes.clear();
            lock.unlock();
        }
    }

    @Override
    public void stop() {
        super.stop();
        sourceBytes.releaseLast();
        destBytes.releaseLast();
    }
}
