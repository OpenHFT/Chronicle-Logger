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

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;

public class Log4j2TestBase {

    // *************************************************************************
    //
    // *************************************************************************

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

    static void log(Logger logger, Level level, String fmt, Object... args) {
        if (Level.TRACE.equals(level)) {
            logger.trace(fmt, args);
        } else if (Level.DEBUG.equals(level)) {
            logger.debug(fmt, args);
        } else if (Level.INFO.equals(level)) {
            logger.info(fmt, args);
        } else if (Level.WARN.equals(level)) {
            logger.warn(fmt, args);
        } else if (Level.ERROR.equals(level)) {
            logger.error(fmt, args);
        }  else if (Level.FATAL.equals(level)) {
            logger.fatal(fmt, args);
        } else {
            throw new UnsupportedOperationException("Unknown level " + level);
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
