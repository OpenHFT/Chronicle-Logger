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
package net.openhft.chronicle.logger.log4j2;

import net.openhft.chronicle.logger.ChronicleLogLevel;
import org.apache.logging.log4j.LogManager;
import org.slf4j.Logger;

import java.io.File;

public class Log4j2TestBase {

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

        return path + "chronicle-log4j2";
    }

    static String basePath(String type) {
        return rootPath()
                + File.separator
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

    static org.apache.logging.log4j.core.Appender getAppender(String name) {
        final org.apache.logging.log4j.core.LoggerContext ctx =
                (org.apache.logging.log4j.core.LoggerContext) LogManager.getContext();

        return ctx.getConfiguration().getAppender(name);
    }
}
