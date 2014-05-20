package com.higherfrequencytrading.chronology;

public interface ChronologyLogEvent {

    static final Object[] EMPTY_ARGS = new Object[] {};

    public byte getVersion();

    public Chronology.Type getType();

    public long getTimeStamp();

    public String getThreadName();

    public ChronologyLogLevel getLevel();

    public String getMessage();

    public Object[] getArgumentArray();

    public boolean hasArguments();

    public String getLoggerName();

    public Throwable getThrowable();
}
