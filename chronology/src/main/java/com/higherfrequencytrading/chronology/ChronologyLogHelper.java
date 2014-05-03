package com.higherfrequencytrading.chronology;


import net.openhft.chronicle.ExcerptTailer;

public class ChronologyLogHelper {


    /**
     * Decode a binary Excerpt
     *
     * @param tailer    the ExcerptTailer
     * @return          the ChronologyLogEvent
     */
    public static ChronologyLogEvent decodeBinary(final ExcerptTailer tailer) {
        BinaryChronologyLogEvent event = new BinaryChronologyLogEvent();
        event.readMarshallable(tailer);

        return event;
    }

    /**
     * Decode a text Excerpt
     *
     * @param tailer    the ExcerptTailer
     * @return          the ChronologyLogEvent
     */
    public static ChronologyLogEvent decodeText(final ExcerptTailer tailer) {
        TextChronologyLogEvent event = new TextChronologyLogEvent();
        event.readMarshallable(tailer);

        return event;
    }
}
