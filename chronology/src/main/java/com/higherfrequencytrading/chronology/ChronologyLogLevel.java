package com.higherfrequencytrading.chronology;


public enum ChronologyLogLevel {
    ERROR(50,"ERROR"),
    WARN (40,"WARN" ),
    INFO (30,"INFO" ),
    DEBUG(20,"DEBUG"),
    TRACE(10,"TRACE");

    /**
     * Array is not cached in Java enum internals, make the single copy to prevent garbage creation
     */
    private static final ChronologyLogLevel[] VALUES = values();

    public final int levelInt;
    public final String levelStr;

    ChronologyLogLevel(int levelInt, String levelStr) {
        this.levelInt = levelInt;
        this.levelStr = levelStr;
    }

    @Override
    public String toString() {
        return levelStr;
    }

    // *************************************************************************
    //
    // *************************************************************************

    public static ChronologyLogLevel fromIntLevel(int levelInt) {
        for(ChronologyLogLevel cll : VALUES) {
            if(cll.levelInt == levelInt) {
                return cll;
            }
        }

        throw new IllegalArgumentException(levelInt + " not a valid level value");
    }

    public static ChronologyLogLevel fromStringLevel(String levelStr) {
        for (ChronologyLogLevel cll : VALUES) {
            if(cll.levelStr.equalsIgnoreCase(levelStr)) {
                return cll;
            }
        }

        throw new IllegalArgumentException(levelStr + " not a valid level value");
    }

    public static int intLevelFromStringLevel(CharSequence levelStr) {
        for (ChronologyLogLevel cll : VALUES) {
            if(cll.levelStr.equalsIgnoreCase(levelStr)) {
                return cll.levelInt;
            }
        }

        throw new IllegalArgumentException(levelStr + " not a valid level value");
    }

    public static int stringLevelFromIntLevel(int levelInt) {
        for(ChronologyLogLevel cll : VALUES) {
            if(cll.levelInt == levelInt) {
                return cll.levelInt;
            }
        }

        throw new IllegalArgumentException(levelInt + " not a valid level value");
    }
}
