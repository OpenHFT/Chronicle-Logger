package com.higherfrequencytrading.chronology.log4j2;

import com.higherfrequencytrading.chronology.*;
import net.openhft.chronicle.ExcerptAppender;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.LogEvent;

public abstract class TextChronicleAppender extends AbstractChronicleAppender {

    private String dateFormat;
    private TimeStampFormatter timeStampFormatter;
    private int stackTradeDepth;

    protected TextChronicleAppender(String name, Filter filter) {
        super(name, filter);

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
    public void append(final LogEvent event) {
        final ExcerptAppender appender = getAppender();
        if (appender != null) {
            appender.startExcerpt();
            timeStampFormatter.format(event.getMillis(), appender);
            appender.append('|');
            toChronologyLogLevel(event.getLevel()).printTo(appender);
            appender.append('|');
            appender.append(event.getThreadName());
            appender.append('|');
            appender.append(event.getLoggerName());
            appender.append('|');
            appender.append(event.getMessage().getFormattedMessage());

            Throwable th = event.getThrown();
            if (th != null) {
                appender.append(" - ");
                ChronologyLogHelper.appendStackTraceAsString(
                    appender,
                    th,
                    Chronology.COMMA,
                    this.stackTradeDepth
                );
            }

            appender.append('\n');
            appender.finish();
        }
    }
}
