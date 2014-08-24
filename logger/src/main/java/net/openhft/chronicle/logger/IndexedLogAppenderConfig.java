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

    public void indexFileCapacity(int indexFileCapacity) {
        config.indexFileCapacity(indexFileCapacity);
    }

    public int indexFileCapacity() {
        return config.indexFileCapacity();
    }

    public int dataBlockSize() {
        return config.dataBlockSize();
    }

    public void useUnsafe(boolean useUnsafe) {
        config.useUnsafe(useUnsafe);
    }

    public int cacheLineSize() {
        return config.cacheLineSize();
    }

    public void byteOrder(ByteOrder byteOrder) {
        config.byteOrder(byteOrder);
    }

    public void indexBlockSize(int indexBlockSize) {
        config.indexBlockSize(indexBlockSize);
    }

    public void synchronousMode(boolean synchronousMode) {
        config.synchronousMode(synchronousMode);
    }

    public void cacheLineSize(int cacheLineSize) {
        config.cacheLineSize(cacheLineSize);
    }

    public void messageCapacity(int messageCapacity) {
        config.messageCapacity(messageCapacity);
    }

    public void minimiseFootprint(boolean minimiseFootprint) {
        config.minimiseFootprint(minimiseFootprint);
    }

    public int indexFileExcerpts() {
        return config.indexFileExcerpts();
    }

    public int indexBlockSize() {
        return config.indexBlockSize();
    }

    public boolean minimiseFootprint() {
        return config.minimiseFootprint();
    }

    public void useCheckedExcerpt(boolean useCheckedExcerpt) {
        config.useCheckedExcerpt(useCheckedExcerpt);
    }

    public boolean synchronousMode() {
        return config.synchronousMode();
    }

    public int messageCapacity() {
        return config.messageCapacity();
    }

    public boolean useUnsafe() {
        return config.useUnsafe();
    }

    public void dataBlockSize(int dataBlockSize) {
        config.dataBlockSize(dataBlockSize);
    }

    public boolean useCheckedExcerpt() {
        return config.useCheckedExcerpt();
    }

    public void indexFileExcerpts(int indexFileExcerpts) {
        config.indexFileExcerpts(indexFileExcerpts);
    }

    public ByteOrder byteOrder() {
        return config.byteOrder();
    }
}
