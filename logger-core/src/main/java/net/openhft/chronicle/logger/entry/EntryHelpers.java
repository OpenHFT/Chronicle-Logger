package net.openhft.chronicle.logger.entry;

import java.time.Instant;
import java.util.concurrent.TimeUnit;

/**
 * Helpers for converting between flatbuffer reps and java reps.
 */
public final class EntryHelpers {

    private EntryHelpers() {

    }

    public static EntryHelpers instance() {
        return LazyHolder.INSTANCE;
    }

    /**
     * Gets the epoch second portion from the milliseconds from epoch.
     */
    public long epochSecondFromMillis(long epochMilli) {
        return Math.floorDiv(epochMilli, (long) 1000);
    }

    /**
     * Gets the nanosecond portion from the the milliseconds from epoch.
     */
    public int nanosFromMillis(long epochMilli) {
        return (int) Math.floorMod(epochMilli, (long) 1000) * 1_000_000;
    }

    /**
     * Gets a time since epoch in milliseconds from the entry timestamp.
     */
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

    /**
     * Creates an instant from an entry timestamp.
     *
     * NOTE: This does an allocation of {@code new Instant()}.
     */
    public Instant instantFromTimestamp(EntryTimestamp ts) {
        return Instant.ofEpochSecond(ts.epochSecond(), ts.nanoAdjust());
    }

    private static class LazyHolder {
        static final EntryHelpers INSTANCE = new EntryHelpers();
    }
}
