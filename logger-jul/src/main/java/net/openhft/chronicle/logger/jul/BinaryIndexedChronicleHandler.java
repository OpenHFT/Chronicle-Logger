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
import net.openhft.chronicle.logger.ChronicleLogAppenderConfig;

import java.io.IOException;
import java.util.logging.LogRecord;

public class BinaryIndexedChronicleHandler extends BinaryChronicleHandler {
    private ExcerptAppender appender;
    private Object lock;
    private ChronicleLogAppenderConfig config;

    public BinaryIndexedChronicleHandler() throws IOException {
        super();

        this.appender = null;
        this.lock = new Object();
        this.config = null;
        this.configure();
    }

    @Override
    public void publish(final LogRecord record) {
        synchronized (this.lock) {
            super.publish(record);
        }
    }

    @Override
    protected ExcerptAppender getAppender() {
        return this.appender;
    }

    // *************************************************************************
    //
    // *************************************************************************

    protected void configure() throws IOException {
        final ChronicleHandlerConfig cfg = new ChronicleHandlerConfig(getClass());

        this.config = cfg.getIndexedAppenderConfig();

        super.configure(cfg);
        super.setFormatMessage(cfg.getBoolean("formatMessage", false));
        super.setChronicle(this.config.build(this.getPath()));
    }
}
