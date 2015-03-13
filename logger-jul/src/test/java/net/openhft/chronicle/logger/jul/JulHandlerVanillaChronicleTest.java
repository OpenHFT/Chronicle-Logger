/*
 * Copyright 2014 Higher Frequency Trading
 *
 * http://www.higherfrequencytrading.com
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

import net.openhft.lang.io.IOTools;
import org.junit.Test;

import java.io.IOException;
import java.util.logging.Logger;

public class JulHandlerVanillaChronicleTest extends JulHandlerTestBase {

    @Test
    public void testVanillaChronicleConfiguration() throws IOException {
        setupLogManager("binary-vanilla-cfg");
        testChronicleConfiguration(
            "binary-vanilla-cfg",
            Logger.getLogger("binary-vanilla-cfg"),
            BinaryVanillaChronicleHandler.class);

        setupLogManager("text-vanilla-cfg");
        testChronicleConfiguration(
            "text-vanilla-cfg",
            Logger.getLogger("text-vanilla-cfg"),
            TextVanillaChronicleHandler.class);
    }

    @Test
    public void testVanillaBinaryAppender() throws IOException {
        final String testId = "binary-vanilla-chronicle";
        IOTools.deleteDir(basePath(testId));

        setupLogManager(testId);
        testBinaryAppender(
            testId,
            Logger.getLogger(testId),
            getVanillaChronicle(testId));
    }

    @Test
    public void testVanillaTextAppender() throws IOException {
        final String testId = "text-vanilla-chronicle";
        IOTools.deleteDir(basePath(testId));

        setupLogManager(testId);
        testTextAppender(
            testId,
            Logger.getLogger(testId),
            getVanillaChronicle(testId));
    }
}
