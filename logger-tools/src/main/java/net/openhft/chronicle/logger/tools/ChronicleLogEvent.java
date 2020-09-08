package net.openhft.chronicle.logger.tools;

import java.time.Instant;

public class ChronicleLogEvent {

    public Instant timestamp;
    public int level;
    public String loggerName;
    public String threadName;
    public byte[] entry;
    public String contentType;
    public String encoding;

    public ChronicleLogEvent(Instant timestamp, int level, String loggerName, String threadName, byte[] entry, String contentType, String encoding) {
        this.timestamp = timestamp;
        this.level = level;
        this.loggerName = loggerName;
        this.threadName = threadName;
        this.entry = entry;
        this.contentType = contentType;
        this.encoding = encoding;
    }


}
