package com.higherfrequencytrading.chronology.logback;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.ThrowableProxy;
import ch.qos.logback.core.spi.FilterReply;
import com.higherfrequencytrading.chronology.Chronology;
import com.higherfrequencytrading.chronology.ChronologyLogHelper;

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
    public void doAppend(final ILoggingEvent event) {
        if(getFilterChainDecision(event) != FilterReply.DENY) {
            appender.startExcerpt();
            appender.append(this.dateFormatCache.get().format(new Date(event.getTimeStamp())));
            appender.append('|');
            appender.append(toStrChronologyLogLevel(event.getLevel()));
            appender.append('|');
            appender.append(event.getThreadName());
            appender.append('|');
            appender.append(event.getLoggerName());
            appender.append('|');
            appender.append(event.getFormattedMessage());

            ThrowableProxy tp = (ThrowableProxy)event.getThrowableProxy();
            if(tp != null) {
                appender.append(" - ");
                appender.append(ChronologyLogHelper.getStackTraceAsString(
                    tp.getThrowable(),
                    Chronology.COMMA,
                    this.stackTradeDepth)
                );
            }

            appender.append(Chronology.NEWLINE);
            appender.finish();
        }
    }
}
