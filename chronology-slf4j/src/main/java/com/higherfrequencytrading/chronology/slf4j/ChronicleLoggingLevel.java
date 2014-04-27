package com.higherfrequencytrading.chronology.slf4j;


import org.slf4j.spi.LocationAwareLogger;

/**
 * @author lburgazzoli
 */
public enum ChronicleLoggingLevel {
    TRACE(LocationAwareLogger.TRACE_INT, "TRACE"),
    DEBUG(LocationAwareLogger.DEBUG_INT, "DEBUG"),
    INFO(LocationAwareLogger.INFO_INT, "INFO"),
    WARN(LocationAwareLogger.WARN_INT, "WARN"),
    ERROR(LocationAwareLogger.ERROR_INT, "ERROR");

    private final int level;
    private final String traceName;

    /**
     * @param level
     * @param traceName
     */
    private ChronicleLoggingLevel(int level, String traceName) {
        this.level = level;
        this.traceName = traceName;
    }

    /**
     * @return
     */
    public int level() {
        return this.level;
    }

    /**
     * @return
     */
    public String traceName() {
        return this.traceName;
    }
}
