/*
 *  Copyright 2014-2025 chronicle.software
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.openhft.chronicle.logger.log4j1;

import net.openhft.chronicle.logger.ChronicleLogWriter;
import net.openhft.chronicle.logger.DefaultChronicleLogWriter;
import net.openhft.chronicle.logger.LogAppenderConfig;

import java.io.IOException;

/**
 * Log4j 1.x appender that writes events to a Chronicle Queue.
 * <p>
 * Configuration is delegated to {@link LogAppenderConfig}.  The setter
 * methods on this class forward any options to that configuration object
 * before the queue is created.  When {@link #activateOptions()} is called the
 * queue is built using the values supplied via these setters.
 */
public final class ChronicleAppender extends AbstractChronicleAppender {

    private final LogAppenderConfig config;

    public ChronicleAppender() {
        this.config = new LogAppenderConfig();
    }

    // *************************************************************************
    // Custom logging options
    // *************************************************************************

    /**
     * Set the Chronicle Queue block size in bytes.
     * Call this before {@link #activateOptions()}.
     *
     * @param blockSize size of each block in bytes
     */
    public void setBlockSize(int blockSize) {
        config.setBlockSize(blockSize);
    }

    /**
     * Configure the internal ring buffer size.
     *
     * @param bufferCapacity capacity in bytes
     */
    public void setBufferCapacity(int bufferCapacity) {
        config.setBufferCapacity(bufferCapacity);
    }

    public String rollCycle() {
        return config.getRollCycle();
    }

    /**
     * Select the roll cycle to apply when building the queue.
     * The value must match an entry in {@code RollCycles}.
     *
     * @param rollCycle roll cycle name
     */
    public void rollCycle(String rollCycle) {
        config.setRollCycle(rollCycle);
    }

    @Override
    protected ChronicleLogWriter createWriter() throws IOException {
        return new DefaultChronicleLogWriter(this.config.build(this.getPath(), this.getWireType()));
    }

    // *************************************************************************
    // LogAppenderConfig
    // *************************************************************************

    LogAppenderConfig getChronicleConfig() {
        return this.config;
    }
}
