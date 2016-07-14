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

import net.openhft.lang.io.ByteStringAppender;
import net.openhft.lang.io.RandomDataInput;
import net.openhft.lang.io.RandomDataOutput;

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

    private final int levelInt;
    private final String levelStr;

    ChronicleLogLevel(int levelInt, String levelStr) {
        this.levelInt = levelInt;
        this.levelStr = levelStr;
    }

    public static ChronicleLogLevel readBinary(final RandomDataInput in) {
        return VALUES[in.readByte()];
    }

    public static ChronicleLogLevel fromStringLevel(final CharSequence levelStr) {
        if (levelStr != null) {
            for (ChronicleLogLevel cll : VALUES) {
                if (ChronicleLog.fastEqualsIgnoreCase(cll.levelStr, levelStr)) {
                    return cll;
                }
            }
        }

        throw new IllegalArgumentException(levelStr + " not a valid level value");
    }

    public boolean isHigherOrEqualTo(final ChronicleLogLevel presumablyLowerLevel) {
        return levelInt >= presumablyLowerLevel.levelInt;
    }

    public void printTo(final ByteStringAppender appender) {
        appender.append(levelStr);
    }

    // *************************************************************************
    //
    // *************************************************************************

    public void writeTo(final RandomDataOutput out) {
        out.writeByte(ordinal());
    }

    @Override
    public String toString() {
        return levelStr;
    }
}
