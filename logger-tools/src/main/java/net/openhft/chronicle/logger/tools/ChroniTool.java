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
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Objects;

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
        private StringBuilder sb = new StringBuilder();

        @Override
        public void process(final ChronicleLogEvent event) {
            sb.setLength(0);
            System.out.println(asString(event, sb));
        }
    };

    public static final ChronicleLogReader READER_TEXT = new TextProcessor() {
        private StringBuilder sb = new StringBuilder();

        @Override
        public void process(final ChronicleLogEvent event) {
            sb.setLength(0);
            System.out.println(asString(event, sb));
        }
    };

    // *************************************************************************
    //
    // *************************************************************************

    public static StringBuilder asString(final ChronicleLogEvent event, final StringBuilder sb) {
        sb.append(DF.format(event.getTimeStamp()));
        sb.append("|");
        sb.append(event.getLevel().toString());
        sb.append("|");
        sb.append(event.getThreadName());
        sb.append("|");
        sb.append(event.getLoggerName());
        sb.append("|");
        sb.append(event.getMessage());

        Object[] args = event.getArgumentArray();
        if(args != null) {
            sb.append("|args {");
            for(int i=0; i<args.length; i++) {
                sb.append(Objects.toString(args[i]));
                if(i != args.length - 1){
                    sb.append(", ");
                }
            }

            sb.append("}");
        }

        final Throwable th = event.getThrowable();
        if(th != null) {
            sb.append("|exception: ");
            sb.append(th.getMessage());

            StackTraceElement[] elements = th.getStackTrace();
            for(int i=0; i<elements.length; i++) {
                sb.append("\n\tat ");
                sb.append(elements[i].toString());
            }

            final Throwable cause = th.getCause();
            if(cause != null) {
                sb.append("\n\tCaused by: ");
                sb.append(cause.getMessage());

                elements = cause.getStackTrace();
                for(int i=0; i<elements.length; i++) {
                    sb.append("\n\t\tat");
                    sb.append(elements[i].toString());
                }
            }
        }

        return sb;
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
