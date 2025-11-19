/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.logger.slf4j;

import net.openhft.chronicle.core.OS;
import net.openhft.chronicle.core.util.Time;
import net.openhft.chronicle.logger.ChronicleLogLevel;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class Slf4jTestBase {

    static final ChronicleLogLevel[] LOG_LEVELS = ChronicleLogLevel.values();

    static String basePath() {
        String path = System.getProperty("java.io.tmpdir");
        String sep = System.getProperty("file.separator");

        if (!path.endsWith(sep)) {
            path += sep;
        }

        return path + "chronicle-slf4j";
    }

    static String basePath(String loggerName) {
        return basePath()
                + System.getProperty("file.separator")
                + loggerName;
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

    static void warmup(Logger logger) {
        for (int i = 0; i < 10; i++) {
            logger.info("warmup");
        }
    }

    /**
     * @return the ChronicleLoggerFactory singleton
     * Works with both SLF4J 1.x (StaticLoggerBinder) and 2.x (ServiceProvider)
     */
    Object getChronicleLoggerFactory() {
        try {
            // Try SLF4J 2.x approach first (ServiceProvider)
            Class<?> providerClass = Class.forName("org.slf4j.impl.ChronicleServiceProvider");
            Object provider = providerClass.getDeclaredConstructor().newInstance();
            providerClass.getMethod("initialize").invoke(provider);
            return providerClass.getMethod("getLoggerFactory").invoke(provider);
        } catch (Exception e) {
            // Fall back to SLF4J 1.x approach (StaticLoggerBinder)
            try {
                Class<?> binderClass = Class.forName("org.slf4j.impl.StaticLoggerBinder");
                Object binder = binderClass.getMethod("getSingleton").invoke(null);
                return binderClass.getMethod("getLoggerFactory").invoke(binder);
            } catch (Exception ex) {
                throw new RuntimeException("Unable to get ChronicleLoggerFactory via SLF4J 1.x or 2.x", ex);
            }
        }
    }

    protected final class RunnableLogger implements Runnable {
        private final Logger logger;
        private final int runs;
        private final String fmt;
        private final String fmtBase = " > val1={}, val2={}, val3={}";

        public RunnableLogger(int runs, int pad, String loggerName) {
            this.logger = LoggerFactory.getLogger(loggerName);
            this.runs = runs;
            this.fmt = StringUtils.rightPad(fmtBase, pad + fmtBase.length() - (4 + 8 + 8), "X");
        }

        @Override
        public void run() {
            for (int i = 0; i < this.runs; i++) {
                this.logger.info(fmt, i, i * 7L, i / 16.0);
            }
        }
    }
}
