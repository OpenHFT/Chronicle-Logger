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

import net.openhft.lang.model.constraints.NotNull;

public final class ChronicleLog {
    public static final String COMMA = ", ";
    public static final String STR_FALSE = "false";
    public static final String STR_TRUE = "true";
    public static final String DEFAULT_DATE_FORMAT = "yyyy.MM.dd-HH:mm:ss.SSS";
    
    public static final byte VERSION = 1;
    private static final int CASE_DIFF = 'A' - 'a';

    // *************************************************************************
    //
    // *************************************************************************

    public static String getLineSeparator() {
        return System.getProperty("line.separator");
    }

    public static String getTmpDir() {
        return System.getProperty("java.io.tmpdir");
    }

    // *************************************************************************
    //
    // *************************************************************************

    /**
     * Package-private for testing.
     *
     * @param upperCase string of A-Z characters
     * @param other     a {@code CharSequence} to compare
     * @return          {@code true} if {@code upperCase} and {@code other} equals ignore case
     */
    public static boolean fastEqualsIgnoreCase(@NotNull String upperCase, @NotNull CharSequence other) {
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

    private ChronicleLog() {
    }
}
