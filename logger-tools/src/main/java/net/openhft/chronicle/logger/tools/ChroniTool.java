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

package net.openhft.chronicle.logger.tools;

import net.openhft.chronicle.Chronicle;
import net.openhft.chronicle.ExcerptTailer;
import net.openhft.chronicle.logger.ChronicleLog;
import net.openhft.chronicle.logger.ChronicleLogEvent;
import net.openhft.chronicle.logger.ChronicleLogHelper;
import net.openhft.chronicle.logger.ChronicleLogProcessor;
import net.openhft.chronicle.logger.ChronicleLogReader;
import net.openhft.lang.io.Bytes;
import net.openhft.lang.model.constraints.NotNull;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

public final class ChroniTool {

    public static final DateFormat DF = new SimpleDateFormat(ChronicleLog.DEFAULT_DATE_FORMAT);

    // *************************************************************************
    //
    // *************************************************************************

    public static abstract class BinaryProcessor implements ChronicleLogReader, ChronicleLogProcessor {
        @Override
        public void read(final Bytes bytes) {
            process(ChronicleLogHelper.decodeBinary(bytes));
        }
    }

    public static abstract class TextProcessor implements ChronicleLogReader, ChronicleLogProcessor {
        @Override
        public void read(final Bytes bytes) {
            process(ChronicleLogHelper.decodeText(bytes));
        }
    }

    // *************************************************************************
    //
    // *************************************************************************

    public static final ChronicleLogReader READER_BINARY = new BinaryProcessor() {
        @Override
        public void process(final ChronicleLogEvent event) {
            System.out.println(asString(event));
        }
    };

    public static final ChronicleLogReader READER_TEXT = new TextProcessor() {
        @Override
        public void process(final ChronicleLogEvent event) {
            System.out.println(asString(event));
        }
    };

    // *************************************************************************
    //
    // *************************************************************************

    public static ChronicleLogReader reader(final ChronicleLogProcessor processor) {
        return new BinaryProcessor() {
            @Override
            public void process(final ChronicleLogEvent event) {
                processor.process(event);
            }
        };
    }

    public static String asString(final ChronicleLogEvent event) {
        if(!event.hasArguments()) {
            return String.format("%s|%s|%s|%s|%s\n",
                DF.format(event.getTimeStamp()),
                event.getLevel().toString(),
                event.getThreadName(),
                event.getLoggerName(),
                event.getMessage());
        } else {
            return "";
        }
    }

    // *************************************************************************
    //
    // *************************************************************************

    public static void process(
            @NotNull final Chronicle chronicle,
            @NotNull final ChronicleLogReader reader,
            boolean waitForData,
            boolean fromEnd) throws IOException {

        ExcerptTailer tailer = null;

        try {
            tailer = fromEnd
                ? chronicle.createTailer().toEnd()
                : chronicle.createTailer();

            while (true) {
                if (tailer.nextIndex()) {
                    reader.read(tailer);
                    tailer.finish();
                } else {
                    if (waitForData) {
                        try {
                            Thread.sleep(50);
                        } catch (InterruptedException e) {
                            // Ignore
                        }
                    } else {
                        break;
                    }
                }
            }
        } finally {
            if (tailer != null) tailer.close();
            chronicle.close();
        }
    }

    private ChroniTool() {}
}
