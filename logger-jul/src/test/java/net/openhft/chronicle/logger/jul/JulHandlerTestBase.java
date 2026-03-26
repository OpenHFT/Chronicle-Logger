/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.logger.jul;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.logging.LogManager;

import static org.junit.jupiter.api.Assertions.*;

class JulHandlerTestBase extends JulTestBase {

    protected static String rootPath() {
        String path = System.getProperty("java.io.tmpdir");
        String sep = System.getProperty("file.separator");

        if (!path.endsWith(sep)) {
            path += sep;
        }

        return path + "chronicle-jul";
    }

    protected static String basePath(String type) {
        return rootPath()
                + System.getProperty("file.separator")
                + type;
    }

    /**
     * @param id the id of the logger
     * @throws IOException if an I/O error occurs
     */
    protected void setupLogManager(String id) throws IOException {
        String cfgPath = System.getProperty("resources.path");
        File cfgFile = new File(cfgPath, id + ".properties");

        assertNotNull(cfgPath);
        assertTrue(cfgFile.exists());

        LogManager manager = LogManager.getLogManager();
        manager.reset();
        manager.readConfiguration(new FileInputStream(cfgFile));
    }
}
