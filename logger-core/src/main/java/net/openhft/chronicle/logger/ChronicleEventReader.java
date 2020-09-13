/*
 * Copyright 2014-2017 Higher Frequency Trading
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

import net.openhft.chronicle.wire.Wire;

import java.time.ZonedDateTime;

/**
 * Constructs a ChronicleLogEvent from Chronicle Wire.
 */
public class ChronicleEventReader {

    public ChronicleLogEvent createLogEvent(Wire wire) {
        ZonedDateTime timestamp = wire.read("instant").zonedDateTime();
        int level = wire.read("level").int32();
        String threadName = wire.read("threadName").text();
        String loggerName = wire.read("loggerName").text();
        byte[] entry = wire.read("entry").bytes();
        String contentType = wire.read("type").text();
        String encoding = wire.read("encoding").text();

        return new ChronicleLogEvent(timestamp.toInstant(), level, threadName, loggerName, entry, contentType, encoding);
    }
}
