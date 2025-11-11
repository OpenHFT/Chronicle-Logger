/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.logger.slf4j2;

import net.openhft.chronicle.logger.ChronicleLogManager;
import net.openhft.chronicle.logger.ChronicleLogWriter;
import org.slf4j.ILoggerFactory;
import org.slf4j.Logger;
import org.slf4j.helpers.NOPLogger;
import org.slf4j.impl.SimpleLogger;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Factory for {@link ChronicleLogger} instances used by the SLF4J 2.x API.
 * <p>
 * It forwards all enabled log events to Chronicle Queue writers.  The factory
 * is discovered via the SLF4J service provider mechanism, so including the
 * {@code chronicle-logger-slf4j-2} jar on the classpath is normally enough to
 * activate it.
 * <p>
 * To customise logging, specify a properties file with the system property
 * {@code chronicle.logger.properties}.  The following keys affect the default
 * logger configuration:
 * <ul>
 * <li>{@code chronicle.logger.root.path}</li>
 * <li>{@code chronicle.logger.root.level}</li>
 * <li>{@code chronicle.logger.root.append}</li>
 * </ul>
 * Example usage:
 * <pre>
 *     Logger log = LoggerFactory.getLogger("my.app");
 *     log.info("Started");
 * </pre>
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
     * Return the {@link ChronicleLogger} associated with the supplied name.
     * <p>
     * Instances are cached.  Names starting with {@code net.openhft} fall back
     * to {@link SimpleLogger} to avoid recursive use of Chronicle loggers.  If
     * a logger cannot be created this method returns {@link NOPLogger#NOP_LOGGER}.
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
     * Clear cached loggers and reload the manager configuration.
     * <p>
     * Primarily used by tests when the properties file has changed.
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
