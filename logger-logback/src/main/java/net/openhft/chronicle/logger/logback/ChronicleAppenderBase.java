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
package net.openhft.chronicle.logger.logback;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.UnsynchronizedAppenderBase;
import ch.qos.logback.core.encoder.Encoder;
import ch.qos.logback.core.joran.spi.DefaultClass;
import net.openhft.chronicle.logger.ChronicleLogWriter;
import net.openhft.chronicle.logger.DefaultChronicleLogWriter;
import net.openhft.chronicle.logger.LogAppenderConfig;
import net.openhft.chronicle.logger.codec.Codec;
import net.openhft.chronicle.logger.codec.CodecRegistry;
import net.openhft.chronicle.queue.ChronicleQueue;

import java.io.IOException;
import java.nio.file.Paths;

public abstract class ChronicleAppenderBase
        extends UnsynchronizedAppenderBase<ILoggingEvent>
        implements Appender<ILoggingEvent> {

    protected ChronicleLogWriter writer;
    private String path;

    protected LogAppenderConfig config;
    protected Encoder<ILoggingEvent> encoder;
    protected Codec codec;

    protected ChronicleAppenderBase() {
        this.config = new LogAppenderConfig();
        this.path = null;
        this.writer = null;
    }

    public LogAppenderConfig getChronicleConfig() {
        return this.config;
    }

    @DefaultClass(value = LogAppenderConfig.class)
    public void setChronicleConfig(final LogAppenderConfig config) {
        this.config = config;
    }

    public Encoder<ILoggingEvent> getEncoder() {
        return this.encoder;
    }

    public void setEncoder(Encoder<ILoggingEvent> encoder) {
        this.encoder = encoder;
    }

    public String getPath() {
        return this.path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    @Override
    public void start() {
        if (encoder == null) {
            addError("Null encoder!");
        }
        if (getPath() == null) {
            addError("Appender " + getName() + " has configuration errors and is not started!");
        } else {
            try {
                this.writer = createWriter();
                this.codec = createCodecRegistry().find(config.contentEncoding);

                LogAppenderConfig.write(config, Paths.get(this.getPath()));
                this.started = true;
            } catch (Exception e) {
                this.writer = null;
                this.codec = null;
                addError("Appender " + getName() + " " + e.getMessage(), e);
            }
        }
    }

    @Override
    public void stop() {
        if (this.writer != null) {
            try {
                this.writer.close();
            } catch (IOException e) {
                addError("Appender " + getName() + " " + e.getMessage());
            }
        }

        this.started = false;
    }

    protected CodecRegistry createCodecRegistry() {
        return CodecRegistry.builder().withDefaults(this.getPath()).build();
    }

    protected ChronicleLogWriter createWriter() throws IOException {
        ChronicleQueue cq = this.config.build(this.getPath());
        return new DefaultChronicleLogWriter(cq);
    }
}
