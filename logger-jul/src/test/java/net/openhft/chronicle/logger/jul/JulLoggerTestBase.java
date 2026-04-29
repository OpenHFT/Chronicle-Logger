/*
 * Copyright 2013-2026 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.logger.jul;

import java.util.logging.LogManager;

class JulLoggerTestBase extends JulTestBase {

    static String basePath() {
        String path = System.getProperty("java.io.tmpdir");
        String sep = System.getProperty("file.separator");

        if (!path.endsWith(sep)) {
            path += sep;
        }

        return path + "chronicle-jul-api";
    }

    static String basePath(String loggerName) {
        return basePath()
                + System.getProperty("file.separator")
                + loggerName;
    }

    static void setupLogger(Class<?> testName) {
        setupLogger(testName.getSimpleName());
    }

    static void setupLogger(String id) {
        System.setProperty(
                "java.util.logging.manager",
                ChronicleLoggerManager.class.getName());
        System.setProperty(
                "sun.util.logging.disableCallerCheck",
                "false");
        System.setProperty(
                "chronicle.logger.properties",
                id.endsWith(".properties") ? id : id + ".properties");

        LogManager.getLogManager().reset();
    }
}
