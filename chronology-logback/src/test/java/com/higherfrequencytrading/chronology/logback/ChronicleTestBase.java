package com.higherfrequencytrading.chronology.logback;

import ch.qos.logback.classic.Level;
import net.openhft.chronicle.IndexedChronicle;
import net.openhft.chronicle.VanillaChronicle;
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
public class ChronicleTestBase {

    // *************************************************************************
    //
    // *************************************************************************

    protected static Level[] LOG_LEVELS = new Level[] {
        Level.TRACE,
        Level.DEBUG,
        Level.INFO,
        Level.WARN,
        Level.ERROR
    };

    protected static String rootPath() {
        return System.getProperty("java.io.tmpdir")
            + File.separator
            + "chronicle-logback";
    }

    protected static String basePath(String type) {
        return rootPath()
            + File.separator
            + type;
    }

    // *************************************************************************
    //
    // *************************************************************************

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

    protected final class RunnableChronicle implements Runnable {
        private final Logger logger;
        private final int runs;
        private final String msg;

        public RunnableChronicle(int runs, int size, String loggerName) {
            this.logger = LoggerFactory.getLogger(loggerName);
            this.runs = runs;
            this.msg = StringUtils.rightPad("", size, "X");
        }

        @Override
        public void run() {
            try {
                for (int i = 0; i < this.runs; i++) {
                    this.logger.info("{},{}", this.msg, i);
                }
            } catch (Exception e) {
                this.logger.warn("Exception", e);
            }
        }
    }
}
