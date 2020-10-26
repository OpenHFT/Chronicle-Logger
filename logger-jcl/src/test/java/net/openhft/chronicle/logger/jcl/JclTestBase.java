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
package net.openhft.chronicle.logger.jcl;

import net.openhft.chronicle.logger.ChronicleLogLevel;
import org.apache.commons.logging.Log;

class JclTestBase {

    // *************************************************************************
    //
    // *************************************************************************

    static final ChronicleLogLevel[] LOG_LEVELS = ChronicleLogLevel.values();

    static String basePath() {
        String path = System.getProperty("java.io.tmpdir");
        String sep = System.getProperty("file.separator");

        if (!path.endsWith(sep)) {
            path += sep;
        }

        return path + "chronicle-jcl";
    }

    static String basePath(String loggerName) {
        return basePath() + System.getProperty("file.separator") + loggerName;
    }

    static void log(Log logger, ChronicleLogLevel level, String message) {
        switch (level) {
            case TRACE:
                logger.trace(message);
                break;

            case DEBUG:
                logger.debug(message);
                break;

            case INFO:
                logger.info(message);
                break;

            case WARN:
                logger.warn(message);
                break;

            case ERROR:
                logger.error(message);
                break;
            default:
                throw new UnsupportedOperationException();
        }
    }
}
