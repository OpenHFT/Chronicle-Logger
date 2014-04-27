package com.higherfrequencytrading.chronology.logback;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.IThrowableProxy;
import ch.qos.logback.core.UnsynchronizedAppenderBase;
import net.openhft.chronicle.Chronicle;
import net.openhft.chronicle.ExcerptAppender;

import java.io.IOException;
import java.util.Map;

public abstract class AbstractChronicleAppender extends UnsynchronizedAppenderBase<ILoggingEvent> {
    public static final int VERSION = 1;

    private String path;
    private boolean includeCallerData;
    private boolean includeMDC;

    private Chronicle chronicle;
    private ExcerptAppender appender;

    public AbstractChronicleAppender() {
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
        this.appender.startExcerpt();
        this.appender.writeInt(VERSION);
        this.appender.writeLong(event.getTimeStamp());
        this.appender.writeInt(event.getLevel().levelInt);
        this.appender.writeUTF(event.getThreadName());
        this.appender.writeUTF(event.getLoggerName());
        this.appender.writeUTF(event.getMessage());

        // Args
        Object[] args = event.getArgumentArray();
        int argsLen = null != args ? args.length : 0;

        appender.writeInt(argsLen);
        for(int i=0; i < argsLen; i++) {
            appender.writeObject(args[i]);
        }

        if(this.includeMDC) {
            // Mapped Diagnostic Context http://logback.qos.ch/manual/mdc.html
            final Map<String, String> mdcProps = event.getMDCPropertyMap();
            this.appender.writeInt(null != mdcProps ? mdcProps.size() : 0);
            if(mdcProps != null) {
                for (Map.Entry<String, String> entry : mdcProps.entrySet()) {
                    this.appender.writeUTF(entry.getKey());
                    this.appender.writeUTF(entry.getValue());
                }
            }
        } else {
            this.appender.writeInt(0);
        }

        if(this.includeCallerData) {
            Object[] callerData = event.getCallerData();
            int callerDataLen = null != callerData ? callerData.length : 0;

            appender.writeInt(callerDataLen);
            for(int i=0; i < callerDataLen; i++) {
                appender.writeObject(callerData[i]);
            }

        } else {
            this.appender.writeInt(0);
        }

        IThrowableProxy throwableProxy = event.getThrowableProxy();
        if(throwableProxy != null) {
            this.appender.writeBoolean(true);
            this.appender.writeObject(throwableProxy);
        } else {
            this.appender.writeBoolean(false);
        }

        this.appender.finish();
    }
}
