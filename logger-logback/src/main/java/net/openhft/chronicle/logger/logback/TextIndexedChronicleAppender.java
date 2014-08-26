/*
 * Copyright 2014 Higher Frequency Trading
 * <p/>
 * http://www.higherfrequencytrading.com
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.openhft.chronicle.logger.logback;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.joran.spi.DefaultClass;
import net.openhft.chronicle.Chronicle;
import net.openhft.chronicle.ExcerptAppender;
import net.openhft.chronicle.IndexedChronicle;
import net.openhft.chronicle.logger.IndexedLogAppenderConfig;

import java.io.IOException;

public class TextIndexedChronicleAppender extends TextChronicleAppender {

    private ExcerptAppender appender;
    private Object lock;
    private IndexedLogAppenderConfig config;

    public TextIndexedChronicleAppender() {
        this.appender = null;
        this.lock = new Object();
        this.config = null;
    }

    @DefaultClass(value = IndexedLogAppenderConfig.class)
    public void setChronicleConfig(final IndexedLogAppenderConfig config) {
        this.config = config;
    }

    public IndexedLogAppenderConfig getChronicleConfig() {
        return this.config;
    }

    @Override
    protected Chronicle createChronicle() throws IOException {
        Chronicle chronicle = (this.config != null)
            ? new IndexedChronicle(this.getPath(), this.config.config())
            : new IndexedChronicle(this.getPath());

        this.appender = chronicle.createAppender();

        return chronicle;
    }

    @Override
    protected ExcerptAppender getAppender() {
        return this.appender;
    }

    @Override
    public void doAppend(final ILoggingEvent event) {
        synchronized (this.lock) {
            super.doAppend(event);
        }
    }
}
