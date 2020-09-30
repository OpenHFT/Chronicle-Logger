package net.openhft.chronicle.logger.logback;

import ch.qos.logback.classic.spi.ILoggingEvent;
import net.openhft.chronicle.logger.entry.EntryHelpers;

import java.util.concurrent.locks.ReentrantLock;

/**
 * A chronicle appender that blocks and is thread-safe.
 *
 * This is useful for when you would rather block than lose logging messages, as
 * async appenders tend to drop messages from the queue under load.
 */
public class BlockingChronicleAppender extends ChronicleAppenderBase {

    /**
     * All synchronization in this class is done via the lock object.
     */
    protected final ReentrantLock lock = new ReentrantLock(false);

    @Override
    public void append(final ILoggingEvent event) {
        lock.lock();
        try {
            byte[] entry = encoder.encode(event);
            long epochMillis = event.getTimeStamp();
            EntryHelpers helpers = EntryHelpers.instance();
            long second = helpers.epochSecondFromMillis(epochMillis);
            int nanos = helpers.nanosFromMillis(epochMillis);
            writer.write(
                    second,
                    nanos,
                    event.getLevel().toInt(),
                    event.getLoggerName(),
                    event.getThreadName(),
                    entry
            );
        } finally {
            lock.unlock();
        }
    }
}
