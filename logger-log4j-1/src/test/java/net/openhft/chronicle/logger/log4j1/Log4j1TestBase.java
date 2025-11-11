/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.logger.log4j1;

import net.openhft.chronicle.core.OS;
import net.openhft.chronicle.core.util.Time;
import net.openhft.chronicle.logger.ChronicleLogLevel;
import org.slf4j.Logger;

import java.io.File;

class Log4j1TestBase {

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

        return path + "chronicle-log4j1";
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
}
