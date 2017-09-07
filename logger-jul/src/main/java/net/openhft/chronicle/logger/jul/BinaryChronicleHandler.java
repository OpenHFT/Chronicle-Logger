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
package net.openhft.chronicle.logger.jul;

import net.openhft.chronicle.logger.ChronicleLogWriter;
import net.openhft.chronicle.logger.DefaultChronicleLogWriter;
import net.openhft.chronicle.logger.LogAppenderConfig;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.LogRecord;

public class BinaryChronicleHandler extends AbstractChronicleHandler {

    public BinaryChronicleHandler() throws IOException {
        ChronicleHandlerConfig handlerCfg = new ChronicleHandlerConfig(getClass());
        String appenderPath = handlerCfg.getString("path", null);
        LogAppenderConfig appenderCfg = handlerCfg.getIndexedAppenderConfig();

        setLevel(handlerCfg.getLevel("level", Level.ALL));
        setFilter(handlerCfg.getFilter("filter", null));

        setWriter(new DefaultChronicleLogWriter(appenderCfg.build(appenderPath)));
    }

    @Override
    protected void doPublish(final LogRecord record, final ChronicleLogWriter writer) {
        writer.write(
            ChronicleHelper.getLogLevel(record),
            record.getMillis(),
            "thread-" + record.getThreadID(),
            record.getLoggerName(),
            record.getMessage(),
            record.getThrown(),
            record.getParameters());
    }
}
