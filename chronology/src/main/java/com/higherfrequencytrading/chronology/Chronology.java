package com.higherfrequencytrading.chronology;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

public class Chronology {
    public static final String NEWLINE   = System.getProperty("line.separator");
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
}
