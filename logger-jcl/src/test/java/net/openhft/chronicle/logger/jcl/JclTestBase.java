/*
 * Copyright 2014 Higher Frequency Trading
 *
 * http://www.higherfrequencytrading.com
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

import net.openhft.chronicle.Chronicle;
import net.openhft.chronicle.ChronicleQueueBuilder;
import net.openhft.chronicle.logger.ChronicleLogConfig;
import net.openhft.chronicle.logger.ChronicleLogLevel;
import net.openhft.lang.io.Bytes;
import net.openhft.lang.io.serialization.BytesMarshallable;
import net.openhft.lang.model.constraints.NotNull;
import org.apache.commons.logging.Log;

import java.io.IOException;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

public class JclTestBase {

    // *************************************************************************
    //
    // *************************************************************************

    protected static final ChronicleLogLevel[] LOG_LEVELS = ChronicleLogLevel.values();

    protected static String basePath(String type) {
        return System.getProperty("java.io.tmpdir")
                + System.getProperty("file.separator")
                + "chronicle-jcl"
                + System.getProperty("file.separator")
                + type
                + System.getProperty("file.separator")
                + new SimpleDateFormat("yyyyMMdd").format(new Date());
    }

    protected static String basePath(String type, String loggerName) {
        return basePath(type)
                + System.getProperty("file.separator")
                + loggerName;
    }

    protected static String indexedBasePath(String loggerName) {
        return basePath(ChronicleLogConfig.TYPE_INDEXED)
                + System.getProperty("file.separator")
                + loggerName;
    }

    protected static String vanillaBasePath(String loggerName) {
        return basePath(ChronicleLogConfig.TYPE_VANILLA)
                + System.getProperty("file.separator")
                + loggerName;
    }

    protected static void log(Log logger, ChronicleLogLevel level, String message) {
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

    // *************************************************************************
    //
    // *************************************************************************

    /**
     * @param id
     * @return
     */
    protected Chronicle getIndexedChronicle(String id) throws IOException {
        return ChronicleQueueBuilder.indexed(basePath(ChronicleLogConfig.TYPE_INDEXED, id)).build();
    }

    /**
     * @param type
     * @param id
     * @return
     */
    protected Chronicle getIndexedChronicle(String type, String id) throws IOException {
        return ChronicleQueueBuilder.indexed(basePath(type, id)).build();
    }

    /**
     * @param id
     * @return
     */
    protected Chronicle getVanillaChronicle(String id) throws IOException {
        return ChronicleQueueBuilder.vanilla(basePath(ChronicleLogConfig.TYPE_VANILLA, id)).build();
    }

    /**
     * @param type
     * @param id
     * @return
     */
    protected Chronicle getVanillaChronicle(String type, String id) throws IOException {
        return ChronicleQueueBuilder.vanilla(basePath(type, id)).build();
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
}
