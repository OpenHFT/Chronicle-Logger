package com.higherfrequencytrading.chronology.tools;

import com.higherfrequencytrading.chronology.ChronologyLogEvent;
import net.openhft.chronicle.IndexedChronicle;
import net.openhft.chronicle.VanillaChronicle;

import java.util.HashSet;
import java.util.Set;

/**
 *
 */
public class ChroniGrep extends ChroniTool {

    // *************************************************************************
    //
    // *************************************************************************

    public static void main(String[] args) {
        try {
            boolean indexed = false;
            boolean binary = true;
            Grep grep = new Grep();

            for (int i = 0; i < args.length - 1; i++) {
                if ("-t".equals(args[i])) {
                    binary = false;
                } else if ("-i".equals(args[i])) {
                    indexed = true;
                } else if (i != args.length - 1) {
                    grep.add(args[i]);
                }
            }

            if (args.length >= 1 && !grep.isEmpty()) {
                ChroniTool.process(
                    indexed
                        ? new IndexedChronicle(args[args.length - 1])
                        : new VanillaChronicle(args[args.length - 1]),
                    binary
                        ? new BinaryGrep(grep)
                        : new TextGrep(grep),
                    false,
                    false
                );
            } else {
                System.err.format("\nUsage: ChroniGrep [-t|-i] regexp1 ... regexpN path");
                System.err.format("\n  -t = text chronicle, default binary");
                System.err.format("\n  -i = IndexedCronicle, default VanillaChronicle");
            }
        } catch (Exception e) {
            e.printStackTrace(System.err);
        }
    }

    // *************************************************************************
    //
    // *************************************************************************

    private static class Grep {
        private Set<String> regexps;

        public Grep() {
            this.regexps = new HashSet<String>();
        }

        public void add(String regexp) {
            this.regexps.add(regexp);
        }

        public boolean isEmpty() {
            return this.regexps.isEmpty();
        }

        boolean matches(String message) {
            for (String regexp : this.regexps) {
                if (message.matches(regexp)) {
                    return true;
                }
            }

            return false;
        }
    };

    private static final class BinaryGrep extends BinaryProcessor {
        private final Grep grep;

        public BinaryGrep(final Grep grep) {
            this.grep = grep;
        }

        @Override
        public void process(final ChronologyLogEvent event) {
            String msg = asString(event);
            if (this.grep.matches(msg)) {
                System.out.println(msg);
            }
        }
    };

    private static final class TextGrep extends TextProcessor {
        private final Grep grep;

        public TextGrep(final Grep grep) {
            this.grep = grep;
        }

        @Override
        public void process(final ChronologyLogEvent event) {
            String msg = asString(event);
            if (this.grep.matches(msg)) {
                System.out.println(msg);
            }
        }
    }
}
