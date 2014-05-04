package com.higherfrequencytrading.chronology;

import net.openhft.lang.io.serialization.BytesMarshallable;

public interface ChronologyLogEvent extends BytesMarshallable {

    public byte getVersion();

    public byte getType();

    public long getTimeStamp();

    public String getThreadName();

    public ChronologyLogLevel getLevel();

    public String getMessage();

    public Object[] getArgumentArray();

    public boolean hasArguments();

    public String getLoggerName();

    public Throwable getThrowable();
}
