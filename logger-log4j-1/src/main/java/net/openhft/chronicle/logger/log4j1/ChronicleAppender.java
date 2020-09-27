/*
 * Copyright 2014-2017 Chronicle Software
 *
 * http://www.chronicle.software
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
package net.openhft.chronicle.logger.log4j1;

import net.openhft.chronicle.logger.ChronicleLogWriter;
import net.openhft.chronicle.logger.DefaultChronicleLogWriter;
import net.openhft.chronicle.logger.LogAppenderConfig;
import net.openhft.chronicle.logger.codec.CodecRegistry;
import net.openhft.chronicle.queue.ChronicleQueue;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public final class ChronicleAppender extends AbstractChronicleAppender {

    private final LogAppenderConfig config;

    public ChronicleAppender() {
        this.config = new LogAppenderConfig();
    }

    // *************************************************************************
    // Custom logging options
    // *************************************************************************

    public void setBlockSize(int blockSize) {
        config.setBlockSize(blockSize);
    }

    public void setBufferCapacity(int bufferCapacity) {
        config.setBufferCapacity(bufferCapacity);
    }

    public String rollCycle() {
        return config.getRollCycle();
    }

    public void rollCycle(String rollCycle) {
        config.setRollCycle(rollCycle);
    }

    @Override
    protected ChronicleLogWriter createWriter() throws IOException {
        ChronicleQueue cq = this.config.build(this.getPath(), this.getWireType());
        Path parent = Paths.get(cq.fileAbsolutePath()).getParent();
        CodecRegistry registry = CodecRegistry.builder().withDefaults(parent).build();
        return new DefaultChronicleLogWriter(registry, cq);
    }

    // *************************************************************************
    // LogAppenderConfig
    // *************************************************************************

    LogAppenderConfig getChronicleConfig() {
        return this.config;
    }
}
