/*
 * Copyright 2014-2017 Chronicle Software
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
package net.openhft.chronicle.logger.slf4j;

import net.openhft.chronicle.logger.ChronicleLogLevel;
import net.openhft.chronicle.logger.ChronicleLogWriter;
import org.slf4j.helpers.MarkerIgnoringBase;

public final class ChronicleLogger extends MarkerIgnoringBase {

    private static final long serialVersionUID = 1L;

    private final ChronicleLogWriter writer;
    protected final ChronicleLogLevel level;

    ChronicleLogger(final ChronicleLogWriter writer, final String name, final ChronicleLogLevel level) {
        this.writer = writer;
        this.name = name;
        this.level = level;
    }

    // *************************************************************************
    //
    // *************************************************************************

    ChronicleLogLevel getLevel() {
        return this.level;
    }

    ChronicleLogWriter getWriter() {
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
        append(ChronicleLogLevel.TRACE, s);
    }

    @Override
    public void trace(String s, Object o1) {
        if (o1 instanceof Throwable) {
            append(ChronicleLogLevel.TRACE, s, (Throwable) o1);

        } else {
            append(ChronicleLogLevel.TRACE, s, null, o1);
        }
    }

    @Override
    public void trace(String s, Object o1, Object o2) {
        if (o2 instanceof Throwable) {
            append(ChronicleLogLevel.TRACE, s, (Throwable) o2, o1);

        } else {
            append(ChronicleLogLevel.TRACE, s, null, o1, o2);
        }
    }

    @Override
    public void trace(String s, Object... objects) {
        append(ChronicleLogLevel.TRACE, s, null, objects);
    }

    @Override
    public void trace(String s, Throwable throwable) {
        append(ChronicleLogLevel.TRACE, s, throwable);
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
        append(ChronicleLogLevel.DEBUG, s);
    }

    @Override
    public void debug(String s, Object o1) {
        if (o1 instanceof Throwable) {
            append(ChronicleLogLevel.DEBUG, s, (Throwable) o1);

        } else {
            append(ChronicleLogLevel.DEBUG, s, null, o1);
        }
    }

    @Override
    public void debug(String s, Object o1, Object o2) {
        if (o2 instanceof Throwable) {
            append(ChronicleLogLevel.DEBUG, s, (Throwable) o2, o1);

        } else {
            append(ChronicleLogLevel.DEBUG, s, null, o1, o2);
        }
    }

    @Override
    public void debug(String s, Object... objects) {
        append(ChronicleLogLevel.DEBUG, s, null, objects);
    }

    @Override
    public void debug(String s, Throwable throwable) {
        append(ChronicleLogLevel.DEBUG, s, throwable);
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
        append(ChronicleLogLevel.INFO, s);
    }

    @Override
    public void info(String s, Object o1) {
        if (o1 instanceof Throwable) {
            append(ChronicleLogLevel.INFO, s, (Throwable) o1);

        } else {
            append(ChronicleLogLevel.INFO, s, null, o1);
        }
    }

    @Override
    public void info(String s, Object o1, Object o2) {
        if (o2 instanceof Throwable) {
            append(ChronicleLogLevel.INFO, s, (Throwable) o2, o1);

        } else {
            append(ChronicleLogLevel.INFO, s, null, o1, o2);
        }
    }

    @Override
    public void info(String s, Object... objects) {
        append(ChronicleLogLevel.INFO, s, null, objects);
    }

    @Override
    public void info(String s, Throwable throwable) {
        append(ChronicleLogLevel.INFO, s, throwable);
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
        append(ChronicleLogLevel.WARN, s);
    }

    @Override
    public void warn(String s, Object o1) {
        if (o1 instanceof Throwable) {
            append(ChronicleLogLevel.WARN, s, (Throwable) o1);

        } else {
            append(ChronicleLogLevel.WARN, s, null, o1);
        }
    }

    @Override
    public void warn(String s, Object o1, Object o2) {
        if (o2 instanceof Throwable) {
            append(ChronicleLogLevel.WARN, s, (Throwable) o2, o1);

        } else {
            append(ChronicleLogLevel.WARN, s, null, o1, o2);
        }
    }

    @Override
    public void warn(String s, Object... objects) {
        append(ChronicleLogLevel.WARN, s, null, objects);
    }

    @Override
    public void warn(String s, Throwable throwable) {
        append(ChronicleLogLevel.WARN, s, throwable);
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
    public void error(String s, Object o1) {
        if (o1 instanceof Throwable) {
            append(ChronicleLogLevel.ERROR, s, (Throwable) o1);

        } else {
            append(ChronicleLogLevel.ERROR, s, null, o1);
        }
    }

    @Override
    public void error(String s, Object o1, Object o2) {
        if (o2 instanceof Throwable) {
            append(ChronicleLogLevel.ERROR, s, (Throwable) o2, o1);

        } else {
            append(ChronicleLogLevel.ERROR, s, null, o1, o2);
        }
    }

    @Override
    public void error(String s, Object... objects) {
        append(ChronicleLogLevel.ERROR, s, null, objects);
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
