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
package net.openhft.chronicle.logger.tools;

import net.openhft.chronicle.logger.ChronicleLogLevel;
import net.openhft.chronicle.logger.ChronicleLogWriter;
import org.jetbrains.annotations.Nullable;

/**
 * Callback used by {@link ChronicleLogReader} to consume log entries.
 * Implementations may print, store or filter the supplied data.
 */
public interface ChronicleLogProcessor {

    /**
     * Handle one log event produced by a {@link ChronicleLogWriter}.
     *
     * @param timestamp  epoch time in milliseconds
     * @param level      severity of the event
     * @param loggerName name of the logger that created the entry
     * @param threadName name of the thread that wrote the entry
     * @param message    message pattern, may contain "{}" placeholders
     * @param throwable  optional stack trace, may be {@code null}
     * @param args       argument values referenced by the message pattern
     */
    void process(
            final long timestamp,
            final ChronicleLogLevel level,
            final String loggerName,
            final String threadName,
            final String message,
            @Nullable final Throwable throwable,
            final Object[] args);
}
