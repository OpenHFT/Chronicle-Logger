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

import net.openhft.chronicle.logger.ChronicleLogWriter;
import net.openhft.chronicle.logger.ChronicleLogWriters;
import net.openhft.chronicle.logger.IndexedLogAppenderConfig;

import java.io.IOException;

public class TextIndexedChronicleAppender extends AbstractTextChronicleAppender {

    private final IndexedLogAppenderConfig config;

    public TextIndexedChronicleAppender() {
        this.config = new IndexedLogAppenderConfig();
    }

    @Override
    protected ChronicleLogWriter createWriter() throws IOException {
        return ChronicleLogWriters.text(
            this.config,
            this.getPath(),
            this.getDateFormat(),
            this.getStackTradeDepth());
    }

    // *************************************************************************
    // IndexedLogAppenderConfig
    // *************************************************************************

    protected IndexedLogAppenderConfig getChronicleConfig() {
        return this.config;
    }

    public void setIndexBlockSize(int indexBlockSize) {
        config.setIndexBlockSize(indexBlockSize);
    }

    public void setSynchronousMode(boolean synchronousMode) {
        config.setSynchronous(synchronousMode);
    }

    public void setCacheLineSize(int cacheLineSize) {
        config.setCacheLineSize(cacheLineSize);
    }

    public void setMessageCapacity(int messageCapacity) {
        config.setMessageCapacity(messageCapacity);
    }

    public void setUseCheckedExcerpt(boolean useCheckedExcerpt) {
        config.setUseCheckedExcerpt(useCheckedExcerpt);
    }

    public void setDataBlockSize(int dataBlockSize) {
        config.setDataBlockSize(dataBlockSize);
    }
}
