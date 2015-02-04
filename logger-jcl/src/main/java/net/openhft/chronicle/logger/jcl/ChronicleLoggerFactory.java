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

import net.openhft.chronicle.Chronicle;
import net.openhft.chronicle.IndexedChronicle;
import net.openhft.chronicle.logger.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogConfigurationException;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.impl.NoOpLog;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ChronicleLoggerFactory extends LogFactory {
    private static final Log NOP_LOGGER = new NoOpLog();

    private final Map<String, Log> loggers;
    private final Map<String, ChronicleLogAppender> appenders;
    private ChronicleLogConfig cfg;

    public ChronicleLoggerFactory() {
        logRawDiagnostic("[CHRONICLE] Initialize ChronicleLoggerFactory");

        this.loggers = new ConcurrentHashMap<>();
        this.appenders = new ConcurrentHashMap<>();
        this.cfg = ChronicleLogConfig.load();

        logRawDiagnostic("[CHRONICLE] ChronicleLoggerFactory initialized");
    }

    @Override
    public void release() {
        for(ChronicleLogAppender appender : appenders.values()) {
            try {
                appender.getChronicle().close();
            } catch (IOException e) {
            }
        }

        this.loggers.clear();
        this.appenders.clear();
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
            return doGetLogger(name);
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

    private synchronized Log doGetLogger(String name) throws Exception {
        if (this.cfg == null) {
            throw new IllegalArgumentException("chronicle-jcl is not configured");
        }

        Log logger = loggers.get(name);
        if (logger == null) {
            String path  = cfg.getString(name, ChronicleLogConfig.KEY_PATH);
            String level = cfg.getString(name, ChronicleLogConfig.KEY_LEVEL);

            if (path != null) {
                logger = new ChronicleLogger(
                    newAppender(path, name),
                    name,
                    ChronicleLogLevel.fromStringLevel(level));

                loggers.put(name, logger);
            } else {
                if (path == null) {
                    throw new IllegalArgumentException(new StringBuilder()
                        .append("chronicle.logger.path is not defined")
                        .append(",")
                        .append("chronicle.logger.logger.")
                        .append(name)
                        .append(".path is not defined")
                        .toString()
                    );
                }
            }
        }

        return logger;
    }

    /**
     * @param path
     * @param name
     * @return
     * @throws java.io.IOException
     */
    private ChronicleLogAppender newAppender(String path, String name) throws Exception {
        ChronicleLogAppender appender = appenders.get(path);
        if(appender == null) {
            final String  type    = cfg.getString(name, ChronicleLogConfig.KEY_TYPE);
            final String  format  = cfg.getString(name, ChronicleLogConfig.KEY_FORMAT);
            final Integer stDepth = cfg.getInteger(ChronicleLogConfig.KEY_STACK_TRACE_DEPTH);

            if(!name.startsWith("net.openhft")) {
                if (ChronicleLogConfig.FORMAT_BINARY.equalsIgnoreCase(format)) {
                    appender = new ChronicleLogAppenders.BinaryWriter(
                        newChronicle(type, path, name)
                    );
                } else if (ChronicleLogConfig.FORMAT_TEXT.equalsIgnoreCase(format)) {
                    appender = new ChronicleLogAppenders.TextWriter(
                        newChronicle(type, path, name),
                        Formatter.INSTANCE,
                        ChronicleLogConfig.DEFAULT_DATE_FORMAT,
                        stDepth
                    );
                }
            } else {
                appender = new ChronicleLogAppenders.SimpleWriter(
                    Formatter.INSTANCE,
                    System.out
                );
            }

            if (appender != null) {
                // If the underlying chronicle is an Indexed chronicle, wrap the appender
                // so it is thread safe (synchronized)
                if (appender.getChronicle() instanceof IndexedChronicle) {
                    appender = new ChronicleLogAppenders.SynchronizedWriter(appender);
                }
            }

            this.appenders.put(path, appender);
        }

        return appender;
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

    static class Formatter implements ChronicleLogFormatter {
        static final Formatter INSTANCE = new Formatter();

        @Override
        public String format(String message, Throwable throwable, Object... args) {
            return message;
        }
    }
}
