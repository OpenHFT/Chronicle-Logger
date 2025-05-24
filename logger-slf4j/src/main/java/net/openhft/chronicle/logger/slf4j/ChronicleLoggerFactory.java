/*
 * Copyright 2014-2020 chronicle.software
 *
 *       https://chronicle.software
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
package net.openhft.chronicle.logger.slf4j;

import net.openhft.chronicle.logger.ChronicleLogManager;
import net.openhft.chronicle.logger.ChronicleLogWriter;
import org.slf4j.ILoggerFactory;
import org.slf4j.Logger;
import org.slf4j.helpers.NOPLogger;
import org.slf4j.impl.SimpleLogger;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Factory for {@link ChronicleLogger} instances used by the SLF4J binding.
 *
 * <p>The factory reads its settings from the properties file supplied via the
 * system property {@code chronicle.logger.properties}. A minimal file looks
 * like:
 *
 * <pre>{@code
 * chronicle.logger.base       = ${java.io.tmpdir}/chronicle-logs/${pid}
 * chronicle.logger.root.path  = ${chronicle.logger.base}/main
 * chronicle.logger.root.level = debug
 * chronicle.logger.root.append = false
 * }</pre>
 *
 * <p>Per logger configuration can be added using the prefix
 * {@code chronicle.logger.&lt;name&gt;.}.
 */
public class ChronicleLoggerFactory implements ILoggerFactory {
    private final Map<String, Logger> loggers;
    private final ChronicleLogManager manager;

    // *************************************************************************
    //
    // *************************************************************************

    /**
     * c-tor
     */
    public ChronicleLoggerFactory() {
        this.loggers = new ConcurrentHashMap<>();
        this.manager = ChronicleLogManager.getInstance();
    }

    // *************************************************************************
    // for testing
    // *************************************************************************

    /**
     * Returns the logger identified by {@code name}. If the configuration
     * cannot be read a {@link NOPLogger} is returned.
     *
     * @param name logical name of the logger
     * @return logger instance or {@link NOPLogger} when disabled
     */
    @Override
    public Logger getLogger(String name) {
        try {
            return doGetLogger(name);
        } catch (Exception e) {
            System.err.println("Unable to initialize chronicle-logger-slf4j (" + name + ")\n  " + e.getMessage());
            e.printStackTrace();
        }

        return NOPLogger.NOP_LOGGER;
    }

    // *************************************************************************
    //
    // *************************************************************************

    /**
     * Reloads the configuration and clears cached loggers. Used mainly by
     * unit tests to reinitialise the factory.
     */
    synchronized void reload() {
        this.loggers.clear();
        this.manager.reload();
    }

    private synchronized Logger doGetLogger(String name) {
        Logger logger = loggers.get(name);
        if (logger == null) {
            if (name != null && name.startsWith("net.openhft")) {
                SimpleLogger.lazyInit();
                logger = new SimpleLogger(name);
            } else {
                final ChronicleLogWriter writer = manager.getWriter(name);
                logger = new ChronicleLogger(writer, name, manager.cfg().getLevel(name));
            }
            loggers.put(name, logger);
        }

        return logger;
    }
}
