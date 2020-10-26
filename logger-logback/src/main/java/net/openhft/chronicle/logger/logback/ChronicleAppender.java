/*
 * Copyright 2014-2020 chronicle.software
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
package net.openhft.chronicle.logger.logback;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.ThrowableProxy;
import ch.qos.logback.core.joran.spi.DefaultClass;
import net.openhft.chronicle.logger.ChronicleLogWriter;
import net.openhft.chronicle.logger.DefaultChronicleLogWriter;
import net.openhft.chronicle.logger.LogAppenderConfig;

import java.io.IOException;

public class ChronicleAppender extends AbstractChronicleAppender {

    private boolean includeCallerData;
    private boolean includeMDC;

    private LogAppenderConfig config;

    public ChronicleAppender() {
        super();

        this.includeCallerData = true;
        this.includeMDC = true;
        this.config = new LogAppenderConfig();
    }

    public LogAppenderConfig getChronicleConfig() {
        return this.config;
    }

    @DefaultClass(value = LogAppenderConfig.class)
    public void setChronicleConfig(final LogAppenderConfig config) {
        this.config = config;
    }

    @Override
    protected ChronicleLogWriter createWriter() throws IOException {
        return new DefaultChronicleLogWriter(this.config.build(this.getPath(), getWireType()));
    }

    // *************************************************************************
    // Custom logging options
    // *************************************************************************

    public boolean isIncludeCallerData() {
        return this.includeCallerData;
    }

    public void setIncludeCallerData(boolean logCallerData) {
        this.includeCallerData = logCallerData;
    }

    public boolean isIncludeMappedDiagnosticContext() {
        return this.includeMDC;
    }

    public void setIncludeMappedDiagnosticContext(boolean logMDC) {
        this.includeMDC = logMDC;
    }

    // *************************************************************************
    //
    // *************************************************************************

    @Override
    public void doAppend(final ILoggingEvent event, final ChronicleLogWriter writer) {
        final ThrowableProxy tp = (ThrowableProxy) event.getThrowableProxy();

        writer.write(
                toChronicleLogLevel(event.getLevel()),
                event.getTimeStamp(),
                event.getThreadName(),
                event.getLoggerName(),
                event.getMessage(),
                tp != null ? tp.getThrowable() : null,
                event.getArgumentArray()
        );
    }
}
