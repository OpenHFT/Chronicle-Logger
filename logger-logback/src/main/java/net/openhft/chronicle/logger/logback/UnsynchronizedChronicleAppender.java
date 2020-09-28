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
import net.openhft.chronicle.logger.entry.EntryHelpers;

/**
 * An unsynchronized chronicle appender.
 *
 * This appender is intended to be used behind
 * async appender thread, or LoggingEventAsyncDisruptorAppender if you're using
 * logstash-logback-encoder.
 */
public class UnsynchronizedChronicleAppender extends ChronicleAppenderBase {

    @Override
    public void append(final ILoggingEvent event) {
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
