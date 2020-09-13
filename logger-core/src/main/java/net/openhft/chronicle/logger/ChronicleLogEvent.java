package net.openhft.chronicle.logger;

import java.time.Instant;

/**
 * A POJO representing a logging event.
 */
public class ChronicleLogEvent {

    public Instant timestamp;
    public int level;
    public String threadName;
    public String loggerName;
    public byte[] entry;
    public String contentType;
    public String encoding;

    public ChronicleLogEvent(Instant timestamp, int level, String threadName, String loggerName, byte[] entry, String contentType, String encoding) {
        this.timestamp = timestamp;
        this.level = level;
        this.threadName = threadName;
        this.loggerName = loggerName;
        this.entry = entry;
        this.contentType = contentType;
        this.encoding = encoding;
    }


}
