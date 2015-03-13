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
package net.openhft.chronicle.logger;

import net.openhft.chronicle.Chronicle;
import net.openhft.chronicle.IndexedChronicle;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ChronicleLogManager {
    private ChronicleLogConfig cfg;
    private Map<String, ChronicleLogWriter> writers;

    private ChronicleLogManager() {
        this.cfg = ChronicleLogConfig.load();
        this.writers = new ConcurrentHashMap<>();
    }

    public ChronicleLogConfig cfg() {
        return this.cfg;
    }

    public void clear() {
        for(final ChronicleLogWriter writer : writers.values()) {
            try {
                writer.close();
            } catch (IOException e) {
            }
        }

        writers.clear();
    }

    public void reload() {
        clear();

        this.cfg = ChronicleLogConfig.load();
        this.writers = new ConcurrentHashMap<>();
    }

    public boolean isBinary(String name) {
        return ChronicleLogConfig.FORMAT_BINARY.equalsIgnoreCase(
            cfg.getString(name, ChronicleLogConfig.KEY_FORMAT)
        );
    }

    public boolean isText(String name) {
        return ChronicleLogConfig.FORMAT_TEXT.equalsIgnoreCase(
            cfg.getString(name, ChronicleLogConfig.KEY_FORMAT)
        );
    }

    public boolean isSimple(String name) {
        for(String pkg : ChronicleLogConfig.PACKAGE_MASK) {
            if(name.startsWith(pkg)) {
                return true;
            }
        }

        return false;
    }

    /**
     * @param name
     * @param name
     * @return
     * @throws java.io.IOException
     */
    public ChronicleLogWriter createWriter(String name) throws Exception {
        if (this.cfg == null) {
            throw new IllegalArgumentException("ChronicleLogManager is not configured");
        }

        final String path = cfg.getString(name, ChronicleLogConfig.KEY_PATH);
        if (path != null) {
            ChronicleLogWriter appender = writers.get(path);
            if (appender == null) {
                final Integer stDepth = cfg.getInteger(ChronicleLogConfig.KEY_STACK_TRACE_DEPTH);
                final String type = cfg.getString(name, ChronicleLogConfig.KEY_TYPE);

                if (!isSimple(name)) {
                    if (isBinary(name)) {
                        appender = new ChronicleLogWriters.BinaryWriter(
                            newChronicle(type, path, name)
                        );
                    } else if (isText(name)) {
                        appender = new ChronicleLogWriters.TextWriter(
                            newChronicle(type, path, name),
                            ChronicleLogConfig.DEFAULT_DATE_FORMAT,
                            stDepth
                        );
                    }
                } else {
                    appender = new ChronicleLogWriters.SimpleWriter(
                        System.out
                    );
                }

                if (appender.getChronicle() instanceof IndexedChronicle) {
                    appender = new ChronicleLogWriters.SynchronizedWriter(appender);
                }

                this.writers.put(path, appender);
            }

            return appender;
        } else {
            throw new IllegalArgumentException(new StringBuilder()
                .append("chronicle.logger.root.path is not defined")
                .append(",")
                .append("chronicle.logger.")
                .append(name)
                .append(".path is not defined")
                .toString()
            );
        }
    }

    /**
     * @param type
     * @param path
     * @param name
     * @return
     * @throws java.io.IOException
     */
    private Chronicle newChronicle(String type, String path, String name) throws Exception {
        if (ChronicleLogConfig.TYPE_INDEXED.equalsIgnoreCase(type)) {
            return newIndexedChronicle(path, name);
        } else if (ChronicleLogConfig.TYPE_VANILLA.equalsIgnoreCase(type)) {
            return newVanillaChronicle(path, name);
        }

        throw new IllegalArgumentException("type should be indexed or vanilla");
    }

    /**
     * Make a VanillaChronicle with default configuration;
     *
     * @param path
     * @param name #param synchronous
     * @return
     */
    private Chronicle newVanillaChronicle(String path, String name) throws IOException {
        final Chronicle chronicle = this.cfg.getVanillaChronicleConfig().build(path);

        if (!cfg.getBoolean(name, ChronicleLogConfig.KEY_APPEND, true)) {
            chronicle.clear();
        }

        return chronicle;
    }

    /**
     * Make an IndexedChronicle with default configuration;
     *
     * @param path
     * @param name
     * @return
     */
    private Chronicle newIndexedChronicle(String path, String name) throws IOException {
        if (!cfg.getBoolean(name, ChronicleLogConfig.KEY_APPEND, true)) {
            new File(path + ".data").delete();
            new File(path + ".index").delete();
        }

        return this.cfg.getIndexedChronicleConfig().build(path);
    }

    // *************************************************************************
    //
    // *************************************************************************

    public static ChronicleLogManager getInstance() {
        return Holder.INSTANCE;
    }

    // *************************************************************************
    //
    // *************************************************************************

    private static class Holder {
        private static final ChronicleLogManager INSTANCE = new ChronicleLogManager();
    }
}
