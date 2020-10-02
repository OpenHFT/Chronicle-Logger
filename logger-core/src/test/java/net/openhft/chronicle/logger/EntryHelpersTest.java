package net.openhft.chronicle.logger;

import net.openhft.chronicle.logger.entry.EntryHelpers;
import org.junit.Test;

import java.time.Instant;

import static org.junit.Assert.assertEquals;

public class EntryHelpersTest {

    @Test
    public void testSecondsFromMillis() {
        Instant now = Instant.now();
        EntryHelpers instance = EntryHelpers.instance();
        // Get something that's only accurate to .123
        long epochMillis = now.toEpochMilli();

        long seconds = instance.epochSecondFromMillis(epochMillis);
        assertEquals(now.getEpochSecond(), seconds);
    }

    @Test
    public void testNanosFromMillis() {
        Instant now = Instant.now();
        EntryHelpers instance = EntryHelpers.instance();
        // Get something that's only accurate to .123
        long epochMillis = now.toEpochMilli();

        // same in 1.8, but won't be same in 11
        int nanos = instance.nanosFromMillis(epochMillis);
        assertEquals(now.getNano(), nanos);
    }

}
