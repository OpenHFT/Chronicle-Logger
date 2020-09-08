/*
 * Copyright 2014-2017 Chronicle Software
 *
 * http://www.chronicle.software
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
package net.openhft.chronicle.logger.jul;

import net.openhft.chronicle.bytes.Bytes;
import net.openhft.chronicle.logger.ChronicleLogWriter;
import net.openhft.chronicle.logger.DefaultChronicleLogWriter;
import net.openhft.chronicle.logger.LogAppenderConfig;

import java.io.IOException;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.time.Instant;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.SimpleFormatter;
import java.util.logging.XMLFormatter;

import static java.lang.invoke.MethodType.methodType;
import static net.openhft.chronicle.logger.ChronicleLogConfig.KEY_WIRETYPE;

public class ChronicleHandler extends AbstractChronicleHandler {

    public ChronicleHandler() throws IOException {
        ChronicleHandlerConfig handlerCfg = new ChronicleHandlerConfig(getClass());
        String appenderPath = handlerCfg.getString("path", null);
        LogAppenderConfig appenderCfg = handlerCfg.getAppenderConfig();

        setFormatter(handlerCfg.getFormatter("formatter", new SimpleFormatter()));
        setLevel(handlerCfg.getLevel("level", Level.ALL));
        setFilter(handlerCfg.getFilter("filter", null));

        setWriter(new DefaultChronicleLogWriter(appenderCfg.build(
                appenderPath,
                handlerCfg.getStringProperty(KEY_WIRETYPE, "BINARY_LIGHT"))
        ));
    }

    @Override
    protected void doPublish(final LogRecord record, final ChronicleLogWriter writer) {
        // if we're running on JDK 1.9, we can get nanoseconds here.
        int level = record.getLevel().intValue();
        Instant instant = getInstant(record);
        String threadName = "thread-" + record.getThreadID();
        String loggerName = record.getLoggerName();
        String format = getFormatter().format(record);
        Bytes entry = Bytes.from(format);
        String charsetEncoding = getEncoding();

        String contentType;
        if (getFormatter() instanceof XMLFormatter) {
            contentType = "text/xml";
        } else {
            contentType = "text/plain";
        }

        // character set encoding
        if (charsetEncoding != null) {
            writer.write(
                    instant,
                    level,
                    threadName,
                    loggerName,
                    entry,
                    contentType + "; charset=" + charsetEncoding,
                    "identity"
            );
        } else {
            writer.write(
                    instant,
                    level,
                    threadName,
                    loggerName,
                    entry,
                    contentType,
                    "identity"
            );
        }
    }

    // if we're running on 9, then we can get the method handle to invoke getInstant,
    // otherwise we want getMillis
    private static MethodHandle instantMethod = null;
    static {
        try {
            MethodHandles.Lookup lookup = MethodHandles.publicLookup();
            instantMethod = lookup.findVirtual(LogRecord.class, "getInstant", methodType(Instant.class));
        }  catch (NoSuchMethodException | IllegalAccessException e) {
            // e.printStackTrace();
        }
    }

    private Instant getInstant(LogRecord record) {
        if (instantMethod != null) {
            try {
                return (Instant) instantMethod.invoke(record);
            } catch (Throwable throwable) {
                return Instant.ofEpochMilli(record.getMillis());
            }
        } else {
            return Instant.ofEpochMilli(record.getMillis());
        }
    }

}
