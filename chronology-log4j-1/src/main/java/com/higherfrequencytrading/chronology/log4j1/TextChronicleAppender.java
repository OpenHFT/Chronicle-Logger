package com.higherfrequencytrading.chronology.log4j1;

import com.higherfrequencytrading.chronology.Chronology;
import org.apache.log4j.spi.LoggingEvent;

import java.util.Date;

public abstract class TextChronicleAppender extends AbstractChronicleAppender {

    private String dateFormat;
    private Chronology.DateFormatCache dateFormatCache;

    protected TextChronicleAppender() {
        super();

        this.dateFormat = null;
        this.dateFormatCache = null;
    }

    // *************************************************************************
    // Custom logging options
    // *************************************************************************

    public void setDateFormat(String dateFormat) {
        this.dateFormat = dateFormat;
        this.dateFormatCache = new Chronology.DateFormatCache(dateFormat);
    }

    public String getDateFormat() {
        return this.dateFormat;
    }

    // *************************************************************************
    //
    // *************************************************************************

    @Override
    protected void append(final LoggingEvent event) {
        createAppender();

        if(this.appender != null) {
            appender.startExcerpt();
            appender.append(this.dateFormatCache.get().format(new Date(event.getTimeStamp())));
            appender.append('|');
            appender.append(toStrChronologyLogLevel(event.getLevel()));
            appender.append('|');
            appender.append(event.getThreadName());
            appender.append('|');
            appender.append(event.getLoggerName());
            appender.append('|');
            appender.append(event.getRenderedMessage());
            appender.append('\n');
            appender.finish();
        }
    }
}
