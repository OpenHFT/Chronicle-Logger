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

package net.openhft.chronicle.logger.slf4j;

import net.openhft.chronicle.logger.ChronicleLogAppender;
import net.openhft.chronicle.logger.ChronicleLogLevel;
import org.slf4j.helpers.MarkerIgnoringBase;

/**
 *
 */
public class ChronicleLogger extends MarkerIgnoringBase {

    private final ChronicleLogAppender writer;
    private final ChronicleLogLevel level;

    /**
     * c-tor
     *
     * @param writer
     * @param name
     */
    public ChronicleLogger(final ChronicleLogAppender writer, final String name) {
        this(writer, name, ChronicleLogLevel.INFO);
    }

    /**
     * c-tor
     *
     * @param writer
     * @param name
     * @param level
     */
    public ChronicleLogger(final ChronicleLogAppender writer, final String name, final ChronicleLogLevel level) {
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
    public ChronicleLogLevel getLevel() {
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
        return isLevelEnabled(ChronicleLogLevel.TRACE);
    }

    @Override
    public void trace(String s) {
        append(ChronicleLogLevel.TRACE,  s);
    }

    @Override
    public void trace(String s, Object o) {
        append(ChronicleLogLevel.TRACE,  s, o);
    }

    @Override
    public void trace(String s, Object o1, Object o2) {
        append(ChronicleLogLevel.TRACE,  s, o1, o2);
    }

    @Override
    public void trace(String s, Object... objects) {
        append(ChronicleLogLevel.TRACE,  s, objects);
    }

    @Override
    public void trace(String s, Throwable throwable) {
        append(ChronicleLogLevel.TRACE,  s, throwable);
    }

    // *************************************************************************
    // DEBUG
    // *************************************************************************

    @Override
    public boolean isDebugEnabled() {
        return isLevelEnabled(ChronicleLogLevel.DEBUG);
    }

    @Override
    public void debug(String s) {
        append(ChronicleLogLevel.DEBUG,  s);
    }

    @Override
    public void debug(String s, Object o) {
        append(ChronicleLogLevel.DEBUG,  s, o);
    }

    @Override
    public void debug(String s, Object o1, Object o2) {
        append(ChronicleLogLevel.DEBUG,  s, o1, o2);
    }

    @Override
    public void debug(String s, Object... objects) {
        append(ChronicleLogLevel.DEBUG,  s, objects);
    }

    @Override
    public void debug(String s, Throwable throwable) {
        append(ChronicleLogLevel.DEBUG,  s, throwable);
    }

    // *************************************************************************
    // INFO
    // *************************************************************************

    @Override
    public boolean isInfoEnabled() {
        return isLevelEnabled(ChronicleLogLevel.INFO);
    }

    @Override
    public void info(String s) {
        append(ChronicleLogLevel.INFO,  s);
    }

    @Override
    public void info(String s, Object o) {
        append(ChronicleLogLevel.INFO,  s, o);
    }

    @Override
    public void info(String s, Object o1, Object o2) {
        append(ChronicleLogLevel.INFO,  s, o1, o2);
    }

    @Override
    public void info(String s, Object... objects) {
        append(ChronicleLogLevel.INFO,  s, objects);
    }

    @Override
    public void info(String s, Throwable throwable) {
        append(ChronicleLogLevel.INFO,  s, throwable);
    }

    // *************************************************************************
    // WARN
    // *************************************************************************

    @Override
    public boolean isWarnEnabled() {
        return isLevelEnabled(ChronicleLogLevel.WARN);
    }

    @Override
    public void warn(String s) {
        append(ChronicleLogLevel.WARN,  s);
    }

    @Override
    public void warn(String s, Object o) {
        append(ChronicleLogLevel.WARN,  s, o);
    }

    @Override
    public void warn(String s, Object o1, Object o2) {
        append(ChronicleLogLevel.WARN,  s, o1, o2);
    }

    @Override
    public void warn(String s, Object... objects) {
        append(ChronicleLogLevel.WARN,  s, objects);
    }

    @Override
    public void warn(String s, Throwable throwable) {
        append(ChronicleLogLevel.WARN,  s, throwable);
    }

    // *************************************************************************
    // ERROR
    // *************************************************************************

    @Override
    public boolean isErrorEnabled() {
        return isLevelEnabled(ChronicleLogLevel.ERROR);
    }

    @Override
    public void error(String s) {
        append(ChronicleLogLevel.ERROR, s);
    }

    @Override
    public void error(String s, Object o) {
        append(ChronicleLogLevel.ERROR, s, o);
    }

    @Override
    public void error(String s, Object o1, Object o2) {
        append(ChronicleLogLevel.ERROR, s, o1, o2);
    }

    @Override
    public void error(String s, Object... objects) {
        append(ChronicleLogLevel.ERROR, s, objects);
    }

    @Override
    public void error(String s, Throwable throwable) {
        append(ChronicleLogLevel.ERROR, s, throwable);
    }

    // *************************************************************************
    // HELPERS
    // *************************************************************************

    private boolean isLevelEnabled(ChronicleLogLevel level) {
        return level.isHigherOrEqualTo(this.level);
    }

    private void append(ChronicleLogLevel level, String message, Object arg1) {
        if(level.isHigherOrEqualTo(this.level)) {
            this.writer.log(level, this.name, message, arg1);
        }
    }

    private void append(ChronicleLogLevel level, String message, Object arg1, Object arg2) {
        if(level.isHigherOrEqualTo(this.level)) {
            this.writer.log(level, this.name, message, arg1, arg2);
        }
    }

    private void append(ChronicleLogLevel level, String message, Object... args) {
        if(level.isHigherOrEqualTo(this.level)) {
            this.writer.log(level, this.name, message, args);
        }
    }

    private void append(ChronicleLogLevel level, String message, Throwable throwable) {
        if(level.isHigherOrEqualTo(this.level)) {
            this.writer.log(level, this.name, message, throwable);
        }
    }
}
