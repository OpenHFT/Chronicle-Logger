package com.higherfrequencytrading.chronology.log4j2;

import com.higherfrequencytrading.chronology.Chronology;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.LogEvent;

import java.util.Date;

public abstract class TextChronicleAppender extends AbstractChronicleAppender {

    private String dateFormat;
    private Chronology.DateFormatCache dateFormatCache;

    protected TextChronicleAppender(String name, Filter filter) {
        super(name, filter);

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
    public void append(final LogEvent event) {
        appender.startExcerpt();
        appender.append(this.dateFormatCache.get().format(new Date(event.getMillis())));
        appender.append('|');
        appender.append(toStrChronologyLogLevel(event.getLevel()));
        appender.append('|');
        appender.append(event.getThreadName());
        appender.append('|');
        appender.append(event.getLoggerName());
        appender.append('|');
        appender.append(event.getMessage().getFormattedMessage());
        appender.append('\n');
        appender.finish();
    }
}
