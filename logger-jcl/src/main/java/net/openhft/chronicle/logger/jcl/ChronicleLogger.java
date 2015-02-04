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
package net.openhft.chronicle.logger.jcl;

import net.openhft.chronicle.logger.ChronicleLogAppender;
import net.openhft.chronicle.logger.ChronicleLogLevel;
import org.apache.commons.logging.Log;

class ChronicleLogger implements Log {

    private final String name;
    private final ChronicleLogAppender appender;
    private final ChronicleLogLevel level;

    /**
     * c-tor
     *
     * @param writer
     * @param name
     */
    ChronicleLogger(final ChronicleLogAppender writer, final String name) {
        this(writer, name, ChronicleLogLevel.INFO);
    }

    /**
     * c-tor
     *
     * @param writer
     * @param name
     * @param level
     */
    ChronicleLogger(final ChronicleLogAppender writer, final String name, final ChronicleLogLevel level) {
        this.appender = writer;
        this.name = name;
        this.level = level;
    }

    // *************************************************************************
    //
    // *************************************************************************

    String name() {
        return this.name;
    }

    ChronicleLogAppender appender() {
        return this.appender;
    }

    ChronicleLogLevel level() {
        return this.level;
    }

    // *************************************************************************
    // DEBUG
    // *************************************************************************

    @Override
    public boolean isDebugEnabled() {
        return isLevelEnabled(ChronicleLogLevel.DEBUG);
    }

    @Override
    public void debug(Object o) {
        append(ChronicleLogLevel.DEBUG, String.valueOf(o));
    }

    @Override
    public void debug(Object o, Throwable throwable) {
        append(ChronicleLogLevel.DEBUG, String.valueOf(o), throwable);
    }

    // *************************************************************************
    // TRACE
    // *************************************************************************

    @Override
    public boolean isTraceEnabled() {
        return isLevelEnabled(ChronicleLogLevel.TRACE);
    }

    @Override
    public void trace(Object o) {
        append(ChronicleLogLevel.TRACE, String.valueOf(o));
    }

    @Override
    public void trace(Object o, Throwable throwable) {
        append(ChronicleLogLevel.TRACE, String.valueOf(o), throwable);
    }

    // *************************************************************************
    // INFO
    // *************************************************************************

    @Override
    public boolean isInfoEnabled() {
        return isLevelEnabled(ChronicleLogLevel.INFO);
    }

    @Override
    public void info(Object o) {
        append(ChronicleLogLevel.INFO, String.valueOf(o));
    }

    @Override
    public void info(Object o, Throwable throwable) {
        append(ChronicleLogLevel.INFO, String.valueOf(o), throwable);
    }

    // *************************************************************************
    // WARN
    // *************************************************************************

    @Override
    public boolean isWarnEnabled() {
        return isLevelEnabled(ChronicleLogLevel.WARN);
    }

    @Override
    public void warn(Object o) {
        append(ChronicleLogLevel.WARN, String.valueOf(o));
    }

    @Override
    public void warn(Object o, Throwable throwable) {
        append(ChronicleLogLevel.WARN, String.valueOf(o), throwable);
    }

    // *************************************************************************
    // ERROR
    // *************************************************************************

    @Override
    public boolean isErrorEnabled() {
        return isLevelEnabled(ChronicleLogLevel.ERROR);
    }

    @Override
    public void error(Object o) {
        append(ChronicleLogLevel.ERROR, String.valueOf(o));
    }

    @Override
    public void error(Object o, Throwable throwable) {
        append(ChronicleLogLevel.ERROR, String.valueOf(o), throwable);
    }

    // *************************************************************************
    // FATAL
    // *************************************************************************

    @Override
    public boolean isFatalEnabled() {
        return isLevelEnabled(ChronicleLogLevel.ERROR);
    }

    @Override
    public void fatal(Object o) {
        append(ChronicleLogLevel.ERROR, String.valueOf(o));
    }

    @Override
    public void fatal(Object o, Throwable throwable) {
        append(ChronicleLogLevel.ERROR, String.valueOf(o), throwable);
    }

    // *************************************************************************
    // HELPERS
    // *************************************************************************

    private boolean isLevelEnabled(ChronicleLogLevel level) {
        return level.isHigherOrEqualTo(this.level);
    }

    private void append(ChronicleLogLevel level, String message, Object arg1) {
        if(level.isHigherOrEqualTo(this.level)) {
            this.appender.log(level, System.currentTimeMillis(), Thread.currentThread(), this.name, message, arg1);
        }
    }

    private void append(ChronicleLogLevel level, String message, Object arg1, Object arg2) {
        if(level.isHigherOrEqualTo(this.level)) {
            this.appender.log(level, System.currentTimeMillis(), Thread.currentThread(), this.name, message, arg1, arg2);
        }
    }

    private void append(ChronicleLogLevel level, String message, Object... args) {
        if(level.isHigherOrEqualTo(this.level)) {
            this.appender.log(level, System.currentTimeMillis(), Thread.currentThread(), this.name, message, null, args);
        }
    }

    private void append(ChronicleLogLevel level, String message, Throwable throwable) {
        if(level.isHigherOrEqualTo(this.level)) {
            this.appender.log(level, System.currentTimeMillis(), Thread.currentThread(), this.name, message, throwable, null);
        }
    }
}
