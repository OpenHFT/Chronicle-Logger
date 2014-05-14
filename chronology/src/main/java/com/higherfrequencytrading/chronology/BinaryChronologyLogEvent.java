package com.higherfrequencytrading.chronology;

import net.openhft.lang.io.Bytes;
import net.openhft.lang.model.constraints.NotNull;

final class BinaryChronologyLogEvent extends ChronologyLogEvent {

    static BinaryChronologyLogEvent read(@NotNull Bytes in) throws IllegalStateException {
        byte version = in.readByte();
        if(version == Chronology.VERSION) {
            byte type       = in.readByte();
            long timestamp  = in.readLong();
            int level      = in.readInt();
            String threadName = in.readUTF();
            String loggerName = in.readUTF();
            String message    = in.readUTF();

            // Args
            // TODO: should args be null ?
            Object[] args = new Object[in.readInt()];
            for(int i=0;i<args.length;i++) {
                args[i] = in.readObject();
            }

            Throwable throwable = in.readBoolean() ? in.readObject(Throwable.class) : null;
            return new BinaryChronologyLogEvent(version, type, timestamp, level, threadName,
                    loggerName, message, args, throwable);
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
    private final int level;
    private final String threadName;
    private final String loggerName;
    private final String message;
    private final Object[] args;
    private final Throwable throwable;

    BinaryChronologyLogEvent(byte version, byte type, long timestamp, int level, String threadName,
                             String loggerName, String message, Object[] args, Throwable throwable) {
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
        return ChronologyLogLevel.fromIntLevel(this.level);
    }

    @Override
    public String getMessage() {
        return this.message;
    }

    @Override
    public Object[] getArgumentArray() {
        return this.args;
    }

    @Override
    public boolean hasArguments() {
        return this.args != null && this.args.length > 0;
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
