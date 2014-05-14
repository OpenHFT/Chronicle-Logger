package com.higherfrequencytrading.chronology.log4j1;

import com.higherfrequencytrading.chronology.Chronology;
import org.apache.log4j.spi.LoggingEvent;
import org.apache.log4j.spi.ThrowableInformation;

public abstract class BinaryChronicleAppender extends AbstractChronicleAppender {

    private boolean includeCallerData;
    private boolean includeMDC;

    protected BinaryChronicleAppender() {
        this.includeCallerData = true;
        this.includeMDC = true;
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

    // *************************************************************************
    //
    // *************************************************************************

    @Override
    protected void append(final LoggingEvent event) {
        createAppender();

        if(this.appender != null) {
            appender.startExcerpt();
            appender.writeByte(Chronology.VERSION);
            appender.writeByte(Chronology.TYPE_LOG4J_1);
            appender.writeLong(event.getTimeStamp());
            appender.writeByte(toIntChronologyLogLevel(event.getLevel()));
            appender.writeUTF(event.getThreadName());
            appender.writeUTF(event.getLoggerName());
            appender.writeUTF(event.getMessage().toString());
            appender.writeInt(0);

            ThrowableInformation ti = event.getThrowableInformation();
            if(ti != null) {
                appender.writeBoolean(true);
                appender.writeObject(ti.getThrowable());
            } else {
                appender.writeBoolean(false);
            }

            appender.finish();
        }
    }
}
