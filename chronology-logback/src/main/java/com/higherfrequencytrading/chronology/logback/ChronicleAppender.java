package com.higherfrequencytrading.chronology.logback;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.UnsynchronizedAppenderBase;
import net.openhft.chronicle.Chronicle;
import net.openhft.chronicle.ExcerptAppender;

import java.io.IOException;

public abstract class ChronicleAppender extends UnsynchronizedAppenderBase<ILoggingEvent> {
    private String path;
    private boolean includeCallerData;
    private boolean includeMDC;

    private Chronicle chronicle;
    private ExcerptAppender appender;

    protected ChronicleAppender() {
        this.path = null;
        this.includeCallerData = true;
        this.includeMDC = true;

        this.chronicle = null;
        this.appender = null;
    }

    // *************************************************************************
    // Custom logging options
    // *************************************************************************

    public void setIncludeCallerData(boolean logCallerData) {
        this.includeCallerData = logCallerData;
    }

    public boolean isIncludeCallerData() {
        return this.includeCallerData;
    }

    public void setIncludeMappedDiagnosticContext(boolean logMDC) {
        this.includeMDC = logMDC;
    }

    public boolean isIncludeMappedDiagnosticContext() {
        return this.includeMDC;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getPath() {
        return this.path;
    }

    // *************************************************************************
    // Chronicle implementation
    // *************************************************************************

    protected abstract Chronicle createChronicle() throws IOException;

    // *************************************************************************
    //
    // *************************************************************************

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

    @Override
    protected void append(final ILoggingEvent event) {
        ChronicleAppenderHelper.write(
            this.appender,
            event,
            this.includeMDC,
            this.includeCallerData);
    }
}
