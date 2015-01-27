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
package net.openhft.chronicle.logger.jul;

import net.openhft.chronicle.ExcerptAppender;
import net.openhft.chronicle.logger.ChronicleLog;
import net.openhft.chronicle.logger.ChronicleLogHelper;
import net.openhft.chronicle.logger.TimeStampFormatter;

import java.io.IOException;
import java.util.logging.LogRecord;

abstract class TextChronicleHandler extends AbstractChronicleHandler {

    private String dateFormat;
    private TimeStampFormatter timeStampFormatter;
    private int stackTradeDepth;

    protected TextChronicleHandler() throws IOException {
        this.dateFormat = ChronicleLog.DEFAULT_DATE_FORMAT;
        this.timeStampFormatter = TimeStampFormatter.fromDateFormat(dateFormat);
        this.stackTradeDepth = -1;
    }

    @Override
    public void publish(final LogRecord record) {
        if(isLoggable(record)) {
            final ExcerptAppender appender = getAppender();
            if(appender != null) {
                appender.startExcerpt();
                timeStampFormatter.format(record.getMillis(), appender);
                appender.append('|');
                ChronicleHandlerHelper.getLogLevel(record).printTo(appender);
                appender.append('|');
                appender.append("thread-" + record.getThreadID());
                appender.append('|');
                appender.append(record.getLoggerName());
                appender.append('|');
                appender.writeUTF(getFormatter().formatMessage(record));
                appender.writeStopBit(0);

                Throwable tp = record.getThrown();
                if (tp != null) {
                    appender.append(" - ");
                    ChronicleLogHelper.appendStackTraceAsString(
                            appender,
                            tp,
                            ChronicleLog.COMMA,
                            this.stackTradeDepth
                    );
                }

                appender.append('\n');
                appender.finish();
            }
        }
    }

    // *************************************************************************
    //
    // *************************************************************************

    protected void setDateFormat(String dateFormat) {
        this.dateFormat = dateFormat;
        this.timeStampFormatter = TimeStampFormatter.fromDateFormat(dateFormat);
    }

    protected String getDateFormat() {
        return this.dateFormat;
    }

    protected void setStackTradeDepth(int stackTradeDepth) {
        this.stackTradeDepth = stackTradeDepth;
    }

    protected int getStackTradeDepth() {
        return this.stackTradeDepth;
    }
}
