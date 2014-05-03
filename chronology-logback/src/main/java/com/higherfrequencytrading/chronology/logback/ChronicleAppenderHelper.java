package com.higherfrequencytrading.chronology.logback;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import com.higherfrequencytrading.chronology.Chronology;
import com.higherfrequencytrading.chronology.ChronologyLogLevel;
import net.openhft.chronicle.ExcerptAppender;

import java.text.DateFormat;
import java.util.Date;

public class ChronicleAppenderHelper {

    /**
     * Write ILoggingEvent to a binary Excerpt
     *
     * @param appender          the ExcerptAppender
     * @param event             the ILoggingEvent
     * @param formatMessage     writeBinary formatted or unformatted message
     * @param includeMDC        include or not Mapped Diagnostic Context
     * @param includeCallerData include or not CallerData
     */
    public static void writeBinary(
        final ExcerptAppender appender,
        final ILoggingEvent event,
        boolean formatMessage,
        boolean includeMDC,
        boolean includeCallerData) {

        appender.startExcerpt();
        appender.writeByte(Chronology.VERSION);
        appender.writeByte(Chronology.TYPE_LOGBACK);
        appender.writeLong(event.getTimeStamp());
        appender.writeInt(toIntChronologyLogLevel(event.getLevel()));
        appender.writeUTF(event.getThreadName());
        appender.writeUTF(event.getLoggerName());

        if(!formatMessage) {
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
     * Write ILoggingEvent to a text Excerpt
     *
     * @param appender          the ExcerptAppender
     * @param event             the ILoggingEvent
     * @param dateFormat        date format to use
     * @param includeMDC        include or not Mapped Diagnostic Context
     * @param includeCallerData include or not CallerData
     */
    public static void writeText(
        final ExcerptAppender appender,
        final ILoggingEvent event,
        final DateFormat dateFormat,
        boolean includeMDC,
        boolean includeCallerData) {

        appender.startExcerpt();
        appender.writeUTF(dateFormat.format(new Date(event.getTimeStamp())));
        appender.writeUTF(toStrChronologyLogLevel(event.getLevel()));
        appender.writeUTF(event.getThreadName());
        appender.writeUTF(event.getLoggerName());
        appender.writeUTF(event.getFormattedMessage());
        appender.finish();
    }

    // *************************************************************************
    //
    // *************************************************************************

    public static int toIntChronologyLogLevel(final Level level) {
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

    public static String toStrChronologyLogLevel(final Level level) {
        switch(level.levelInt) {
            case Level.DEBUG_INT:
                return ChronologyLogLevel.DEBUG.levelStr;
            case Level.TRACE_INT:
                return ChronologyLogLevel.TRACE.levelStr;
            case Level.INFO_INT:
                return ChronologyLogLevel.INFO.levelStr;
            case Level.WARN_INT:
                return ChronologyLogLevel.WARN.levelStr;
            case Level.ERROR_INT:
                return ChronologyLogLevel.ERROR.levelStr;
            default:
                throw new IllegalArgumentException(level.levelInt + " not a valid level value");
        }
    }
}
