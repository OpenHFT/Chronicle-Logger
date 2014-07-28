package com.higherfrequencytrading.chronology.logback;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.filter.Filter;
import ch.qos.logback.core.spi.ContextAwareBase;
import ch.qos.logback.core.spi.FilterAttachableImpl;
import ch.qos.logback.core.spi.FilterReply;
import net.openhft.chronicle.logger.ChronicleLogLevel;
import net.openhft.chronicle.Chronicle;
import net.openhft.chronicle.ExcerptAppender;

import java.io.IOException;
import java.util.List;

public abstract class AbstractChronicleAppender
    extends ContextAwareBase
    implements Appender<ILoggingEvent> {

    private final FilterAttachableImpl<ILoggingEvent> filterAttachable;

    private String name;
    private boolean started = false;

    private String path;

    protected Chronicle chronicle;

    protected AbstractChronicleAppender() {
        this.filterAttachable = new FilterAttachableImpl<ILoggingEvent>();

        this.name = null;
        this.started = false;
        this.path = null;

        this.chronicle = null;
    }

    // *************************************************************************
    // Custom logging options
    // *************************************************************************

    public void setPath(String path) {
        this.path = path;
    }

    public String getPath() {
        return this.path;
    }

    // *************************************************************************
    // Chronicle implementation
    // *************************************************************************

    protected abstract Chronicle createChronicle() throws IOException;


    protected abstract ExcerptAppender getAppender();

    // *************************************************************************
    //
    // *************************************************************************

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public boolean isStarted() {
        return started;
    }

    public void addFilter(Filter<ILoggingEvent> newFilter) {
        this.filterAttachable.addFilter(newFilter);
    }

    public void clearAllFilters() {
        this.filterAttachable.clearAllFilters();
    }

    public List<Filter<ILoggingEvent>> getCopyOfAttachedFiltersList() {
        return this.filterAttachable.getCopyOfAttachedFiltersList();
    }

    public FilterReply getFilterChainDecision(ILoggingEvent event) {
        return this.filterAttachable.getFilterChainDecision(event);
    }

    @Override
    public void start() {
        if(getPath() == null) {
            addError("Appender " + getName() + " has configuration errors and is not started!");
        } else {
            try {
                this.chronicle = createChronicle();
            } catch(IOException e) {
                this.chronicle = null;
                addError("Appender " + getName() + " " + e.getMessage());
            }
        }
    }

    @Override
    public void stop() {
        if(this.chronicle != null) {
            try {
                this.chronicle.close();
            } catch(IOException e) {
                addError("Appender " + getName() + " " + e.getMessage());
            }
        }
    }

    // *************************************************************************
    //
    // *************************************************************************

    public static ChronicleLogLevel toChronologyLogLevel(final Level level) {
        switch(level.levelInt) {
            case Level.DEBUG_INT:
                return ChronicleLogLevel.DEBUG;
            case Level.TRACE_INT:
                return ChronicleLogLevel.TRACE;
            case Level.INFO_INT:
                return ChronicleLogLevel.INFO;
            case Level.WARN_INT:
                return ChronicleLogLevel.WARN;
            case Level.ERROR_INT:
                return ChronicleLogLevel.ERROR;
            default:
                throw new IllegalArgumentException(level.levelInt + " not a valid level value");
        }
    }
}
