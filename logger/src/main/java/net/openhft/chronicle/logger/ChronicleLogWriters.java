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
import net.openhft.lang.model.constraints.NotNull;

import java.io.Closeable;
import java.io.IOException;
import java.io.PrintStream;

public class ChronicleLogWriters {

    public interface ExcerptAppenderProvider {
        ExcerptAppender get();
    }

    // *************************************************************************
    //
    // *************************************************************************

    public static class IndexedExcerptAppenderProvider implements ExcerptAppenderProvider {
        private ExcerptAppender appender;

        public IndexedExcerptAppenderProvider(@NotNull final Chronicle chronicle) {
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

        public VanillaExcerptAppenderProvider(@NotNull final Chronicle chronicle) {
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

    public abstract static class AbstractChronicleLogWriter implements ChronicleLogWriter {

        private final ExcerptAppenderProvider appenderProvider;
        private final Chronicle chronicle;

        public AbstractChronicleLogWriter(@NotNull Chronicle chronicle) throws IOException {
            this.chronicle = chronicle;
            this.appenderProvider = (chronicle instanceof VanillaChronicle)
                ? new VanillaExcerptAppenderProvider(chronicle)
                : new IndexedExcerptAppenderProvider(chronicle);
        }

        @Override
        public Chronicle getChronicle() {
            return this.chronicle;
        }

        @Override
        public void close() throws IOException {
            if (this.chronicle != null) {
                this.chronicle.close();
            }
        }

        protected ExcerptAppender getAppender() {
            return this.appenderProvider.get();
        }
    }

    // *************************************************************************
    //
    // *************************************************************************

    public static final class BinaryWriter extends AbstractChronicleLogWriter {

        public BinaryWriter(@NotNull Chronicle chronicle) throws IOException {
            super(chronicle);
        }

        private void logCommon(
                final ExcerptAppender appender,
                final ChronicleLogLevel level,
                final long timestamp,
                final String threadName,
                final String loggerName,
                final String message) {
            appender.writeByte(ChronicleLog.VERSION);
            appender.writeLong(timestamp);
            level.writeTo(appender);
            appender.writeUTFΔ(threadName);
            appender.writeUTFΔ(loggerName);
            appender.writeUTFΔ(message);
        }

        @Override
        public void write(
            final ChronicleLogLevel level,
            final long timestamp,
            final String threadName,
            final String loggerName,
            final String message) {
            final ExcerptAppender appender = getAppender();
            if (appender != null) {
                appender.startExcerpt();
                logCommon(appender, level, timestamp, threadName, loggerName, message);
                appender.finish();
            }
        }

        @Override
        public void write(
            final ChronicleLogLevel level,
            final long timestamp,
            final String threadName,
            final String loggerName,
            final String message,
            final Throwable throwable) {
            final ExcerptAppender appender = getAppender();
            if (appender != null) {
                appender.startExcerpt();

                logCommon(appender, level, timestamp, threadName, loggerName, message);

                appender.writeStopBit(0);

                if(throwable != null) {
                    appender.writeBoolean(true);
                    appender.writeObject(throwable);

                } else {
                    appender.writeBoolean(false);
                }

                appender.finish();
            }
        }

        @Override
        public void write(
            final ChronicleLogLevel level,
            final long timestamp,
            final String threadName,
            final String loggerName,
            final String message,
            final Throwable throwable,
            final Object arg1) {
            final ExcerptAppender appender = getAppender();
            if (appender != null) {
                appender.startExcerpt();

                logCommon(appender, level, timestamp, threadName, loggerName, message);

                appender.writeStopBit(1);
                appender.writeObject(arg1);

                if(throwable != null) {
                    appender.writeBoolean(true);
                    appender.writeObject(throwable);

                } else {
                    appender.writeBoolean(false);
                }

                appender.finish();
            }
        }

        @Override
        public void write(
            final ChronicleLogLevel level,
            final long timestamp,
            final String threadName,
            final String loggerName,
            final String message,
            final Throwable throwable,
            final Object arg1,
            final Object arg2) {
            final ExcerptAppender appender = getAppender();
            if (appender != null) {
                appender.startExcerpt();

                logCommon(appender, level, timestamp, threadName, loggerName, message);

                appender.writeStopBit(2);
                appender.writeObject(arg1);
                appender.writeObject(arg2);

                if(throwable != null) {
                    appender.writeBoolean(true);
                    appender.writeObject(throwable);

                } else {
                    appender.writeBoolean(false);
                }

                appender.finish();
            }
        }

        @Override
        public void write(
            final ChronicleLogLevel level,
            final long timestamp,
            final String threadName,
            final String loggerName,
            final String message,
            final Throwable throwable,
            final Object[] args) {
            final ExcerptAppender appender = getAppender();
            if (appender != null) {
                appender.startExcerpt();

                logCommon(appender, level, timestamp, threadName, loggerName, message);

                if(args != null) {
                    appender.writeStopBit(args.length);
                    for(int i=0;i <args.length; i++) {
                        appender.writeObject(args[i]);
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

    public static class TextWriter extends AbstractChronicleLogWriter {
        private final TimeStampFormatter timeStampFormatter;
        private final int stackTraceDepth;

        public TextWriter(
                @NotNull Chronicle chronicle,
                String dateFormat,
                Integer stackTraceDepth) throws IOException {

            super(chronicle);

            this.stackTraceDepth = stackTraceDepth != null ? stackTraceDepth : -1;
            this.timeStampFormatter = TimeStampFormatter.fromDateFormat(dateFormat);
        }

        @Override
        public void write(
            final ChronicleLogLevel level,
            long timestamp,
            final String threadName,
            final String loggerName,
            final String message) {
            write(level, timestamp, threadName, loggerName, message, null);
        }

        @Override
        public void write(
            final ChronicleLogLevel level,
            long timestamp,
            final String threadName,
            final String loggerName,
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
                appender.append(loggerName);
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
        public void write(
            ChronicleLogLevel level,
            long timestamp,
            String threadName,
            String loggerName,
            String message,
            Throwable throwable,
            Object arg1) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void write(
            ChronicleLogLevel level,
            long timestamp,
            String threadName,
            String loggerName,
            String message,
            Throwable throwable,
            Object arg1,
            Object arg2) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void write(
            ChronicleLogLevel level,
            long timestamp,
            String threadName,
            String loggerName,
            String message, Throwable throwable, Object[] args) {
        }
    }

    // *************************************************************************
    //
    // *************************************************************************

    public static class SimpleWriter implements ChronicleLogWriter {
        private final TimeStampFormatter timeStampFormatter;
        private final PrintStream stream;

        public SimpleWriter(
                @NotNull PrintStream stream) throws IOException {
            this.timeStampFormatter = TimeStampFormatter.fromDateFormat(ChronicleLog.DEFAULT_DATE_FORMAT);
            this.stream = stream;
        }

        @Override
        public Chronicle getChronicle() {
            return null;
        }

        @Override
        public void close() throws IOException {
        }

        @Override
        public void write(
            final ChronicleLogLevel level,
            long timestamp,
            final String threadName,
            final String loggerName,
            final String message,
            final Throwable throwable) {
            if (throwable == null) {
                stream.printf("%s|%s|%s|%s|%s\n",
                    timeStampFormatter.format(timestamp),
                    level.toString(),
                    threadName,
                    loggerName,
                    message);

            } else {
                stream.printf("%s|%s|%s|%s|%s|%s\n",
                    timeStampFormatter.format(timestamp),
                    level.toString(),
                    threadName,
                    loggerName,
                    message,
                    throwable.toString());
            }
        }

        @Override
        public void write(
            final ChronicleLogLevel level,
            final long timestamp,
            final String threadName,
            final String loggerName,
            final String message) {
            write(level, timestamp, threadName, loggerName, message, null);
        }

        @Override
        public void write(
            final ChronicleLogLevel level,
            final long timestamp,
            final String threadName,
            final String loggerName,
            final String message,
            final Throwable throwable,
            final Object arg1) {
            write(level, timestamp, threadName, loggerName, message, throwable);
        }

        @Override
        public void write(
            final ChronicleLogLevel level,
            final long timestamp,
            final String threadName,
            final String loggerName,
            final String message,
            final Throwable throwable,
            final Object arg1,
            final Object arg2) {
            write(level, timestamp, threadName, loggerName, message, throwable);
        }

        @Override
        public void write(
            final ChronicleLogLevel level,
            final long timestamp,
            final String threadName,
            final String loggerName,
            final String message,
            final Throwable throwable,
            final Object[] args) {
            write(level, timestamp, threadName, loggerName, message, throwable);
        }
    }

    // *************************************************************************
    //
    // *************************************************************************

    public static final class SynchronizedWriter implements ChronicleLogWriter, Closeable {
        private final ChronicleLogWriter writer;
        private final Object sync;

        public SynchronizedWriter(final ChronicleLogWriter writer) {
            this.writer = writer;
            this.sync = new Object();
        }

        public ChronicleLogWriter writer() {
            return writer;
        }

        @Override
        public Chronicle getChronicle() {
            return this.writer.getChronicle();
        }

        @Override
        public void close() throws IOException {
            synchronized(this.sync) {
                this.writer.close();
            }
        }

        @Override
        public void write(
            ChronicleLogLevel level,
            long timestamp,
            String threadName,
            String loggerName,
            String message) {
            synchronized (this.sync) {
                this.writer.write(level, timestamp, threadName, loggerName, message);
            }
        }

        @Override
        public void write(
            ChronicleLogLevel level,
            long timestamp,
            String threadName,
            String loggerName,
            String message,
            Throwable throwable) {
            synchronized (this.sync) {
                this.writer.write(level, timestamp, threadName, loggerName, message, throwable);
            }
        }

        @Override
        public void write(
            ChronicleLogLevel level,
            long timestamp,
            String threadName,
            String loggerName,
            String message,
            Throwable throwable,
            Object arg1) {
            synchronized (this.sync) {
                this.writer.write(level, timestamp, threadName, loggerName, message, throwable, arg1);
            }
        }

        @Override
        public void write(
            ChronicleLogLevel level,
            long timestamp,
            String threadName,
            String loggerName,
            String message,
            Throwable throwable,
            Object arg1,
            Object arg2) {
            synchronized (this.sync) {
                this.writer.write(level, timestamp, threadName, loggerName, message, throwable, arg1, arg2);
            }
        }

        @Override
        public void write(
            ChronicleLogLevel level,
            long timestamp,
            String threadName,
            String loggerName,
            String message,
            Throwable throwable,
            Object[] args) {
            synchronized (this.sync) {
                this.writer.write(level, timestamp, threadName, loggerName, message, throwable, args);
            }
        }
    }

    // *************************************************************************
    //
    // *************************************************************************

    public static ChronicleLogWriter binary(
        @NotNull Chronicle chronicle) throws IOException {

        return chronicle instanceof VanillaChronicle
            ? new BinaryWriter(chronicle)
            : new SynchronizedWriter(new BinaryWriter(chronicle));
    }

    public static ChronicleLogWriter binary(
            @NotNull ChronicleLogAppenderConfig cfg, String path) throws IOException {

        return binary(cfg.build(path));
    }

    // *************************************************************************
    //
    // *************************************************************************

    public static ChronicleLogWriter text(
            @NotNull Chronicle chronicle, String dateFormat, Integer stackTraceDepth) throws IOException {

        return chronicle instanceof VanillaChronicle
            ? new TextWriter(chronicle, dateFormat, stackTraceDepth)
            : new SynchronizedWriter(new TextWriter(chronicle, dateFormat, stackTraceDepth));
    }

    public static ChronicleLogWriter text(
            @NotNull ChronicleLogAppenderConfig cfg, String path, String dateFormat, Integer stackTraceDepth)
            throws IOException {
        return text(cfg.build(path), dateFormat, stackTraceDepth);
    }
}
