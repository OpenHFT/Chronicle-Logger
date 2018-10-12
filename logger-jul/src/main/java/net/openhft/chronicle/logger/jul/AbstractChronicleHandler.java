/*
 * Copyright 2014-2017 Chronicle Software
 *
 * http://www.chronicle.software
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

import net.openhft.chronicle.logger.ChronicleLogWriter;

import java.io.IOException;
import java.util.logging.Filter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;

abstract class AbstractChronicleHandler extends Handler {

    private String path;
    private ChronicleLogWriter writer;

    protected AbstractChronicleHandler() {
        this.path = null;
        this.writer = null;
    }

    @Override
    public void flush() {
    }

    @Override
    public void close() throws SecurityException {
        if (this.writer != null) {
            try {
                this.writer.close();
            } catch (IOException e) {
                // Ignore
            }
        }
    }

    @Override
    public void publish(final LogRecord record) {
        if ((writer != null) && isLoggable(record)) {
            doPublish(record, this.writer);
        }
    }

    // *************************************************************************
    //
    // *************************************************************************

    protected abstract void doPublish(final LogRecord record, final ChronicleLogWriter writer);

    protected final void setWriter(ChronicleLogWriter appender) {
        this.writer = appender;
    }

    @Override
    public final void setFilter(Filter newFilter) {
        super.setFilter(newFilter);
    }

    @Override
    public final synchronized void setLevel(Level newLevel) {
        super.setLevel(newLevel);
    }
}
