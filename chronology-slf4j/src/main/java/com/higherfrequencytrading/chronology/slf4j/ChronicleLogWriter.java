package com.higherfrequencytrading.chronology.slf4j;


import net.openhft.chronicle.Chronicle;

import java.io.Closeable;

/**
 *
 */
public interface ChronicleLogWriter extends Closeable {
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
    public void log(int level, String name, String message, Object... args);
}
