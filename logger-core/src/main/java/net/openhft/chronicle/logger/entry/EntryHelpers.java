package net.openhft.chronicle.logger.entry;

import java.time.Instant;

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
