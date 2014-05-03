package com.higherfrequencytrading.chronology;

import net.openhft.lang.io.Bytes;
import net.openhft.lang.io.StopCharTester;
import net.openhft.lang.model.constraints.NotNull;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

public class TextChronologyLogEvent implements ChronologyLogEvent {

    //TODO: check
    private static final DateFormat DATE_FORMAT = new SimpleDateFormat(Chronology.DEFAULT_DATE_FORMAT);
    private static final Object[] EMPTY_ARGS = new Object[] {};
    private static final StopCharTester PIPE_TESTER = new StopCharTester() {
        @Override
        public boolean isStopChar(int ch) throws IllegalStateException {
            return ch == '|';
        }
    };

    private byte version;
    private byte type;
    private long timestamp;
    private int level;
    private String threadName;
    private String loggerName;
    private String message;

    public TextChronologyLogEvent() {
        this.version        = 0;
        this.type           = 0;
        this.timestamp      = -1;
        this.level          = -1;
        this.threadName     = null;
        this.loggerName     = null;
        this.message        = null;
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
        return EMPTY_ARGS;
    }

    @Override
    public boolean hasArguments() {
        return false;
    }

    @Override
    public String getLoggerName() {
        return this.loggerName;
    }

    // *********************************************************************
    //
    // *********************************************************************

    @Override
    public void readMarshallable(@NotNull Bytes in) throws IllegalStateException {
        StringBuilder sb = new StringBuilder();

        // timestamp
        in.parseUTF(sb,PIPE_TESTER);
        try {
            //TODO: store date as long even in text?
            this.timestamp = DATE_FORMAT.parse(sb.toString()).getTime();
        } catch(Exception e) {
            this.timestamp = 0;
        }

        // level
        in.parseUTF(sb, PIPE_TESTER);
        this.level = ChronologyLogLevel.intLevelfromStringLevel(sb.toString());

        // thread name
        in.parseUTF(sb, PIPE_TESTER);
        this.threadName = sb.toString();

        // logger name
        in.parseUTF(sb, PIPE_TESTER);
        this.loggerName = sb.toString();

        // message
        this.message = in.readLine();
    }

    @Override
    public void writeMarshallable(@NotNull Bytes out) {
        throw new UnsupportedOperationException();
    }
}
