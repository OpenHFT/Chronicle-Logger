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
import ch.qos.logback.core.encoder.Encoder;
import ch.qos.logback.core.joran.spi.DefaultClass;
import net.openhft.chronicle.logger.ChronicleLogWriter;
import net.openhft.chronicle.logger.DefaultChronicleLogWriter;
import net.openhft.chronicle.logger.LogAppenderConfig;
import net.openhft.chronicle.logger.codec.CodecRegistry;
import net.openhft.chronicle.logger.entry.EntryHelpers;
import net.openhft.chronicle.queue.ChronicleQueue;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * XXX This appender is not thread-safe
 *
 * XXX Need to make version extending AppenderBase or wrap in an async-appender
 * and error if not wrapped in another appender
 */
public class ChronicleAppender extends AbstractChronicleAppender {

    protected LogAppenderConfig config;
    protected Encoder<ILoggingEvent> encoder;
    private String contentType = null;
    private String contentEncoding = null;

    public ChronicleAppender() {
        super();
        this.config = new LogAppenderConfig();
    }

    public LogAppenderConfig getChronicleConfig() {
        return this.config;
    }

    @DefaultClass(value = LogAppenderConfig.class)
    public void setChronicleConfig(final LogAppenderConfig config) {
        this.config = config;
    }

    @Override
    protected ChronicleLogWriter createWriter() throws IOException {
        ChronicleQueue cq = this.config.build(this.getPath(), getWireType());
        Path parent = Paths.get(cq.fileAbsolutePath()).getParent();
        CodecRegistry registry = CodecRegistry.builder().withDefaults(parent).build();
        return new DefaultChronicleLogWriter(registry, cq);
    }

    public Encoder<ILoggingEvent> getEncoder() {
        return this.encoder;
    }

    public void setEncoder(Encoder<ILoggingEvent> encoder) {
        this.encoder = encoder;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public void setContentEncoding(String contentEncoding) {
        this.contentEncoding = contentEncoding;
    }

    @Override
    public void start() {
        if (encoder == null) {
            addError("Null encoder!");
        } else {
            super.start();
        }
    }

    // *************************************************************************
    //
    // *************************************************************************

    @Override
    public void doAppend(final ILoggingEvent event, final ChronicleLogWriter writer) {
        byte[] entry = encoder.encode(event);
        long epochMillis = event.getTimeStamp();
        EntryHelpers helpers = EntryHelpers.instance();
        long second = helpers.epochSecondFromMillis(epochMillis);
        int nanos = helpers.nanosFromMillis(epochMillis);
        writer.write(
                second,
                nanos,
                event.getLevel().toInt(),
                event.getLoggerName(),
                event.getThreadName(),
                entry,
                contentType,
                contentEncoding
        );
    }

}
