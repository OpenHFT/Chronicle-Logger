package com.higherfrequencytrading.chronology.logback;

import ch.qos.logback.classic.spi.ILoggingEvent;
import net.openhft.chronicle.Chronicle;
import net.openhft.chronicle.ChronicleConfig;
import net.openhft.chronicle.ExcerptAppender;
import net.openhft.chronicle.IndexedChronicle;

import java.io.IOException;

public class BinaryIndexedChronicleAppender extends BinaryChronicleAppender {

    private ChronicleConfig config;
    private ExcerptAppender appender;
    private Object lock;

    public BinaryIndexedChronicleAppender() {
        this.config = null;
        this.appender = null;
        this.lock = new Object();
    }

    public void setConfig(ChronicleConfig config) {
        this.config = config;
    }

    @Override
    protected Chronicle createChronicle() throws IOException {
        Chronicle chronicle = (this.config != null)
            ? new IndexedChronicle(this.getPath(),this.config)
            : new IndexedChronicle(this.getPath());

        this.appender = chronicle.createAppender();

        return chronicle;
    }

    @Override
    protected ExcerptAppender getAppender() {
        return this.appender;
    }

    @Override
    public void doAppend(final ILoggingEvent event) {
        synchronized (this.lock) {
            super.doAppend(event);
        }
    }
}
