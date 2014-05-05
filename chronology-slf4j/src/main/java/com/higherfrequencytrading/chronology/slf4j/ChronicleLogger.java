package com.higherfrequencytrading.chronology.slf4j;

import com.higherfrequencytrading.chronology.ChronologyLogLevel;
import org.slf4j.helpers.MarkerIgnoringBase;

/**
 *
 */
public class ChronicleLogger extends MarkerIgnoringBase {

    private final ChronicleLogAppender writer;
    private final ChronologyLogLevel level;

    /**
     * c-tor
     *
     * @param writer
     * @param name
     */
    public ChronicleLogger(final ChronicleLogAppender writer, final String name) {
        this(writer, name, ChronologyLogLevel.INFO);
    }

    /**
     * c-tor
     *
     * @param writer
     * @param name
     * @param level
     */
    public ChronicleLogger(final ChronicleLogAppender writer, final String name, final ChronologyLogLevel level) {
        this.writer = writer;
        this.name = name;
        this.level = level;
    }

    // *************************************************************************
    //
    // *************************************************************************

    /**
     * @return
     */
    public ChronologyLogLevel getLevel() {
        return this.level;
    }

    /**
     * @return
     */
    public ChronicleLogAppender getWriter() {
        return this.writer;
    }

    // *************************************************************************
    // TRACE
    // *************************************************************************

    @Override
    public boolean isTraceEnabled() {
        return isLevelEnabled(ChronologyLogLevel.TRACE);
    }

    @Override
    public void trace(String s) {
        if (isLevelEnabled(ChronologyLogLevel.TRACE)) {
            this.writer.log(ChronologyLogLevel.TRACE, this.name, s);
        }
    }

    @Override
    public void trace(String s, Object o) {
        if (isLevelEnabled(ChronologyLogLevel.TRACE)) {
            this.writer.log(ChronologyLogLevel.TRACE, this.name, s, o);
        }
    }

    @Override
    public void trace(String s, Object o1, Object o2) {
        if (isLevelEnabled(ChronologyLogLevel.TRACE)) {
            this.writer.log(ChronologyLogLevel.TRACE, this.name, s, o1, o2);
        }
    }

    @Override
    public void trace(String s, Object... objects) {
        if (isLevelEnabled(ChronologyLogLevel.TRACE)) {
            this.writer.log(ChronologyLogLevel.TRACE, this.name, s, objects);
        }
    }

    @Override
    public void trace(String s, Throwable throwable) {
        if (isLevelEnabled(ChronologyLogLevel.TRACE)) {
            this.writer.log(ChronologyLogLevel.TRACE, this.name, s, throwable);
        }
    }

    // *************************************************************************
    // DEBUG
    // *************************************************************************

    @Override
    public boolean isDebugEnabled() {
        return isLevelEnabled(ChronologyLogLevel.DEBUG);
    }

    @Override
    public void debug(String s) {
        if (isLevelEnabled(ChronologyLogLevel.DEBUG)) {
            this.writer.log(ChronologyLogLevel.DEBUG, this.name, s);
        }
    }

    @Override
    public void debug(String s, Object o) {
        if (isLevelEnabled(ChronologyLogLevel.DEBUG)) {
            this.writer.log(ChronologyLogLevel.DEBUG, this.name, s, o);
        }
    }

    @Override
    public void debug(String s, Object o1, Object o2) {
        if (isLevelEnabled(ChronologyLogLevel.DEBUG)) {
            this.writer.log(ChronologyLogLevel.DEBUG, this.name, s, o1, o2);
        }
    }

    @Override
    public void debug(String s, Object... objects) {
        if (isLevelEnabled(ChronologyLogLevel.DEBUG)) {
            this.writer.log(ChronologyLogLevel.DEBUG, this.name, s, objects);
        }
    }

    @Override
    public void debug(String s, Throwable throwable) {
        if (isLevelEnabled(ChronologyLogLevel.DEBUG)) {
            this.writer.log(ChronologyLogLevel.DEBUG, this.name, s, throwable);
        }
    }

    // *************************************************************************
    // INFO
    // *************************************************************************

    @Override
    public boolean isInfoEnabled() {
        return isLevelEnabled(ChronologyLogLevel.INFO);
    }

