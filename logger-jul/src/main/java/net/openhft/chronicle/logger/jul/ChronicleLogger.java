/*
 * Copyright 2014 Higher Frequency Trading
 *
 * http://www.higherfrequencytrading.com
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

import java.text.MessageFormat;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

abstract class ChronicleLogger extends Logger {

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

        super.setLevel(ChronicleHandlerHelper.getLogLevel(level));
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
    }

    @Override
    public void log(final Level level, final String msg) {
    }

    @Override
    public void log(final Level level, final String msg, final Object param1) {
    }

    @Override
    public void log(final Level level, final String msg, final Object[] params) {
    }

    @Override
    public void log(final Level level, final String msg, final Throwable thrown) {
    }

    @Override
    public void logp(final Level level, final String sourceClass, final String sourceMethod, final String msg) {
        log(level, msg);
    }

    @Override
    public void logp(final Level level, final String sourceClass, final String sourceMethod, final String msg,
                     final Object param1) {
        log(level, msg, param1);
    }

    @Override
    public void logp(final Level level, final String sourceClass, final String sourceMethod, final String msg,
                     final Object[] params) {
        log(level, msg, params);
    }

    @Override
    public void logp(final Level level, final String sourceClass, final String sourceMethod, final String msg,
                     final Throwable thrown) {
        log(level, msg, thrown);
    }

    @Override
    public void severe(final String msg) {
    }

    @Override
    public void warning(final String msg) {
    }

    @Override
    public void info(final String msg) {
    }

    @Override
    public void config(final String msg) {
    }

    @Override
    public void fine(final String msg) {
    }

    @Override
    public void finer(final String msg) {
    }

    @Override
    public void finest(final String msg) {
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

    protected abstract void append(final Level level, final String msg);
    protected abstract void append(final Level level, final String msg, final Object param1);
    protected abstract void append(final Level level, final String msg, final Object[] params);
    protected abstract void append(final Level level, final String msg, final Throwable thrown);

    // *************************************************************************
    //
    // *************************************************************************

    public static class Binary extends ChronicleLogger {
        public Binary(ChronicleLogWriter writer, String name, ChronicleLogLevel level) {
            super(writer, name, level);
        }

        @Override
        protected void append(final Level level, String msg) {
            if(isLoggable(level)) {
                writer.write(
                    this.level,
                    System.currentTimeMillis(),
                    Thread.currentThread().getName(),
                    this.name,
                    msg);
            }
        }

        @Override
        protected void append(final Level level, String msg, Object param1) {
            if(isLoggable(level)) {
                writer.write(
                    this.level,
                    System.currentTimeMillis(),
                    Thread.currentThread().getName(),
                    this.name,
                    msg,
                    null,
                    param1);
            }
        }

        @Override
        protected void append(final Level level, String msg, Object[] params) {
            if(isLoggable(level)) {
                writer.write(
                    this.level,
                    System.currentTimeMillis(),
                    Thread.currentThread().getName(),
                    this.name,
                    msg,
                    null,
                    params);
            }
        }

        @Override
        protected void append(final Level level, String msg, Throwable thrown) {
            if(isLoggable(level)) {
                writer.write(
                    this.level,
                    System.currentTimeMillis(),
                    Thread.currentThread().getName(),
                    this.name,
                    msg,
                    thrown);
            }
        }
    }

    public static class Text extends ChronicleLogger {
        public Text(ChronicleLogWriter writer, String name, ChronicleLogLevel level) {
            super(writer, name, level);
        }

        @Override
        protected void append(final Level level, String msg) {
            if(isLoggable(level)) {
                writer.write(
                    this.level,
                    System.currentTimeMillis(),
                    Thread.currentThread().getName(),
                    this.name,
                    msg,
                    null);
            }
        }

        @Override
        protected void append(final Level level, String msg, Object param1) {
            if(isLoggable(level)) {
                writer.write(
                    this.level,
                    System.currentTimeMillis(),
                    Thread.currentThread().getName(),
                    this.name,
                    MessageFormat.format(msg, param1),
                    null);
            }
        }

        @Override
        protected void append(final Level level, String msg, Object[] params) {
            if(isLoggable(level)) {
                writer.write(
                    this.level,
                    System.currentTimeMillis(),
                    Thread.currentThread().getName(),
                    this.name,
                    MessageFormat.format(msg, params),
                    null);
            }
        }

        @Override
        protected void append(final Level level, String msg, Throwable thrown) {
            if(isLoggable(level)) {
                writer.write(
                    this.level,
                    System.currentTimeMillis(),
                    Thread.currentThread().getName(),
                    this.name,
                    msg,
                    thrown);
            }
        }
    }
}
