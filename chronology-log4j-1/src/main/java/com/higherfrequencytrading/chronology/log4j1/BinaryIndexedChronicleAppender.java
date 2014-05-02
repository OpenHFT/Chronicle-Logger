package com.higherfrequencytrading.chronology.log4j1;

import net.openhft.chronicle.Chronicle;
import net.openhft.chronicle.ChronicleConfig;
import net.openhft.chronicle.IndexedChronicle;
import org.apache.log4j.spi.LoggingEvent;

import java.io.IOException;

public class BinaryIndexedChronicleAppender extends ChronicleAppender {

    private ChronicleConfig config;
    private Object lock;

    public BinaryIndexedChronicleAppender() {
        this.config = null;
        this.lock = new Object();
    }

    public void setConfig(ChronicleConfig config) {
        this.config = config;
    }

    @Override
    protected Chronicle createChronicle() throws IOException {
        return (this.config != null)
            ? new IndexedChronicle(this.getPath(),this.config)
            : new IndexedChronicle(this.getPath());
    }

    @Override
    protected void append(LoggingEvent event) {
        synchronized (this.lock) {
            super.append(event);
        }
    }
}
