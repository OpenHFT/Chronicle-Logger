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

import net.openhft.chronicle.Chronicle;
import net.openhft.chronicle.ExcerptAppender;
import net.openhft.chronicle.VanillaChronicle;


import java.io.Closeable;
import java.io.IOException;

public class ChronicleLogAppenders {

    // *************************************************************************
    //
    // *************************************************************************

    public static interface ExcerptAppenderProvider {
        public ExcerptAppender get();
    }

    public static class IndexedExcerptAppenderProvider implements ExcerptAppenderProvider {
        private ExcerptAppender appender;

        public IndexedExcerptAppenderProvider(final Chronicle chronicle) {
            try {
                this.appender = chronicle.createAppender();
            } catch (IOException e) {
                this.appender = null;

                e.printStackTrace();
            }
        }

        @Override
        public ExcerptAppender get() {
            return this.appender;
        }
    }

    public static class VanillaExcerptAppenderProvider implements ExcerptAppenderProvider {
        private final Chronicle chronicle;

        public VanillaExcerptAppenderProvider(final Chronicle chronicle) {
            this.chronicle = chronicle;
        }

        @Override
        public ExcerptAppender get() {
            try {
                return this.chronicle.createAppender();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }
    }

    // *************************************************************************
    //
    // *************************************************************************

    public static abstract class AbstractChronicleLogWriter implements ChronicleLogAppender {

        protected final ExcerptAppenderProvider appenderProvider;
        private final Chronicle chronicle;

        public AbstractChronicleLogWriter(Chronicle chronicle) throws IOException {
            this.chronicle = chronicle;
            this.appenderProvider = (chronicle instanceof VanillaChronicle)
                ? new VanillaExcerptAppenderProvider(chronicle)
                : new IndexedExcerptAppenderProvider(chronicle);
        }

        @Override
        public Chronicle getChronicle() {
            return this.chronicle;
        }

        public ExcerptAppender getAppender() {
            return this.appenderProvider.get();
        }

        @Override
        public void close() throws IOException {
            if (this.chronicle != null) {
                this.chronicle.close();
            }
        }
    }

    // *************************************************************************
    //
    // *************************************************************************

    public static final class BinaryWriter extends AbstractChronicleLogWriter {

        public BinaryWriter(Chronicle chronicle) throws IOException {
            super(chronicle);
        }

        private void logCommon(
                final ExcerptAppender appender,
                final ChronicleLogLevel level,
                final long timestamp,
                final String threadName,
                final String name,
                final String message) {

            appender.writeByte(ChronicleLog.VERSION);
            appender.writeLong(timestamp);
            level.writeTo(appender);
            appender.writeUTF(threadName);
            appender.writeUTF(name);
            appender.writeUTF(message);
        }

        @Override
        public void log(
                final ChronicleLogLevel level,
                final long timestamp,
                final Thread thread,
                final String name,
                final String message,
                final Object arg1) {

            log(level, timestamp, thread.getName(), name, message, arg1);
        }

        @Override
        public void log(
                final ChronicleLogLevel level,
                final long timestamp,
                final String threadName,
                final String name,
                final String message,
                final Object arg1) {

            final ExcerptAppender appender = getAppender();
            if (appender != null) {
                appender.startExcerpt();

                logCommon(appender, level, timestamp, threadName, name, message);

                if (!(arg1 instanceof Throwable)) {
                    appender.writeStopBit(1);
                    appender.writeObject(arg1);
                    appender.writeBoolean(false);
                } else {
                    appender.writeStopBit(0);
                    appender.writeBoolean(true);
                    appender.writeObject(arg1);
                }

                appender.finish();
            }
        }

        @Override
        public void log(
                final ChronicleLogLevel level,
                final long timestamp,
                final Thread thread,
                final String name,
                final String message,
                final Object arg1,
                final Object arg2) {

            log(level, timestamp, thread.getName(), name, message, arg1, arg2);
        }

        @Override
        public void log(
                final ChronicleLogLevel level,
                final long timestamp,
                final String threadName,
                final String name,
                final String message,
                final Object arg1,
                final Object arg2) {

            final ExcerptAppender appender = getAppender();
            if (appender != null) {
                appender.startExcerpt();

                logCommon(appender, level, timestamp, threadName, name, message);

                if (!(arg2 instanceof Throwable)) {
                    appender.writeStopBit(2);
                    appender.writeObject(arg1);
                    appender.writeObject(arg2);
                    appender.writeBoolean(false);
                } else {
                    appender.writeStopBit(1);
                    appender.writeObject(arg1);
                    appender.writeBoolean(true);
                    appender.writeObject(arg2);
                }

                appender.finish();
            }
        }

        @Override
        public void log(
                final ChronicleLogLevel level,
                final long timestamp,
                final Thread thread,
                final String name,
                final String message,
                final Throwable throwable,
                final Object... args) {

            log(level, timestamp, thread.getName(), name, message, throwable, args);
        }

