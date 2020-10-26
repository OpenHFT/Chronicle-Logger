/*
 * Copyright 2014-2020 chronicle.software
 *
 * http://www.chronicle.software
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.openhft.chronicle.logger.jul;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.logging.LogManager;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class JulHandlerTestBase extends JulTestBase {

    // *************************************************************************
    //
    // *************************************************************************

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
     * @param id
     * @throws IOException
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

    // *************************************************************************
    //
    // *************************************************************************

}
