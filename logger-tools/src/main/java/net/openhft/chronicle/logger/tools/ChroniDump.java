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
import net.openhft.chronicle.logger.ChronicleLogReader;
import net.openhft.lang.io.Bytes;

/**
 *
 */
public final class ChroniDump {

    private final static char[] HEX_DIGITS = {
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
            'A', 'B', 'C', 'D', 'E', 'F'
    };

    // *************************************************************************
    //
    // *************************************************************************

    private static final ChronicleLogReader HEXDUMP = new ChronicleLogReader() {
        @Override
        public void read(final Bytes bytes) {
            StringBuilder result = new StringBuilder();

            for (long i = 0; bytes.remaining() > 0; i++) {
                long size = bytes.remaining();

                for (int n = 0; n < Math.min(16, size); n++) {
                    //Read byte, no consume
                    byte b = bytes.readByte((i * 16) + n);

                    result.append(" ");
                    result.append(HEX_DIGITS[(b >>> 4) & 0x0F]);
                    result.append(HEX_DIGITS[b & 0x0F]);
                }

                size = bytes.remaining();

                for (int n = 0; n < 16 - size; n++) {
                    result.append("   ");
                }

                result.append(" ==> ");

                for (int n = 0; n < Math.min(16, size); n++) {
                    //Read byte, consume
                    byte b = bytes.readByte();
                    result.append((b > ' ' && b < '~') ? (char) b : '.');
                }

                result.append('\n');
            }

            System.out.println(result.toString());
        }
    };

    // *************************************************************************
    //
    // *************************************************************************

    public static void main(String[] args) {
        try {
            boolean indexed = false;
            boolean compressed = true;

            for (int i = 0; i < args.length - 1; i++) {
                if ("-i".equals(args[i])) {
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
                    HEXDUMP,
                    false,
                    false
                );

            } else {
                System.err.format("\nUsage: ChroniDump [-i|-u] path");
                System.err.format("\n  -u = use uncompressed object serialization, default compressed");
                System.err.format("\n  -i = IndexedChronicle, default VanillaChronicle");
            }
        } catch (Exception e) {
            e.printStackTrace(System.err);
        }
    }

    private ChroniDump() {}
}
