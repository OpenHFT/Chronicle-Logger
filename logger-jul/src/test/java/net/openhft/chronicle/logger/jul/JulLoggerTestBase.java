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

import net.openhft.chronicle.core.OS;
import net.openhft.chronicle.core.util.Time;

import java.util.logging.LogManager;

class JulLoggerTestBase extends JulTestBase {

    // *************************************************************************
    //
    // *************************************************************************

    static String basePath() {
        String path = OS.getTarget();
        String sep = System.getProperty("file.separator");

        if (!path.endsWith(sep)) {
            path += sep;
        }

        return path + "chronicle-jul-api" + Time.uniqueId();
    }

    static String basePath(String loggerName) {
        return basePath()
                + System.getProperty("file.separator")
                + loggerName;
    }

    // *************************************************************************
    //
    // *************************************************************************

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
