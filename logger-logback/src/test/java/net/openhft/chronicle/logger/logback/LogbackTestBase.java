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
package net.openhft.chronicle.logger.logback;

import ch.qos.logback.classic.LoggerContext;
import net.openhft.chronicle.logger.ChronicleLogLevel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LogbackTestBase {

    // *************************************************************************
    //
    // *************************************************************************

    static final ChronicleLogLevel[] LOG_LEVELS = ChronicleLogLevel.values();

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

    static void log(Logger logger, ChronicleLogLevel level, String fmt, Object... args) {
        switch (level) {
            case TRACE:
                logger.trace(fmt, args);
                break;

            case DEBUG:
                logger.debug(fmt, args);
                break;

            case INFO:
                logger.info(fmt, args);
                break;

            case WARN:
                logger.warn(fmt, args);
                break;

            case ERROR:
                logger.error(fmt, args);
                break;
            default:
                throw new UnsupportedOperationException();
        }
    }

    // *************************************************************************
    //
    // *************************************************************************

    LoggerContext getLoggerContext() {
        return (LoggerContext) LoggerFactory.getILoggerFactory();
    }
}
