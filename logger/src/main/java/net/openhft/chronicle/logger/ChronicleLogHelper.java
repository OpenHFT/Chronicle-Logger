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


import net.openhft.chronicle.ExcerptAppender;
import net.openhft.lang.io.Bytes;

public final class ChronicleLogHelper {

    /**
     * Decode a binary stream, i. e. Excerpt
     *
     * @param in        the source of event in binary form (i. e. Excerpt)
     * @return          the ChronicleLogEvent
     */
    public static ChronicleLogEvent decodeBinary(final Bytes in) {
        return BinaryChronicleLogEvent.read(in);
    }

    /**
     * Decode a text stream, i. e. Excerpt
     *
     * @param in        the source of event in text form (i. e. Excerpt)
     * @return          the ChronicleLogEvent
     */
    public static ChronicleLogEvent decodeText(final Bytes in) {
        return TextChronicleLogEvent.read(in);
    }

    /**
     * Append a string representation of Throwable to Excerpt
     *
     * @param   throwable   the Throwable
     * @param   separator   the separator to use to separate StackTraceElement
     * @param   depth       the number of StackTraceElement to dump
     * @return              the string representation of the Throwable
     */
    public static ExcerptAppender appendStackTraceAsString(
        final ExcerptAppender appender, final Throwable throwable, String separator, int depth) {

        final StackTraceElement[] elements = throwable.getStackTrace();
        final int nbElements = (depth == -1) ? elements.length : Math.min(depth,elements.length);
        final int sepLen = separator.length();

        String tmp = null;

        appender.append(throwable.toString());
        if(nbElements > 0) {
            appender.append(separator);
        }

        for (int i=0;i < nbElements; i++) {
            tmp = elements[i].toString();
            if(appender.remaining() > (tmp.length() + sepLen)) {
                appender.append(tmp);
                appender.append(separator);
            } else {
                for(int fill=0;fill<3 && appender.remaining() > 0;fill++){
                    appender.append('.');
                }

                break;
            }
        }

        return appender;
    }

    private ChronicleLogHelper() {}
}
