/*
 * Copyright 2014 Higher Frequency Trading
 *
 * http://www.higherfrequencytrading.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.openhft.chronicle.logger;

import net.openhft.lang.io.Bytes;
import net.openhft.lang.io.StopCharTester;
import net.openhft.lang.model.constraints.NotNull;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

final class TextChronicleLogEvent implements ChronicleLogEvent {

    //TODO: check
    private static final DateFormat DATE_FORMAT = new SimpleDateFormat(ChronicleLog.DEFAULT_DATE_FORMAT);

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
    private final long timestamp;

    // *********************************************************************
    //
    // *********************************************************************
    private final ChronicleLogLevel level;
    private final String threadName;
    private final String loggerName;
    private final String message;

    private TextChronicleLogEvent(long timestamp, ChronicleLogLevel level, String threadName,
                                  String loggerName, String message) {
        this.timestamp = timestamp;
        this.level = level;
        this.threadName = threadName;
        this.loggerName = loggerName;
        this.message = message;
    }

    static TextChronicleLogEvent read(@NotNull Bytes in) throws IllegalStateException {
        StringBuilder sb = sbCache.get();
        sb.setLength(0);

        // timestamp
        in.parseUtf8(sb, PIPE_TESTER);
        long timestamp = 0;
        try {
            //TODO: store date as long even in text?
            // haven't found a simple way to get rid of this intermediate conversion to String
            timestamp = DATE_FORMAT.parse(sb.toString()).getTime();
        } catch (Exception e) {
            // Ignore
        }

        // level
        in.parseUtf8(sb, PIPE_TESTER);
        ChronicleLogLevel level = ChronicleLogLevel.fromStringLevel(sb);

        // thread name
        in.parseUtf8(sb, PIPE_TESTER);
        String threadName = sb.toString();

        // logger name
        in.parseUtf8(sb, PIPE_TESTER);
        String loggerName = sb.toString();

        // message
        String message = in.readLine();

        return new TextChronicleLogEvent(
                timestamp,
                level,
                threadName,
                loggerName,
                message);
    }

    // *********************************************************************
    //
    // *********************************************************************

    @Override
    public byte getVersion() {
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
    public ChronicleLogLevel getLevel() {
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
