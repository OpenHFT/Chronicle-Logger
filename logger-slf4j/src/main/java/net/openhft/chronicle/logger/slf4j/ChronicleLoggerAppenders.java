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

package net.openhft.chronicle.logger.slf4j;

import net.openhft.chronicle.Chronicle;
import net.openhft.chronicle.logger.ChronicleLogAppenders;
import net.openhft.chronicle.logger.ChronicleLogLevel;
import org.slf4j.helpers.FormattingTuple;
import org.slf4j.helpers.MessageFormatter;

import java.io.IOException;

public class ChronicleLoggerAppenders extends ChronicleLogAppenders {

    // *************************************************************************
    //
    // *************************************************************************

    public static final class BinaryFormattingWriter extends AbstractBinaryFormattingWriter {

        public BinaryFormattingWriter(Chronicle chronicle) throws IOException {
            super(chronicle);
        }

        @Override
        public void log(ChronicleLogLevel level, String name, String message, Object arg1) {
            final FormattingTuple ft = MessageFormatter.format(message, arg1);
            doLog(level, name, ft.getMessage(), ft.getThrowable());
        }

        @Override
        public void log(ChronicleLogLevel level, String name, String message, Object arg1, Object arg2) {
            final FormattingTuple ft = MessageFormatter.format(message, arg1, arg2);
            doLog(level, name, ft.getMessage(), ft.getThrowable());
        }

        @Override
        public void log(ChronicleLogLevel level, String name, String message, Object... args) {
            final FormattingTuple ft = MessageFormatter.arrayFormat(message, args);
            doLog(level, name, ft.getMessage(), ft.getThrowable());
        }

        @Override
        public void log(ChronicleLogLevel level, String name, String message, Throwable throwable) {
            final FormattingTuple ft = MessageFormatter.format(message, throwable);
            doLog(level, name, ft.getMessage(), ft.getThrowable());
        }
    }

    // *************************************************************************
    //
    // *************************************************************************

    public static final class TextWriter extends AbstractTextWriter {
        public TextWriter(Chronicle chronicle, String dateFormat, Integer stackTraceDepth) throws IOException {
            super(
                chronicle,
                dateFormat != null
                    ? dateFormat
                    : ChronicleLoggingConfig.DEFAULT_DATE_FORMAT,
                stackTraceDepth != null
                    ? stackTraceDepth
                    : -1);
        }

        @Override
        public void log(ChronicleLogLevel level, String name, String message, Object arg1) {
            final FormattingTuple ft = MessageFormatter.format(message, arg1);
            doLog(level, name, ft.getMessage(), ft.getThrowable());
        }

        @Override
        public void log(ChronicleLogLevel level, String name, String message, Object arg1, Object arg2) {
            final FormattingTuple ft = MessageFormatter.format(message, arg1, arg2);
            doLog(level, name, ft.getMessage(), ft.getThrowable());
        }

        @Override
        public void log(ChronicleLogLevel level, String name, String message, Object... args) {
            final FormattingTuple ft = MessageFormatter.arrayFormat(message, args);
            doLog(level, name, ft.getMessage(), ft.getThrowable());
        }

        @Override
        public void log(ChronicleLogLevel level, String name, String message, Throwable throwable) {
            final FormattingTuple ft = MessageFormatter.format(message, throwable);
            doLog(level, name, ft.getMessage(), ft.getThrowable());
        }
    }
}
