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

public class IndexedLogAppenderConfig extends ChronicleLogConfig {

    private final ChronicleConfig chronicleConfig;

    public IndexedLogAppenderConfig() {
        this(ChronicleConfig.DEFAULT);
    }

    public IndexedLogAppenderConfig(final ChronicleConfig config) {
        this.chronicleConfig = config.clone();
    }

    public ChronicleConfig cfg() {
        return this.chronicleConfig;
    }

    public void setIndexFileCapacity(int indexFileCapacity) {
        chronicleConfig.indexFileCapacity(indexFileCapacity);
    }

    public int getIndexFileCapacity() {
        return chronicleConfig.indexFileCapacity();
    }

    public int getDataBlockSize() {
        return chronicleConfig.dataBlockSize();
    }

    public void setUseUnsafe(boolean useUnsafe) {
        chronicleConfig.useUnsafe(useUnsafe);
    }

    public int getCacheLineSize() {
        return chronicleConfig.cacheLineSize();
    }

    public void setByteOrder(ByteOrder byteOrder) {
        chronicleConfig.byteOrder(byteOrder);
    }

    public void setIndexBlockSize(int indexBlockSize) {
        chronicleConfig.indexBlockSize(indexBlockSize);
    }

    public void setSynchronousMode(boolean synchronousMode) {
        chronicleConfig.synchronousMode(synchronousMode);
    }

    public void setCacheLineSize(int cacheLineSize) {
        chronicleConfig.cacheLineSize(cacheLineSize);
    }

    public void setMessageCapacity(int messageCapacity) {
        chronicleConfig.messageCapacity(messageCapacity);
    }

    public void setMinimiseFootprint(boolean minimiseFootprint) {
        chronicleConfig.minimiseFootprint(minimiseFootprint);
    }

    public int getIndexFileExcerpts() {
        return chronicleConfig.indexFileExcerpts();
    }

    public int getIndexBlockSize() {
        return chronicleConfig.indexBlockSize();
    }

    public boolean getMinimiseFootprint() {
        return chronicleConfig.minimiseFootprint();
    }

    public void setUseCheckedExcerpt(boolean useCheckedExcerpt) {
        chronicleConfig.useCheckedExcerpt(useCheckedExcerpt);
    }

    public boolean getSynchronousMode() {
        return chronicleConfig.synchronousMode();
    }

    public int getMessageCapacity() {
        return chronicleConfig.messageCapacity();
    }

    public boolean getUseUnsafe() {
        return chronicleConfig.useUnsafe();
    }

    public void setDataBlockSize(int dataBlockSize) {
        chronicleConfig.dataBlockSize(dataBlockSize);
    }

    public boolean getUseCheckedExcerpt() {
        return chronicleConfig.useCheckedExcerpt();
    }

    public void setIndexFileExcerpts(int indexFileExcerpts) {
        chronicleConfig.indexFileExcerpts(indexFileExcerpts);
    }

    public ByteOrder getByteOrder() {
        return chronicleConfig.byteOrder();
    }
}
