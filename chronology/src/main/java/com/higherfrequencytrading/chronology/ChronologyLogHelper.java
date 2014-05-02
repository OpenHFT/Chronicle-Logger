package com.higherfrequencytrading.chronology;


import net.openhft.chronicle.ExcerptTailer;

public class ChronologyLogHelper {
    /**
     * Decode an Excerpt
     *
     * @param tailer    the ExcerptTailer
     * @return          the ChronologyLogEvent
     */
    public static ChronologyLogEvent decode(final ExcerptTailer tailer) {
        final ChronologyLogEvent event = new ChronologyLogEvent();
        event.readMarshallable(tailer);

        return event;
    }
}
