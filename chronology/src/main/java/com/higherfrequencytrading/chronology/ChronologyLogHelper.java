package com.higherfrequencytrading.chronology;


import net.openhft.chronicle.ExcerptAppender;
import net.openhft.lang.io.Bytes;

public final class ChronologyLogHelper {

    /**
     * Decode a binary stream, i. e. Excerpt
     *
     * @param in        the source of event in binary form (i. e. Excerpt)
     * @return          the ChronologyLogEvent
     */
    public static ChronologyLogEvent decodeBinary(final Bytes in) {
        return BinaryChronologyLogEvent.read(in);
    }

    /**
     * Decode a text stream, i. e. Excerpt
     *
     * @param in        the source of event in text form (i. e. Excerpt)
     * @return          the ChronologyLogEvent
     */
    public static ChronologyLogEvent decodeText(final Bytes in) {
        return TextChronologyLogEvent.read(in);
    }

    /**
     * Append a string representation of Throwable to Excerpt
     *
     * @param   throwable   the Throwable
     * @param   separator   the separator to use to separate StackTraceElement
     * @param   depth       the number of StackTraceElement to dump
     * @return              the string representation of the Throwable
     */
    public static ExcerptAppender appendStackTraceAsString(final ExcerptAppender appender, final Throwable throwable, String separator, int depth) {
        StackTraceElement[] elements = throwable.getStackTrace();
        int nbElements = (depth == -1) ? elements.length : Math.min(depth,elements.length);
        int sepLen = separator.length();
        String tmp = null;

        appender.append(throwable.toString());
        if(nbElements > 0) {
            appender.append(separator);
        }

        for (int i=0;i < nbElements; i++) {
            tmp = elements[i].toString();
            if(appender.remaining() > (tmp.length() + sepLen)) {
                appender.append(tmp);
                appender.append(separator);
            } else {
                for(int fill=0;fill<3 && appender.remaining() > 0;fill++){
                    appender.append('.');
                }
                break;
            }
        }

        return appender;
    }

    private ChronologyLogHelper() {}
}
