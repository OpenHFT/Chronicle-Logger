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

package net.openhft.chronicle.logger;

import net.openhft.chronicle.VanillaChronicleConfig;

public class VanillaLogAppenderConfig {

    private final VanillaChronicleConfig config;

    public VanillaLogAppenderConfig() {
        this(VanillaChronicleConfig.DEFAULT);
    }

    public VanillaLogAppenderConfig(final VanillaChronicleConfig config) {
        this.config = config.clone();
    }

    public VanillaChronicleConfig config() {
        return this.config;
    }

    public VanillaChronicleConfig cycleFormat(String cycleFormat) {
        return config.cycleFormat(cycleFormat);
    }

    public long getDataBlockSize() {
        return config.dataBlockSize();
    }

    public int getCycleLength() {
        return config.cycleLength();
    }

    public void getDataBlockSize(int dataBlockSize) {
        config.dataBlockSize(dataBlockSize);
    }

    public int getDataCacheCapacity() {
        return config.dataCacheCapacity();
    }

    public String getCycleFormat() {
        return config.cycleFormat();
    }

    public void setDataCacheCapacity(int dataCacheCapacity) {
        config.dataCacheCapacity(dataCacheCapacity);
    }

    public boolean getCleanupOnClose() {
        return config.cleanupOnClose();
    }

    public boolean getSynchronous() {
        return config.synchronous();
    }

    public long getEntriesPerCycle() {
        return config.entriesPerCycle();
    }

    public int getDefaultMessageSize() {
        return config.defaultMessageSize();
    }

    public void setCycleLength(int cycleLength) {
        config.cycleLength(cycleLength);
    }

    public void setCleanupOnClose(boolean cleanupOnClose) {
        config.cleanupOnClose(cleanupOnClose);
    }

    public void setSynchronous(boolean synchronous) {
        config.synchronous(synchronous);
    }

    public long getIndexBlockSize() {
        return config.indexBlockSize();
    }

    public void setDefaultMessageSize(int defaultMessageSize) {
        config.defaultMessageSize(defaultMessageSize);
    }

    public void setUseCheckedExcerpt(boolean useCheckedExcerpt) {
        config.useCheckedExcerpt(useCheckedExcerpt);
    }

    public int getIndexCacheCapacity() {
        return config.indexCacheCapacity();
    }

    public void setEntriesPerCycle(long entriesPerCycle) {
        config.entriesPerCycle(entriesPerCycle);
    }

    public void setCycleLength(int cycleLength, boolean check) {
        config.cycleLength(cycleLength, check);
    }

    public void setIndexCacheCapacity(int indexCacheCapacity) {
        config.indexCacheCapacity(indexCacheCapacity);
    }

    public boolean getUseCheckedExcerpt() {
        return config.useCheckedExcerpt();
    }

    public void setIndexBlockSize(long indexBlockSize) {
        config.indexBlockSize(indexBlockSize);
    }
}
