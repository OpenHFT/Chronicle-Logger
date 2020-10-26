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
package net.openhft.chronicle.logger.slf4j;

import net.openhft.chronicle.logger.ChronicleLogLevel;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.impl.StaticLoggerBinder;

class Slf4jTestBase {

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

    // *************************************************************************
    //
    // *************************************************************************

    /**
     * @return the ChronicleLoggerFactory singleton
     */
    ChronicleLoggerFactory getChronicleLoggerFactory() {
        return (ChronicleLoggerFactory) StaticLoggerBinder.getSingleton().getLoggerFactory();
    }

    // *************************************************************************
    //
    // *************************************************************************

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
