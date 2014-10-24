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

public interface ChronicleLogEvent {

    static final Object[] EMPTY_ARGS = new Object[] {};

    public byte getVersion();

    public ChronicleLog.Type getType();

    public long getTimeStamp();

    public String getThreadName();

    public ChronicleLogLevel getLevel();

    public String getMessage();

    public Object[] getArgumentArray();

    public boolean hasArguments();

    public String getLoggerName();

    public Throwable getThrowable();
}
