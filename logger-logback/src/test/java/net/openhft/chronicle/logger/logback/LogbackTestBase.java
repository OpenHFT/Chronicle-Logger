/*
 * Copyright 2014 Higher Frequency Trading
 * <p/>
 * http://www.higherfrequencytrading.com
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.openhft.chronicle.logger.logback;

import ch.qos.logback.classic.LoggerContext;
import net.openhft.chronicle.IndexedChronicle;
import net.openhft.chronicle.VanillaChronicle;
import net.openhft.chronicle.logger.ChronicleLogLevel;
import net.openhft.lang.io.Bytes;
import net.openhft.lang.io.serialization.BytesMarshallable;
import net.openhft.lang.model.constraints.NotNull;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;

/**
 *
 */
public class LogbackTestBase {

    // *************************************************************************
    //
    // *************************************************************************

    protected static ChronicleLogLevel[] LOG_LEVELS = ChronicleLogLevel.values();

    // *************************************************************************
    //
    // *************************************************************************

    protected static String rootPath() {
        return System.getProperty("java.io.tmpdir")
                + File.separator
                + "chronology-logback";
    }

    protected static String basePath(String type) {
        return rootPath()
                + File.separator
                + type;
    }

    protected static void log(Logger logger, ChronicleLogLevel level, String fmt, Object... args) {
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

    protected static void warmup(Logger logger) {
        final String str = StringUtils.rightPad("X", 64);
        for (int i = 0; i < 10; i++) {
            logger.info(str);
        }
    }

    // *************************************************************************
    //
    // *************************************************************************

    protected LoggerContext getLoggerContext() {
        return (LoggerContext) LoggerFactory.getILoggerFactory();
    }

    /**
     * @param type
     * @return
     */
    protected IndexedChronicle getIndexedChronicle(String type) throws IOException {
        return new IndexedChronicle(basePath(type));
    }

    /**
     * @param type
     * @return
     */
    protected VanillaChronicle getVanillaChronicle(String type) throws IOException {
        return new VanillaChronicle(basePath(type));
    }

    // *************************************************************************
    //
    // *************************************************************************

    protected final static class MySerializableData implements Serializable {
        private final Object data;

        public MySerializableData(Object data) {
            this.data = data;
        }

        @Override
        public String toString() {
            return this.data.toString();
        }
    }

    protected final static class MyMarshallableData implements BytesMarshallable {
        private Object data;

        public MyMarshallableData() {
            this(null);
        }

        public MyMarshallableData(Object data) {
            this.data = data;
        }

        @Override
        public void readMarshallable(@NotNull Bytes in) throws IllegalStateException {
            this.data = in.readObject();
        }

        @Override
        public void writeMarshallable(@NotNull Bytes out) {
            out.writeObject(data);
        }

        @Override
        public String toString() {
            return this.data.toString();
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
