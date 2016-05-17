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

package net.openhft.chronicle.logger.tools;

import net.openhft.chronicle.ChronicleQueueBuilder;

/**
 *
 */
public final class ChroniTail {

    // *************************************************************************
    //
    // *************************************************************************

    public static void main(String[] args) {
        try {
            boolean indexed = false;
            boolean binary = true;
            boolean compressed = true;

            for (int i = 0; i < args.length - 1; i++) {
                if ("-t".equals(args[i])) {
                    binary = false;

                } else if ("-i".equals(args[i])) {
                    indexed = true;

                } else if ("-u".equals(args[i])) {
                    compressed = false;
                }
            }

            if (args.length >= 1) {
                ChroniTool.process(
                    indexed
                        ? ChronicleQueueBuilder.indexed(args[args.length - 1])
                            .useCompressedObjectSerializer(compressed)
                            .build()
                        : ChronicleQueueBuilder.vanilla(args[args.length - 1])
                            .useCompressedObjectSerializer(compressed)
                            .build(),
                    binary
                        ? ChroniTool.READER_BINARY
                        : ChroniTool.READER_TEXT,
                    true,
                    true
                );

            } else {
                System.err.format("%nUsage: ChroniTail [-t|-i|-u] path");
                System.err.format("%n  -t = text chronicle, default binary");
                System.err.format("%n  -u = use uncompressed object serialization, default compressed");
                System.err.format("%n  -i = IndexedChronicle, default VanillaChronicle");
            }
        } catch (Exception e) {
            e.printStackTrace(System.err);
        }
    }

    private ChroniTail() {}
}
