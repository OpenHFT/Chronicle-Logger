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

package net.openhft.chronicle.logger.slf4j;

import net.openhft.chronicle.Chronicle;
import net.openhft.chronicle.ExcerptAppender;
import net.openhft.chronicle.VanillaChronicle;
import net.openhft.chronicle.logger.ChronicleLog;
import net.openhft.chronicle.logger.ChronicleLogHelper;
import net.openhft.chronicle.logger.ChronicleLogLevel;
import net.openhft.chronicle.logger.TimeStampFormatter;
import org.slf4j.helpers.FormattingTuple;
import org.slf4j.helpers.MessageFormatter;

import java.io.Closeable;
import java.io.IOException;

public class ChronicleLogAppenders {

    // *************************************************************************
    //
    // *************************************************************************

    private static interface ExcerptAppenderProvider {
        public ExcerptAppender get();
    }

    private static class IndexedExcerptAppenderProvider implements ExcerptAppenderProvider {
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

    private static class VanillaExcerptAppenderProvider implements ExcerptAppenderProvider {
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

    private static abstract class AbstractChronicleLogWriter implements ChronicleLogAppender {

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

        private void logCommon(ExcerptAppender appender, ChronicleLogLevel level, String name, String message) {
            appender.writeByte(ChronicleLog.VERSION);
            ChronicleLog.Type.SLF4J.writeTo(appender);
            appender.writeLong(System.currentTimeMillis());
            level.writeTo(appender);
            appender.writeUTF(Thread.currentThread().getName());
            appender.writeUTF(name);
            appender.writeUTF(message);
        }

