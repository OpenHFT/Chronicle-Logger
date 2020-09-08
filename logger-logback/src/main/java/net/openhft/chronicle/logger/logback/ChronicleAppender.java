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
import ch.qos.logback.classic.spi.ThrowableProxy;
import ch.qos.logback.core.encoder.Encoder;
import ch.qos.logback.core.joran.spi.DefaultClass;
import net.openhft.chronicle.bytes.BytesStore;
import net.openhft.chronicle.logger.ChronicleLogWriter;
import net.openhft.chronicle.logger.DefaultChronicleLogWriter;
import net.openhft.chronicle.logger.LogAppenderConfig;

import java.io.IOException;
import java.time.Instant;

public class ChronicleAppender extends AbstractChronicleAppender {

    protected LogAppenderConfig config;
    protected Encoder<ILoggingEvent> encoder;

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
        return new DefaultChronicleLogWriter(this.config.build(this.getPath(), getWireType()));
    }

    public Encoder<ILoggingEvent> getEncoder() {
        return this.encoder;
    }

    public void setEncoder(Encoder<ILoggingEvent> encoder) {
        this.encoder = encoder;
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
        byte[] encode = encoder.encode(event);
        writer.write(
                Instant.ofEpochMilli(event.getTimeStamp()),
                event.getLevel().toInt(),
                event.getThreadName(),
                event.getLoggerName(),
                (encode != null) ? BytesStore.wrap(encode) : BytesStore.empty()
        );
    }
}
