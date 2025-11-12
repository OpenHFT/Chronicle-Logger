/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.logger.jul;

import net.openhft.chronicle.logger.ChronicleLogLevel;

import java.util.logging.Level;
import java.util.logging.Logger;

public class JulTestBase {

    protected static final ChronicleLogLevel[] LOG_LEVELS = ChronicleLogLevel.values();

    protected static void log(Logger logger, ChronicleLogLevel level, String fmt, Object... args) {
        switch (level) {
            case TRACE:
                logger.log(Level.FINER, fmt, args);
                break;

            case DEBUG:
                logger.log(Level.FINE, fmt, args);
                break;

            case INFO:
                logger.log(Level.INFO, fmt, args);
                break;

            case WARN:
                logger.log(Level.WARNING, fmt, args);
                break;

            case ERROR:
                logger.log(Level.SEVERE, fmt, args);
                break;
            default:
                throw new UnsupportedOperationException();
        }
    }
}
