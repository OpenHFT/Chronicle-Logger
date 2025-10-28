/*
 *  Copyright 2014-2025 chronicle.software
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.openhft.chronicle.logger;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import net.openhft.chronicle.queue.ChronicleQueue;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages {@link ChronicleLogWriter} instances using a shared configuration.
 * <p>
 * The manager loads {@link ChronicleLogConfig} once and returns cached writers
 * so that loggers may be shared safely between threads.
 */
public class ChronicleLogManager {
    private ChronicleLogConfig cfg;
    private Map<String, ChronicleLogWriter> writers;

    private ChronicleLogManager() {
        this.cfg = ChronicleLogConfig.load();
        this.writers = new ConcurrentHashMap<>();
    }

    /**
     * Return the singleton manager.
     *
     * @return the global instance
     */
    public static ChronicleLogManager getInstance() {
        return Holder.INSTANCE;
    }

    /**
     * Provide the current configuration.
     *
     * @return configuration in use, or {@code null} if loading failed
     */
    public ChronicleLogConfig cfg() {
        return this.cfg;
    }

    /**
     * Close all cached writers and forget them.
     */
    public void clear() {
        for (final ChronicleLogWriter writer : writers.values()) {
            try {
                writer.close();
            } catch (IOException e) {
                System.err.println("Unable to close ChronicleLogWriter instance.");
            }
        }

        writers.clear();
    }

    /**
     * Reload the configuration and remove all writers.
     */
    public void reload() {
        clear();

        this.cfg = ChronicleLogConfig.load();
        this.writers = new ConcurrentHashMap<>();
    }

    /**
     * Obtain a writer for the supplied logger name.
     *
     * @param name configuration entry to read
     * @return cached writer instance
     * @throws IllegalArgumentException if the configuration is missing or does not define a path
     */
    public ChronicleLogWriter getWriter(String name) {
        if (this.cfg == null) {
            throw new IllegalArgumentException("ChronicleLogManager is not configured");
        }

        final String path = cfg.getString(name, ChronicleLogConfig.KEY_PATH);
        if (path != null) {
            // Creating a Queue takes some time. Other threads might be blocked for longer periods.
            return writers.computeIfAbsent(path, p -> new DefaultChronicleLogWriter(newChronicle(p, name)));
        } else {
            throw new IllegalArgumentException(
                    "chronicle.logger.root.path is not defined, chronicle.logger." + name + ".path is not defined"
            );
        }
    }

    // *************************************************************************
    //
    // *************************************************************************

    @SuppressFBWarnings(value = "UCF_USELESS_CONTROL_FLOW",
            justification = "CLG-FN-002: append=false queues reuse files until explicit clear is implemented.")
    private ChronicleQueue newChronicle(String path, String name) {
        final String wireType = cfg.getString(name, ChronicleLogConfig.KEY_WIRETYPE);
        ChronicleQueue cq = this.cfg.getAppenderConfig().build(path, wireType);
        if (!cfg.getBoolean(name, ChronicleLogConfig.KEY_APPEND, true)) { // NOPMD - EmptyControlStatement
            // Queue clear is currently unsupported; files rotate naturally on reopen.
        }
        return cq;
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
