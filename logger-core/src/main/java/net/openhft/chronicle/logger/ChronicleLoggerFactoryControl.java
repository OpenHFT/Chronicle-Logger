/*
 * Copyright 2013-2026 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.logger;

/**
 * Common control hook for Chronicle logger factories across SLF4J bindings.
 * <p>
 * Exposed so tests and tooling can reset configuration between runs without
 * relying on reflection or implementation-specific classes.
 */
public interface ChronicleLoggerFactoryControl {

    /**
     * Clear cached loggers and reload the underlying configuration.
     */
    void reload();
}
