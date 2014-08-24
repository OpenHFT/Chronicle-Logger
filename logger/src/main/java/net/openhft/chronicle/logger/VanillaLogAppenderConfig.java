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

    public long dataBlockSize() {
        return config.dataBlockSize();
    }

    public int cycleLength() {
        return config.cycleLength();
    }

    public VanillaChronicleConfig dataBlockSize(int dataBlockSize) {
        return config.dataBlockSize(dataBlockSize);
    }

    public int dataCacheCapacity() {
        return config.dataCacheCapacity();
    }

    public String cycleFormat() {
        return config.cycleFormat();
    }

    public VanillaChronicleConfig dataCacheCapacity(int dataCacheCapacity) {
        return config.dataCacheCapacity(dataCacheCapacity);
    }

    public boolean cleanupOnClose() {
        return config.cleanupOnClose();
    }

    public boolean synchronous() {
        return config.synchronous();
    }

    public long entriesPerCycle() {
        return config.entriesPerCycle();
    }

    public int defaultMessageSize() {
        return config.defaultMessageSize();
    }

    public VanillaChronicleConfig cycleLength(int cycleLength) {
        return config.cycleLength(cycleLength);
    }

    public VanillaChronicleConfig cleanupOnClose(boolean cleanupOnClose) {
        return config.cleanupOnClose(cleanupOnClose);
    }

    public VanillaChronicleConfig synchronous(boolean synchronous) {
        return config.synchronous(synchronous);
    }

    public long indexBlockSize() {
        return config.indexBlockSize();
    }

    public VanillaChronicleConfig defaultMessageSize(int defaultMessageSize) {
        return config.defaultMessageSize(defaultMessageSize);
    }

    public VanillaChronicleConfig useCheckedExcerpt(boolean useCheckedExcerpt) {
        return config.useCheckedExcerpt(useCheckedExcerpt);
    }

    public int indexCacheCapacity() {
        return config.indexCacheCapacity();
    }

    public VanillaChronicleConfig entriesPerCycle(long entriesPerCycle) {
        return config.entriesPerCycle(entriesPerCycle);
    }

    public VanillaChronicleConfig cycleLength(int cycleLength, boolean check) {
        return config.cycleLength(cycleLength, check);
    }

    public VanillaChronicleConfig indexCacheCapacity(int indexCacheCapacity) {
        return config.indexCacheCapacity(indexCacheCapacity);
    }

    public boolean useCheckedExcerpt() {
        return config.useCheckedExcerpt();
    }

    public VanillaChronicleConfig indexBlockSize(long indexBlockSize) {
        return config.indexBlockSize(indexBlockSize);
    }
}
