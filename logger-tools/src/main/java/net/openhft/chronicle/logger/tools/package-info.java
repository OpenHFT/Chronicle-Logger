/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
/**
 * Command-line tools for working with Chronicle logs.
 * <p>
 * Utilities in this package provide simple inspection and processing of
 * Chronicle log directories, including tailing, printing and transforming
 * stored log events. They are intended for operational use and ad-hoc
 * diagnostics rather than as embedded libraries.
 * <p>
 * Tools rely on Chronicle Queue and Chronicle Wire for storage and
 * serialisation. They assume exclusive access to the target directory when
 * performing destructive operations.
 */
package net.openhft.chronicle.logger.tools;
