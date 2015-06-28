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
package net.openhft.chronicle.logger.jul;

import net.openhft.chronicle.logger.ChronicleLogLevel;
import net.openhft.chronicle.logger.ChronicleLogManager;
import net.openhft.chronicle.logger.ChronicleLogWriter;

import java.util.Collections;
import java.util.Enumeration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.LogManager;
import java.util.logging.Logger;

public class ChronicleLoggerManager extends LogManager {

    private final Map<String, Logger> loggers;
    private final ChronicleLogManager manager;

    public ChronicleLoggerManager() {
        this.loggers = new ConcurrentHashMap<>();
        this.manager = ChronicleLogManager.getInstance();
    }

    @Override
    public boolean addLogger(final Logger logger) {
        return false;
    }

    @Override
    public Logger getLogger(final String name) {
        try {
            return doGetLogger(name);
        } catch(Exception e) {
            System.err.println(
                new StringBuilder("Unable to initialize chronicle-logger-jul ")
                    .append("(")
                    .append(name)
                    .append(")")
                    .append("\n  ")
                    .append(e.getMessage())
                    .toString()
            );
        }

        return ChronicleLogger.Null.INSTANCE;
    }

    @Override
    public Enumeration<String> getLoggerNames() {
        return Collections.enumeration(this.loggers.keySet());
    }

    @Override
    public void reset() throws SecurityException {
        this.loggers.clear();
        this.manager.clear();
    }

    // *************************************************************************
    //
    // *************************************************************************

    private synchronized Logger doGetLogger(String name)   {
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
