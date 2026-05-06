/*
 * Copyright 2013-2026 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.logger.slf4j;

import net.openhft.chronicle.logger.ChronicleLogLevel;
import net.openhft.chronicle.logger.ChronicleLogWriter;

/**
 * SLF4J {@link org.slf4j.Logger} implementation backed by a
 * {@link ChronicleLogWriter}.
 *
 * <p>The writer is obtained from {@link ChronicleLoggerFactory} and
 * messages are forwarded when the requested {@link ChronicleLogLevel}
 * is enabled.</p>
 */
// SLF4J 1.x base retained for backwards compatibility with existing deployments.
// The type is deprecated in SLF4J 2.x but still supported, so we suppress the
// deprecation warning here to keep the binder usable without breaking callers.
@SuppressWarnings({"serial", "deprecation"})
public final class ChronicleLogger extends org.slf4j.helpers.MarkerIgnoringBase {

    private static final long serialVersionUID = 1L;
    protected final ChronicleLogLevel level;
    private final ChronicleLogWriter writer;

    /**
     * Package-private constructor used by the factory.
     *
     * @param writer backing Chronicle writer
     * @param name   logger name
     * @param level  minimum log level
     */
    ChronicleLogger(final ChronicleLogWriter writer, final String name, final ChronicleLogLevel level) {
        this.writer = writer;
        this.name = name;
        this.level = level;
    }

    ChronicleLogLevel getLevel() {
        return this.level;
    }

    ChronicleLogWriter getWriter() {
        return this.writer;
    }

    // TRACE
    /**
     * Tests whether {@code TRACE} level messages will be logged.
     *
     * @return {@code true} if the level is enabled
     */
    @Override
    public boolean isTraceEnabled() {
        return isLevelEnabled(ChronicleLogLevel.TRACE);
    }

    /**
     * Log a message at {@code TRACE} level.
     *
     * @param s message text
     */
    @Override
    public void trace(String s) {
        append(ChronicleLogLevel.TRACE, s);
    }

    /**
     * Log a message with one argument or {@link Throwable}.
     */
    @Override
    public void trace(String s, Object o1) {
        if (o1 instanceof Throwable) {
            append(ChronicleLogLevel.TRACE, s, (Throwable) o1);

        } else {
            append(ChronicleLogLevel.TRACE, s, null, o1);
        }
    }

    /**
     * Log a message with two arguments or one argument and a
     * {@link Throwable}.
     */
    @Override
    public void trace(String s, Object o1, Object o2) {
        if (o2 instanceof Throwable) {
            append(ChronicleLogLevel.TRACE, s, (Throwable) o2, o1);

        } else {
            append(ChronicleLogLevel.TRACE, s, null, o1, o2);
        }
    }

    /**
     * Log a message with optional arguments.
     */
    @Override
    public void trace(String s, Object... objects) {
        append(ChronicleLogLevel.TRACE, s, null, objects);
    }

    /**
     * Log a message with a {@link Throwable}.
     */
    @Override
    public void trace(String s, Throwable throwable) {
        append(ChronicleLogLevel.TRACE, s, throwable);
    }

    // DEBUG
    /**
     * Tests whether {@code DEBUG} level messages will be logged.
     *
     * @return {@code true} if the level is enabled
     */
    @Override
    public boolean isDebugEnabled() {
        return isLevelEnabled(ChronicleLogLevel.DEBUG);
    }

    /**
     * Log a message at {@code DEBUG} level.
     *
     * @param s message text
     */
    @Override
    public void debug(String s) {
        append(ChronicleLogLevel.DEBUG, s);
    }

    /**
     * Log a message with one argument or {@link Throwable}.
     */
    @Override
    public void debug(String s, Object o1) {
        if (o1 instanceof Throwable) {
            append(ChronicleLogLevel.DEBUG, s, (Throwable) o1);

        } else {
            append(ChronicleLogLevel.DEBUG, s, null, o1);
        }
    }

    /**
     * Log a message with two arguments or one argument and a
     * {@link Throwable}.
     */
    @Override
    public void debug(String s, Object o1, Object o2) {
        if (o2 instanceof Throwable) {
            append(ChronicleLogLevel.DEBUG, s, (Throwable) o2, o1);

        } else {
            append(ChronicleLogLevel.DEBUG, s, null, o1, o2);
        }
    }

    /**
     * Log a message with optional arguments.
     */
    @Override
    public void debug(String s, Object... objects) {
        append(ChronicleLogLevel.DEBUG, s, null, objects);
    }

    /**
     * Log a message with a {@link Throwable}.
     */
    @Override
    public void debug(String s, Throwable throwable) {
        append(ChronicleLogLevel.DEBUG, s, throwable);
    }

    // INFO
    /**
     * Tests whether {@code INFO} level messages will be logged.
     *
     * @return {@code true} if the level is enabled
     */
    @Override
    public boolean isInfoEnabled() {
        return isLevelEnabled(ChronicleLogLevel.INFO);
    }

    /**
     * Log a message at {@code INFO} level.
     *
     * @param s message text
     */
    @Override
    public void info(String s) {
        append(ChronicleLogLevel.INFO, s);
    }

    /**
     * Log a message with one argument or {@link Throwable}.
     */
    @Override
    public void info(String s, Object o1) {
        if (o1 instanceof Throwable) {
            append(ChronicleLogLevel.INFO, s, (Throwable) o1);

        } else {
            append(ChronicleLogLevel.INFO, s, null, o1);
        }
    }

