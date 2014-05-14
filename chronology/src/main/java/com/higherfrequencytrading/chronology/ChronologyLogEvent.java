package com.higherfrequencytrading.chronology;

import net.openhft.lang.io.Bytes;

public abstract class ChronologyLogEvent {

    static final Object[] EMPTY_ARGS = new Object[] {};

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

    public abstract byte getVersion();

    public abstract Chronology.Type getType();

    public abstract long getTimeStamp();

    public abstract String getThreadName();

    public abstract ChronologyLogLevel getLevel();

    public abstract String getMessage();

    public abstract Object[] getArgumentArray();

    public abstract boolean hasArguments();

    public abstract String getLoggerName();

    public abstract Throwable getThrowable();
}
