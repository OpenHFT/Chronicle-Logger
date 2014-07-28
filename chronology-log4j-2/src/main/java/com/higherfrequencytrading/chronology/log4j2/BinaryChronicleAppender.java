package com.higherfrequencytrading.chronology.log4j2;


import com.higherfrequencytrading.chronology.Chronology;
import net.openhft.chronicle.ExcerptAppender;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.message.Message;

public abstract class BinaryChronicleAppender extends AbstractChronicleAppender {

    private boolean includeCallerData;
    private boolean includeMDC;
    private boolean formatMessage;

    protected BinaryChronicleAppender(String name, Filter filter) {
        super(name, filter);

        this.includeCallerData = true;
        this.includeMDC = true;
        this.formatMessage = false;
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
    public void append(final LogEvent event) {
        final ExcerptAppender appender = getAppender();
        if(appender != null) {
            appender.startExcerpt();
            appender.writeByte(Chronology.VERSION);
            Chronology.Type.LOG4J_2.writeTo(appender);
            appender.writeLong(event.getMillis());
            toChronologyLogLevel(event.getLevel()).writeTo(appender);
            appender.writeUTF(event.getThreadName());
            appender.writeUTF(event.getLoggerName());

            if (!formatMessage) {
                Message message = event.getMessage();

                appender.writeUTF(event.getMessage().getFormat());

                // Args
                Object[] args = message.getParameters();
                int argsLen = null != args ? args.length : 0;

                appender.writeStopBit(argsLen);
                for (int i = 0; i < argsLen; i++) {
                    appender.writeObject(args[i]);
                }
            }
            else {
                appender.writeUTF(event.getMessage().getFormattedMessage());
                appender.writeStopBit(0);
            }

            if (event.getThrown() != null) {
                appender.writeBoolean(true);
                appender.writeObject(event.getThrown());
            }
            else {
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

            IThrowableProxy throwableProxy = event.getThrowableProxy();
            if(throwableProxy != null) {
                appender.writeBoolean(true);
                appender.writeObject(throwableProxy);
            } else {
                appender.writeBoolean(false);
            }
            */

            appender.finish();
        }
    }
}
