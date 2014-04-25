package com.higherfrequencytrading.chronology.logback;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.UnsynchronizedAppenderBase;
import net.openhft.chronicle.Chronicle;
import net.openhft.chronicle.ExcerptAppender;

import java.io.IOException;

public abstract class AbstractChronicleAppender extends UnsynchronizedAppenderBase<ILoggingEvent> {
    private String path;
    private Chronicle chronicle;
    private ExcerptAppender appender;

    public AbstractChronicleAppender() {
        this.path = null;
        this.chronicle = null;
        this.appender = null;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getPath() {
        return this.path;
    }

    @Override
    protected void append(ILoggingEvent event) {
        this.appender.startExcerpt();
        this.appender.writeLong(event.getTimeStamp());
        this.appender.writeEnum(event.getThreadName());
        this.appender.writeInt(event.getLevel().levelInt);
        this.appender.writeEnum(event.getMessage());
        this.appender.finish();
    }

    @Override
    public void start() {
        if(getPath() == null) {
            addError("Appender " + getName() + " has configuration errors and is not started!");
        } else {
            try {
                this.chronicle = createChronicle();
                this.appender  = this.chronicle.createAppender();
            } catch(IOException e) {
                this.chronicle = null;
                this.appender  = null;
                addError("Appender " + getName() + " " + e.getMessage());
            }

            super.start();
        }
    }

    @Override
    public void stop() {
        if(this.chronicle != null) {
            try {
                this.chronicle.close();
            } catch(IOException e) {
                addError("Appender " + getName() + " " + e.getMessage());
            }
        }

        super.stop();
    }

    protected abstract Chronicle createChronicle() throws IOException;
}
