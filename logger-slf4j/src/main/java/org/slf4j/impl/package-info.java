/*
 * Copyright 2013-2026 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
/**
 * SLF4J&nbsp;1.7.x SPI implementation for Chronicle logging.
 * <p>
 * This package hosts the {@code StaticLoggerBinder} and related classes
 * required by the SLF4J 1.x service-provider contract. They install the
 * Chronicle logger factory as the active {@code ILoggerFactory} so that
 * calls via the classic {@code LoggerFactory} API are backed by Chronicle.
 * <p>
 * These classes are loaded reflectively by SLF4J and should not normally be
 * referenced directly. Their names and presence follow SLF4J expectations
 * but internal details may evolve over time.
 */
package org.slf4j.impl;
