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

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.ThrowableProxy;
import ch.qos.logback.core.spi.FilterReply;
import net.openhft.chronicle.ExcerptAppender;
import net.openhft.chronicle.logger.ChronicleLog;
import net.openhft.chronicle.logger.ChronicleLogHelper;
import net.openhft.chronicle.logger.TimeStampFormatter;

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
            final ExcerptAppender appender = getAppender();
            if (appender != null) {
                appender.startExcerpt();
                timeStampFormatter.format(event.getTimeStamp(), appender);
                appender.append('|');
                toChronicleLogLevel(event.getLevel()).printTo(appender);
                appender.append('|');
                appender.append(event.getThreadName());
                appender.append('|');
                appender.append(event.getLoggerName());
                appender.append('|');
                appender.append(event.getFormattedMessage());

                ThrowableProxy tp = (ThrowableProxy) event.getThrowableProxy();
                if (tp != null) {
                    appender.append(" - ");
                    ChronicleLogHelper.appendStackTraceAsString(
                        appender,
                        tp.getThrowable(),
                        ChronicleLog.COMMA,
                        this.stackTradeDepth
                    );
                }

                appender.append(ChronicleLog.NEWLINE);
                appender.finish();
            }
        }
    }
}
