/*
 * Copyright 2014-2017 Chronicle Software
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
package net.openhft.chronicle.logger;

import net.openhft.chronicle.queue.ChronicleQueue;

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
        for (final ChronicleLogWriter writer : writers.values()) {
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

    public ChronicleLogWriter getWriter(String name) throws IOException {
        if (this.cfg == null) {
            throw new IllegalArgumentException("ChronicleLogManager is not configured");
        }

        final String path = cfg.getString(name, ChronicleLogConfig.KEY_PATH);
        if (path != null) {
            ChronicleLogWriter logWriter = writers.get(path);
            if (logWriter == null) {
                logWriter = new DefaultChronicleLogWriter(newChronicle(path, name));
                this.writers.put(path, logWriter);
            }

            return logWriter;

        } else {
            throw new IllegalArgumentException(
                    "chronicle.logger.root.path is not defined, chronicle.logger." + name + ".path is not defined"
            );
        }
    }

    private ChronicleQueue newChronicle(String path, String name) throws IOException {
        final String wireType = cfg.getString(name, ChronicleLogConfig.KEY_WIRETYPE);
        ChronicleQueue cq = this.cfg.getAppenderConfig().build(path, wireType);
        if (!cfg.getBoolean(name, ChronicleLogConfig.KEY_APPEND, true)) {
            // TODO re-enable when it's implemented. ATM it throws UnsupportedOperationException...
            //cq.clear();
        }
        return cq;
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

        private Holder() {

        }
    }
}
