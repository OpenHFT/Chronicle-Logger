package com.higherfrequencytrading.chronology.slf4j;

import com.higherfrequencytrading.chronology.ChronologyLogLevel;
import net.openhft.chronicle.IndexedChronicle;
import net.openhft.chronicle.VanillaChronicle;
import net.openhft.lang.io.Bytes;
import net.openhft.lang.io.serialization.BytesMarshallable;
import net.openhft.lang.model.constraints.NotNull;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.impl.StaticLoggerBinder;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Slf4jTestBase {

    // *************************************************************************
    //
    // *************************************************************************

    protected static ChronologyLogLevel[] LOG_LEVELS = ChronologyLogLevel.values();

    protected static String basePath(String type) {
        return System.getProperty("java.io.tmpdir")
            + File.separator
            + "chronology-slf4j"
            + File.separator
            + type
            + File.separator
            + new SimpleDateFormat("yyyyMMdd").format(new Date());
    }

    protected static String basePath(String type, String loggerName) {
        return basePath(type)
            + File.separator
            + loggerName;
    }

    protected static void log(Logger logger, ChronologyLogLevel level, String fmt, Object... args) {
        switch(level) {
            case TRACE:
                logger.trace(fmt,args);
                break;
            case DEBUG:
                logger.debug(fmt,args);
                break;
            case INFO:
                logger.info(fmt,args);
                break;
            case WARN:
                logger.warn(fmt,args);
                break;
            case ERROR:
                logger.error(fmt,args);
                break;
            default:
                throw new UnsupportedOperationException();
        }
    }

    // *************************************************************************
    //
    // *************************************************************************

    /**
     * @return the ChronicleLoggerFactory singleton
     */
    protected ChronicleLoggerFactory getChronicleLoggerFactory() {
        return (ChronicleLoggerFactory) StaticLoggerBinder.getSingleton().getLoggerFactory();
    }

    /**
     * @param type
     * @param id
     * @return
     */
    protected IndexedChronicle getIndexedChronicle(String type, String id) throws IOException {
        return new IndexedChronicle(basePath(type, id));
    }

    /**
     * @param type
     * @param id
     * @return
     */
    protected VanillaChronicle getVanillaChronicle(String type, String id) throws IOException {
        return new VanillaChronicle(basePath(type, id));
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
            this.fmt = StringUtils.rightPad(fmtBase, pad + fmtBase.length(), "X");
        }

        @Override
        public void run() {
            for (int i = 0; i < this.runs; i++) {
                this.logger.info(fmt,i, i * 7,i / 16);
            }
        }
    }
}
