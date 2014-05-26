package com.higherfrequencytrading.chronology;

import net.openhft.lang.io.Bytes;
import net.openhft.lang.model.constraints.NotNull;

final class BinaryChronologyLogEvent implements ChronologyLogEvent {

    static BinaryChronologyLogEvent read(@NotNull Bytes in) throws IllegalStateException {
        byte version = in.readByte();
        if(version == Chronology.VERSION) {
            Chronology.Type type = Chronology.Type.read(in);
            long timestamp = in.readLong();
            ChronologyLogLevel level = ChronologyLogLevel.readBinary(in);
            String threadName = in.readUTF();
            String loggerName = in.readUTF();
            String message = in.readUTF();

            // Args
            long argsLen = in.readStopBit();
            if (argsLen < 0 || argsLen > Integer.MAX_VALUE) {
                throw new IllegalStateException();
            }

            Object[] args = null;
            if (argsLen != 0) {
                args = new Object[(int) argsLen];
                for (int i = 0; i < argsLen; i++) {
                    args[i] = in.readObject();
                }
            }

            final Throwable throwable = in.readBoolean() ? in.readObject(Throwable.class) : null;
            return new BinaryChronologyLogEvent(
                version,
                type,
                timestamp,
                level,
                threadName,
                loggerName,
                message,
                args,
                throwable);
        } else {
            throw new UnsupportedClassVersionError();
        }
    }

    // *********************************************************************
    //
    // *********************************************************************

    private final byte version;
    private final Chronology.Type type;
    private final long timestamp;
    private final ChronologyLogLevel level;
    private final String threadName;
    private final String loggerName;
    private final String message;
    private final Object[] args;
    private final Throwable throwable;

    private BinaryChronologyLogEvent(byte version, Chronology.Type type, long timestamp,
        ChronologyLogLevel level, String threadName, String loggerName,
        String message, Object[] args, Throwable throwable) {
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
    public Chronology.Type getType() {
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
