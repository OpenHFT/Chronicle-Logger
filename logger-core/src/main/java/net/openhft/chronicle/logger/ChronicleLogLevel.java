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
package net.openhft.chronicle.logger;

import org.jetbrains.annotations.NotNull;

/**
 * Levels used by Chronicle Logger.
 *
 * <p>Each level has an integer value:
 * <ul>
 *     <li>ERROR = 50</li>
 *     <li>WARN = 40</li>
 *     <li>INFO = 30</li>
 *     <li>DEBUG = 20</li>
 *     <li>TRACE = 10</li>
 * </ul>
 * A higher value represents a more severe event.
 */

public enum ChronicleLogLevel {
    ERROR(50, "ERROR"),
    WARN(40, "WARN"),
    INFO(30, "INFO"),
    DEBUG(20, "DEBUG"),
    TRACE(10, "TRACE");

    /**
     * Array is not cached in Java enum internals, make the single copy to prevent garbage creation
     */
    private static final ChronicleLogLevel[] VALUES = values();

    private static final int CASE_DIFF = 'A' - 'a';

    private final int levelInt;
    private final String levelStr;

    ChronicleLogLevel(int levelInt, String levelStr) {
        this.levelInt = levelInt;
        this.levelStr = levelStr;
    }

    /**
     * Parses a textual level name.
     *
     * @param levelStr case-insensitive name such as {@code "INFO"}
     * @return the matching level
     * @throws IllegalArgumentException if the name is unknown
     */
    public static ChronicleLogLevel fromStringLevel(final CharSequence levelStr) {
        if (levelStr != null) {
            for (ChronicleLogLevel cll : VALUES) {
                if (fastEqualsIgnoreCase(cll.levelStr, levelStr)) {
                    return cll;
                }
            }
        }

        throw new IllegalArgumentException(levelStr + " not a valid level value");
    }

    /**
     * Package-private for testing.
     *
     * @param upperCase string of A-Z characters
     * @param other     a {@code CharSequence} to compare
     * @return {@code true} if {@code upperCase} and {@code other} equals ignore case
     */
    private static boolean fastEqualsIgnoreCase(@NotNull String upperCase, @NotNull CharSequence other) {
        int l;
        if ((l = upperCase.length()) != other.length()) {
            return false;
        }

        for (int i = 0; i < l; i++) {
            int uC, oC;
            if ((uC = upperCase.charAt(i)) != (oC = other.charAt(i)) && (uC != oC + CASE_DIFF)) {
                return false;
            }
        }

        return true;
    }

    /**
     * Tests whether this level is at least as severe as the given level.
     */
    public boolean isHigherOrEqualTo(final ChronicleLogLevel presumablyLowerLevel) {
        return levelInt >= presumablyLowerLevel.levelInt;
    }

    @Override
    public String toString() {
        return levelStr;
    }
}
