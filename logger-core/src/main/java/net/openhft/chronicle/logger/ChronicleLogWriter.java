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
package net.openhft.chronicle.logger;

import java.io.Closeable;
import java.time.Instant;

/**
 * Writes a logging event out to a chronicle log.
 *
 * XXX Use epochseconds and nanos here
 */
public interface ChronicleLogWriter extends Closeable {

    /**
     * Writes a logging event to the store.
     *
     * @param level the integer level of the log event.
     * @param epochSecond event timestamp.
     * @param nanos event timestamp.
     * @param loggerName logger that caused this event.
     * @param threadName thread name of the event.
     * @param entry the bytes containing the event.
     */
    void write(
            final long epochSecond,
            final int nanos,
            final int level,
            final String loggerName,
            final String threadName,
            final byte[] entry);

    /**
     * Writes a logging event to the store.
     *
     * @param level the integer level of the log event.
     * @param epochSecond event timestamp.
     * @param nanos event timestamp.
     * @param threadName thread name of the event.
     * @param loggerName logger that caused this event.
     * @param entry the bytes containing the encoded event.
     * @param contentType the content type of the entry.
     * @param contentEncoding the encoding (compression, usually) of the entry.
     */
    void write(
            final long epochSecond,
            final int nanos,
            final int level,
            final String loggerName,
            final String threadName,
            final byte[] entry,
            final String contentType,
            final String contentEncoding);
}
