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

public class IndexedLogAppenderConfig extends ChronicleLogAppenderConfig {

    private static final String[] KEYS = new String[] {
        "synchronous",
        "useCheckedExcerpt",
        "cacheLineSize",
        "cacheLineSize",
        "dataBlockSize",
        "messageCapacity",
        "indexBlockSize"
    };

    private final ChronicleQueueBuilder.IndexedChronicleQueueBuilder builder;

    public IndexedLogAppenderConfig() {
        this.builder = ChronicleQueueBuilder.indexed((File)null);
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

    public int getCacheLineSize() {
        return this.builder.cacheLineSize();
    }

    public void setCacheLineSize(int cacheLineSize) {
        this.builder.cacheLineSize(cacheLineSize);
    }

    public int getDataBlockSize() {
        return this.builder.dataBlockSize();
    }

    public void setDataBlockSize(int dataBlockSize) {
        this.builder.dataBlockSize(dataBlockSize);
    }

    public int getMessageCapacity() {
        return this.builder.messageCapacity();
    }

    public void setMessageCapacity(int messageCapacity) {
        this.builder.messageCapacity(messageCapacity);
    }

    public int getIndexBlockSize() {
        return this.builder.indexBlockSize();
    }

    public void setIndexBlockSize(int indexBlockSize) {
        this.builder.indexBlockSize(indexBlockSize);
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
        return ChronicleQueueBuilder.indexed(path)
            .synchronous(this.builder.synchronous())
            .useCheckedExcerpt(this.builder.useCheckedExcerpt())
            .cacheLineSize(this.builder.cacheLineSize())
            .dataBlockSize(this.builder.dataBlockSize())
            .messageCapacity(this.builder.messageCapacity())
            .indexBlockSize(this.builder.indexBlockSize())
            .build();
    }
}
