/*
 * Copyright 2014-2020 chronicle.software
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

import org.jetbrains.annotations.NotNull;

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

    public boolean isHigherOrEqualTo(final ChronicleLogLevel presumablyLowerLevel) {
        return levelInt >= presumablyLowerLevel.levelInt;
    }

    @Override
    public String toString() {
        return levelStr;
    }
}
