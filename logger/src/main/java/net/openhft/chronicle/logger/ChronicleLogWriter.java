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

import net.openhft.chronicle.Chronicle;

import java.io.Closeable;

public interface ChronicleLogWriter extends Closeable {

    public Chronicle getChronicle();

    public void write(
        ChronicleLogLevel level,
        long timestamp,
        String threadName,
        String loggerName,
        String message);

    public void write(
        ChronicleLogLevel level,
        long timestamp,
        String threadName,
        String loggerName,
        String message,
        Throwable throwable);

    public void write(
        ChronicleLogLevel level,
        long timestamp,
        String threadName,
        String loggerName,
        String message,
        Throwable throwable,
        Object arg1);

    public void write(
        ChronicleLogLevel level,
        long timestamp,
        String threadName,
        String loggerName,
        String message,
        Throwable throwable,
        Object arg1,
        Object arg2);

    public void write(
        ChronicleLogLevel level,
        long timestamp,
        String threadName,
        String loggerName,
        String message,
        Throwable throwable,
        Object[] args);
}
