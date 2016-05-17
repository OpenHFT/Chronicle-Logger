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
import net.openhft.chronicle.logger.VanillaLogAppenderConfig;

import java.io.IOException;

public class BinaryVanillaChronicleAppender extends AbstractBinaryChronicleAppender {
    private VanillaLogAppenderConfig config;

    public BinaryVanillaChronicleAppender() {
        this.config = new VanillaLogAppenderConfig();
    }

    @Override
    protected ChronicleLogWriter createWriter() throws IOException {
        return ChronicleLogWriters.binary(this.config, this.getPath());
    }

    // *************************************************************************
    // VanillaLogAppenderConfig
    // *************************************************************************

    protected VanillaLogAppenderConfig getChronicleConfig() {
        return this.config;
    }

    public void setDataCacheCapacity(int dataCacheCapacity) {
        config.setDataCacheCapacity(dataCacheCapacity);
    }

    public void setCycleLength(int cycleLength) {
        config.setCycleLength(cycleLength);
    }

    public void setCleanupOnClose(boolean cleanupOnClose) {
        config.setCleanupOnClose(cleanupOnClose);
    }

    public void setSynchronous(boolean synchronous) {
        config.setSynchronous(synchronous);
    }

    public void setDefaultMessageSize(int defaultMessageSize) {
        config.setDefaultMessageSize(defaultMessageSize);
    }

    public void setUseCheckedExcerpt(boolean useCheckedExcerpt) {
        config.setUseCheckedExcerpt(useCheckedExcerpt);
    }

    public void setEntriesPerCycle(long entriesPerCycle) {
        config.setEntriesPerCycle(entriesPerCycle);
    }

    public void setCycleLength(int cycleLength, boolean check) {
        config.setCycleLength(cycleLength, check);
    }

    public void setIndexCacheCapacity(int indexCacheCapacity) {
        config.setIndexCacheCapacity(indexCacheCapacity);
    }

    public void setIndexBlockSize(long indexBlockSize) {
        config.setIndexBlockSize(indexBlockSize);
    }
}
