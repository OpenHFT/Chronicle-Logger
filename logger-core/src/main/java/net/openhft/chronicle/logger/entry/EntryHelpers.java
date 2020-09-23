package net.openhft.chronicle.logger.entry;

import java.time.Instant;

public final class EntryHelpers {

    private EntryHelpers() {

    }

    public static EntryHelpers instance() {
        return LazyHolder.INSTANCE;
    }

    public long epochSecondFromMillis(long epochMilli) {
        return Math.floorDiv(epochMilli, (long) 1000);
    }

    public int nanosFromMillis(long epochMilli) {
        return (int) Math.floorMod(epochMilli, (long) 1000);
    }

    public long millisFromTimestamp(EntryTimestamp ts) {
        long seconds = ts.epochSecond();
        int nanos = ts.nanoAdjust();
        if (seconds == 0) {
            return 0;
        }
        if (seconds < 0 && nanos > 0) {
            long millis = Math.multiplyExact(seconds+1, (long) 1000);
            long adjustment = nanos / 1000_000 - 1000;
            return Math.addExact(millis, adjustment);
        } else {
            long millis = Math.multiplyExact(seconds, (long) 1000);
            return Math.addExact(millis, nanos / 1000_000);
        }
    }

    public Instant instantFromTimestamp(EntryTimestamp ts) {
        return Instant.ofEpochSecond(ts.epochSecond(), ts.nanoAdjust());
    }

    private static class LazyHolder {
        static final EntryHelpers INSTANCE = new EntryHelpers();
    }
}
