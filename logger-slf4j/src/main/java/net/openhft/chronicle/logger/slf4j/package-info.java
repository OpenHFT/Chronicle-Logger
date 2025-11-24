/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
/**
 * SLF4J&nbsp;1.7.x binding backed by Chronicle logging.
 * <p>
 * The classes in this package provide an {@code ILoggerFactory} and
 * supporting infrastructure that route SLF4J 1.x log events to Chronicle
 * writers. This allows existing code using the classic SLF4J API to log to
 * Chronicle without code changes.
 * <p>
 * Configuration is driven by properties (for example
 * {@code chronicle.logger.root.path}) and by SLF4J conventions rather than
 * by direct use of these types. Where practical the behaviour mirrors that
 * of the SLF4J 2.x binding to ease migration.
 */
package net.openhft.chronicle.logger.slf4j;
