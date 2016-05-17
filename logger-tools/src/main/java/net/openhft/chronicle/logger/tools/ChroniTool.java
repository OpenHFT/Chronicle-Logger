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

import net.openhft.chronicle.Chronicle;
import net.openhft.chronicle.ExcerptTailer;
import net.openhft.chronicle.logger.*;
import net.openhft.lang.io.Bytes;
import net.openhft.lang.model.constraints.NotNull;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Objects;

public final class ChroniTool {

    public static final DateFormat DF = new SimpleDateFormat(ChronicleLog.DEFAULT_DATE_FORMAT);

    // *************************************************************************
    //
    // *************************************************************************

    public abstract static class BinaryProcessor implements ChronicleLogReader, ChronicleLogProcessor {
        @Override
        public void read(final Bytes bytes) {
            process(ChronicleLogHelper.decodeBinary(bytes));
        }
    }

    public abstract static class TextProcessor implements ChronicleLogReader, ChronicleLogProcessor {
        @Override
        public void read(final Bytes bytes) {
            process(ChronicleLogHelper.decodeText(bytes));
        }
    }

    // *************************************************************************
    //
    // *************************************************************************

    public static final ChronicleLogReader READER_BINARY = new BinaryProcessor() {
        private StringWriter writer = new StringWriter();

        @Override
        public void process(final ChronicleLogEvent event) {
            writer.getBuffer().setLength(0);
            System.out.println(asString(event, writer));
        }
    };

    public static final ChronicleLogReader READER_TEXT = new TextProcessor() {
        private StringWriter writer = new StringWriter();

        @Override
        public void process(final ChronicleLogEvent event) {
            writer.getBuffer().setLength(0);
            System.out.println(asString(event, writer));
        }
    };

    // *************************************************************************
    //
    // *************************************************************************

    public static StringWriter asString(final ChronicleLogEvent event, final StringWriter writer) {
        writer.append(DF.format(event.getTimeStamp()));
        writer.append("|");
        writer.append(event.getLevel().toString());
        writer.append("|");
        writer.append(event.getThreadName());
        writer.append("|");
        writer.append(event.getLoggerName());
        writer.append("|");
        writer.append(event.getMessage());

        Object[] args = event.getArgumentArray();
        if(args != null && args.length > 0) {
            writer.append("|args {");
            for(int i=0; i<args.length; i++) {
                writer.append(Objects.toString(args[i]));
                if(i != args.length - 1){
                    writer.append(", ");
                }
            }

            writer.append("}");
        }

        final Throwable th = event.getThrowable();
        if(th != null) {
            writer.append("|exception: ");
            writer.append(th.toString());
            writer.append("\n");
            th.printStackTrace(new PrintWriter(writer));
        }

        return writer;
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
                : chronicle.createTailer().toStart();

            for (; ;) {
                if (tailer.nextIndex()) {
                    reader.read(tailer);
                    tailer.finish();

                } else {
                    if (waitForData) {
                        try {
                            Jvm.pause(50);
                        } catch (InterruptedException ignored) {
                        }
                    } else {
                        break;
                    }
                }
            }
        } finally {
            if (tailer != null) {
                tailer.close();
            }
        }
    }

    private ChroniTool() {}
}
