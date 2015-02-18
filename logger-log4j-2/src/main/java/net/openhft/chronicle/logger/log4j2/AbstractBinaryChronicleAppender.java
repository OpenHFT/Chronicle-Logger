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

package net.openhft.chronicle.logger.log4j2;

import net.openhft.chronicle.logger.ChronicleLogWriter;
import net.openhft.lang.model.constraints.NotNull;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.LogEvent;

abstract class AbstractBinaryChronicleAppender extends AbstractChronicleAppender {

    private boolean includeCallerData;
    private boolean includeMDC;

    protected AbstractBinaryChronicleAppender(final String name, final Filter filter, final String path) {
        super(name, filter, path);

        this.includeCallerData = true;
        this.includeMDC = true;
    }

    // *************************************************************************
    // Custom logging options
    // *************************************************************************

    public void setIncludeCallerData(boolean logCallerData) {
        this.includeCallerData = logCallerData;
    }

    public boolean isIncludeCallerData() {
        return this.includeCallerData;
    }

    public void setIncludeMappedDiagnosticContext(boolean logMDC) {
        this.includeMDC = logMDC;
    }

    public boolean isIncludeMappedDiagnosticContext() {
        return this.includeMDC;
    }

    // *************************************************************************
    //
    // *************************************************************************

    @Override
    public void doAppend(final @NotNull LogEvent event, final @NotNull ChronicleLogWriter writer) {
        writer.write(
            toChronicleLogLevel(event.getLevel()),
            event.getTimeMillis(),
            event.getThreadName(),
            event.getLoggerName(),
            event.getMessage().getFormat(),
            event.getThrown(),
            event.getMessage().getParameters()
        );
    }
}
