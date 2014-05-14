package com.higherfrequencytrading.chronology;

import net.openhft.lang.io.Bytes;
import net.openhft.lang.io.StopCharTester;
import net.openhft.lang.model.constraints.NotNull;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

final class TextChronologyLogEvent extends ChronologyLogEvent {

    //TODO: check
    private static final DateFormat DATE_FORMAT = new SimpleDateFormat(Chronology.DEFAULT_DATE_FORMAT);

    private static final StopCharTester PIPE_TESTER = new StopCharTester() {
        @Override
        public boolean isStopChar(int ch) throws IllegalStateException {
            return ch == '|';
        }
    };

    private static final ThreadLocal<StringBuilder> sbCache = new ThreadLocal<StringBuilder>() {
        @Override
        protected StringBuilder initialValue() {
            return new StringBuilder();
        }
    };

    static TextChronologyLogEvent read(@NotNull Bytes in) throws IllegalStateException {
        StringBuilder sb = sbCache.get();
        sb.setLength(0);

        // timestamp
        in.parseUTF(sb, PIPE_TESTER);
        long timestamp = 0;
        try {
            //TODO: store date as long even in text?
            // haven't found a simple way to get rid of this intermediate conversion to String
            timestamp = DATE_FORMAT.parse(sb.toString()).getTime();
        } catch(Exception e) {
            // Ignore
        }

        // level
        in.parseUTF(sb, PIPE_TESTER);
        ChronologyLogLevel level = ChronologyLogLevel.fromStringLevel(sb);

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
    private final ChronologyLogLevel level;
    private final String threadName;
    private final String loggerName;
    private final String message;

    TextChronologyLogEvent(long timestamp, ChronologyLogLevel level, String threadName,
                           String loggerName, String message) {
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
    public Chronology.Type getType() {
        return Chronology.Type.UNKNOWN;
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
