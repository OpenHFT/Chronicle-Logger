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

package net.openhft.chronicle.logger.log4j1;

import net.openhft.chronicle.logger.ChronicleLogLevel;
import net.openhft.chronicle.logger.ChronicleLogWriter;
import org.apache.log4j.Appender;
import org.apache.log4j.Layout;
import org.apache.log4j.Level;
import org.apache.log4j.helpers.LogLog;
import org.apache.log4j.helpers.OnlyOnceErrorHandler;
import org.apache.log4j.spi.*;

import java.io.IOException;

public abstract class AbstractChronicleAppender implements Appender, OptionHandler {

    private Filter filter;
    private String name;
    private ErrorHandler errorHandler;

    protected ChronicleLogWriter writer;

    private String path;

    protected AbstractChronicleAppender() {
        this.path = null;
        this.writer = null;
        this.name = null;
        this.errorHandler = new OnlyOnceErrorHandler();
    }

    // *************************************************************************
    // Custom logging options
    // *************************************************************************

    @Override
    public void activateOptions() {
        if(path != null) {
            try {
                this.writer = createWriter();
            } catch (IOException e) {
                LogLog.warn("Exception ["+name+"].",e);
            }
        } else {
            LogLog.warn("path option not set for appender ["+name+"].");
        }
    }

    @Override
    public void addFilter(Filter newFilter) {
        if(filter == null) {
            filter = newFilter;

        } else {
            filter.setNext(newFilter);
        }
    }

    @Override
    public void clearFilters() {
        filter = null;
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

    @Override
    public void finalize() {
        // An appender might be closed then garbage collected. There is no
        // point in closing twice.
        if(this.writer == null) {
            LogLog.debug("Finalizing appender named [" + name + "].");
            close();
        }
    }

    @Override
    public ErrorHandler getErrorHandler() {
        return this.errorHandler;
    }

    @Override
    public Filter getFilter() {
        return this.filter;
    }

    @Override
    public Layout getLayout() {
        return null;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public synchronized void setErrorHandler(ErrorHandler eh) {
        if(eh == null) {
            // We do not throw exception here since the cause is probably a
            // bad cfg file.
            LogLog.warn("You have tried to set a null error-handler.");

        } else {
            this.errorHandler = eh;
        }
    }

    @Override
    public void setLayout(Layout layout) {
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public void doAppend(LoggingEvent event) {
        if(this.writer != null) {
            for(Filter f = this.filter; f != null;f = f.getNext()) {
                switch(f.decide(event)) {
                    case Filter.DENY:
                        return;
                    case Filter.ACCEPT:
                        f = null;
                        break;
                }
            }

            Throwable throwable = null;
            ThrowableInformation ti = event.getThrowableInformation();
            if(ti != null) {
                throwable = ti.getThrowable();
            }

            writer.write(
                toChronicleLogLevel(event.getLevel()),
                event.getTimeStamp(),
                event.getThreadName(),
                event.getLoggerName(),
                event.getMessage().toString(),
                throwable
            );

        } else {
            LogLog.error("Attempted to append to closed appender named ["+name+"].");
            return;
        }
    }

    // *************************************************************************
    // Chronicle implementation
    // *************************************************************************

    protected abstract ChronicleLogWriter createWriter() throws IOException;

    // *************************************************************************
    //
    // *************************************************************************

    @Override
    public void close() {
        if(this.writer != null) {
            try {
                this.writer.close();
            } catch(IOException e) {
                LogLog.warn("Failed to close the writer", e);
            }
        }
    }

    @Override
    public boolean requiresLayout() {
        return false;
    }

    // *************************************************************************
    //
    // *************************************************************************

    public static ChronicleLogLevel toChronicleLogLevel(final Level level) {
        switch(level.toInt()) {
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
                throw new IllegalArgumentException(level.toInt() + " not a valid level value");
        }
    }
}
