package com.higherfrequencytrading.chronology.log4j1;

import net.openhft.chronicle.Chronicle;
import net.openhft.chronicle.ChronicleConfig;
import net.openhft.chronicle.ExcerptAppender;
import net.openhft.chronicle.IndexedChronicle;
import org.apache.log4j.spi.LoggingEvent;

import java.io.IOException;

public class TextIndexedChronicleAppender extends TextChronicleAppender {

    private ChronicleConfig config;
    private Object lock;
    private ExcerptAppender appender;

    public TextIndexedChronicleAppender() {
        this.config = null;
        this.lock = new Object();
        this.appender = null;
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
    public void doAppend(final LoggingEvent event) {
        synchronized (this.lock) {
            super.doAppend(event);
        }
    }
}
