package com.higherfrequencytrading.chronology.logback;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.IThrowableProxy;
import ch.qos.logback.classic.spi.LoggerContextVO;
import net.openhft.chronicle.ExcerptAppender;
import net.openhft.chronicle.ExcerptTailer;
import net.openhft.lang.io.Bytes;
import net.openhft.lang.io.serialization.BytesMarshallable;
import net.openhft.lang.model.constraints.NotNull;
import org.slf4j.Marker;
import org.slf4j.helpers.MessageFormatter;

import java.util.HashMap;
import java.util.Map;

public class ChronicleAppenderHelper {

    public static final int VERSION = 1;

    /**
     * Write ILoggingEvent to an Excerpt
     *
     * @param appender          the ExcerptAppender
     * @param event             the ILoggingEvent
     * @param includeMDC        include or not Mapped Diagnostic Context
     * @param includeCallerData include or not CallerData
     */
    public static void write(final ExcerptAppender appender, final ILoggingEvent event, boolean includeMDC, boolean includeCallerData) {
        appender.startExcerpt();
        appender.writeInt(VERSION);
        appender.writeLong(event.getTimeStamp());
        appender.writeInt(event.getLevel().levelInt);
        appender.writeUTF(event.getThreadName());
        appender.writeUTF(event.getLoggerName());
        appender.writeUTF(event.getMessage());

        // Args
        Object[] args = event.getArgumentArray();
        int argsLen = null != args ? args.length : 0;

        appender.writeInt(argsLen);
        for(int i=0; i < argsLen; i++) {
            appender.writeObject(args[i]);
        }

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

        appender.finish();
    }

    /**
     * Read an ILoggingEvent from an Excerpt
     *
     * @param tailer    the ExcerptTailer
     * @return          the ILoggingEvent
     */
    public static ILoggingEvent read(final ExcerptTailer tailer) {
        final MarshallableLoggingEvent event = new MarshallableLoggingEvent();
        event.readMarshallable(tailer);

        return event;
    }

    // *************************************************************************
    //
    // *************************************************************************

    /**
     * A wrapper BytesMarshallable class for ILoggingEvent
     */
    private static final class MarshallableLoggingEvent implements ILoggingEvent, BytesMarshallable {

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

        // *********************************************************************
        //
        // *********************************************************************

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
            if(this.fmtMessage == null) {
                this.fmtMessage = MessageFormatter.arrayFormat(this.message, this.args).getMessage();
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
            return this.callerData != null && this.callerData.length > 0;
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

        // *********************************************************************
        //
        // *********************************************************************

        @Override
        public void readMarshallable(@NotNull Bytes in) throws IllegalStateException {
            if(in.readInt() == VERSION) {
                this.timestamp  = in.readLong();
                this.level      = in.readInt();
                this.threadName = in.readUTF();
                this.loggerName = in.readUTF();
                this.message    = in.readUTF();

                // Args
                // TODO: should this.args be null ?
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
                // TODO: should this.callerData be null ?
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
}
