/*
 * Copyright 2014-2020 chronicle.software
 *
 * http://www.chronicle.software
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.openhft.chronicle.logger.jul;

import net.openhft.chronicle.logger.ChronicleLogLevel;
import net.openhft.chronicle.logger.ChronicleLogWriter;

import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

class ChronicleLogger extends Logger {

    protected final String name;
    protected final ChronicleLogWriter writer;
    protected final ChronicleLogLevel level;

    /**
     * c-tor
     *
     * @param writer
     * @param name
     * @param level
     */
    ChronicleLogger(final ChronicleLogWriter writer, final String name, final ChronicleLogLevel level) {
        super(name, null);

        this.writer = writer;
        this.name = name;
        this.level = level;

        /*
         * Set level of super class using final method
         */
        setLevel(level);
    }

    private final void setLevel(final ChronicleLogLevel level) {
        super.setLevel(ChronicleHelper.getLogLevel(level));
    }

    // *************************************************************************
    //
    // *************************************************************************

    String name() {
        return this.name;
    }

    ChronicleLogWriter writer() {
        return this.writer;
    }

    ChronicleLogLevel level() {
        return this.level;
    }

    // *************************************************************************
    //
    // *************************************************************************

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public void setParent(final Logger parent) {
        throw new UnsupportedOperationException("Cannot set parent logger");
    }

    @Override
    public void log(final LogRecord record) {
        append(record);
    }

    @Override
    public void log(final Level level, final String msg) {
        append(level, msg);
    }

    @Override
    public void log(final Level level, final String msg, final Object param1) {
        append(level, msg, param1);
    }

    @Override
    public void log(final Level level, final String msg, final Object[] params) {
        append(level, msg, params);
    }

    @Override
    public void log(final Level level, final String msg, final Throwable thrown) {
        append(level, msg, thrown);
    }

    @Override
    public void logp(final Level level, final String sourceClass, final String sourceMethod, final String msg) {
        append(level, msg);
    }

    @Override
    public void logp(final Level level, final String sourceClass, final String sourceMethod, final String msg,
                     final Object param1) {
        append(level, msg, param1);
    }

    @Override
    public void logp(final Level level, final String sourceClass, final String sourceMethod, final String msg,
                     final Object[] params) {
        append(level, msg, params);
    }

    @Override
    public void logp(final Level level, final String sourceClass, final String sourceMethod, final String msg,
                     final Throwable thrown) {
        append(level, msg, thrown);
    }

    @Override
    public void logrb(final Level level, final String sourceClass, final String sourceMethod, final String bundleName,
                      final String msg) {
        append(level, msg);
    }

    @Override
    public void logrb(final Level level, final String sourceClass, final String sourceMethod, final String bundleName,
                      final String msg, final Object param1) {
        append(level, msg, param1);
    }

    @Override
    public void logrb(final Level level, final String sourceClass, final String sourceMethod, final String bundleName,
                      final String msg, final Object[] params) {
        append(level, msg, params);
    }

    @Override
    public void logrb(final Level level, final String sourceClass, final String sourceMethod, final String bundleName,
                      final String msg, final Throwable thrown) {
        append(level, msg, thrown);
    }

    @Override
    public void severe(final String msg) {
        append(Level.SEVERE, msg);
    }

    @Override
    public void warning(final String msg) {
        append(Level.WARNING, msg);
    }

    @Override
    public void info(final String msg) {
        append(Level.INFO, msg);
    }

    @Override
    public void config(final String msg) {
        append(Level.CONFIG, msg);
    }

    @Override
    public void fine(final String msg) {
        append(Level.FINE, msg);
    }

    @Override
    public void finer(final String msg) {
        append(Level.FINER, msg);
    }

    @Override
    public void finest(final String msg) {
        append(Level.FINEST, msg);
    }

    @Override
    public void entering(final String sourceClass, final String sourceMethod) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void entering(final String sourceClass, final String sourceMethod, final Object param1) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void entering(final String sourceClass, final String sourceMethod, final Object[] params) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void exiting(final String sourceClass, final String sourceMethod) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void exiting(final String sourceClass, final String sourceMethod, final Object result) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void throwing(final String sourceClass, final String sourceMethod, final Throwable thrown) {
        throw new UnsupportedOperationException();
    }

    // *************************************************************************
    // HELPERS
    // *************************************************************************

    @Override
    public boolean isLoggable(Level level) {
        return isLoggable(ChronicleHelper.getLogLevel(level));
    }

    public boolean isLoggable(ChronicleLogLevel level) {
        return level.isHigherOrEqualTo(this.level);
    }

    protected void append(final LogRecord record) {
        final ChronicleLogLevel clevel = ChronicleHelper.getLogLevel(record);
        if (isLoggable(clevel)) {
            writer.write(
                    clevel,
                    record.getMillis(),
                    "thread-" + record.getThreadID(),
                    record.getLoggerName(),
                    record.getMessage(),
                    record.getThrown(),
                    record.getParameters());
        }
    }

    protected void append(final Level level, String msg) {
        final ChronicleLogLevel clevel = ChronicleHelper.getLogLevel(level);
        if (isLoggable(clevel)) {
            writer.write(
                    clevel,
                    System.currentTimeMillis(),
                    Thread.currentThread().getName(),
                    this.name,
                    msg);
        }
    }

    protected void append(final Level level, String msg, Object... params) {
        final ChronicleLogLevel clevel = ChronicleHelper.getLogLevel(level);
        if (isLoggable(clevel)) {
            writer.write(
                    clevel,
                    System.currentTimeMillis(),
                    Thread.currentThread().getName(),
                    this.name,
                    msg,
                    null,
                    params);
        }
    }

    protected void append(final Level level, String msg, Throwable thrown) {
        final ChronicleLogLevel clevel = ChronicleHelper.getLogLevel(level);
        if (isLoggable(clevel)) {
            writer.write(
                    clevel,
                    System.currentTimeMillis(),
                    Thread.currentThread().getName(),
                    this.name,
                    msg,
                    thrown);
        }
    }

    public static class Null extends ChronicleLogger {
        public static final ChronicleLogger INSTANCE = new Null();

        private Null() {
            super(null, null, null);
        }

        @Override
        protected void append(final LogRecord record) {
        }

        @Override
        protected void append(final Level level, String msg) {
        }

        @Override
        protected void append(final Level level, String msg, Object[] params) {
        }

        @Override
        protected void append(final Level level, String msg, Throwable thrown) {
        }
    }
}
