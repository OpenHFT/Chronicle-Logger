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
package net.openhft.chronicle.logger.logback;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;
import net.openhft.chronicle.core.io.IOTools;
import net.openhft.chronicle.logger.LogAppenderConfig;
import net.openhft.chronicle.queue.ChronicleQueue;
import org.jetbrains.annotations.NotNull;
import org.junit.After;
import org.junit.Before;
import org.slf4j.ILoggerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.nio.file.Path;
import java.util.Objects;

import static ch.qos.logback.classic.Level.*;
import static java.util.Objects.requireNonNull;

public abstract class LogbackTestBase {
    private LoggerContext context;

    // *************************************************************************
    //
    // *************************************************************************

    @Before
    public void setup() throws JoranException {
        LoggerContext context = new LoggerContext();
        String resourcePath = requireNonNull(getResource());
        URL resource = requireNonNull(getClass().getResource(resourcePath));
        JoranConfigurator configurator = new JoranConfigurator();
        configurator.setContext(context);
        configurator.doConfigure(resource);
        this.context = context;
    }

    @After
    public void tearDown() {
        IOTools.deleteDirWithFiles(rootPath());
    }

    abstract String getResource();

    static String rootPath() {
        String path = System.getProperty("java.io.tmpdir");
        String sep = System.getProperty("file.separator");

        if (!path.endsWith(sep)) {
            path += sep;
        }

        return path + "chronicle-logback";
    }

    static String basePath(String type) {
        return rootPath()
                + System.getProperty("file.separator")
                + type;
    }

    static void log(Logger logger, Level level, String fmt, Object... args) {
        if (TRACE.equals(level)) {
            logger.trace(fmt, args);
        } else if (DEBUG.equals(level)) {
            logger.debug(fmt, args);
        } else if (INFO.equals(level)) {
            logger.info(fmt, args);
        } else if (WARN.equals(level)) {
            logger.warn(fmt, args);
        } else if (ERROR.equals(level)) {
            logger.error(fmt, args);
        } else {
            throw new UnsupportedOperationException("Level not found " + level);
        }
    }

    // *************************************************************************
    //
    // *************************************************************************

    LoggerContext getLoggerContext() {
        return context;
    }
}
