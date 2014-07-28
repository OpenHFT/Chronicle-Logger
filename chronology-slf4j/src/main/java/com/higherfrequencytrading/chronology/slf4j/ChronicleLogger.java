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
        append(ChronologyLogLevel.TRACE,  s);
    }

    @Override
    public void trace(String s, Object o) {
        append(ChronologyLogLevel.TRACE,  s, o);
    }

    @Override
    public void trace(String s, Object o1, Object o2) {
        append(ChronologyLogLevel.TRACE,  s, o1, o2);
    }

    @Override
    public void trace(String s, Object... objects) {
        append(ChronologyLogLevel.TRACE,  s, objects);
    }

    @Override
    public void trace(String s, Throwable throwable) {
        append(ChronologyLogLevel.TRACE,  s, throwable);
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
        append(ChronologyLogLevel.DEBUG,  s);
    }

    @Override
    public void debug(String s, Object o) {
        append(ChronologyLogLevel.DEBUG,  s, o);
    }

    @Override
    public void debug(String s, Object o1, Object o2) {
        append(ChronologyLogLevel.DEBUG,  s, o1, o2);
    }

    @Override
    public void debug(String s, Object... objects) {
        append(ChronologyLogLevel.DEBUG,  s, objects);
    }

    @Override
    public void debug(String s, Throwable throwable) {
        append(ChronologyLogLevel.DEBUG,  s, throwable);
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
        append(ChronologyLogLevel.INFO,  s);
    }

    @Override
    public void info(String s, Object o) {
        append(ChronologyLogLevel.INFO,  s, o);
    }

    @Override
    public void info(String s, Object o1, Object o2) {
        append(ChronologyLogLevel.INFO,  s, o1, o2);
    }

    @Override
    public void info(String s, Object... objects) {
        append(ChronologyLogLevel.INFO,  s, objects);
    }

    @Override
    public void info(String s, Throwable throwable) {
        append(ChronologyLogLevel.INFO,  s, throwable);
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
        append(ChronologyLogLevel.WARN,  s);
    }

    @Override
    public void warn(String s, Object o) {
        append(ChronologyLogLevel.WARN,  s, o);
    }

    @Override
    public void warn(String s, Object o1, Object o2) {
        append(ChronologyLogLevel.WARN,  s, o1, o2);
    }

    @Override
    public void warn(String s, Object... objects) {
        append(ChronologyLogLevel.WARN,  s, objects);
    }

    @Override
    public void warn(String s, Throwable throwable) {
        append(ChronologyLogLevel.WARN,  s, throwable);
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
        append(ChronologyLogLevel.ERROR, s);
    }

    @Override
    public void error(String s, Object o) {
        append(ChronologyLogLevel.ERROR, s, o);
    }

    @Override
    public void error(String s, Object o1, Object o2) {
        append(ChronologyLogLevel.ERROR, s, o1, o2);
    }

    @Override
    public void error(String s, Object... objects) {
        append(ChronologyLogLevel.ERROR, s, objects);
    }

    @Override
    public void error(String s, Throwable throwable) {
        append(ChronologyLogLevel.ERROR, s, throwable);
    }

    // *************************************************************************
    // HELPERS
    // *************************************************************************

    private boolean isLevelEnabled(ChronologyLogLevel level) {
        return level.isHigherOrEqualTo(this.level);
    }

    private void append(ChronologyLogLevel level, String message, Object arg1) {
        if(level.isHigherOrEqualTo(this.level)) {
            this.writer.log(level, this.name, message, arg1);
        }
    }

    private void append(ChronologyLogLevel level, String message, Object arg1, Object arg2) {
        if(level.isHigherOrEqualTo(this.level)) {
            this.writer.log(level, this.name, message, arg1, arg2);
        }
    }

    private void append(ChronologyLogLevel level, String message, Object... args) {
        if(level.isHigherOrEqualTo(this.level)) {
            this.writer.log(level, this.name, message, args);
        }
    }

    private void append(ChronologyLogLevel level, String message, Throwable throwable) {
        if(level.isHigherOrEqualTo(this.level)) {
            this.writer.log(level, this.name, message, throwable);
        }
    }
}
