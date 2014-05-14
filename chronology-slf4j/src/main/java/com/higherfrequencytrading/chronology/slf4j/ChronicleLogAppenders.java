package com.higherfrequencytrading.chronology.slf4j;

import com.higherfrequencytrading.chronology.*;
import net.openhft.chronicle.Chronicle;
import net.openhft.chronicle.ExcerptAppender;
import org.slf4j.helpers.FormattingTuple;
import org.slf4j.helpers.MessageFormatter;

import java.io.Closeable;
import java.io.IOException;

public class ChronicleLogAppenders {

    // *************************************************************************
    //
    // *************************************************************************

    private static abstract class AbstractChronicleLogWriter implements ChronicleLogAppender {

        protected final ExcerptAppender appender;
        private final Chronicle chronicle;

        public AbstractChronicleLogWriter(Chronicle chronicle) throws IOException {
            this.chronicle = chronicle;
            this.appender = this.chronicle.createAppender();
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
    }

    // *************************************************************************
    //
    // *************************************************************************

    /**
     *
     */
    public static final class BinaryWriter extends AbstractChronicleLogWriter {

        public BinaryWriter(Chronicle chronicle) throws IOException {
            super(chronicle);
        }

        /**
         * This is the internal implementation for logging slf4j messages.
         *
         * @param level   One of the LOG_LEVEL_XXX constants defining the slf4j level
         * @param name    The logger name
         * @param message The message
         * @param message The message arguments
         */
        @Override
        public void log(ChronologyLogLevel level, String name, String message, Object... args) {
            this.appender.startExcerpt();
            this.appender.writeByte(Chronology.VERSION);
            this.appender.writeByte(Chronology.TYPE_SLF4J);
            this.appender.writeLong(System.currentTimeMillis());
            this.appender.writeByte(level.levelInt);
            this.appender.writeUTF(Thread.currentThread().getName());
            this.appender.writeUTF(name);
            this.appender.writeUTF(message);

            if(args.length > 0 && args[args.length - 1] instanceof Throwable) {
                this.appender.writeInt(args.length - 1);
                for (int i=0;i<args.length - 1; i++) {
                    this.appender.writeObject(args[i]);
                }

                this.appender.writeBoolean(true);
                this.appender.writeObject(args[args.length - 1]);
            } else {
                this.appender.writeInt(args.length);
                for (int i=0;i<args.length; i++) {
                    this.appender.writeObject(args[i]);
                }

                this.appender.writeBoolean(false);
            }

            this.appender.finish();
        }
    }

    /**
     *
     */
    public static final class BinaryFormattingWriter extends AbstractChronicleLogWriter {

        public BinaryFormattingWriter(Chronicle chronicle) throws IOException {
            super(chronicle);
        }

        /**
         * This is the internal implementation for logging slf4j messages.
         *
         * @param level   One of the LOG_LEVEL_XXX constants defining the slf4j level
         * @param name    The logger name
         * @param message The message
         * @param message The message arguments
         */
        @Override
        public void log(ChronologyLogLevel level, String name, String message, Object... args) {
            final FormattingTuple tp = MessageFormatter.arrayFormat(message,args);

            this.appender.startExcerpt();
            this.appender.writeByte(Chronology.VERSION);
            this.appender.writeByte(Chronology.TYPE_SLF4J);
            this.appender.writeLong(System.currentTimeMillis());
            this.appender.writeByte(level.levelInt);
            this.appender.writeUTF(Thread.currentThread().getName());
            this.appender.writeUTF(name);
            this.appender.writeUTF(tp.getMessage());
            this.appender.writeInt(0);

            if(tp.getThrowable() != null) {
                this.appender.writeBoolean(true);
                this.appender.writeObject(tp.getThrowable());
            } else {
                this.appender.writeBoolean(false);
            }

            this.appender.finish();
        }
    }

    // *************************************************************************
    //
    // *************************************************************************

    /**
     *
     */
    public static final class TextWriter extends AbstractChronicleLogWriter {

        private final TimeStampFormatter timeStampFormatter;
        private final int stackTraceDepth;

        /**
         * c-tor
         *
         * @param chronicle
         * @param dateFormat
         * @throws IOException
         */
        public TextWriter(Chronicle chronicle, String dateFormat, Integer stackTraceDepth) throws IOException {
            super(chronicle);

            this.stackTraceDepth = stackTraceDepth != null ? stackTraceDepth : -1;
            this.timeStampFormatter = TimeStampFormatter.fromDateFormat(
                dateFormat != null
                    ? dateFormat
                    : ChronicleLoggingConfig.DEFAULT_DATE_FORMAT
            );
        }

        /**
         * This is the internal implementation for logging slf4j messages.
         *
         * @param level   One of the LOG_LEVEL_XXX constants defining the slf4j level
         * @param name    The logger name
         * @param message The message
         * @param message The message arguments
         */
        @Override
        public void log(ChronologyLogLevel level, String name, String message, Object... args) {
            final FormattingTuple tp = MessageFormatter.arrayFormat(message, args);

            appender.startExcerpt();
            timeStampFormatter.format(System.currentTimeMillis(), appender);
            appender.append('|');
            appender.append(level.levelStr);
            appender.append('|');
            appender.append(Thread.currentThread().getName());
            appender.append('|');
            appender.append(name);
            appender.append('|');
            appender.append(tp.getMessage());

            if(tp.getThrowable() != null) {
                appender.append(" - ");
                ChronologyLogHelper.appendStackTraceAsString(
                    appender,
                    tp.getThrowable(),
                    Chronology.COMMA,
                    this.stackTraceDepth);
            }

            appender.append('\n');
            appender.finish();
        }
    }

    // *************************************************************************
    //
    // *************************************************************************

    /**
     *
     */
    public static final class SynchronizedWriter implements ChronicleLogAppender, Closeable {
        private final ChronicleLogAppender writer;
        private final Object sync;

        /**
         * @param writer
         */
        public SynchronizedWriter(final ChronicleLogAppender writer) {
            this.writer = writer;
            this.sync = new Object();
        }

        @Override
        public Chronicle getChronicle() {
            return this.writer.getChronicle();
        }

        @Override
        public void log(ChronologyLogLevel level, String name, String message, Object... args) {
            synchronized (this.sync) {
                this.writer.log(level, name, message, args);
            }

        }

        @Override
        public void close() throws IOException {
            this.writer.close();
        }
    }
}
