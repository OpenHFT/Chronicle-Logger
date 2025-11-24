/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
/**
 * SLF4J&nbsp;2.x binding backed by Chronicle logging.
 * <p>
 * This package provides the {@code ChronicleLoggerFactory} and related
 * infrastructure used when Chronicle is selected as the SLF4J 2.x
 * provider. It connects the SLF4J API to Chronicle log writers so that
 * application code using {@code org.slf4j.Logger} can emit events into
 * Chronicle-backed queues without being aware of the underlying storage.
 * <p>
 * Configuration is typically supplied via properties (for example
 * {@code chronicle.logger.root.path}) rather than by using these classes
 * directly. The package is part of the public integration surface for
 * Chronicle logging.
 */
package net.openhft.chronicle.logger.slf4j2;
