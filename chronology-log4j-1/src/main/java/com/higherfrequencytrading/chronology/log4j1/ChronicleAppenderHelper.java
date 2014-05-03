package com.higherfrequencytrading.chronology.log4j1;

import com.higherfrequencytrading.chronology.Chronology;
import com.higherfrequencytrading.chronology.ChronologyLogLevel;
import net.openhft.chronicle.ExcerptAppender;
import org.apache.log4j.Level;
import org.apache.log4j.spi.LoggingEvent;

public class ChronicleAppenderHelper {

    /**
     * Write ILoggingEvent to an Excerpt
     *
     * @param appender          the ExcerptAppender
     * @param event             the ILoggingEvent
     * @param formatMessage     writeBinary formatted or unformatted message
     * @param includeMDC        include or not Mapped Diagnostic Context
     * @param includeCallerData include or not CallerData
     */
    public static void writeBinary(
        final ExcerptAppender appender,
        final LoggingEvent event,
        boolean formatMessage,
        boolean includeMDC,
        boolean includeCallerData) {

        appender.startExcerpt();
        appender.writeByte(Chronology.VERSION);
        appender.writeByte(Chronology.TYPE_LOG4J_1);
        appender.writeLong(event.getTimeStamp());
        appender.writeInt(toChronologyLogLevel(event.getLevel()));
        appender.writeUTF(event.getThreadName());
        appender.writeUTF(event.getLoggerName());
        appender.writeUTF(event.getMessage().toString());
        appender.writeInt(0);
        appender.finish();
    }

    // *************************************************************************
    //
    // *************************************************************************

    public static int toChronologyLogLevel(final Level level) {
        switch(level.toInt()) {
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
                throw new IllegalArgumentException(level.toInt() + " not a valid level value");
        }
    }
}
