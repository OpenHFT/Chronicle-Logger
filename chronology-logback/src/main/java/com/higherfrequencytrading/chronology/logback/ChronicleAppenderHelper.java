package com.higherfrequencytrading.chronology.logback;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import com.higherfrequencytrading.chronology.Chronology;
import com.higherfrequencytrading.chronology.ChronologyLogEvent;
import com.higherfrequencytrading.chronology.ChronologyLogLevel;
import net.openhft.chronicle.ExcerptAppender;
import net.openhft.chronicle.ExcerptTailer;

public class ChronicleAppenderHelper {

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
        appender.writeByte(Chronology.VERSION);
        appender.writeByte(Chronology.TYPE_LOGBACK);
        appender.writeLong(event.getTimeStamp());
        appender.writeInt(toChronologyLogLevel(event.getLevel()));
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

    /**
     * Read an ChronologyLogEvent from an Excerpt
     *
     * @param tailer    the ExcerptTailer
     * @return          the ILoggingEvent
     */
    public static ChronologyLogEvent read(final ExcerptTailer tailer) {
        final ChronologyLogEvent event = new ChronologyLogEvent();
        event.readMarshallable(tailer);

        return event;
    }

    // *************************************************************************
    //
    // *************************************************************************

    public static int toChronologyLogLevel(final Level level) {
        switch(level.levelInt) {
            case Level.DEBUG_INT:
                return ChronologyLogLevel.DEBUG.levelInt;
            case Level.TRACE_INT:
                return ChronologyLogLevel.TRACE.levelInt;
            case Level.INFO_INT:
                return ChronologyLogLevel.INFO.levelInt;
            case Level.WARN_INT:
                return ChronologyLogLevel.WARN.levelInt;
            case Level.ERROR_INT:
                return ChronologyLogLevel.ERROR.levelInt;
            default:
                throw new IllegalArgumentException(level.levelInt + " not a valid level value");
        }
    }

    // *************************************************************************
    //
    // *************************************************************************

    /*
        @Override
        public void readMarshallable(@NotNull Bytes in) throws IllegalStateException {
            if(in.readInt() == Chronology.VERSION) {
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
    */
}
