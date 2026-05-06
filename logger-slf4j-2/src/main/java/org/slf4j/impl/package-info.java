/*
 * Copyright 2013-2026 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
/**
 * SLF4J&nbsp;2.x service-provider implementation for Chronicle logging.
 * <p>
 * Types in this package implement the SLF4J service-provider interface and
 * wire the SLF4J API to the Chronicle logger factory. They are discovered
 * via Java's service-loader mechanism and are not intended to be referenced
 * directly by application code.
 * <p>
 * Behaviour follows the contracts defined by the SLF4J API. The set of
 * implementation classes may change between releases without prior notice.
 */
package org.slf4j.impl;
