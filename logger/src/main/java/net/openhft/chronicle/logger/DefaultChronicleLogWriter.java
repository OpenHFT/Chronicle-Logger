/*
 * Copyright 2014 Higher Frequency Trading
 *
 * http://www.higherfrequencytrading.com
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

import net.openhft.chronicle.queue.ChronicleQueue;
import net.openhft.chronicle.wire.DocumentContext;
import net.openhft.chronicle.wire.ValueWriter;
import net.openhft.chronicle.wire.Wire;
import net.openhft.lang.model.constraints.NotNull;

import java.io.Closeable;
import java.io.IOException;
import java.util.Arrays;

public class DefaultChronicleLogWriter implements ChronicleLogWriter {

    private final ChronicleQueue cq;

    public DefaultChronicleLogWriter(@NotNull ChronicleQueue cq) throws IOException {
        this.cq = cq;
    }

    @Override
    public void close() throws IOException {
        cq.close();
    }

    @Override
    public void write(
            final ChronicleLogLevel level,
            final long timestamp,
            final String threadName,
            final String loggerName,
            final String message) {
        write(level, timestamp, threadName, loggerName, message, null);
    }

    @Override
    public void write(
            final ChronicleLogLevel level,
            final long timestamp,
            final String threadName,
            final String loggerName,
            final String message,
            final Throwable throwable,
            final Object... args) {
        try (final DocumentContext dc = cq.acquireAppender().writingDocument()) {
            Wire wire = dc.wire();
            assert wire != null;
            wire
                    .write("ts").int64(timestamp)
                    .write("level").asEnum(level)
                    .write("threadName").text(threadName)
                    .write("loggerName").text(loggerName)
                    .write("message").text(message);

            if (throwable != null) {
                wire.write("throwable").throwable(throwable);
            }

            if (args != null && args.length > 0) {
                wire.write("args").sequence(vo -> {
                    for (Object o : args)
                        vo.object(o);
                });
            }
        }
    }
}
