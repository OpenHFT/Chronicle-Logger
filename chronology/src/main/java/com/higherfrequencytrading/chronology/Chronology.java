package com.higherfrequencytrading.chronology;

import net.openhft.lang.io.RandomDataInput;
import net.openhft.lang.io.RandomDataOutput;


public final class Chronology {
    public static final String NEWLINE   = System.getProperty("line.separator");
    public static final String COMMA     = ", ";
    public static final String STR_FALSE = "false";
    public static final String STR_TRUE  = "true";

    public static final byte VERSION      = 1;

    public enum Type {
        UNKNOWN, SLF4J, LOGBACK, LOG4J_1, LOG4J_2;

        private static final Type[] VALUES = values();

        public void writeTo(RandomDataOutput out) {
            out.writeByte(ordinal());
        }

        public static Type read(RandomDataInput in) {
            return VALUES[in.readByte()];
        }
    }

    public static final String          DEFAULT_DATE_FORMAT       = "yyyy.MM.dd-HH:mm:ss.SSS";

    private Chronology() {}
}