    @Override
    public void info(String s) {
        if (isLevelEnabled(ChronologyLogLevel.INFO)) {
            this.writer.log(ChronologyLogLevel.INFO, this.name, s);
        }
    }

    @Override
    public void info(String s, Object o) {
        if (isLevelEnabled(ChronologyLogLevel.INFO)) {
            this.writer.log(ChronologyLogLevel.INFO, this.name, s, o);
        }
    }

    @Override
    public void info(String s, Object o1, Object o2) {
        if (isLevelEnabled(ChronologyLogLevel.INFO)) {
            this.writer.log(ChronologyLogLevel.INFO, this.name, s, o1, o2);
        }
    }

    @Override
    public void info(String s, Object... objects) {
        if (isLevelEnabled(ChronologyLogLevel.INFO)) {
            this.writer.log(ChronologyLogLevel.INFO, this.name, s, objects);
        }
    }

    @Override
    public void info(String s, Throwable throwable) {
        if (isLevelEnabled(ChronologyLogLevel.INFO)) {
            this.writer.log(ChronologyLogLevel.INFO, this.name, s, throwable);
        }
    }

    // *************************************************************************
    // WARN
    // *************************************************************************

    @Override
    public boolean isWarnEnabled() {
        return isLevelEnabled(ChronologyLogLevel.WARN);
    }

    @Override
    public void warn(String s) {
        if (isLevelEnabled(ChronologyLogLevel.WARN)) {
            this.writer.log(ChronologyLogLevel.WARN, this.name, s);
        }
    }

    @Override
    public void warn(String s, Object o) {
        if (isLevelEnabled(ChronologyLogLevel.WARN)) {
            this.writer.log(ChronologyLogLevel.WARN, this.name, s, o);
        }
    }

    @Override
    public void warn(String s, Object o1, Object o2) {
        if (isLevelEnabled(ChronologyLogLevel.WARN)) {
            this.writer.log(ChronologyLogLevel.WARN, this.name, s, o1, o2);
        }
    }

    @Override
    public void warn(String s, Object... objects) {
        if (isLevelEnabled(ChronologyLogLevel.WARN)) {
            this.writer.log(ChronologyLogLevel.WARN, this.name, s, objects);
        }
    }

    @Override
    public void warn(String s, Throwable throwable) {
        if (isLevelEnabled(ChronologyLogLevel.WARN)) {
            this.writer.log(ChronologyLogLevel.WARN, this.name, s, throwable);
        }
    }

    // *************************************************************************
    // ERROR
    // *************************************************************************

    @Override
    public boolean isErrorEnabled() {
        return isLevelEnabled(ChronologyLogLevel.ERROR);
    }

    @Override
    public void error(String s) {
        if (isLevelEnabled(ChronologyLogLevel.ERROR)) {
            this.writer.log(ChronologyLogLevel.ERROR, this.name, s);
        }
    }

    @Override
    public void error(String s, Object o) {
        if (isLevelEnabled(ChronologyLogLevel.ERROR)) {
            this.writer.log(ChronologyLogLevel.ERROR, this.name, s, o);
        }
    }

    @Override
    public void error(String s, Object o1, Object o2) {
        if (isLevelEnabled(ChronologyLogLevel.ERROR)) {
            this.writer.log(ChronologyLogLevel.ERROR, this.name, s, o1, o2);
        }
    }

    @Override
    public void error(String s, Object... objects) {
        if (isLevelEnabled(ChronologyLogLevel.ERROR)) {
            this.writer.log(ChronologyLogLevel.ERROR, this.name, s, objects);
        }
    }

    @Override
    public void error(String s, Throwable throwable) {
        if (isLevelEnabled(ChronologyLogLevel.ERROR)) {
            this.writer.log(ChronologyLogLevel.ERROR, this.name, s, throwable);
        }
    }

    // *************************************************************************
    // HELPERS
    // *************************************************************************

    /**
     * Is the given slf4j level enabled?
     *
     * @param level is this level enabled?
     */
    private boolean isLevelEnabled(final ChronologyLogLevel level) {
        // slf4j level are numerically ordered so can use simple numeric
        // comparison
        return (level.levelInt >= this.level.levelInt);
    }
}