        @Override
        public void log(ChronicleLogLevel level, String name, String message, Object arg1) {
            final ExcerptAppender appender = getAppender();
            if(appender != null) {
                appender.startExcerpt();

                logCommon(appender, level, name, message);

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
        public void log(ChronicleLogLevel level, String name, String message, Object arg1, Object arg2) {
            final ExcerptAppender appender = getAppender();
            if(appender != null) {
                appender.startExcerpt();

                logCommon(appender, level, name, message);

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
        public void log(ChronicleLogLevel level, String name, String message, Object... args) {
            final ExcerptAppender appender = getAppender();
            if(appender != null) {
                appender.startExcerpt();

                logCommon(appender, level, name, message);

                if (args.length > 0 && args[args.length - 1] instanceof Throwable) {
                    appender.writeStopBit(args.length - 1);
                    for (int i = 0; i < args.length - 1; i++) {
                        appender.writeObject(args[i]);
                    }

                    appender.writeBoolean(true);
                    appender.writeObject(args[args.length - 1]);
                } else {
                    appender.writeStopBit(args.length);
                    for (Object arg : args) {
                        appender.writeObject(arg);
                    }

                    appender.writeBoolean(false);
                }

                appender.finish();
            }
        }

        @Override
        public void log(ChronicleLogLevel level, String name, String message, Throwable throwable) {
            final ExcerptAppender appender = getAppender();
            if(appender != null) {
                appender.startExcerpt();

                logCommon(appender, level, name, message);

                appender.writeStopBit(0);
                appender.writeBoolean(true);
                appender.writeObject(throwable);

                appender.finish();
            }
        }
    }

    // *************************************************************************
    //
    // *************************************************************************

    public static final class BinaryFormattingWriter extends AbstractChronicleLogWriter {

        public BinaryFormattingWriter(Chronicle chronicle) throws IOException {
            super(chronicle);
        }

        private Throwable logCommon(ExcerptAppender appender, ChronicleLogLevel level, String name, FormattingTuple tp) {
            appender.writeByte(ChronicleLog.VERSION);
            ChronicleLog.Type.SLF4J.writeTo(appender);
            appender.writeLong(System.currentTimeMillis());
            level.writeTo(appender);
            appender.writeUTF(Thread.currentThread().getName());
            appender.writeUTF(name);
            appender.writeUTF(tp.getMessage());
            appender.writeStopBit(0);

            return tp.getThrowable();
        }

        @Override
        public void log(ChronicleLogLevel level, String name, String message, Object arg1) {
            final ExcerptAppender appender = getAppender();
            if(appender != null) {
                appender.startExcerpt();

                final FormattingTuple ft = MessageFormatter.format(message, arg1);
                final Throwable thw = logCommon(appender,level,name,ft);

                if (thw == null) {
                    appender.writeBoolean(false);
                } else {
                    appender.writeBoolean(true);
                    appender.writeObject(thw);
                }

                appender.finish();
            }
        }

        @Override
        public void log(ChronicleLogLevel level, String name, String message, Object arg1, Object arg2) {
            final ExcerptAppender appender = getAppender();
            if(appender != null) {
                appender.startExcerpt();

                final FormattingTuple ft = MessageFormatter.format(message, arg1, arg2);
                final Throwable thw = logCommon(appender,level,name,ft);

                if (thw == null) {
                    appender.writeBoolean(false);
                } else {
                    appender.writeBoolean(true);
                    appender.writeObject(thw);
                }

                appender.finish();
            }
        }

        @Override
        public void log(ChronicleLogLevel level, String name, String message, Object... args) {
            final ExcerptAppender appender = getAppender();
            if(appender != null) {
                appender.startExcerpt();

                final FormattingTuple ft = MessageFormatter.arrayFormat(message, args);
                final Throwable thw = logCommon(appender,level,name,ft);

                if (thw == null) {
                    appender.writeBoolean(false);
                } else {
                    appender.writeBoolean(true);
                    appender.writeObject(thw);
                }

                appender.finish();
            }
        }

        @Override
        public void log(ChronicleLogLevel level, String name, String message, Throwable throwable) {
            final ExcerptAppender appender = getAppender();
            if(appender != null) {
                appender.startExcerpt();

                final FormattingTuple ft = MessageFormatter.format(message, throwable);
                final Throwable thw = logCommon(appender,level,name,ft);

                if (thw == null) {
                    appender.writeBoolean(false);
                } else {
                    appender.writeBoolean(true);
                    appender.writeObject(thw);
                }

                appender.finish();
            }
        }
    }

    // *************************************************************************
    //
    // *************************************************************************

    public static final class TextWriter extends AbstractChronicleLogWriter {

        private final TimeStampFormatter timeStampFormatter;
        private final int stackTraceDepth;

        public TextWriter(Chronicle chronicle, String dateFormat, Integer stackTraceDepth) throws IOException {
            super(chronicle);

            this.stackTraceDepth = stackTraceDepth != null ? stackTraceDepth : -1;
            this.timeStampFormatter = TimeStampFormatter.fromDateFormat(
                dateFormat != null
                    ? dateFormat
                    : ChronicleLoggingConfig.DEFAULT_DATE_FORMAT
            );
        }

        private Throwable logCommon(ExcerptAppender appender, ChronicleLogLevel level, String name, FormattingTuple tp) {
            timeStampFormatter.format(System.currentTimeMillis(), appender);
            appender.append('|');
            level.printTo(appender);
            appender.append('|');
            appender.append(Thread.currentThread().getName());
            appender.append('|');
            appender.append(name);
            appender.append('|');
            appender.append(tp.getMessage());

            return tp.getThrowable();
        }

        @Override
        public void log(ChronicleLogLevel level, String name, String message, Object arg1) {
            final ExcerptAppender appender = getAppender();
            if (appender != null) {
                appender.startExcerpt();

                final FormattingTuple ft = MessageFormatter.format(message, arg1);
                final Throwable thw = logCommon(appender,level,name,ft);

                if (thw != null) {
                    appender.append(" - ");
                    ChronicleLogHelper.appendStackTraceAsString(
                        appender,
                        thw,
                        ChronicleLog.COMMA,
                        this.stackTraceDepth);
                }

                appender.append('\n');
                appender.finish();
            }
        }

        @Override
        public void log(ChronicleLogLevel level, String name, String message, Object arg1, Object arg2) {
            final ExcerptAppender appender = getAppender();
            if (appender != null) {
                appender.startExcerpt();

                final FormattingTuple ft = MessageFormatter.format(message, arg1, arg2);
                final Throwable thw = logCommon(appender,level,name,ft);

                if (thw != null) {
                    appender.append(" - ");
                    ChronicleLogHelper.appendStackTraceAsString(
                        appender,
                        thw,
                        ChronicleLog.COMMA,
                        this.stackTraceDepth);
                }

                appender.append('\n');
                appender.finish();
            }
        }

        @Override
        public void log(ChronicleLogLevel level, String name, String message, Object... args) {
            final ExcerptAppender appender = getAppender();
            if (appender != null) {
                appender.startExcerpt();

                final FormattingTuple ft = MessageFormatter.arrayFormat(message, args);
                final Throwable thw = logCommon(appender,level,name,ft);

                if (thw != null) {
                    appender.append(" - ");
                    ChronicleLogHelper.appendStackTraceAsString(
                        appender,
                        thw,
                        ChronicleLog.COMMA,
                        this.stackTraceDepth);
                }

                appender.append('\n');
                appender.finish();
            }
        }

        @Override
        public void log(ChronicleLogLevel level, String name, String message, Throwable throwable) {
            final ExcerptAppender appender = getAppender();
            if (appender != null) {
                appender.startExcerpt();

                final FormattingTuple ft = MessageFormatter.format(message, throwable);
                final Throwable thw = logCommon(appender,level,name,ft);

                if (thw != null) {
                    appender.append(" - ");
                    ChronicleLogHelper.appendStackTraceAsString(
                        appender,
                        thw,
                        ChronicleLog.COMMA,
                        this.stackTraceDepth);
                }

                appender.append('\n');
                appender.finish();
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
        public void log(ChronicleLogLevel level, String name, String message, Object arg1) {
            synchronized (this.sync) {
                this.writer.log(level, name, message, arg1);
            }
        }

        @Override
        public void log(ChronicleLogLevel level, String name, String message, Object arg1, Object arg2) {
            synchronized (this.sync) {
                this.writer.log(level, name, message, arg1, arg2);
            }
        }

        @Override
        public void log(ChronicleLogLevel level, String name, String message, Object... args) {
            synchronized (this.sync) {
                this.writer.log(level, name, message, args);
            }
        }

        @Override
        public void log(ChronicleLogLevel level, String name, String message, Throwable throwable) {
            synchronized (this.sync) {
                this.writer.log(level, name, message, throwable);
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
