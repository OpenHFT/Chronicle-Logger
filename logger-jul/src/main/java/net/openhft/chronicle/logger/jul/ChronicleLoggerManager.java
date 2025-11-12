/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.logger.jul;

import net.openhft.chronicle.logger.ChronicleLogManager;
import net.openhft.chronicle.logger.ChronicleLogWriter;

import java.io.IOException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.LogManager;
import java.util.logging.Logger;

/**
 * {@link LogManager} implementation that creates and caches JUL loggers backed
 * by Chronicle.  Each call to {@link #getLogger(String)} returns the same
 * {@link ChronicleLogger} instance for a given name.  Loggers are backed by
 * {@link ChronicleLogWriter} instances supplied by {@link ChronicleLogManager}.
 */
public class ChronicleLoggerManager extends LogManager {

    private final Map<String, Logger> loggers;
    private final ChronicleLogManager manager;

    public ChronicleLoggerManager() {
        this.loggers = new ConcurrentHashMap<>();
        this.manager = ChronicleLogManager.getInstance();
    }

    /**
     * Rejects attempts to add loggers externally.  The manager creates loggers
     * on demand so this method always returns {@code false}.
     */
    @Override
    public boolean addLogger(final Logger logger) {
        return false;
    }

    /**
     * Return the named logger.  A new {@link ChronicleLogger} is created if one
     * has not already been cached.  If creation fails a null logger instance is
     * returned so logging continues without throwing an exception.
     */
    @Override
    public Logger getLogger(final String name) {
        try {
            return doGetLogger(name);
        } catch (Exception e) {
            System.err.println("Unable to initialize chronicle-logger-jul (" + name + ")\n  " + e.getMessage());
        }

        return ChronicleLogger.Null.INSTANCE;
    }

    @Override
    public Enumeration<String> getLoggerNames() {
        return Collections.enumeration(this.loggers.keySet());
    }

    /**
     * Remove all cached loggers and close their writers via the underlying
     * {@link ChronicleLogManager}.
     */
    @Override
    public void reset() throws SecurityException {
        this.loggers.clear();
        this.manager.clear();
    }

    /**
     * Internal helper that creates or retrieves the logger for the supplied
     * name.  The method is synchronised to avoid duplicate creation when many
     * threads request the same logger concurrently.
     */
    private synchronized Logger doGetLogger(String name) throws IOException {
        Logger logger = loggers.get(name);
        if (logger == null) {
            final ChronicleLogWriter writer = manager.getWriter(name);
            logger = new ChronicleLogger(
                    writer,
                    name,
                    manager.cfg().getLevel(name));
            loggers.put(name, logger);
        }

        return logger;
    }
}
