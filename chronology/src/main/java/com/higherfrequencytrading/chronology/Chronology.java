package com.higherfrequencytrading.chronology;

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

}
