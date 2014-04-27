package com.higherfrequencytrading.chronology.logback.tools;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.IThrowableProxy;
import ch.qos.logback.classic.spi.LoggerContextVO;
import com.higherfrequencytrading.chronology.logback.AbstractChronicleAppender;
import net.openhft.lang.io.Bytes;
import net.openhft.lang.io.serialization.BytesMarshallable;
import net.openhft.lang.model.constraints.NotNull;
import org.slf4j.Marker;
import org.slf4j.helpers.MessageFormatter;

import java.util.HashMap;
import java.util.Map;

public class MarshallableLoggingEvent implements ILoggingEvent, BytesMarshallable{

    private long timestamp;
    private int level;
    private String threadName;
    private String loggerName;
    private String message;
    private String fmtMessage;
    private Object[] args;
    private StackTraceElement[] callerData;
    private Map<String,String> mdc;
    private IThrowableProxy throwableProxy;

    public MarshallableLoggingEvent() {
        this.timestamp      = -1;
        this.level          = -1;
        this.threadName     = null;
        this.loggerName     = null;
        this.message        = null;
        this.fmtMessage     = null;
        this.args           = null;
        this.callerData     = null;
        this.mdc            = null;
        this.throwableProxy = null;
    }

    // *************************************************************************
    //
    // *************************************************************************

    @Override
    public long getTimeStamp() {
        return this.timestamp;
    }

    @Override
    public String getThreadName() {
        return this.threadName;
    }

    @Override
    public Level getLevel() {
        return Level.toLevel(this.level);
    }

    @Override
    public String getMessage() {
        return this.message;
    }

    @Override
    public Object[] getArgumentArray() {
        return this.args;
    }

    @Override
    public String getFormattedMessage() {
        if(this.fmtMessage != null) {
            this.fmtMessage = MessageFormatter.arrayFormat(this.message,this.args).getMessage();
        }

        return this.fmtMessage;
    }

    @Override
    public String getLoggerName() {
        return this.loggerName;
    }

    @Override
    public IThrowableProxy getThrowableProxy() {
        return this.throwableProxy;
    }

    @Override
    public StackTraceElement[] getCallerData() {
        return this.callerData;
    }

    @Override
    public boolean hasCallerData() {
        return this.callerData != null ? this.callerData.length > 0 : false;
    }

    @Override
    public Map<String, String> getMDCPropertyMap() {
        return this.mdc;
    }

    @Override
    public Marker getMarker() {
        throw new UnsupportedOperationException();
    }

    @Override
    public LoggerContextVO getLoggerContextVO() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void prepareForDeferredProcessing() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Map<String, String> getMdc() {
        throw new UnsupportedOperationException();
    }

    // *************************************************************************
    //
    // *************************************************************************

    @Override
    public void readMarshallable(@NotNull Bytes in) throws IllegalStateException {
        if(in.readInt() == AbstractChronicleAppender.VERSION) {
            this.timestamp  = in.readLong();
            this.level      = in.readInt();
            this.threadName = in.readUTF();
            this.loggerName = in.readUTF();
            this.message    = in.readUTF();

            // Args
            this.args = new Object[in.readInt()];
            for(int i=0;i<this.args.length;i++) {
                this.args[i] = in.readObject();
            }

            // Mapped Diagnostic Context
            this.mdc = new HashMap<String,String>();
            for(int i=in.readInt()-1;i>=0;i--) {
                String k = in.readUTF();
                String v = in.readUTF();
                this.mdc.put(k,v);
            }

            // Caller Data
            this.callerData = new StackTraceElement[in.readInt()];
            for(int i=0;i<this.callerData.length;i++) {
                this.callerData[i] = in.readObject(StackTraceElement.class);
            }

            if(in.readBoolean()) {
                this.throwableProxy = in.readObject(IThrowableProxy.class);
            }

        } else {
            throw new UnsupportedClassVersionError();
        }
    }

    @Override
    public void writeMarshallable(@NotNull Bytes out) {
        throw new UnsupportedOperationException();
    }
}
