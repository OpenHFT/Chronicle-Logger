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

import net.openhft.chronicle.Chronicle;
import net.openhft.chronicle.ExcerptAppender;
import net.openhft.chronicle.logger.ChronicleLog;

import java.io.IOException;
import java.util.logging.ChronicleHandlerConfig;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;

public abstract class BinaryChronicleHandler extends Handler {

    private String path;
    private Chronicle chronicle;
    private boolean formatMessage;

    protected BinaryChronicleHandler() throws IOException {
        this.path = null;
        this.chronicle = null;
        this.formatMessage = false;

        configure();
    }

    @Override
    public void publish(final LogRecord record) {
        if(isLoggable(record)) {
            final ExcerptAppender appender = getAppender();
            if (appender != null) {
                appender.startExcerpt();
                appender.writeByte(ChronicleLog.VERSION);
                appender.writeLong(record.getMillis());
                ChronicleHandleHelper.getLogLevel(record).writeTo(appender);
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

    @Override
    public void flush() {
    }

    @Override
    public void close() throws SecurityException {
        if(this.chronicle != null) {
            try {
                this.chronicle.close();
            } catch (IOException e) {
                // Ignore
            }
        }
    }

    // *************************************************************************
    //
    // *************************************************************************

    protected abstract Chronicle createChronicle() throws IOException;
    protected abstract ExcerptAppender getAppender();

    // *************************************************************************
    //
    // *************************************************************************

    protected String getPath() {
        return this.path;
    }

    protected Chronicle getChronicle() {
        return this.chronicle;
    }

    protected void configure() throws IOException {
        final ChronicleHandlerConfig cfg = new ChronicleHandlerConfig(getClass());

        this.path = cfg.getString("path", null);
        this.formatMessage = cfg.getBoolean("formatMessage", false);

        setLevel(cfg.getLevel("level", Level.ALL));
        setFilter(cfg.getFilter("filter", null));

        this.chronicle = createChronicle();
    }
}
