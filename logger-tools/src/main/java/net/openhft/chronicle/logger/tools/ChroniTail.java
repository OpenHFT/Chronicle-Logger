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
package net.openhft.chronicle.logger.tools;

import net.openhft.chronicle.wire.WireType;

public final class ChroniTail {

    private ChroniTail() {
    }

    public static void main(String[] args) {
        try {

            if (args.length >= 1) {
                int i = 0;
                final WireType wt;
                if ("-w".equals(args[i++])) {
                    wt = WireType.valueOf(args[i++].trim().toUpperCase());
                } else {
                    wt = WireType.BINARY_LIGHT;
                }

                ChronicleLogReader reader = new ChronicleLogReader(args[i].trim(), wt);

                reader.processLogs(ChronicleLogReader::printf, true);

            } else {
                System.err.println("\nUsage: ChroniTail [-w <wireType>] <path>");
                System.err.println("  <wireType> - wire format, default BINARY_LIGHT");
                System.err.println("  <path>     - base path of Chronicle Logs storage");
            }
        } catch (Exception e) {
            e.printStackTrace(System.err);
        }
    }
}
