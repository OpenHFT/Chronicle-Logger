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

import net.openhft.chronicle.logger.ChronicleLogAppender;
import net.openhft.chronicle.logger.ChronicleLogFormatter;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;

abstract class AbstractChronicleHandler extends Handler {

    private String path;
    private ChronicleLogAppender appender;

    protected AbstractChronicleHandler() {
        this.path = null;
        this.appender = null;
    }

    @Override
    public void flush() {
    }

    @Override
    public void close() throws SecurityException {
        if(this.appender != null && this.appender.getChronicle() != null) {
            try {
                this.appender.getChronicle().close();
            } catch (IOException e) {
                // Ignore
            }
        }
    }

    @Override
    public void publish(final LogRecord record) {
        if((appender != null) && isLoggable(record)) {
            this.appender.log(
                ChronicleHandlerHelper.getLogLevel(record),
                record.getMillis(),
                "thread-" + record.getThreadID(),
                record.getLoggerName(),
                record.getMessage(),
                record.getThrown(),
                record.getParameters());
        }
    }

    // *************************************************************************
    //
    // *************************************************************************

    protected ChronicleLogAppender getAppender() {
        return this.appender;
    }

    protected void setAppender(ChronicleLogAppender appender) {
        this.appender = appender;
    }

    protected void configure(ChronicleHandlerConfig cfg) throws IOException {
        setLevel(cfg.getLevel("level", Level.ALL));
        setFilter(cfg.getFilter("filter", null));
    }

    // *************************************************************************
    //
    // *************************************************************************

    static class Formatter implements ChronicleLogFormatter {
        static final Formatter INSTANCE = new Formatter();

        @Override
        public String format(String message, Object arg1) {
            return MessageFormat.format(message, arg1);
        }

        @Override
        public String format(String message, Object arg1, Object arg2) {
            return MessageFormat.format(message, arg1, arg2);
        }

        @Override
        public String format(String message, Throwable throwable, Object... args) {
            return MessageFormat.format(message, args);
        }
    }
}
