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
package net.openhft.chronicle.logger;

import net.openhft.chronicle.VanillaChronicleConfig;

public class VanillaLogAppenderConfig extends ChronicleLogConfig {

    private final VanillaChronicleConfig chronicleConfig;

    public VanillaLogAppenderConfig() {
        this(VanillaChronicleConfig.DEFAULT);
    }

    public VanillaLogAppenderConfig(final VanillaChronicleConfig config) {
        this.chronicleConfig = config.clone();
    }

    public VanillaChronicleConfig cfg() {
        return this.chronicleConfig;
    }

    public VanillaChronicleConfig cycleFormat(String cycleFormat) {
        return chronicleConfig.cycleFormat(cycleFormat);
    }

    public long getDataBlockSize() {
        return chronicleConfig.dataBlockSize();
    }

    public int getCycleLength() {
        return chronicleConfig.cycleLength();
    }

    public void getDataBlockSize(int dataBlockSize) {
        chronicleConfig.dataBlockSize(dataBlockSize);
    }

    public int getDataCacheCapacity() {
        return chronicleConfig.dataCacheCapacity();
    }

    public String getCycleFormat() {
        return chronicleConfig.cycleFormat();
    }

    public void setDataCacheCapacity(int dataCacheCapacity) {
        chronicleConfig.dataCacheCapacity(dataCacheCapacity);
    }

    public boolean getCleanupOnClose() {
        return chronicleConfig.cleanupOnClose();
    }

    public boolean getSynchronous() {
        return chronicleConfig.synchronous();
    }

    public long getEntriesPerCycle() {
        return chronicleConfig.entriesPerCycle();
    }

    public int getDefaultMessageSize() {
        return chronicleConfig.defaultMessageSize();
    }

    public void setCycleLength(int cycleLength) {
        chronicleConfig.cycleLength(cycleLength);
    }

    public void setCleanupOnClose(boolean cleanupOnClose) {
        chronicleConfig.cleanupOnClose(cleanupOnClose);
    }

    public void setSynchronous(boolean synchronous) {
        chronicleConfig.synchronous(synchronous);
    }

    public long getIndexBlockSize() {
        return chronicleConfig.indexBlockSize();
    }

    public void setDefaultMessageSize(int defaultMessageSize) {
        chronicleConfig.defaultMessageSize(defaultMessageSize);
    }

    public void setUseCheckedExcerpt(boolean useCheckedExcerpt) {
        chronicleConfig.useCheckedExcerpt(useCheckedExcerpt);
    }

    public int getIndexCacheCapacity() {
        return chronicleConfig.indexCacheCapacity();
    }

    public void setEntriesPerCycle(long entriesPerCycle) {
        chronicleConfig.entriesPerCycle(entriesPerCycle);
    }

    public void setCycleLength(int cycleLength, boolean check) {
        chronicleConfig.cycleLength(cycleLength, check);
    }

    public void setIndexCacheCapacity(int indexCacheCapacity) {
        chronicleConfig.indexCacheCapacity(indexCacheCapacity);
    }

    public boolean getUseCheckedExcerpt() {
        return chronicleConfig.useCheckedExcerpt();
    }

    public void setIndexBlockSize(long indexBlockSize) {
        chronicleConfig.indexBlockSize(indexBlockSize);
    }
}
