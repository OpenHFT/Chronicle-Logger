package com.higherfrequencytrading.chronology.log4j1;

import net.openhft.chronicle.ExcerptAppender;
import net.openhft.chronicle.ExcerptTailer;
import org.apache.log4j.spi.LoggingEvent;

public class ChronicleAppenderHelper {
    public static final int VERSION = 1;

    public static void write(final ExcerptAppender appender, final LoggingEvent event, boolean includeMDC, boolean includeCallerData) {
    }

    public static LoggingEvent read(final ExcerptTailer tailer) {
        return null;
    }

    // *************************************************************************
    //
    // *************************************************************************
}
