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

import net.openhft.chronicle.ChronicleConfig;

import java.nio.ByteOrder;

public class IndexedLogAppenderConfig {

    private final ChronicleConfig config;
    private int capacity;

    public IndexedLogAppenderConfig() {
        this(ChronicleConfig.DEFAULT);
    }

    public IndexedLogAppenderConfig(final ChronicleConfig config) {
        this.config = config.clone();
    }

    public ChronicleConfig config() {
        return this.config;
        }

        public void setIndexFileCapacity(int indexFileCapacity) {
            config.indexFileCapacity(indexFileCapacity);
        }

        public int getIndexFileCapacity() {
            return config.indexFileCapacity();
        }

    public int getDataBlockSize() {
        return config.dataBlockSize();
    }

    public void setUseUnsafe(boolean useUnsafe) {
        config.useUnsafe(useUnsafe);
    }

    public int getCacheLineSize() {
        return config.cacheLineSize();
    }

    public void setByteOrder(ByteOrder byteOrder) {
        config.byteOrder(byteOrder);
    }

    public void setIndexBlockSize(int indexBlockSize) {
        config.indexBlockSize(indexBlockSize);
    }

    public void setSynchronousMode(boolean synchronousMode) {
        config.synchronousMode(synchronousMode);
    }

    public void setCacheLineSize(int cacheLineSize) {
        config.cacheLineSize(cacheLineSize);
    }

    public void setMessageCapacity(int messageCapacity) {
        config.messageCapacity(messageCapacity);
    }

    public void seMinimiseFootprint(boolean minimiseFootprint) {
        config.minimiseFootprint(minimiseFootprint);
    }

    public int getIndexFileExcerpts() {
        return config.indexFileExcerpts();
    }

    public int getIndexBlockSize() {
        return config.indexBlockSize();
    }

    public boolean getMinimiseFootprint() {
        return config.minimiseFootprint();
    }

    public void setUseCheckedExcerpt(boolean useCheckedExcerpt) {
        config.useCheckedExcerpt(useCheckedExcerpt);
    }

    public boolean getSynchronousMode() {
        return config.synchronousMode();
    }

    public int getMessageCapacity() {
        return config.messageCapacity();
    }

    public boolean getUseUnsafe() {
        return config.useUnsafe();
    }

    public void setDataBlockSize(int dataBlockSize) {
        config.dataBlockSize(dataBlockSize);
    }

    public boolean getUseCheckedExcerpt() {
        return config.useCheckedExcerpt();
    }

    public void setIndexFileExcerpts(int indexFileExcerpts) {
        config.indexFileExcerpts(indexFileExcerpts);
    }

    public ByteOrder getByteOrder() {
        return config.byteOrder();
    }
}
