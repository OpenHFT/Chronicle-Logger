/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.logger.jcl;

import net.openhft.chronicle.logger.ChronicleLogLevel;
import net.openhft.chronicle.logger.ChronicleLogWriter;
import org.apache.commons.logging.Log;

/**
 * Logger implementation for the Commons Logging API.
 *
 * <p>Messages are written using a {@link ChronicleLogWriter} and are only
 * emitted when their {@link ChronicleLogLevel} is high enough for this
 * logger.
 */
class ChronicleLogger implements Log {

    private final String name;
    private final ChronicleLogWriter appender;
    private final ChronicleLogLevel level;

    ChronicleLogger(final ChronicleLogWriter writer, final String name, final ChronicleLogLevel level) {
        this.appender = writer;
        this.name = name;
        this.level = level;
    }

    String name() {
        return this.name;
    }

    ChronicleLogWriter writer() {
        return this.appender;
    }

    ChronicleLogLevel level() {
        return this.level;
    }

    // DEBUG
    @Override
    public boolean isDebugEnabled() {
        return isLevelEnabled(ChronicleLogLevel.DEBUG);
    }

    /**
     * Write a DEBUG message when DEBUG is enabled.
     * The call is ignored if DEBUG is below this logger's level.
     */
    @Override
    public void debug(Object o) {
        append(ChronicleLogLevel.DEBUG, String.valueOf(o));
    }

    /**
     * Write a DEBUG message with an attached {@link Throwable}.
     * The call is ignored if DEBUG is below this logger's level.
     */
    @Override
    public void debug(Object o, Throwable throwable) {
        append(ChronicleLogLevel.DEBUG, String.valueOf(o), throwable);
    }

    // TRACE
    @Override
    public boolean isTraceEnabled() {
        return isLevelEnabled(ChronicleLogLevel.TRACE);
    }

    /**
     * Write a TRACE message when TRACE is enabled.
     * The call is ignored if TRACE is below this logger's level.
     */
    @Override
    public void trace(Object o) {
        append(ChronicleLogLevel.TRACE, String.valueOf(o));
    }

    /**
     * Write a TRACE message with an attached {@link Throwable}.
     * The call is ignored if TRACE is below this logger's level.
     */
    @Override
    public void trace(Object o, Throwable throwable) {
        append(ChronicleLogLevel.TRACE, String.valueOf(o), throwable);
    }

    // INFO
    @Override
    public boolean isInfoEnabled() {
        return isLevelEnabled(ChronicleLogLevel.INFO);
    }

    /**
     * Write an INFO message when INFO is enabled.
     * The call is ignored if INFO is below this logger's level.
     */
    @Override
    public void info(Object o) {
        append(ChronicleLogLevel.INFO, String.valueOf(o));
    }

    /**
     * Write an INFO message with an attached {@link Throwable}.
     * The call is ignored if INFO is below this logger's level.
     */
    @Override
    public void info(Object o, Throwable throwable) {
        append(ChronicleLogLevel.INFO, String.valueOf(o), throwable);
    }

    // WARN
    @Override
    public boolean isWarnEnabled() {
        return isLevelEnabled(ChronicleLogLevel.WARN);
    }

    /**
     * Write a WARN message when WARN is enabled.
     * The call is ignored if WARN is below this logger's level.
     */
    @Override
    public void warn(Object o) {
        append(ChronicleLogLevel.WARN, String.valueOf(o));
    }

    /**
     * Write a WARN message with an attached {@link Throwable}.
     * The call is ignored if WARN is below this logger's level.
     */
    @Override
    public void warn(Object o, Throwable throwable) {
        append(ChronicleLogLevel.WARN, String.valueOf(o), throwable);
    }

    // ERROR
    @Override
    public boolean isErrorEnabled() {
        return isLevelEnabled(ChronicleLogLevel.ERROR);
    }

    /**
     * Write an ERROR message when ERROR is enabled.
     * The call is ignored if ERROR is below this logger's level.
     */
    @Override
    public void error(Object o) {
        append(ChronicleLogLevel.ERROR, String.valueOf(o));
    }

    /**
     * Write an ERROR message with an attached {@link Throwable}.
     * The call is ignored if ERROR is below this logger's level.
     */
    @Override
    public void error(Object o, Throwable throwable) {
        append(ChronicleLogLevel.ERROR, String.valueOf(o), throwable);
    }

    // FATAL
    @Override
    public boolean isFatalEnabled() {
        return isLevelEnabled(ChronicleLogLevel.ERROR);
    }

    /**
     * Write a FATAL message when ERROR is enabled.
     * The call is ignored if ERROR is below this logger's level.
     */
    @Override
    public void fatal(Object o) {
        append(ChronicleLogLevel.ERROR, String.valueOf(o));
    }

    /**
     * Write a FATAL message with an attached {@link Throwable}.
     * The call is ignored if ERROR is below this logger's level.
     */
    @Override
    public void fatal(Object o, Throwable throwable) {
        append(ChronicleLogLevel.ERROR, String.valueOf(o), throwable);
    }

    // HELPERS
    private boolean isLevelEnabled(ChronicleLogLevel level) {
        return level.isHigherOrEqualTo(this.level);
    }

    private void append(ChronicleLogLevel level, String message) {
        if (level.isHigherOrEqualTo(this.level)) {
            this.appender.write(
                    level,
                    System.currentTimeMillis(),
                    Thread.currentThread().getName(),
                    this.name,
                    message,
                    null);
        }
    }

    private void append(ChronicleLogLevel level, String message, Throwable throwable) {
        if (level.isHigherOrEqualTo(this.level)) {
            this.appender.write(
                    level,
                    System.currentTimeMillis(),
                    Thread.currentThread().getName(),
                    this.name,
                    message,
                    throwable);
        }
    }
}
