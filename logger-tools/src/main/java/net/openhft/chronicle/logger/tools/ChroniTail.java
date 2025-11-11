/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.logger.tools;

import net.openhft.chronicle.wire.WireType;

/**
 * Command line tool that tails a Chronicle log and prints
 * new entries as soon as they are written.
 * <p>
 * The tool creates a {@link ChronicleLogReader} in waiting mode so it
 * behaves like <code>tail -f</code> on a Chronicle Queue.
 */
public final class ChroniTail {

    private ChroniTail() {
    }

    /**
     * Tail the log specified on the command line.
     *
     * @param args command line options
     *             <ul>
     *             <li><code>-w &lt;wireType&gt;</code> optional wire format,
     *             default is BINARY_LIGHT</li>
     *             <li><code>&lt;path&gt;</code> base path of Chronicle log storage</li>
     *             </ul>
     */
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
