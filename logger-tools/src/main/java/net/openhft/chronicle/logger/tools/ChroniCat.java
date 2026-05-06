/*
 * Copyright 2013-2026 chronicle.software; SPDX-License-Identifier: Apache-2.0
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
