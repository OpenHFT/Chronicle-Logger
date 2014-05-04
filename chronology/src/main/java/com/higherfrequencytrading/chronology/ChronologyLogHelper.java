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

    /**
     * Returns the string representation of Throwable.
     *
     * @param   throwable   the Throwable
     * @return              the string representation of the Throwable
     */
    public static String getStackTraceAsString(final Throwable throwable) {
        return getStackTraceAsString(throwable,Chronology.NEWLINE,-1);
    }

    /**
     * Returns the string representation of Throwable.
     *
     * @param   throwable   the Throwable
     * @return              the string representation of the Throwable
     */
    public static String getStackTraceAsString(final Throwable throwable, String separator) {
        StringBuilder sb = new StringBuilder();
        sb.append(throwable.toString());
        sb.append(separator);

        for (StackTraceElement element : throwable.getStackTrace()) {
            sb.append(element.toString());
            sb.append(separator);
        }

        return getStackTraceAsString(throwable,separator,-1);
    }



    /**
     * Returns the string representation of Throwable.
     *
     * @param   throwable   the Throwable
     * @param   separator   the separator to use to separate StackTraceElement
     * @param   depth       the number of StackTraceElement to dump
     * @return              the string representation of the Throwable
     */
    public static String getStackTraceAsString(final Throwable throwable, String separator, int depth) {
        StackTraceElement[] elements = throwable.getStackTrace();
        int nbElements = (depth == -1) ? elements.length : Math.min(depth,elements.length);

        StringBuilder sb = new StringBuilder();
        sb.append(throwable.toString());
        if(nbElements > 0) {
            sb.append(separator);
        }

        for (int i=0;i < nbElements ; i++) {
            sb.append(elements[i].toString());
            sb.append(separator);
        }

        return sb.toString();
    }
}
