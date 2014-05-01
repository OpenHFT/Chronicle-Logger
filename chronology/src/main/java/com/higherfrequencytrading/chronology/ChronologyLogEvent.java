package com.higherfrequencytrading.chronology;

import net.openhft.lang.io.Bytes;
import net.openhft.lang.io.serialization.BytesMarshallable;
import net.openhft.lang.model.constraints.NotNull;

public class ChronologyLogEvent implements BytesMarshallable {

    private byte version;
    private byte type;
    private long timestamp;
    private int level;
    private String threadName;
    private String loggerName;
    private String message;
    private String fmtMessage;
    private Object[] args;

    public ChronologyLogEvent() {
        this.version        = 0;
        this.type           = 0;
        this.timestamp      = -1;
        this.level          = -1;
        this.threadName     = null;
        this.loggerName     = null;
        this.message        = null;
        this.fmtMessage     = null;
        this.args           = null;
    }

    // *********************************************************************
    //
    // *********************************************************************

    public byte getVersion() {
        return this.version;
    }

    public byte getType() {
        return this.type;
    }

    public long getTimeStamp() {
        return this.timestamp;
    }

    public String getThreadName() {
        return this.threadName;
    }

    public ChronologyLogLevel getLevel() {
        return ChronologyLogLevel.fromIntLevel(this.level);
    }

    public String getMessage() {
        return this.message;
    }

    public Object[] getArgumentArray() {
        return this.args;
    }

    public boolean hasArguments() {
        return this.args != null && this.args.length > 0;
    }

    public String getLoggerName() {
        return this.loggerName;
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
        } else {
            throw new UnsupportedClassVersionError();
        }
    }

    @Override
    public void writeMarshallable(@NotNull Bytes out) {
        throw new UnsupportedOperationException();
    }
}
