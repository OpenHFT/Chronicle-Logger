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

import net.openhft.chronicle.Chronicle;
import net.openhft.chronicle.ChronicleQueueBuilder;

import java.io.File;
import java.io.IOException;

public class VanillaLogAppenderConfig extends ChronicleLogAppenderConfig {
    private static final String[] KEYS = new String[] {
        //TODO
        /*
        private boolean synchronous;
        private boolean useCheckedExcerpt;
        private String cycleFormat;
        private int cycleLength;
        private int defaultMessageSize;
        private int dataCacheCapacity;
        private int indexCacheCapacity;
        private long indexBlockSize;
        private long dataBlockSize;
        private long entriesPerCycle;
        private boolean cleanupOnClose;
        */
    };

    private final ChronicleQueueBuilder.VanillaChronicleQueueBuilder builder;

    public VanillaLogAppenderConfig() {
        this.builder = ChronicleQueueBuilder.vanilla((File)null);
    }

    // *************************************************************************
    //
    // *************************************************************************

    public boolean isSynchronous() {
        return this.builder.synchronous();
    }

    public void setSynchronous(boolean synchronous) {
        this.builder.synchronous(synchronous);
    }

    public boolean isUseCheckedExcerpt() {
        return this.builder.useCheckedExcerpt();
    }

    public void setUseCheckedExcerpt(boolean useCheckedExcerpt) {
        this.builder.useCheckedExcerpt(useCheckedExcerpt);
    }

    public String getCycleFormat() {
        return this.builder.cycleFormat();
    }

    public void setCycleFormat(String cycleFormat) {
        this.builder.cycleFormat(cycleFormat);
    }

    public int getCycleLength() {
        return this.builder.cycleLength();
    }

    public void setCycleLength(int cycleLength) {
        this.builder.cycleLength(cycleLength);
    }

    public void setCycleLength(int cycleLength, boolean check) {
        this.builder.cycleLength(cycleLength, check);
    }

    public int getDefaultMessageSize() {
        return this.builder.defaultMessageSize();
    }

    public void setDefaultMessageSize(int defaultMessageSize) {
        this.builder.defaultMessageSize(defaultMessageSize);
    }

    public int getDataCacheCapacity() {
        return this.builder.dataCacheCapacity();
    }

    public void setDataCacheCapacity(int dataCacheCapacity) {
        this.builder.dataCacheCapacity(dataCacheCapacity);
    }

    public int getIndexCacheCapacity() {
        return this.builder.indexCacheCapacity();
    }

    public void setIndexCacheCapacity(int indexCacheCapacity) {
        this.builder.indexCacheCapacity(indexCacheCapacity);
    }

    public long getIndexBlockSize() {
        return this.builder.indexBlockSize();
    }

    public void setIndexBlockSize(long indexBlockSize) {
        this.builder.indexBlockSize(indexBlockSize);
    }

    public long getDataBlockSize() {
        return this.builder.dataBlockSize();
    }

    public void setDataBlockSize(long dataBlockSize) {
        this.builder.dataBlockSize(dataBlockSize);
    }

    public long getEntriesPerCycle() {
        return this.builder.entriesPerCycle();
    }

    public void setEntriesPerCycle(long entriesPerCycle) {
        this.builder.entriesPerCycle(entriesPerCycle);
    }

    public boolean isCleanupOnClose() {
        return this.builder.cleanupOnClose();
    }

    public void setCleanupOnClose(boolean cleanupOnClose) {
        this.builder.cleanupOnClose(cleanupOnClose);
    }

    // *************************************************************************
    //
    // *************************************************************************

    @Override
    public String[] keys() {
        return KEYS;
    }

    @Override
    public Chronicle build(String path) throws IOException {
        return ChronicleQueueBuilder.vanilla(path)
            .synchronous(this.builder.synchronous())
            .useCheckedExcerpt(this.builder.useCheckedExcerpt())
            .cycleFormat(this.builder.cycleFormat())
            .cycleLength(this.builder.cycleLength())
            .defaultMessageSize(this.builder.defaultMessageSize())
            .dataCacheCapacity(this.builder.dataCacheCapacity())
            .indexCacheCapacity(this.builder.indexCacheCapacity())
            .indexBlockSize(this.builder.indexBlockSize())
            .dataBlockSize((int) this.builder.dataBlockSize())
            .entriesPerCycle(this.builder.entriesPerCycle())
            .cleanupOnClose(this.builder.cleanupOnClose())
            .build();
    }
}
