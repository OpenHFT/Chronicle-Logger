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

package net.openhft.chronicle.logger.log4j1;

import net.openhft.chronicle.Chronicle;
import net.openhft.chronicle.ExcerptAppender;
import net.openhft.chronicle.VanillaChronicle;
import net.openhft.chronicle.logger.VanillaLogAppenderConfig;

import java.io.IOException;

public class TextVanillaChronicleAppender extends TextChronicleAppender {

    private VanillaLogAppenderConfig config;

    public TextVanillaChronicleAppender() {
        this.config = new VanillaLogAppenderConfig();
    }

    @Override
    protected Chronicle createChronicle() throws IOException {
        return new VanillaChronicle(this.getPath(), this.config.cfg());
    }

    @Override
    protected ExcerptAppender getAppender() {
        try {
            return this.chronicle.createAppender();
        } catch(IOException e) {
            e.printStackTrace();
        }

        return null;
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
