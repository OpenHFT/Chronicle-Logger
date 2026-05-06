/*
 * Copyright 2013-2026 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
/**
 * Logback appenders that write events to Chronicle logs.
 * <p>
 * The classes in this package bridge Logback's logging model to Chronicle
 * storage, typically backed by Chronicle Queue. They translate
 * {@code ILoggingEvent} instances into Chronicle messages, preserving
 * structured data such as mapped diagnostic context (MDC), caller data and
 * stack traces when configured to do so.
 * <p>
 * These appenders are intended to be configured via standard Logback XML
 * or Groovy configuration. They form part of the supported Chronicle
 * logging surface but their internal helper classes may change between
 * releases.
 */
package net.openhft.chronicle.logger.logback;
