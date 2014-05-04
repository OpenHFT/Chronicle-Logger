package com.higherfrequencytrading.chronology;

import net.openhft.lang.io.Bytes;
import net.openhft.lang.model.constraints.NotNull;

public class BinaryChronologyLogEvent implements ChronologyLogEvent {

    private byte version;
    private byte type;
    private long timestamp;
    private int level;
    private String threadName;
    private String loggerName;
    private String message;
    private String fmtMessage;
    private Object[] args;
    private Throwable throwable;

    public BinaryChronologyLogEvent() {
        this.version        = 0;
        this.type           = 0;
        this.timestamp      = -1;
        this.level          = -1;
        this.threadName     = null;
        this.loggerName     = null;
        this.message        = null;
        this.fmtMessage     = null;
        this.args           = null;
        this.throwable      = null;
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

    // *********************************************************************
    //
    // *********************************************************************

    @Override
    public void readMarshallable(@NotNull Bytes in) throws IllegalStateException {
        this.version = in.readByte();
        if(this.version == Chronology.VERSION) {
            this.type       = in.readByte();
            this.timestamp  = in.readLong();
            this.level      = in.readInt();
            this.threadName = in.readUTF();
            this.loggerName = in.readUTF();
            this.message    = in.readUTF();

            // Args
            // TODO: should this.args be null ?
            this.args = new Object[in.readInt()];
            for(int i=0;i<this.args.length;i++) {
                this.args[i] = in.readObject();
            }

            if(in.readBoolean()) {
                this.throwable = in.readObject(Throwable.class);
            }
        } else {
            throw new UnsupportedClassVersionError();
        }
    }

    @Override
    public void writeMarshallable(@NotNull Bytes out) {
        throw new UnsupportedOperationException();
    }
}
