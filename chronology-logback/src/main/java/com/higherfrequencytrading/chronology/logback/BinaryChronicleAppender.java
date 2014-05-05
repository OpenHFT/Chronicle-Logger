package com.higherfrequencytrading.chronology.logback;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.ThrowableProxy;
import ch.qos.logback.core.spi.FilterReply;
import com.higherfrequencytrading.chronology.Chronology;

public abstract class BinaryChronicleAppender extends AbstractChronicleAppender {

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
            appender.startExcerpt();
            appender.writeByte(Chronology.VERSION);
            appender.writeByte(Chronology.TYPE_LOGBACK);
            appender.writeLong(event.getTimeStamp());
            appender.writeInt(toIntChronologyLogLevel(event.getLevel()));
            appender.writeUTF(event.getThreadName());
            appender.writeUTF(event.getLoggerName());

            if (!formatMessage) {
                appender.writeUTF(event.getMessage());

                // Args
                Object[] args = event.getArgumentArray();
                int argsLen = null != args ? args.length : 0;

                appender.writeInt(argsLen);
                for (int i = 0; i < argsLen; i++) {
                    appender.writeObject(args[i]);
                }
            } else {
                appender.writeUTF(event.getFormattedMessage());
                appender.writeInt(0);
            }

            ThrowableProxy tp = (ThrowableProxy)event.getThrowableProxy();
            if(tp != null) {
                appender.writeBoolean(true);
                appender.writeObject(tp.getThrowable());
            } else {
                appender.writeBoolean(false);
            }

            /*
            if(includeMDC) {
                // Mapped Diagnostic Context http://logback.qos.ch/manual/mdc.html
                final Map<String, String> mdcProps = event.getMDCPropertyMap();
                appender.writeInt(null != mdcProps ? mdcProps.size() : 0);
                if(mdcProps != null) {
                    for (Map.Entry<String, String> entry : mdcProps.entrySet()) {
                        appender.writeUTF(entry.getKey());
                        appender.writeUTF(entry.getValue());
                    }
                }
            } else {
                appender.writeInt(0);
            }

            if(includeCallerData) {
                Object[] callerData = event.getCallerData();
                int callerDataLen = null != callerData ? callerData.length : 0;

                appender.writeInt(callerDataLen);
                for(int i=0; i < callerDataLen; i++) {
                    appender.writeObject(callerData[i]);
                }

            } else {
                appender.writeInt(0);
            }
            */

            appender.finish();
        }
    }
}
