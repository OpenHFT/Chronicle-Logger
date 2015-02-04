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

import net.openhft.chronicle.tools.ChronicleTools;
import org.junit.Test;

import java.io.IOException;

public class JulIndexedChronicleTest extends JulTestBase {

    @Test
    public void testIndexedChronicleConfiguration() throws IOException {
        testChronicleConfiguration("binary-indexed-cfg", BinaryIndexedChronicleHandler.class);
        testChronicleConfiguration("text-indexed-cfg", TextIndexedChronicleHandler.class);
    }

    @Test
    public void testIndexedBinaryAppender() throws IOException {
        final String testId = "binary-indexed-chronicle";
        ChronicleTools.deleteOnExit(basePath(testId));

        testBinaryAppender(testId, getIndexedChronicle(testId));
    }

    @Test
    public void testIndexedTextAppender() throws IOException {
        final String testId = "text-indexed-chronicle";
        ChronicleTools.deleteOnExit(basePath(testId));

        testTextAppender(testId, getIndexedChronicle(testId));
    }
}
