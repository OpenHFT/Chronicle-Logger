/*
 * Copyright 2013-2026 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package org.slf4j.impl;

import net.openhft.chronicle.logger.slf4j.ChronicleLoggerFactory;
import org.slf4j.ILoggerFactory;

/**
 * Binds the SLF4J API to the Chronicle logger factory.
 *
 * <p>This binder installs {@link ChronicleLoggerFactory} as the SLF4J provider.
 */
// SLF4J 1.x SPI retained so this artefact can still act as a classic
// StaticLoggerBinder for applications that expect that contract. The
// LoggerFactoryBinder interface is deprecated in SLF4J 2.x but remains
// supported, so we suppress the deprecation warning here.
@SuppressWarnings("deprecation")
public class StaticLoggerBinder implements org.slf4j.spi.LoggerFactoryBinder {

    private static final StaticLoggerBinder SINGLETON = new StaticLoggerBinder();
    private static final String loggerFactoryClassStr = ChronicleLoggerFactory.class.getName();
    /**
     * Declare the version of the SLF4J API this implementation is compiled
     * against. The value of this field is usually modified with each release.
     */
    // to avoid constant folding by the compiler, this field must *not* be final
    public static String REQUESTED_API_VERSION = "1.7.30";  // !final
    /**
     * The ILoggerFactory instance returned by the {@link #getLoggerFactory}
     * method should always be the same object
     */
    private final ILoggerFactory loggerFactory;

    /**
     * c-tor
     */
    private StaticLoggerBinder() {
        loggerFactory = new ChronicleLoggerFactory();
    }

    /**
     * Return the singleton of this class.
     *
     * @return the StaticLoggerBinder singleton
     */
    public static StaticLoggerBinder getSingleton() {
        return SINGLETON;
    }

    @Override
    public ILoggerFactory getLoggerFactory() {
        return loggerFactory;
    }

    @Override
    public String getLoggerFactoryClassStr() {
        return loggerFactoryClassStr;
    }
}
