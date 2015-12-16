/*
 * Copyright 2014 Higher Frequency Trading
 *
 * http://www.higherfrequencytrading.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.openhft.chronicle.logger.logback;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.filter.Filter;
import ch.qos.logback.core.spi.ContextAwareBase;
import ch.qos.logback.core.spi.FilterAttachableImpl;
import ch.qos.logback.core.spi.FilterReply;
import net.openhft.chronicle.logger.ChronicleLogLevel;
import net.openhft.chronicle.logger.ChronicleLogWriter;

import java.io.IOException;
import java.util.List;

public abstract class AbstractChronicleAppender
    extends ContextAwareBase
    implements Appender<ILoggingEvent> {

    private final FilterAttachableImpl<ILoggingEvent> filterAttachable;

    private String name;
    private boolean started = false;

    private String path;

    protected ChronicleLogWriter writer;

    protected AbstractChronicleAppender() {
        this.filterAttachable = new FilterAttachableImpl<>();
        this.name = null;
        this.started = false;
        this.path = null;
        this.writer = null;
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

    protected abstract ChronicleLogWriter createWriter() throws IOException;
    protected abstract void doAppend(final ILoggingEvent event, final ChronicleLogWriter writer);

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

    @Override
    public void addFilter(Filter<ILoggingEvent> newFilter) {
        this.filterAttachable.addFilter(newFilter);
    }

    @Override
    public void clearAllFilters() {
        this.filterAttachable.clearAllFilters();
    }

    @Override
    public List<Filter<ILoggingEvent>> getCopyOfAttachedFiltersList() {
        return this.filterAttachable.getCopyOfAttachedFiltersList();
    }

    @Override
    public FilterReply getFilterChainDecision(ILoggingEvent event) {
        return this.filterAttachable.getFilterChainDecision(event);
    }

    @Override
    public void start() {
        if(getPath() == null) {
            addError("Appender " + getName() + " has configuration errors and is not started!");

        } else {
            try {
                this.writer = createWriter();
                this.started = true;
            } catch(IOException e) {
                this.writer = null;
                addError("Appender " + getName() + " " + e.getMessage());
            }
        }
    }

    @Override
    public void stop() {
        if(this.writer != null) {
            try {
                this.writer.close();
            } catch(IOException e) {
                addError("Appender " + getName() + " " + e.getMessage());
            }
        }

        this.started = false;
    }

    @Override
    public void doAppend(final ILoggingEvent event) {
        if (getFilterChainDecision(event) != FilterReply.DENY) {
            doAppend(event, writer);
        }
    }

    // *************************************************************************
    //
    // *************************************************************************

    public static ChronicleLogLevel toChronicleLogLevel(final Level level) {
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
