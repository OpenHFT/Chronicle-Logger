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
 * <li><code>chronicle.logger.root.shortName</code></li>
 * <li><code>chronicle.logger.root.append</code></li>
 * <li><code>chronicle.logger.root.format</code></li>
 * <li><code>chronicle.logger.root.type</code></li>
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
        } catch(Exception e) {
            System.err.println(
                new StringBuilder("Unable to initialize chronicle-logger-slf4j ")
                    .append("(")
                    .append(name)
                    .append(")")
                    .append("\n  ")
                    .append(e.getMessage())
                    .toString()
            );
        }

        return NOPLogger.NOP_LOGGER;
    }

    // *************************************************************************
    //
    // *************************************************************************

    /**
     * Preload loggers
     */
    public synchronized void warmup() {
    }

    /**
     * close underlying Chronicles
     */
    public synchronized void shutdown() {
        this.manager.clear();
        this.loggers.clear();
    }

    /**
     *
     */
    public synchronized void reload() {
        shutdown();

        this.manager.reload();
    }

    // *************************************************************************
    //
    // *************************************************************************

    private synchronized Logger doGetLogger(String name) throws Exception {
        Logger logger = loggers.get(name);
        if (logger == null) {
            final ChronicleLogWriter writer = manager.createWriter(name);
            if(manager.isSimple(name)) {
                logger = new ChronicleLogger.Text(
                    writer,
                    name,
                    ChronicleLogLevel.WARN);
            } else if(manager.isBinary(name)) {
                logger = new ChronicleLogger.Binary(
                    writer,
                    name,
                    manager.cfg().getLevel(name));
            } else if(manager.isText(name)) {
                logger = new ChronicleLogger.Text(
                    writer,
                    name,
                    manager.cfg().getLevel(name));
            }

            if(logger != null) {
                loggers.put(name, logger);
            } else {
                System.err.println(
                    new StringBuilder("Unable to get a logger for ")
                        .append("(")
                        .append(name)
                        .append(")")
                        .toString()
                );
            }
        }

        return logger;
    }
}

