package com.higherfrequencytrading.chronology.logback;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.spi.FilterReply;

public abstract class BinaryChronicleAppender extends ChronicleAppender {

    private boolean includeCallerData;
    private boolean includeMDC;
    private boolean formatMessage;

    protected BinaryChronicleAppender() {
        super();

        this.includeCallerData = true;
        this.includeMDC        = true;
        this.formatMessage     = false;
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

    // *************************************************************************
    //
    // *************************************************************************

    @Override
    public void doAppend(final ILoggingEvent event) {
        if(getFilterChainDecision(event) != FilterReply.DENY) {
            ChronicleAppenderHelper.writeBinary(
                appender,
                event,
                this.formatMessage,
                this.includeMDC,
                this.includeCallerData);
        }
    }
}
