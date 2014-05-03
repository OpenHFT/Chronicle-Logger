package com.higherfrequencytrading.chronology;


public enum ChronologyLogLevel {
    ERROR(50,"ERROR"),
    WARN (40,"WARN" ),
    INFO (30,"INFO" ),
    DEBUG(20,"DEBUG"),
    TRACE(10,"TRACE");

    public final int levelInt;
    public final String levelStr;

    ChronologyLogLevel(int levelInt, String levelStr) {
        this.levelInt = levelInt;
        this.levelStr = levelStr;
    }

    // *************************************************************************
    //
    // *************************************************************************

    public static ChronologyLogLevel fromIntLevel(int levelInt) {
        for(ChronologyLogLevel cll : ChronologyLogLevel.values()) {
            if(cll.levelInt == levelInt) {
                return cll;
            }
        }

        throw new IllegalArgumentException(levelInt + " not a valid level value");
    }

    public static ChronologyLogLevel fromStringLevel(String levelStr) {
        for(ChronologyLogLevel cll : ChronologyLogLevel.values()) {
            if(cll.levelStr.equalsIgnoreCase(levelStr)) {
                return cll;
            }
        }

        throw new IllegalArgumentException(levelStr + " not a valid level value");
    }

    public static int intLevelfromStringLevel(String levelStr) {
        for(ChronologyLogLevel cll : ChronologyLogLevel.values()) {
            if(cll.levelStr.equalsIgnoreCase(levelStr)) {
                return cll.levelInt;
            }
        }

        throw new IllegalArgumentException(levelStr + " not a valid level value");
    }

    public static int stringLevelfromintLevel(int levelInt) {
        for(ChronologyLogLevel cll : ChronologyLogLevel.values()) {
            if(cll.levelInt == levelInt) {
                return cll.levelInt;
            }
        }

        throw new IllegalArgumentException(levelInt + " not a valid level value");
    }
}
