/*
 * Copyright 2014 Higher Frequency Trading
 * <p/>
 * http://www.higherfrequencytrading.com
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.openhft.chronicle.logger;


import net.openhft.chronicle.*;
import net.openhft.chronicle.tools.ChronicleTools;
import net.openhft.lang.io.RandomDataInput;
import net.openhft.lang.io.RandomDataOutput;

import java.io.IOException;

public final class ChronicleLog {
    public static final String NEWLINE = System.getProperty("line.separator");
    public static final String TMPDIR = System.getProperty("java.io.tmpdir");
    public static final String COMMA = ", ";
    public static final String STR_FALSE = "false";
    public static final String STR_TRUE = "true";
    public static final String DEFAULT_DATE_FORMAT = "yyyy.MM.dd-HH:mm:ss.SSS";
    
    public static final byte VERSION = 1;

    public enum Type {
        UNKNOWN, SLF4J, LOGBACK, LOG4J_1, LOG4J_2;

        private static final Type[] VALUES = values();

        public void writeTo(final RandomDataOutput out) {
            out.writeByte(ordinal());
        }

        public static Type read(final RandomDataInput in) {
            return VALUES[in.readByte()];
        }
    }

    // *************************************************************************
    //
    // *************************************************************************

    public static void warmup() {
        //noinspection UnusedDeclaration needed to laod class.
        boolean vanillaDone = VanillaWarmup.DONE;
        boolean indexedDone = IndexedWarmup.DONE;
    }

    // *************************************************************************
    //
    // *************************************************************************

    private static class VanillaWarmup {
        public  static final boolean DONE;
        private static final int WARMUP_ITER = 1000;

        static {
            VanillaChronicleConfig cc = new VanillaChronicleConfig();
            cc.dataBlockSize(64);
            cc.indexBlockSize(64);

            String basePath = TMPDIR + "/vanilla-warmup-" + Math.random();
            ChronicleTools.deleteDirOnExit(basePath);

            try {
                final VanillaChronicle chronicle = new VanillaChronicle(basePath, cc);
                final ExcerptAppender appender = chronicle.createAppender();
                final ExcerptTailer tailer = chronicle.createTailer();

                for (int i = 0; i < WARMUP_ITER; i++) {
                    appender.startExcerpt();
                    appender.writeInt(i);
                    appender.finish();
                    boolean b = tailer.nextIndex() || tailer.nextIndex();
                    tailer.readInt();
                    tailer.finish();
                }

                chronicle.close();
                chronicle.clear();

                System.gc();
                DONE = true;
            } catch (IOException e) {
                throw new AssertionError();
            }
        }
    }

    private static class IndexedWarmup {
        public  static final boolean DONE;
        private static final int WARMUP_ITER = 1000;

        static {
            ChronicleConfig cc = ChronicleConfig.SMALL.clone();
            cc.dataBlockSize(64);
            cc.indexBlockSize(64);

            String basePath = TMPDIR + "/indexed-warmup-" + Math.random();
            ChronicleTools.deleteOnExit(basePath);

            try {
                final IndexedChronicle chronicle = new IndexedChronicle(basePath, cc);
                final ExcerptAppender appender = chronicle.createAppender();
                final ExcerptTailer tailer = chronicle.createTailer();

                for (int i = 0; i < WARMUP_ITER; i++) {
                    appender.startExcerpt();
                    appender.writeInt(i);
                    appender.finish();
                    boolean b = tailer.nextIndex() || tailer.nextIndex();
                    tailer.readInt();
                    tailer.finish();
                }

                chronicle.close();

                System.gc();
                DONE = true;
            } catch (IOException e) {
                throw new AssertionError();
            }
        }
    }
    

    private ChronicleLog() {
    }
}
