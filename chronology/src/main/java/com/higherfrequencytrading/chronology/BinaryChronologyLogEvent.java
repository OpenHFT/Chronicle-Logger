package com.higherfrequencytrading.chronology;

import net.openhft.lang.io.Bytes;
import net.openhft.lang.model.constraints.NotNull;

final class BinaryChronologyLogEvent extends ChronologyLogEvent {

    static BinaryChronologyLogEvent read(@NotNull Bytes in) throws IllegalStateException {
        byte version = in.readByte();
        if(version == Chronology.VERSION) {
            byte type       = in.readByte();
            long timestamp  = in.readLong();
            byte level      = in.readByte();
            String threadName = in.readUTF();
            String loggerName = in.readUTF();
            String message    = in.readUTF();

            // Args
            long argsLen = in.readStopBit();
            if (argsLen < 0 || argsLen > Integer.MAX_VALUE)
                throw new IllegalStateException();
            Object[] args = null;
            if (argsLen != 0) {
                args = new Object[(int) argsLen];
                for (int i = 0; i < argsLen; i++) {
                    args[i] = in.readObject();
                }
            }

            Throwable throwable = in.readBoolean() ? in.readObject(Throwable.class) : null;
            return new BinaryChronologyLogEvent(version, type, timestamp,
                    ChronologyLogLevel.fromIntLevel(level), threadName, loggerName, message, args,
                    throwable);
        } else {
            throw new UnsupportedClassVersionError();
        }
    }

    // *********************************************************************
    //
    // *********************************************************************

    private final byte version;
    private final byte type;
    private final long timestamp;
    private final ChronologyLogLevel level;
    private final String threadName;
    private final String loggerName;
    private final String message;
    private final Object[] args;
    private final Throwable throwable;

    BinaryChronologyLogEvent(byte version, byte type, long timestamp, ChronologyLogLevel level,
                             String threadName, String loggerName, String message, Object[] args,
                             Throwable throwable) {
        this.version = version;
        this.type = type;
        this.timestamp = timestamp;
        this.level = level;
        this.threadName = threadName;
        this.loggerName = loggerName;
        this.message = message;
        this.args = args;
        this.throwable = throwable;
    }

    // *********************************************************************
    //
    // *********************************************************************

    @Override
    public byte getVersion() {
        return this.version;
    }

    @Override
    public byte getType() {
        return this.type;
    }

    @Override
    public long getTimeStamp() {
        return this.timestamp;
    }

    @Override
    public String getThreadName() {
        return this.threadName;
    }

    @Override
    public ChronologyLogLevel getLevel() {
        return this.level;
    }

    @Override
    public String getMessage() {
        return this.message;
    }

    @Override
    public Object[] getArgumentArray() {
        return this.args != null ? args : EMPTY_ARGS;
    }

    @Override
    public boolean hasArguments() {
        return this.args != null;
    }

    @Override
    public String getLoggerName() {
        return this.loggerName;
    }

    @Override
    public Throwable getThrowable() {
        return this.throwable;
    }
}
