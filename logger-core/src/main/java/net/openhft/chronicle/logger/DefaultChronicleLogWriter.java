/*
 * Copyright 2014-2020 chronicle.software
 *
 *       https://chronicle.software
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
package net.openhft.chronicle.logger;

import net.openhft.chronicle.queue.ChronicleQueue;
import net.openhft.chronicle.wire.DocumentContext;
import net.openhft.chronicle.wire.Wire;
import net.openhft.chronicle.wire.WireType;
import org.jetbrains.annotations.NotNull;

import java.text.SimpleDateFormat;

public class DefaultChronicleLogWriter implements ChronicleLogWriter {

    private static final ThreadLocal<Boolean> REENTRANCY_FLAG = ThreadLocal.withInitial(() -> false);
    private static final ThreadLocal<SimpleDateFormat> tsFormatter = ThreadLocal.withInitial(() -> new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS"));

    private final ChronicleQueue cq;

    public DefaultChronicleLogWriter(@NotNull ChronicleQueue cq) {
        this.cq = cq;
    }

    @Override
    public void close() {
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
        if (REENTRANCY_FLAG.get()) {
            if (throwable == null) {
                System.out.printf("%s|%s|%s|%s|%s%n",
                        tsFormatter.get().format(timestamp),
                        level.toString(),
                        threadName,
                        loggerName,
                        message);

            } else {
                System.out.printf("%s|%s|%s|%s|%s|%s%n",
                        tsFormatter.get().format(timestamp),
                        level.toString(),
                        threadName,
                        loggerName,
                        message,
                        throwable.toString());
            }
            return;
        }
        REENTRANCY_FLAG.set(true);
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
                        try {
                            vo.object(o);
                        } catch (IllegalArgumentException unsupported) {
                            vo.text(o.toString());
                        }
                });
            }
        } finally {
            REENTRANCY_FLAG.set(false);
        }
    }

    public WireType getWireType() {
        return cq.wireType();
    }
}
