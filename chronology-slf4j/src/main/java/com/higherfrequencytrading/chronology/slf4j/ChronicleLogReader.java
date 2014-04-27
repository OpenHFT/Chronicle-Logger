package com.higherfrequencytrading.chronology.slf4j;



import net.openhft.lang.io.Bytes;

/**
 *
 */
public interface ChronicleLogReader {
    /**
     * @param bytes
     */
    public void read(Bytes bytes);
}
