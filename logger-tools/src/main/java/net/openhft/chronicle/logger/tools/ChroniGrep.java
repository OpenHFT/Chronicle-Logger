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
import net.openhft.chronicle.logger.ChronicleLogEvent;

import java.io.StringWriter;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

/**
 *
 */
public final class ChroniGrep {

    // *************************************************************************
    //
    // *************************************************************************

    public static void main(String[] args) {
        try {
            boolean indexed = false;
            boolean binary = true;
            boolean compressed = true;

            Grep grep = new Grep();

            for (int i = 0; i < args.length - 1; i++) {
                if ("-t".equals(args[i])) {
                    binary = false;

                } else if ("-i".equals(args[i])) {
                    indexed = true;

                } else if ("-u".equals(args[i])) {
                    compressed = false;

                } else if (i != args.length - 1) {
                    grep.add(args[i]);
                }
            }

            if (args.length >= 1 && !grep.isEmpty()) {
                ChroniTool.process(
                    indexed
                        ? ChronicleQueueBuilder.indexed(args[args.length - 1])
                            .useCompressedObjectSerializer(compressed)
                            .build()
                        : ChronicleQueueBuilder.vanilla(args[args.length - 1])
                            .useCompressedObjectSerializer(compressed)
                            .build(),
                    binary
                        ? new BinaryGrep(grep)
                        : new TextGrep(grep),
                    false,
                    false
                );

            } else {
                System.err.format("%nUsage: ChroniGrep [-t|-i|-u] regexp1 ... regexpN path");
                System.err.format("%n  -u = use uncompressed object serialization, default compressed");
                System.err.format("%n  -t = text chronicle, default binary");
                System.err.format("%n  -i = IndexedChronicle, default VanillaChronicle");
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

        boolean matches(CharSequence message) {
            for (String regexp : this.regexps) {
                if (Pattern.matches(regexp, message)) {
                    return true;
                }
            }

            return false;
        }
    }

    private static final class BinaryGrep extends ChroniTool.BinaryProcessor {
        private final Grep grep;
        private final StringWriter writer;

        public BinaryGrep(final Grep grep) {
            this.grep = grep;
            this.writer = new StringWriter();
        }

        @Override
        public void process(final ChronicleLogEvent event) {
            writer.getBuffer().setLength(0);
            ChroniTool.asString(event, writer).toString();
            if (this.grep.matches(writer.getBuffer())) {
                System.out.println(writer.getBuffer());
            }
        }
    }

    private static final class TextGrep extends ChroniTool.TextProcessor {
        private final Grep grep;
        private final StringWriter writer;

        public TextGrep(final Grep grep) {
            this.grep = grep;
            this.writer = new StringWriter();
        }

        @Override
        public void process(final ChronicleLogEvent event) {
            writer.getBuffer().setLength(0);
            ChroniTool.asString(event, writer).toString();
            if (this.grep.matches(writer.getBuffer())) {
                System.out.println(writer.getBuffer());
            }
        }
    }

    private ChroniGrep() {}
}
