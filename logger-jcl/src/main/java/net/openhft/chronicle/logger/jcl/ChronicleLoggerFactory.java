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

import net.openhft.chronicle.logger.ChronicleLogConfig;
import net.openhft.chronicle.logger.ChronicleLogLevel;
import net.openhft.chronicle.logger.ChronicleLogManager;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogConfigurationException;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.impl.NoOpLog;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ChronicleLoggerFactory extends LogFactory {
    private static final Log NOP_LOGGER = new NoOpLog();

    private final Map<String, ChronicleLogger> loggers;
    private ChronicleLogManager manager;

    public ChronicleLoggerFactory() {
        logRawDiagnostic("[CHRONICLE] Initialize ChronicleLoggerFactory");

        this.loggers = new ConcurrentHashMap<>();
        this.manager = new ChronicleLogManager();

        logRawDiagnostic("[CHRONICLE] ChronicleLoggerFactory initialized");
    }

    @Override
    public void release() {
        this.loggers.clear();
        this.manager.clear();
    }

    @Override
    public Object getAttribute(String s) {
        return null;
    }

    @Override
    public void setAttribute(String s, Object o) {
    }

    @Override
    public String[] getAttributeNames() {
        return new String[0];
    }

    @Override
    public void removeAttribute(String s) {
    }

    @Override
    public Log getInstance(Class type) throws LogConfigurationException {
        return getInstance(type.getName());
    }

    @Override
    public Log getInstance(String name) throws LogConfigurationException {
        try {
            return getLogger(name);
        } catch(Exception e) {
            System.err.println(
                new StringBuilder("Unable to inzialize chronicle-jcl ")
                    .append("(")
                    .append(name)
                    .append(")")
                    .append("\n  ")
                    .append(e.getMessage())
                    .toString()
            );
        }

        return NOP_LOGGER;
    }


    // *************************************************************************
    //
    // *************************************************************************

    private synchronized Log getLogger(String name) throws Exception {

        ChronicleLogger logger = loggers.get(name);
        if (logger == null) {
            String level = manager.cfg().getString(name, ChronicleLogConfig.KEY_LEVEL);

            logger = new ChronicleLogger(
                manager.createWriter(name),
                name,
                ChronicleLogLevel.fromStringLevel(level));

            loggers.put(name, logger);
        }

        return logger;
    }
}
