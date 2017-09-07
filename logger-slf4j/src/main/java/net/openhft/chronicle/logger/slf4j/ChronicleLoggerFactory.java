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

package net.openhft.chronicle.logger.slf4j;

import net.openhft.chronicle.logger.*;
import org.slf4j.ILoggerFactory;
import org.slf4j.Logger;
import org.slf4j.helpers.NOPLogger;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * <p>Simple implementation of Logger that sends all enabled slf4j messages,
 * for all defined loggers, to one or more VanillaChronicle..
 * </p>
 * <p>
 * To configure this sl4j binding you need to specify the location of a properties
 * files via system properties:
 * </p>
 * <code>-Dchronicle.logger.properties=${pathOfYourPropertiesFile}</code>
 * <p>
 * The following system properties are supported to configure the behavior of this
 * logger:
 * </p>
 * <ul>
 * <li><code>chronicle.logger.root.path</code></li>
 * <li><code>chronicle.logger.root.level</code></li>
 * <li><code>chronicle.logger.root.append</code></li>
 * </ul>
 */
public class ChronicleLoggerFactory implements ILoggerFactory {
    private final Map<String, Logger> loggers;
    private ChronicleLogManager manager;

    /**
     * c-tor
     */
    public ChronicleLoggerFactory() {
        this.loggers = new ConcurrentHashMap<>();
        this.manager = ChronicleLogManager.getInstance();
    }

    // *************************************************************************
    //
    // *************************************************************************

    /**
     * Return an appropriate {@link ChronicleLogger} instance by name.
     */
    @Override
    public Logger getLogger(String name) {
        try {
            return doGetLogger(name);
        } catch (Exception e) {
            System.err.println("Unable to initialize chronicle-logger-slf4j (" + name + ")\n  " + e.getMessage());
        }

        return NOPLogger.NOP_LOGGER;
    }

    // *************************************************************************
    // for testing
    // *************************************************************************

    synchronized void reload() {
        this.loggers.clear();
        this.manager.reload();
    }

    // *************************************************************************
    //
    // *************************************************************************

    private synchronized Logger doGetLogger(String name) throws IOException {
        Logger logger = loggers.get(name);
        if (logger == null) {
            final ChronicleLogWriter writer = manager.getWriter(name);
            logger = new ChronicleLogger(writer, name, manager.cfg().getLevel(name));

            loggers.put(name, logger);
        }

        return logger;
    }
}

