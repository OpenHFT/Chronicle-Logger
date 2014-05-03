package com.higherfrequencytrading.chronology.logback;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.spi.FilterReply;
import com.higherfrequencytrading.chronology.Chronology;

public abstract class TextChronicleAppender extends ChronicleAppender {

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
    public void doAppend(final ILoggingEvent event) {
        if(getFilterChainDecision(event) != FilterReply.DENY) {
            ChronicleAppenderHelper.writeText(
                appender,
                event,
                this.dateFormatCache.get(),
                false,
                false);
        }
    }
}
