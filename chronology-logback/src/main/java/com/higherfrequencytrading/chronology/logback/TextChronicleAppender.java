package com.higherfrequencytrading.chronology.logback;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.ThrowableProxy;
import ch.qos.logback.core.spi.FilterReply;
import com.higherfrequencytrading.chronology.*;

public abstract class TextChronicleAppender extends AbstractChronicleAppender {

    private String dateFormat;
    private TimeStampFormatter timeStampFormatter;
    private int stackTradeDepth;

    protected TextChronicleAppender() {
        super();

        this.dateFormat = null;
        this.timeStampFormatter = null;
        this.stackTradeDepth = -1;
    }

    // *************************************************************************
    // Custom logging options
    // *************************************************************************

    public void setDateFormat(String dateFormat) {
        this.dateFormat = dateFormat;
        this.timeStampFormatter = TimeStampFormatter.fromDateFormat(dateFormat);
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
            timeStampFormatter.format(event.getTimeStamp(), appender);
            appender.append('|');
            toChronologyLogLevel(event.getLevel()).printTo(appender);
            appender.append('|');
            appender.append(event.getThreadName());
            appender.append('|');
            appender.append(event.getLoggerName());
            appender.append('|');
            appender.append(event.getFormattedMessage());

            ThrowableProxy tp = (ThrowableProxy)event.getThrowableProxy();
            if(tp != null) {
                appender.append(" - ");
                ChronologyLogHelper.appendStackTraceAsString(
                    this.appender,
                    tp.getThrowable(),
                    Chronology.COMMA,
                    this.stackTradeDepth
                );
            }

            appender.append(Chronology.NEWLINE);
            appender.finish();
        }
    }
}
