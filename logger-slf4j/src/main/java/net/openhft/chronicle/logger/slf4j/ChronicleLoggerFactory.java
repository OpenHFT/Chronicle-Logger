/*
 * Copyright 2014 Higher Frequency Trading
 * <p/>
 * http://www.higherfrequencytrading.com
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.openhft.chronicle.logger.slf4j;

import net.openhft.chronicle.*;
import net.openhft.chronicle.logger.ChronicleLogLevel;
import org.slf4j.ILoggerFactory;
import org.slf4j.Logger;
import org.slf4j.helpers.NOPLogger;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * <p>Simple implementation of Logger that sends all enabled slf4j messages,
 * for all defined loggers, to one or more VanillaChronicle..
 * <p/>
 * To configure this sl4j binding you need to specify the location of a properties
 * files via system properties:
 * <p/>
 * <code>-Dslf4j.chronicle.properties=${pathOfYourPropertiesFile}<code>
 * </p>
 * <p/>
 * The following system properties are supported to configure the behavior of this
 * logger:
 * <p/>
 * <ul>
 * <li><code>slf4j.chronicle.path</code></li>
 * <li><code>slf4j.chronicle.level</code></li>
 * <li><code>slf4j.chronicle.shortName</code></li>
 * <li><code>slf4j.chronicle.append</code></li>
 * <li><code>slf4j.chronicle.format</code></li>
 * <li><code>slf4j.chronicle.type</code></li>
 * </ul>
 */
public class ChronicleLoggerFactory implements ILoggerFactory {
    private final Map<String, Logger> loggers;
    private final Map<String, ChronicleLogAppender> appenders;
    private ChronicleLoggingConfig cfg;

    /**
     * c-tor
     */
    public ChronicleLoggerFactory() {
        this(ChronicleLoggingConfig.load());
    }

    /**
     * c-tor
     */
    public ChronicleLoggerFactory(final ChronicleLoggingConfig cfg) {
        this.loggers = new ConcurrentHashMap<String, Logger>();
        this.appenders = new ConcurrentHashMap<String, ChronicleLogAppender>();
        this.cfg = ChronicleLoggingConfig.load();
    }

    // *************************************************************************
    //
    // *************************************************************************

    /**
     * Return an appropriate {@link ChronicleLogger} instance by name.
     * <p/>
     */
    @Override
    public Logger getLogger(String name) {
        try {
            return doGetLogger(name);
        } catch(Exception e) {
            System.err.println(
                new StringBuilder("Unable to inzialize chroncile-slf4j ")
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
        for (ChronicleLogAppender appender : this.appenders.values()) {
            try {
                appender.getChronicle().close();
            } catch (IOException e) {
                System.err.println(e.getMessage());
            }
        }

        loggers.clear();
        appenders.clear();
    }

    /**
     *
     */
    public synchronized void relaod() {
        shutdown();

        this.cfg = ChronicleLoggingConfig.load();
    }

    // *************************************************************************
    //
    // *************************************************************************

    private synchronized Logger doGetLogger(String name) throws Exception {
        if (this.cfg == null) {
            throw new IllegalArgumentException("chronicle-slf4j is not configured");
        }

        Logger logger = loggers.get(name);
        if (logger == null) {
            String path  = cfg.getString(name, ChronicleLoggingConfig.KEY_PATH);
            String level = cfg.getString(name, ChronicleLoggingConfig.KEY_LEVEL);

            if (path != null) {
                logger = new ChronicleLogger(
                    newAppender(path, name),
                    name,
                    ChronicleLogLevel.fromStringLevel(level));

                loggers.put(name, logger);
            } else {
                if (path == null) {
                    throw new IllegalArgumentException(new StringBuilder()
                        .append("slf4j.chronicle.path is not defined")
                        .append(",")
                        .append("slf4j.chronicle.logger.")
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
     * @throws IOException
     */
    private ChronicleLogAppender newAppender(String path, String name) throws Exception {
        ChronicleLogAppender appender = appenders.get(path);
        if(appender == null) {
            String type       = cfg.getString(name, ChronicleLoggingConfig.KEY_TYPE);
            String format     = cfg.getString(name, ChronicleLoggingConfig.KEY_FORMAT);
            String binaryMode = cfg.getString(ChronicleLoggingConfig.KEY_BINARY_MODE);
            Integer stDepth   = cfg.getInteger(ChronicleLoggingConfig.KEY_STACK_TRACE_DEPTH);

            if (ChronicleLoggingConfig.FORMAT_BINARY.equalsIgnoreCase(format)) {
                Chronicle chronicle = newChronicle(type, path, name);
                appender = ChronicleLoggingConfig.BINARY_MODE_SERIALIZED.equalsIgnoreCase(binaryMode)
                    ? new ChronicleLogAppenders.BinaryWriter(chronicle)
                    : new ChronicleLogAppenders.BinaryFormattingWriter(chronicle);
            } else if (ChronicleLoggingConfig.FORMAT_TEXT.equalsIgnoreCase(format)) {
                appender = new ChronicleLogAppenders.TextWriter(
                    newChronicle(type, path, name),
                    null, // TODO: this.cfg.getString(name, ChronicleLoggingConfig.KEY_DATE_FORMAT)
                    stDepth
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
     * @throws IOException
     */
    private Chronicle newChronicle(String type, String path, String name) throws Exception {
        if (ChronicleLoggingConfig.TYPE_INDEXED.equalsIgnoreCase(type)) {
            return newIndexedChronicle(path, name);
        } else if (ChronicleLoggingConfig.TYPE_VANILLA.equalsIgnoreCase(type)) {
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
        final VanillaChronicle chronicle = new VanillaChronicle(
            path,
            this.cfg.getVanillaChronicleConfig().cfg());

        if (!cfg.getBoolean(name, ChronicleLoggingConfig.KEY_APPEND, true)) {
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
        if (!cfg.getBoolean(name, ChronicleLoggingConfig.KEY_APPEND, true)) {
            new File(path + ".data").delete();
            new File(path + ".index").delete();
        }

        return new IndexedChronicle(path, this.cfg.getIndexedChronicleConfig().cfg());
    }
}

