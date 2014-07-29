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

package net.openhft.chronicle.logger.log4j1;

import net.openhft.chronicle.ExcerptAppender;
import net.openhft.chronicle.logger.ChronicleLog;
import org.apache.log4j.spi.LoggingEvent;
import org.apache.log4j.spi.ThrowableInformation;

public abstract class BinaryChronicleAppender extends AbstractChronicleAppender {

    private boolean includeCallerData;
    private boolean includeMDC;

    protected BinaryChronicleAppender() {
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
    protected void append(final LoggingEvent event) {
        final ExcerptAppender appender = getAppender();
        if(appender != null) {
            appender.startExcerpt();
            appender.writeByte(ChronicleLog.VERSION);
            ChronicleLog.Type.LOG4J_1.writeTo(appender);
            appender.writeLong(event.getTimeStamp());
            toChronicleLogLevel(event.getLevel()).writeTo(appender);
            appender.writeUTF(event.getThreadName());
            appender.writeUTF(event.getLoggerName());
            appender.writeUTF(event.getMessage().toString());
            appender.writeStopBit(0);

            ThrowableInformation ti = event.getThrowableInformation();
            if(ti != null) {
                appender.writeBoolean(true);
                appender.writeObject(ti.getThrowable());
            } else {
                appender.writeBoolean(false);
            }

            appender.finish();
        }
    }
}
