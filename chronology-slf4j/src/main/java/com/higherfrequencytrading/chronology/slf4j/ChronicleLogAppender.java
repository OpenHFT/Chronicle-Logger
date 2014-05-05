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
     * @param args
     */
    public void log(ChronologyLogLevel level, String name, String message, Object... args);
}
