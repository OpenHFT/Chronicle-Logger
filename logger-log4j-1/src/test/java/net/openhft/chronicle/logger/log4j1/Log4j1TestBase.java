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
package net.openhft.chronicle.logger.log4j1;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import java.io.File;

import static org.apache.log4j.Level.*;

class Log4j1TestBase {

    // *************************************************************************
    //
    // *************************************************************************

    static String rootPath() {
        String path = System.getProperty("java.io.tmpdir");
        String sep = System.getProperty("file.separator");

        if (!path.endsWith(sep)) {
            path += sep;
        }

        return path + "chronicle-log4j1";
    }

    static String basePath(String type) {
        return rootPath()
                + File.separator
                + type;
    }

    static void log(Logger logger, Level level, String fmt, Throwable args) {
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
        } else if (FATAL.equals(level)) {
            logger.fatal(fmt, args);
        } else {
            throw new UnsupportedOperationException();
        }
    }
}
