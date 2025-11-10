//
// Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
//

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
package net.openhft.chronicle.logger.jul;

import net.openhft.chronicle.logger.ChronicleLogLevel;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.LogRecord;

/**
 * Converts JUL {@link Level} values to Chronicle log levels and back.
 * The maps hold the closest equivalents and fall back to DEBUG or FINE
 * when a particular level is not recognised.
 */
class ChronicleHelper {

    private static final Map<Level, ChronicleLogLevel> julToCHronicleLevelMap;
    private static final Map<ChronicleLogLevel, Level> chronicleToJulLevelMap;

    static {
        julToCHronicleLevelMap = new HashMap<>();
        julToCHronicleLevelMap.put(Level.ALL, ChronicleLogLevel.TRACE);
        julToCHronicleLevelMap.put(Level.FINEST, ChronicleLogLevel.TRACE);
        julToCHronicleLevelMap.put(Level.FINER, ChronicleLogLevel.TRACE);
        julToCHronicleLevelMap.put(Level.FINE, ChronicleLogLevel.DEBUG);
        julToCHronicleLevelMap.put(Level.CONFIG, ChronicleLogLevel.DEBUG);
        julToCHronicleLevelMap.put(Level.INFO, ChronicleLogLevel.INFO);
        julToCHronicleLevelMap.put(Level.WARNING, ChronicleLogLevel.WARN);
        julToCHronicleLevelMap.put(Level.SEVERE, ChronicleLogLevel.ERROR);

        chronicleToJulLevelMap = new HashMap<>();
        chronicleToJulLevelMap.put(ChronicleLogLevel.TRACE, Level.FINER);
        chronicleToJulLevelMap.put(ChronicleLogLevel.DEBUG, Level.FINE);
        chronicleToJulLevelMap.put(ChronicleLogLevel.INFO, Level.INFO);
        chronicleToJulLevelMap.put(ChronicleLogLevel.WARN, Level.WARNING);
        chronicleToJulLevelMap.put(ChronicleLogLevel.ERROR, Level.SEVERE);
    }

    private ChronicleHelper() {

    }

    static ChronicleLogLevel getLogLevel(final LogRecord julRecord) {
        return getLogLevel(julRecord.getLevel());
    }

    /**
     * Maps a JUL level to the closest Chronicle level.
     * Defaults to {@link ChronicleLogLevel#DEBUG} when the mapping is missing.
     */
    static ChronicleLogLevel getLogLevel(final Level julLevel) {
        ChronicleLogLevel level = julToCHronicleLevelMap.get(julLevel);
        return level != null ? level : ChronicleLogLevel.DEBUG;
    }

    /**
     * Maps a Chronicle level to the nearest JUL equivalent.
     * Defaults to {@link Level#FINE} when no specific mapping exists.
     */
    static Level getLogLevel(final ChronicleLogLevel chronicleLevel) {
        Level level = chronicleToJulLevelMap.get(chronicleLevel);
        return level != null ? level : Level.FINE;
    }
}
