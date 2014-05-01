package com.higherfrequencytrading.chronology.tools;

import com.higherfrequencytrading.chronology.ChronologyLogReader;
import net.openhft.chronicle.IndexedChronicle;
import net.openhft.chronicle.VanillaChronicle;
import net.openhft.lang.io.Bytes;

/**
 *
 */
public class ChroniDump {

    private final static char[] HEX_DIGITS = {
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
            'A', 'B', 'C', 'D', 'E', 'F'
    };

    // *************************************************************************
    //
    // *************************************************************************

    private static final ChronologyLogReader HEXDUMP = new ChronologyLogReader() {
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

            for (int i = 0; i < args.length - 1; i++) {
                if ("-i".equals(args[i])) {
                    indexed = true;
                }
            }

            if (args.length >= 1) {
                ChroniTool.process(
                    indexed
                        ? new IndexedChronicle(args[args.length - 1])
                        : new VanillaChronicle(args[args.length - 1]),
                    HEXDUMP,
                    false,
                    false
                );
            } else {
                System.err.format("\nUsage: ChroniDump [-i] path");
                System.err.format("\n  -i = IndexedCronicle, default VanillaChronicle");
            }
        } catch (Exception e) {
            e.printStackTrace(System.err);
        }
    }
}
