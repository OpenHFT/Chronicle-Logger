/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.logger.jcl;

import net.openhft.chronicle.logger.ChronicleLogLevel;
import org.apache.commons.logging.Log;

import java.nio.file.FileSystems;

class JclTestBase {

    static final ChronicleLogLevel[] LOG_LEVELS = ChronicleLogLevel.values();

    static String basePath() {
        String path = System.getProperty("java.io.tmpdir");
        String sep = FileSystems.getDefault().getSeparator();

        if (!path.endsWith(sep)) {
            path += sep;
        }

        return path + "chronicle-jcl";
    }

    static String basePath(String loggerName) {
        return basePath() + FileSystems.getDefault().getSeparator() + loggerName;
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