        @Override
        public void log(
                final ChronicleLogLevel level,
                final long timestamp,
                final String threadName,
                final String name,
                final String message,
                final Throwable throwable,
                final Object... args) {

            final ExcerptAppender appender = getAppender();
            if (appender != null) {
                appender.startExcerpt();

                logCommon(appender, level, timestamp, threadName, name, message);

                if(args != null) {
                    appender.writeStopBit(args.length);
                    for (Object arg : args) {
                        appender.writeObject(arg);
                    }
                } else {
                    appender.writeStopBit(0);
                }

                if(throwable != null) {
                    appender.writeBoolean(true);
                    appender.writeObject(throwable);
                } else {
                    appender.writeBoolean(false);
                }

                appender.finish();
            }
        }
    }

    // *************************************************************************
    //
    // *************************************************************************

    public static class BinaryFormattingWriter extends AbstractChronicleLogWriter {
        private final ChronicleLogFormatter formatter;

        public BinaryFormattingWriter(Chronicle chronicle, ChronicleLogFormatter formatter) throws IOException {
            super(chronicle);

            this.formatter = formatter;
        }

        protected void doLog(
                final ChronicleLogLevel level,
                final long timestamp,
                final String threadName,
                final String name,
                final String message,
                final Throwable throwable) {

            final ExcerptAppender appender = getAppender();
            if (appender != null) {
                appender.startExcerpt();
                appender.writeByte(ChronicleLog.VERSION);
                appender.writeLong(timestamp);
                level.writeTo(appender);
                appender.writeUTF(threadName);
                appender.writeUTF(name);
                appender.writeUTF(message);
                appender.writeStopBit(0);

                if (throwable == null) {
                    appender.writeBoolean(false);
                } else {
                    appender.writeBoolean(true);
                    appender.writeObject(throwable);
                }

                appender.finish();
            }
        }

        @Override
        public void log(
                final ChronicleLogLevel level,
                final long timestamp,
                final Thread thread,
                final String name,
                final String message,
                final Object arg1) {

            log(level, timestamp, thread.getName(), name, message, arg1);
        }

        @Override
        public void log(
                final ChronicleLogLevel level,
                final long timestamp,
                final String threadName,
                final String name,
                final String message,
                final Object arg1) {

            doLog(level, timestamp, threadName, name, formatter.format(message, arg1), null);
        }

        @Override
        public void log(
                final ChronicleLogLevel level,
                final long timestamp,
                final Thread thread,
                final String name,
                final String message,
                final Object arg1,
                final Object arg2) {

            log(level, timestamp, thread.getName(), name, message, arg1, arg2);
        }

        @Override
        public void log(
                final ChronicleLogLevel level,
                final long timestamp,
                final String threadName,
                final String name,
                final String message,
                final Object arg1,
                final Object arg2) {

            doLog(level, timestamp, threadName, name, formatter.format(message, arg1, arg2), null);
        }

        @Override
        public void log(
                final ChronicleLogLevel level,
                final long timestamp,
                final Thread thread,
                final String name,
                final String message,
                final Throwable throwable,
                final Object... args) {

            log(level, timestamp, thread.getName(), name, message, throwable, args);
        }

        @Override
        public void log(
                final ChronicleLogLevel level,
                final long timestamp,
                final String threadName,
                final String name,
                final String message,
                final Throwable throwable,
                final Object... args) {

            if (args == null || args.length == 0) {
                doLog(level, timestamp, threadName, name, message, throwable);
            } else {
                String fmtmsg = message;
                if (args.length == 1) {
                    fmtmsg = formatter.format(message, args[0]);
                } else if (args.length == 2) {
                    fmtmsg = formatter.format(message, args[0], args[1]);
                } else {
                    fmtmsg = formatter.format(message, (Object[]) args);
                }

                final Object lastEntry = args[args.length - 1];
                if (lastEntry instanceof Throwable) {
                    doLog(level, timestamp, threadName, name, fmtmsg, (Throwable) lastEntry);
                } else {
                    doLog(level, timestamp, threadName, name, fmtmsg, null);
                }
            }

        }
    }

    // *************************************************************************
    //
    // *************************************************************************

    public static class TextWriter extends AbstractChronicleLogWriter {
        private final ChronicleLogFormatter formatter;
        private final TimeStampFormatter timeStampFormatter;
        private final int stackTraceDepth;

        public TextWriter(Chronicle chronicle, ChronicleLogFormatter formatter, String dateFormat, Integer stackTraceDepth) throws IOException {
            super(chronicle);

            this.formatter = formatter;
            this.stackTraceDepth = stackTraceDepth != null ? stackTraceDepth : -1;
            this.timeStampFormatter = TimeStampFormatter.fromDateFormat(dateFormat);
        }

