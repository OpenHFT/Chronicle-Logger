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

import java.io.IOException;
import java.util.logging.LogRecord;

abstract class BinaryChronicleHandler extends AbstractChronicleHandler {

    private boolean formatMessage;

    protected BinaryChronicleHandler() throws IOException {
        this.formatMessage = false;
    }

    @Override
    public void publish(final LogRecord record) {
        if(isLoggable(record)) {
            final ExcerptAppender appender = getAppender();
            if (appender != null) {
                appender.startExcerpt();
                appender.writeByte(ChronicleLog.VERSION);
                appender.writeLong(record.getMillis());
                ChronicleHandlerHelper.getLogLevel(record).writeTo(appender);
                appender.writeUTF("thread-" + record.getThreadID());
                appender.writeUTF(record.getLoggerName());

                if (!formatMessage) {
                    appender.writeUTF(record.getMessage());

                    // Args
                    Object[] args = record.getParameters();
                    int argsLen = null != args ? args.length : 0;

                    appender.writeStopBit(argsLen);
                    for (int i = 0; i < argsLen; i++) {
                        appender.writeObject(args[i]);
                    }
                } else {
                    appender.writeUTF(getFormatter().formatMessage(record));
                    appender.writeStopBit(0);
                }

                Throwable tp = record.getThrown();
                if (tp != null) {
                    appender.writeBoolean(true);
                    appender.writeObject(tp);
                }
                else {
                    appender.writeBoolean(false);
                }

                appender.finish();
            }
        }
    }

    // *************************************************************************
    //
    // *************************************************************************

    protected void setFormatMessage(boolean formatMessage) {
        this.formatMessage = formatMessage;
    }
}
