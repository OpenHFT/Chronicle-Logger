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

package net.openhft.chronicle.logger.logback;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.ThrowableProxy;
import ch.qos.logback.core.spi.FilterReply;
import net.openhft.chronicle.ExcerptAppender;
import net.openhft.chronicle.logger.ChronicleLog;

public abstract class BinaryChronicleAppender extends AbstractChronicleAppender {

    private boolean includeCallerData;
    private boolean includeMDC;
    private boolean formatMessage;

    protected BinaryChronicleAppender() {
        super();

        this.includeCallerData = true;
        this.includeMDC        = true;
        this.formatMessage     = false;
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

    public void setFormatMessage(boolean formatMessage) {
        this.formatMessage = formatMessage;
    }

    public boolean isFormatMessage() {
        return this.formatMessage;
    }

    // *************************************************************************
    //
    // *************************************************************************

    @Override
    public void doAppend(final ILoggingEvent event) {
        if(getFilterChainDecision(event) != FilterReply.DENY) {
            final ExcerptAppender appender = getAppender();
            if (appender != null) {
                appender.startExcerpt();
                appender.writeByte(ChronicleLog.VERSION);
                ChronicleLog.Type.LOGBACK.writeTo(appender);
                appender.writeLong(event.getTimeStamp());
                toChronicleLogLevel(event.getLevel()).writeTo(appender);
                appender.writeUTF(event.getThreadName());
                appender.writeUTF(event.getLoggerName());

                if (!formatMessage) {
                    appender.writeUTF(event.getMessage());

                    // Args
                    Object[] args = event.getArgumentArray();
                    int argsLen = null != args ? args.length : 0;

                    appender.writeStopBit(argsLen);
                    for (int i = 0; i < argsLen; i++) {
                        appender.writeObject(args[i]);
                    }
                }
                else {
                    appender.writeUTF(event.getFormattedMessage());
                    appender.writeStopBit(0);
                }

                ThrowableProxy tp = (ThrowableProxy) event.getThrowableProxy();
                if (tp != null) {
                    appender.writeBoolean(true);
                    appender.writeObject(tp.getThrowable());
                }
                else {
                    appender.writeBoolean(false);
                }

                /*
                if(includeMDC) {
                    // Mapped Diagnostic Context http://logback.qos.ch/manual/mdc.html
                    final Map<String, String> mdcProps = event.getMDCPropertyMap();
                    appender.writeInt(null != mdcProps ? mdcProps.size() : 0);
                    if(mdcProps != null) {
                        for (Map.Entry<String, String> entry : mdcProps.entrySet()) {
                            appender.writeUTF(entry.getKey());
                            appender.writeUTF(entry.getValue());
                        }
                    }
                } else {
                    appender.writeInt(0);
                }

                if(includeCallerData) {
                    Object[] callerData = event.getCallerData();
                    int callerDataLen = null != callerData ? callerData.length : 0;

                    appender.writeInt(callerDataLen);
                    for(int i=0; i < callerDataLen; i++) {
                        appender.writeObject(callerData[i]);
                    }
                } else {
                    appender.writeInt(0);
                }
                */

                appender.finish();
            }
        }
    }
}
