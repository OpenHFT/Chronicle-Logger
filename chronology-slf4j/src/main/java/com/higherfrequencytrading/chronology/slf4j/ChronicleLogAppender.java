package com.higherfrequencytrading.chronology.slf4j;


import com.higherfrequencytrading.chronology.ChronologyLogLevel;
import net.openhft.chronicle.Chronicle;

import java.io.Closeable;

/**
 *
 */
public interface ChronicleLogAppender extends Closeable {
    /**
     * @return
     */
    public Chronicle getChronicle();

    /**
     * @param level
     * @param name
     * @param message
     * @param arg1
     */
    public void log(ChronologyLogLevel level, String name, String message, Object arg1);

    /**
     * @param level
     * @param name
     * @param message
     * @param arg1
     * @param arg2
     */
    public void log(ChronologyLogLevel level, String name, String message, Object arg1, Object arg2);

    /**
     * @param level
     * @param name
     * @param message
     * @param args
     */
    public void log(ChronologyLogLevel level, String name, String message, Object... args);

    /**
     * @param level
     * @param name
     * @param message
     * @param throwable
     */
    public void log(ChronologyLogLevel level, String name, String message, Throwable throwable);
}
