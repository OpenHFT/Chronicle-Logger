/*
 * Copyright 2014-2017 Chronicle Software
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
package net.openhft.chronicle.logger.tools;

import net.openhft.chronicle.logger.ChronicleEntryProcessor;
import net.openhft.chronicle.logger.ChronicleEventReader;
import net.openhft.chronicle.logger.DefaultChronicleEntryProcessor;
import net.openhft.chronicle.logger.codec.Codec;
import net.openhft.chronicle.logger.codec.CodecRegistry;
import net.openhft.chronicle.queue.ChronicleQueue;

import java.nio.file.Path;
import java.nio.file.Paths;

public final class ChroniCat {

    private ChroniCat() {
    }

    public static void main(String[] args) {
        ChronicleQueue cq = ChronicleArgs.createChronicleQueue(args);
        if (cq == null) {
            System.err.println("\nUsage: ChroniCat [-w <wireType>] <path>");
            System.err.println("  <wireType> - wire format, default BINARY_LIGHT");
            System.err.println("  <path>     - base path of Chronicle Logs storage");
            System.exit(-1);
        }

        ChronicleEventReader reader = new ChronicleEventReader();
        Path parent = Paths.get(cq.fileAbsolutePath()).getParent();
        CodecRegistry registry = CodecRegistry.builder().withDefaults(parent).build();
        ChronicleEntryProcessor<String> entryProcessor = new DefaultChronicleEntryProcessor(registry);
        ChronicleLogProcessor logProcessor = e -> System.out.println(entryProcessor.apply(e));
        logProcessor.processLogs(cq, reader, false);
    }

}
