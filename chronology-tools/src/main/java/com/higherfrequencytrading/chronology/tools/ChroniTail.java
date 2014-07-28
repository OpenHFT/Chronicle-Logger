package com.higherfrequencytrading.chronology.tools;

import net.openhft.chronicle.IndexedChronicle;
import net.openhft.chronicle.VanillaChronicle;

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

            for (int i = 0; i < args.length - 1; i++) {
                if ("-t".equals(args[i])) {
                    binary = false;
                }
                if ("-i".equals(args[i])) {
                    indexed = true;
                }
            }

            if (args.length >= 1) {
                ChroniTool.process(
                    indexed
                        ? new IndexedChronicle(args[args.length - 1])
                        : new VanillaChronicle(args[args.length - 1]),
                    binary
                        ? ChroniTool.READER_BINARY
                        : ChroniTool.READER_TEXT,
                    true,
                    true
                );
            } else {
                System.err.format("\nUsage: ChroniTail [-t|-i] path");
                System.err.format("\n  -t = text chronicle, default binary");
                System.err.format("\n  -i = IndexedCronicle, default VanillaChronicle");
            }
        } catch (Exception e) {
            e.printStackTrace(System.err);
        }
    }

    private ChroniTail() {}
}
