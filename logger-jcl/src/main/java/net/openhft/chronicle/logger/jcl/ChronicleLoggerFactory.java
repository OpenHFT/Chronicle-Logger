/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.logger.jcl;

import net.openhft.chronicle.logger.ChronicleLogConfig;
import net.openhft.chronicle.logger.ChronicleLogLevel;
import net.openhft.chronicle.logger.ChronicleLogManager;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogConfigurationException;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.impl.NoOpLog;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Commons Logging {@link LogFactory} that creates and caches
 * {@link ChronicleLogger} instances.
 * <p>
 * Loggers are looked up by name and backed by a
 * {@link net.openhft.chronicle.logger.ChronicleLogWriter} obtained from
 * {@link ChronicleLogManager}. Configuration is read once by the manager from
 * {@code chronicle.logger.properties} or one of the default locations. If
 * the configuration cannot be resolved a no-op logger is returned.
 */
public class ChronicleLoggerFactory extends LogFactory {
    private static final Log NOP_LOGGER = new NoOpLog();

    private final Map<String, ChronicleLogger> loggers;
    private final ChronicleLogManager manager;

    /**
     * Builds the factory and initialises the {@link ChronicleLogManager} used
     * to configure log writers. The manager loads its configuration from the
     * standard properties file on first use.
     */
    public ChronicleLoggerFactory() {
        logRawDiagnostic("[CHRONICLE] Initialize ChronicleLoggerFactory");

        this.loggers = new ConcurrentHashMap<>();
        this.manager = ChronicleLogManager.getInstance();

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

    /**
     * Obtain a logger using the class name.
     */
    @SuppressWarnings("rawtypes")
    @Override
    public Log getInstance(Class type) throws LogConfigurationException {
        // Delegate to the name based variant
        return getInstance(type.getName());
    }

    /**
     * Returns a cached {@link ChronicleLogger} configured for the supplied
     * name. The writer and level are resolved from the manager configuration.
     * A {@link NoOpLog} is returned when configuration fails.
     */
    @Override
    public Log getInstance(String name) throws LogConfigurationException {
        try {
            return getLogger(name);
        } catch (Exception e) {
            System.err.println("Unable to initialise chronicle-jcl (" + name + ")\n  " + e.getMessage());
        }

        return NOP_LOGGER;
    }

    // *************************************************************************
    //
    // *************************************************************************

    /**
     * Create or return a cached logger for the given name. The method is
     * synchronised so that only one instance per name is created.
     */
    private synchronized Log getLogger(String name) throws IOException {
        ChronicleLogger logger = loggers.get(name);
        if (logger == null) {
            loggers.put(
                    name,
                    logger = new ChronicleLogger(
                            manager.getWriter(name),
                            name,
                            ChronicleLogLevel.fromStringLevel(
                                    manager.cfg().getString(name, ChronicleLogConfig.KEY_LEVEL)
                            )
                    )
            );
        }

        return logger;
    }
}
