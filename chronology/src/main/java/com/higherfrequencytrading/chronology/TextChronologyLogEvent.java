package com.higherfrequencytrading.chronology;

import net.openhft.lang.io.Bytes;
import net.openhft.lang.io.StopCharTester;
import net.openhft.lang.model.constraints.NotNull;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

final class TextChronologyLogEvent extends ChronologyLogEvent {

    //TODO: check
    private static final DateFormat DATE_FORMAT = new SimpleDateFormat(Chronology.DEFAULT_DATE_FORMAT);
    private static final Object[] EMPTY_ARGS = new Object[] {};
    private static final StopCharTester PIPE_TESTER = new StopCharTester() {
        @Override
        public boolean isStopChar(int ch) throws IllegalStateException {
            return ch == '|';
        }
    };

    static TextChronologyLogEvent read(@NotNull Bytes in) throws IllegalStateException {
        StringBuilder sb = new StringBuilder();
        sb.setLength(0);

        // timestamp
        in.parseUTF(sb, PIPE_TESTER);
        long timestamp = 0;
        try {
            //TODO: store date as long even in text?
            timestamp = DATE_FORMAT.parse(sb.toString()).getTime();
        } catch(Exception e) {
            // Ignore
        }

        // level
        in.parseUTF(sb, PIPE_TESTER);
        int level = ChronologyLogLevel.intLevelFromStringLevel(sb.toString());

        // thread name
        in.parseUTF(sb, PIPE_TESTER);
        String threadName = sb.toString();

        // logger name
        in.parseUTF(sb, PIPE_TESTER);
        String loggerName = sb.toString();

        // message
        String message = in.readLine();

        return new TextChronologyLogEvent(timestamp, level, threadName, loggerName, message);
    }

    // *********************************************************************
    //
    // *********************************************************************

    private final long timestamp;
    private final int level;
    private final String threadName;
    private final String loggerName;
    private final String message;

    TextChronologyLogEvent(long timestamp, int level, String threadName, String loggerName,
                           String message) {
        this.timestamp = timestamp;
        this.level = level;
        this.threadName = threadName;
        this.loggerName = loggerName;
        this.message = message;
    }

    // *********************************************************************
    //
    // *********************************************************************

    @Override
    public byte getVersion() {
        return 0;
    }

    @Override
    public byte getType() {
        return 0;
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

    @Override
    public Throwable getThrowable() {
        return null;
    }
}
