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
import net.openhft.lang.model.constraints.NotNull;

final class BinaryChronicleLogEvent implements ChronicleLogEvent {

    static BinaryChronicleLogEvent read(@NotNull Bytes in) throws IllegalStateException {
        byte version = in.readByte();
        if(version == ChronicleLog.VERSION) {
            ChronicleLog.Type type = ChronicleLog.Type.read(in);
            long timestamp = in.readLong();
            ChronicleLogLevel level = ChronicleLogLevel.readBinary(in);
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
            return new BinaryChronicleLogEvent(
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
            throw new IllegalStateException("message version= " + version);
        }
    }

    // *********************************************************************
    //
    // *********************************************************************

    private final byte version;
    private final ChronicleLog.Type type;
    private final long timestamp;
    private final ChronicleLogLevel level;
    private final String threadName;
    private final String loggerName;
    private final String message;
    private final Object[] args;
    private final Throwable throwable;

    private BinaryChronicleLogEvent(byte version, ChronicleLog.Type type, long timestamp,
        ChronicleLogLevel level, String threadName, String loggerName,
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
    public ChronicleLog.Type getType() {
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
    public ChronicleLogLevel getLevel() {
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