        protected void doLog(
                final ChronicleLogLevel level,
                long timestamp,
                final String threadName,
                final String name,
                final String message,
                final Throwable throwable) {

            final ExcerptAppender appender = getAppender();
            if (appender != null) {
                appender.startExcerpt();
                timeStampFormatter.format(timestamp, appender);
                appender.append('|');
                level.printTo(appender);
                appender.append('|');
                appender.append(threadName);
                appender.append('|');
                appender.append(name);
                appender.append('|');
                appender.append(message);

                if (throwable != null) {
                    appender.append(" - ");
                    ChronicleLogHelper.appendStackTraceAsString(
                        appender,
                        throwable,
                        ChronicleLog.COMMA,
                        this.stackTraceDepth);
                }

                appender.append('\n');
                appender.finish();
            }
        }

        @Override
        public void log(
                final ChronicleLogLevel level,
                final long timestamp,
                final Thread thread,
                final String name,
                final String message,
                final Object arg1) {

            log(level, timestamp, thread.getName(), name, message, arg1);
        }

        @Override
        public void log(
                final ChronicleLogLevel level,
                final long timestamp,
                final String threadName,
                final String name,
                final String message,
                final Object arg1) {

            doLog(level, timestamp, threadName, name, formatter.format(message, arg1), null);
        }

        @Override
        public void log(
                final ChronicleLogLevel level,
                final long timestamp,
                final Thread thread,
                final String name,
                final String message,
                final Object arg1,
                final Object arg2) {

            log(level, timestamp, thread.getName(), name, message, arg1, arg2);
        }

        @Override
        public void log(
                final ChronicleLogLevel level,
                final long timestamp,
                final String threadName,
                final String name,
                final String message,
                final Object arg1,
                final Object arg2) {

            doLog(level, timestamp, threadName, name, formatter.format(message, arg1, arg2), null);
        }

        @Override
        public void log(
                final ChronicleLogLevel level,
                final long timestamp,
                final Thread thread,
                final String name,
                final String message,
                final Throwable throwable,
                final Object... args) {

            log(level, timestamp, thread.getName(), name, message, throwable, args);
        }

        @Override
        public void log(
                final ChronicleLogLevel level,
                final long timestamp,
                final String threadName,
                final String name,
                final String message,
                final Throwable throwable,
                final Object... args) {

            if (args == null || args.length == 0) {
                doLog(level, timestamp, threadName, name, message, throwable);
            } else {
                if (throwable == null) {
                    String fmtmsg = message;
                    if (args.length == 1) {
                        fmtmsg = formatter.format(message, args[0]);
                    } else if (args.length == 2) {
                        fmtmsg = formatter.format(message, args[0], args[1]);
                    } else {
                        fmtmsg = formatter.format(message, args);
                    }

                    final Object lastEntry = args[args.length - 1];
                    if (lastEntry instanceof Throwable) {
                        doLog(level, timestamp, threadName, name, fmtmsg, (Throwable) lastEntry);
                    } else {
                        doLog(level, timestamp, threadName, name, fmtmsg, null);
                    }
                } else {
                    final String fmtmsg = formatter.format(message, args);
                    doLog(level, timestamp, threadName, name, fmtmsg, throwable);
                }
            }
        }
    }

    // *************************************************************************
    //
    // *************************************************************************

    public static final class SynchronizedWriter implements ChronicleLogAppender, Closeable {
        private final ChronicleLogAppender writer;
        private final Object sync;

        public SynchronizedWriter(final ChronicleLogAppender writer) {
            this.writer = writer;
            this.sync = new Object();
        }

        @Override
        public Chronicle getChronicle() {
            return this.writer.getChronicle();
        }

        @Override
        public void log(ChronicleLogLevel level, long timestamp, Thread thread, String name, String message, Object arg1) {
            this.log(level, timestamp, thread.getName(), name, message, arg1);
        }

        @Override
        public void log(ChronicleLogLevel level, long timestamp, String threadName, String name, String message, Object arg1) {
            synchronized (this.sync) {
                this.writer.log(level, timestamp, threadName, name, message, arg1);
            }
        }

        @Override
        public void log(ChronicleLogLevel level, long timestamp, Thread thread, String name, String message, Object arg1, Object arg2) {
            this.log(level, timestamp, thread.getName(), name, message, arg1, arg2);
        }

        @Override
        public void log(ChronicleLogLevel level, long timestamp, String threadName, String name, String message, Object arg1, Object arg2) {
            synchronized (this.sync) {
                this.writer.log(level, timestamp, threadName, name, message, arg1, arg2);
            }
        }

        @Override
        public void log(ChronicleLogLevel level, long timestamp, Thread thread, String name, String message, Throwable throwable, Object... args) {
            this.log(level, timestamp, thread.getName(), name, message, throwable, args);
        }

        @Override
        public void log(ChronicleLogLevel level, long timestamp, String threadName, String name, String message, Throwable throwable, Object... args) {
            synchronized (this.sync) {
                this.writer.log(level, timestamp, threadName, name, message, throwable, args);
            }
        }


        @Override
        public void close() throws IOException {
            synchronized(this.sync) {
                this.writer.close();
            }
        }
    }
}
