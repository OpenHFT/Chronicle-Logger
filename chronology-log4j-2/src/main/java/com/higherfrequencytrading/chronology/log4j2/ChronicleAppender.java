package com.higherfrequencytrading.chronology.log4j2;


import net.openhft.chronicle.Chronicle;
import net.openhft.chronicle.ExcerptAppender;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;

import java.io.IOException;

public abstract class ChronicleAppender extends AbstractAppender {
    private String path;
    private boolean includeCallerData;
    private boolean includeMDC;
    private boolean formatMessage;

    private Chronicle chronicle;
    private ExcerptAppender appender;

    protected ChronicleAppender(String name, Filter filter) {
        super(name, filter, null, true);

        this.path = null;
        this.includeCallerData = true;
        this.includeMDC = true;
        this.formatMessage = false;

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

    public void setFormatMessage(boolean formatMessage) {
        this.formatMessage = formatMessage;
    }

    public boolean isFormatMessage() {
        return this.formatMessage;
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
            LOGGER.error("Appender " + getName() + " has configuration errors and is not started!");
        } else {
            try {
                this.chronicle = createChronicle();
                this.appender  = this.chronicle.createAppender();
            } catch(IOException e) {
                this.chronicle = null;
                this.appender  = null;
                LOGGER.error("Appender " + getName() + " " + e.getMessage());
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
                LOGGER.error("Appender " + getName() + " " + e.getMessage());
            }
        }

        super.stop();
    }

    @Override
    public void append(final LogEvent event) {
        ChronicleAppenderHelper.writeBinary(
            this.appender,
            event,
            this.formatMessage,
            this.includeMDC,
            this.includeCallerData);
    }
}
