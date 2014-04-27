package com.higherfrequencytrading.chronology.slf4j;


import java.util.Date;

public interface ChronicleLogProcessor {
    /**
     * Text log
     *
     * @param message
     */
    public void process(String message);

    /**
     * Binary log
     *
     * @param timestamp
     * @param level
     * @param threadId
     * @param threadName
     * @param name
     * @param message
     * @param args
     */
    public void process(Date timestamp, int level, long threadId, String threadName, String name, String message, Object... args);
}
