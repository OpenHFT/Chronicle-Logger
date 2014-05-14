package com.higherfrequencytrading.chronology;


import net.openhft.lang.io.ByteStringAppender;
import net.openhft.lang.io.RandomDataInput;
import net.openhft.lang.io.RandomDataOutput;
import net.openhft.lang.model.constraints.NotNull;


public enum ChronologyLogLevel {
    ERROR(50,"ERROR"),
    WARN (40,"WARN" ),
    INFO (30,"INFO" ),
    DEBUG(20,"DEBUG"),
    TRACE(10,"TRACE");

    private static final int CASE_DIFF = 'A' - 'a';

    /**
     * Package-private for testing.
     *
     * @param upperCase string of A-Z characters
     * @param other     a {@code CharSequence} to compare
     * @return          {@code true} if {@code upperCase} and {@code other} equals ignore case
     */
    static boolean fastEqualsIgnoreCase(@NotNull String upperCase,
                                        @NotNull CharSequence other) {
        int l;
        if ((l = upperCase.length()) != other.length())
            return false;
        for (int i = 0; i < l; i++) {
            int uC, oC;
            if ((uC = upperCase.charAt(i)) != (oC = other.charAt(i)) && (uC != oC + CASE_DIFF))
                return false;
        }
        return true;
    }

    /**
     * Array is not cached in Java enum internals, make the single copy to prevent garbage creation
     */
    private static final ChronologyLogLevel[] VALUES = values();

    private final int levelInt;
    public final String levelStr;

    ChronologyLogLevel(int levelInt, String levelStr) {
        this.levelInt = levelInt;
        this.levelStr = levelStr;
    }

    public boolean isHigherOrEqualTo(ChronologyLogLevel presumablyLowerLevel) {
        return levelInt >= presumablyLowerLevel.levelInt;
    }

    public void printTo(ByteStringAppender appender) {
        appender.append(levelStr);
    }

    public void writeTo(RandomDataOutput out) {
        out.writeByte(ordinal());
    }

    @Override
    public String toString() {
        return levelStr;
    }

    // *************************************************************************
    //
    // *************************************************************************

    public static ChronologyLogLevel readBinary(RandomDataInput in) {
        return VALUES[in.readByte()];
    }

    public static ChronologyLogLevel fromStringLevel(CharSequence levelStr) {
        if (levelStr != null) {
            for (ChronologyLogLevel cll : VALUES) {
                if (fastEqualsIgnoreCase(cll.levelStr, levelStr)) {
                    return cll;
                }
            }
        }
        throw new IllegalArgumentException(levelStr + " not a valid level value");
    }
}
