package com.higherfrequencytrading.chronology.slf4j.impl;

import net.openhft.lang.io.Bytes;

import java.util.Date;

/**
 *
 */
public abstract class AbstractTextChronicleLogReader extends AbstractChronicleLogReader {
    @Override
    public void read(Bytes bytes) {
        this.process(bytes.readLine());
    }

    @Override
    public void process(Date timestamp, int level, long threadId, String threadName, String name, String message, Object... args) {
        throw new UnsupportedOperationException();
    }
}
