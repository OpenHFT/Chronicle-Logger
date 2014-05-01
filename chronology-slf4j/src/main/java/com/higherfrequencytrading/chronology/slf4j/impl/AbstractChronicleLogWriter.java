package com.higherfrequencytrading.chronology.slf4j.impl;

import com.higherfrequencytrading.chronology.slf4j.ChronicleLogWriter;
import net.openhft.chronicle.Chronicle;
import net.openhft.chronicle.ExcerptAppender;

import java.io.IOException;

/**
 *
 */
public abstract class AbstractChronicleLogWriter implements ChronicleLogWriter {

    protected final ExcerptAppender appender;
    private final Chronicle chronicle;

    /**
     * @param chronicle
     * @throws java.io.IOException
     */
    public AbstractChronicleLogWriter(Chronicle chronicle) throws IOException {
        this.chronicle = chronicle;
        this.appender = this.chronicle.createAppender();
    }

    @Override
    public Chronicle getChronicle() {
        return this.chronicle;
    }

    @Override
    public void close() throws IOException {
        if (this.chronicle != null) {
            this.chronicle.close();
        }
    }
}
