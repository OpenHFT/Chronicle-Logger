package com.higherfrequencytrading.chronology;

import net.openhft.chronicle.ExcerptAppender;
import net.openhft.chronicle.ExcerptTailer;
import net.openhft.chronicle.VanillaChronicle;
import net.openhft.chronicle.VanillaChronicleConfig;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

public class Chronology {
    public static final String NEWLINE   = System.getProperty("line.separator");
    public static final String TMPDIR    = System.getProperty("java.io.tmpdir");
    public static final String COMMA     = ", ";
    public static final String STR_FALSE = "false";
    public static final String STR_TRUE  = "true";

    public static final byte VERSION      = 1;

    public static final byte TYPE_SLF4J   = 1;
    public static final byte TYPE_LOGBACK = 2;
    public static final byte TYPE_LOG4J_1 = 3;
    public static final byte TYPE_LOG4J_2 = 4;

    public static final String          DEFAULT_DATE_FORMAT       = "yyyy.MM.dd-HH:mm:ss.SSS";
    public static final DateFormatCache DEFAULT_DATE_FORMAT_CACHE = new DateFormatCache();

    // *************************************************************************
    //
    // *************************************************************************

    public static final class DateFormatCache extends ThreadLocal<DateFormat> {
        private final String format;

        public DateFormatCache() {
            this(DEFAULT_DATE_FORMAT);
        }

        public DateFormatCache(String format) {
            this.format = format;
        }

        @Override
        protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat(this.format);
        }
    }

    // *************************************************************************
    //
    // *************************************************************************

    public static void warmup() {
        //noinspection UnusedDeclaration needed to laod class.
        boolean done = Warmup.DONE;
    }

    // *************************************************************************
    //
    // *************************************************************************

    private static class Warmup {
        public  static final boolean DONE;
        private static final int WARMUP_ITER = 1000;

        static {
            VanillaChronicleConfig cc = new VanillaChronicleConfig();
            cc.dataBlockSize(64);
            cc.indexBlockSize(64);

            String basePath = TMPDIR + "/warmup-" + Math.random();
            //ChronicleTools.deleteDirOnExit(basePath);

            try {
                final VanillaChronicle chronicle = new VanillaChronicle(basePath, cc);
                final ExcerptAppender  appender = chronicle.createAppender();
                final ExcerptTailer    tailer   = chronicle.createTailer();

                for (int i = 0; i < WARMUP_ITER; i++) {
                    appender.startExcerpt();
                    appender.writeInt(i);
                    appender.finish();
                    boolean b = tailer.nextIndex() || tailer.nextIndex();
                    tailer.readInt();
                    tailer.finish();
                }

                chronicle.close();
                chronicle.clear();

                System.gc();
                DONE = true;
            } catch (IOException e) {
                throw new AssertionError();
            }
        }
    }

}
