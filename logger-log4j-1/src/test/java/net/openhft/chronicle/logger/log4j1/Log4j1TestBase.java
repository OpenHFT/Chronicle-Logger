/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.logger.log4j1;

import net.openhft.chronicle.logger.ChronicleLogLevel;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import java.io.File;

class Log4j1TestBase {

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

    static void log(Logger logger, ChronicleLogLevel level, String message) {
        switch (level) {
            case TRACE:
                logger.log(Level.TRACE, message);
                break;

            case DEBUG:
                logger.log(Level.DEBUG, message);
                break;

            case INFO:
                logger.log(Level.INFO, message);
                break;

            case WARN:
                logger.log(Level.WARN, message);
                break;

            case ERROR:
                logger.log(Level.ERROR, message);
                break;
            default:
                throw new UnsupportedOperationException();
        }
    }
}
