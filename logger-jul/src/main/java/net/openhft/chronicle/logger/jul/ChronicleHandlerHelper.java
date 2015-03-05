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
package net.openhft.chronicle.logger.jul;

import net.openhft.chronicle.logger.ChronicleLogLevel;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.LogRecord;

public class ChronicleHandlerHelper {

    private static final Map<Level, ChronicleLogLevel> julToCHronicleLevelMap;

    static {
        julToCHronicleLevelMap = new HashMap<>();
        julToCHronicleLevelMap.put(Level.ALL     , ChronicleLogLevel.TRACE);
        julToCHronicleLevelMap.put(Level.CONFIG  , ChronicleLogLevel.TRACE);
        julToCHronicleLevelMap.put(Level.FINEST  , ChronicleLogLevel.TRACE);
        julToCHronicleLevelMap.put(Level.FINER   , ChronicleLogLevel.TRACE);
        julToCHronicleLevelMap.put(Level.FINE    , ChronicleLogLevel.DEBUG);
        julToCHronicleLevelMap.put(Level.INFO    , ChronicleLogLevel.INFO);
        julToCHronicleLevelMap.put(Level.WARNING , ChronicleLogLevel.WARN);
        julToCHronicleLevelMap.put(Level.SEVERE  , ChronicleLogLevel.ERROR);
    }

    public static ChronicleLogLevel getLogLevel(final LogRecord julRecord) {
        return getLogLevel(julRecord.getLevel());
    }

    public static ChronicleLogLevel getLogLevel(final Level julLevel) {
        ChronicleLogLevel level = julToCHronicleLevelMap.get(julLevel);
        return level != null ? level : ChronicleLogLevel.DEBUG;
    }
}
