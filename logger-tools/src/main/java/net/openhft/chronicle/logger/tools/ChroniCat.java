//
// Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
//

/*
 *  Copyright 2014-2025 chronicle.software
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.openhft.chronicle.logger.tools;

import net.openhft.chronicle.wire.WireType;

/**
 * Command line tool that prints the contents of a Chronicle log and exits.
 *
 * <p>Usage: {@code ChroniCat [-w <wireType>] <path>}.</p>
 * <p>Example: {@code ChroniCat -w TEXT /var/log/myApp}</p>
 */
public final class ChroniCat {

    private ChroniCat() {
    }

    /**
     * Start the tool.
     *
     * @param args optional {@code -w <wireType>} then the path to the log directory
     */
    public static void main(String[] args) {
        try {

            if (args.length >= 1) {
                int i = 0;
                final WireType wt;
                if ("-w".equals(args[i])) {
                    wt = WireType.valueOf(args[++i].trim().toUpperCase());
                    i++;
                } else {
                    wt = WireType.BINARY_LIGHT;
                }

                ChronicleLogReader reader = new ChronicleLogReader(args[i].trim(), wt);

                reader.processLogs(ChronicleLogReader::printf, false);

            } else {
                System.err.println("\nUsage: ChroniCat [-w <wireType>] <path>");
                System.err.println("  <wireType> - wire format, default BINARY_LIGHT");
                System.err.println("  <path>     - base path of Chronicle Logs storage");
            }
        } catch (Exception e) {
            e.printStackTrace(System.err);
        }
    }
}
