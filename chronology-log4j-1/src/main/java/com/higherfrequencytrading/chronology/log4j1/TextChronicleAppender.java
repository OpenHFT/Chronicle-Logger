package com.higherfrequencytrading.chronology.log4j1;

import com.higherfrequencytrading.chronology.Chronology;
import com.higherfrequencytrading.chronology.ChronologyLogHelper;
import org.apache.log4j.spi.LoggingEvent;
import org.apache.log4j.spi.ThrowableInformation;

import java.util.Date;

public abstract class TextChronicleAppender extends AbstractChronicleAppender {

    private String dateFormat;
    private Chronology.DateFormatCache dateFormatCache;
    private int stackTradeDepth;

    protected TextChronicleAppender() {
        super();

        this.dateFormat = null;
        this.dateFormatCache = null;
        this.stackTradeDepth = -1;
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

    public void setStackTradeDepth(int stackTradeDepth) {
        this.stackTradeDepth = stackTradeDepth;
    }

    public int getStackTradeDepth() {
        return this.stackTradeDepth;
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

            ThrowableInformation ti = event.getThrowableInformation();
            if(ti != null) {
                appender.append(" - ");
                ChronologyLogHelper.appendStackTraceAsString(
                    this.appender,
                    ti.getThrowable(),
                    Chronology.COMMA,
                    this.stackTradeDepth
                );
            }

            appender.append('\n');
            appender.finish();
        }
    }
}
