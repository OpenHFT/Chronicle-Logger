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

package net.openhft.chronicle.logger.log4j2;

import net.openhft.chronicle.ExcerptAppender;
import net.openhft.chronicle.logger.ChronicleLog;
import net.openhft.chronicle.logger.ChronicleLogHelper;
import net.openhft.chronicle.logger.TimeStampFormatter;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.LogEvent;

public abstract class TextChronicleAppender extends AbstractChronicleAppender {

    private String dateFormat;
    private TimeStampFormatter timeStampFormatter;
    private int stackTraceDepth;

    protected TextChronicleAppender(final String name, final Filter filter, final String path) {
        super(name, filter, path);

        this.dateFormat = null;
        this.timeStampFormatter = null;
        this.stackTraceDepth = -1;
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

    public void setStackTraceDepth(int stackTraceDepth) {
        this.stackTraceDepth = stackTraceDepth;
    }

    public int getStackTraceDepth() {
        return this.stackTraceDepth;
    }

    // *************************************************************************
    //
    // *************************************************************************

    @Override
    public void append(final LogEvent event) {
        final ExcerptAppender appender = getAppender();
        if (appender != null) {
            appender.startExcerpt();
            timeStampFormatter.format(event.getTimeMillis(), appender);
            appender.append('|');
            toChronicleLogLevel(event.getLevel()).printTo(appender);
            appender.append('|');
            appender.append(event.getThreadName());
            appender.append('|');
            appender.append(event.getLoggerName());
            appender.append('|');
            appender.append(event.getMessage().getFormattedMessage());

            Throwable th = event.getThrown();
            if (th != null) {
                appender.append(" - ");
                ChronicleLogHelper.appendStackTraceAsString(
                    appender,
                    th,
                    ChronicleLog.COMMA,
                    this.stackTraceDepth
                );
            }

            appender.append('\n');
            appender.finish();
        }
    }
}