    /**
     * Log a message with two arguments or one argument and a
     * {@link Throwable}.
     */
    @Override
    public void info(String s, Object o1, Object o2) {
        if (o2 instanceof Throwable) {
            append(ChronicleLogLevel.INFO, s, (Throwable) o2, o1);

        } else {
            append(ChronicleLogLevel.INFO, s, null, o1, o2);
        }
    }

    /**
     * Log a message with optional arguments.
     */
    @Override
    public void info(String s, Object... objects) {
        append(ChronicleLogLevel.INFO, s, null, objects);
    }

    /**
     * Log a message with a {@link Throwable}.
     */
    @Override
    public void info(String s, Throwable throwable) {
        append(ChronicleLogLevel.INFO, s, throwable);
    }

    // WARN
    /**
     * Tests whether {@code WARN} level messages will be logged.
     *
     * @return {@code true} if the level is enabled
     */
    @Override
    public boolean isWarnEnabled() {
        return isLevelEnabled(ChronicleLogLevel.WARN);
    }

    /**
     * Log a message at {@code WARN} level.
     *
     * @param s message text
     */
    @Override
    public void warn(String s) {
        append(ChronicleLogLevel.WARN, s);
    }

    /**
     * Log a message with one argument or {@link Throwable}.
     */
    @Override
    public void warn(String s, Object o1) {
        if (o1 instanceof Throwable) {
            append(ChronicleLogLevel.WARN, s, (Throwable) o1);

        } else {
            append(ChronicleLogLevel.WARN, s, null, o1);
        }
    }

    /**
     * Log a message with two arguments or one argument and a
     * {@link Throwable}.
     */
    @Override
    public void warn(String s, Object o1, Object o2) {
        if (o2 instanceof Throwable) {
            append(ChronicleLogLevel.WARN, s, (Throwable) o2, o1);

        } else {
            append(ChronicleLogLevel.WARN, s, null, o1, o2);
        }
    }

    /**
     * Log a message with optional arguments.
     */
    @Override
    public void warn(String s, Object... objects) {
        append(ChronicleLogLevel.WARN, s, null, objects);
    }

    /**
     * Log a message with a {@link Throwable}.
     */
    @Override
    public void warn(String s, Throwable throwable) {
        append(ChronicleLogLevel.WARN, s, throwable);
    }

    // ERROR
    /**
     * Tests whether {@code ERROR} level messages will be logged.
     *
     * @return {@code true} if the level is enabled
     */
    @Override
    public boolean isErrorEnabled() {
        return isLevelEnabled(ChronicleLogLevel.ERROR);
    }

    /**
     * Log a message at {@code ERROR} level.
     *
     * @param s message text
     */
    @Override
    public void error(String s) {
        append(ChronicleLogLevel.ERROR, s);
    }

    /**
     * Log a message with one argument or {@link Throwable}.
     */
    @Override
    public void error(String s, Object o1) {
        if (o1 instanceof Throwable) {
            append(ChronicleLogLevel.ERROR, s, (Throwable) o1);

        } else {
            append(ChronicleLogLevel.ERROR, s, null, o1);
        }
    }

    /**
     * Log a message with two arguments or one argument and a
     * {@link Throwable}.
     */
    @Override
    public void error(String s, Object o1, Object o2) {
        if (o2 instanceof Throwable) {
            append(ChronicleLogLevel.ERROR, s, (Throwable) o2, o1);

        } else {
            append(ChronicleLogLevel.ERROR, s, null, o1, o2);
        }
    }

    /**
     * Log a message with optional arguments.
     */
    @Override
    public void error(String s, Object... objects) {
        append(ChronicleLogLevel.ERROR, s, null, objects);
    }

    /**
     * Log a message with a {@link Throwable}.
     */
    @Override
    public void error(String s, Throwable throwable) {
        append(ChronicleLogLevel.ERROR, s, throwable);
    }

    // HELPERS
    /**
     * Return {@code true} if the supplied level is at or above the
     * configured level for this logger.
     */
    private boolean isLevelEnabled(ChronicleLogLevel level) {
        return level.isHigherOrEqualTo(this.level);
    }

    /**
     * Write a log entry without arguments.
     */
    protected void append(ChronicleLogLevel level, String message) {
        if (isLevelEnabled(level)) {
            writer.write(
                    level,
                    System.currentTimeMillis(),
                    Thread.currentThread().getName(),
                    name,
                    message,
                    null);
        }
    }

    /**
     * Write a log entry with a {@link Throwable}.
     */
    protected void append(ChronicleLogLevel level, String message, Throwable throwable) {
        if (isLevelEnabled(level)) {
            writer.write(
                    level,
                    System.currentTimeMillis(),
                    Thread.currentThread().getName(),
                    name,
                    message,
                    throwable);
        }
    }

    /**
     * Write a log entry with optional arguments.
     */
    protected void append(ChronicleLogLevel level, String message, Throwable throwable, Object... args) {
        if (isLevelEnabled(level)) {
            writer.write(
                    level,
                    System.currentTimeMillis(),
                    Thread.currentThread().getName(),
                    name,
                    message,
                    throwable,
                    args);
        }
    }
}
