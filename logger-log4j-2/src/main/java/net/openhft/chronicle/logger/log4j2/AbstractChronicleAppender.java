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

package net.openhft.chronicle.logger.log4j2;


import net.openhft.chronicle.Chronicle;
import net.openhft.chronicle.ExcerptAppender;
import net.openhft.chronicle.logger.ChronicleLogLevel;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.appender.AbstractAppender;

import java.io.IOException;

public abstract class AbstractChronicleAppender extends AbstractAppender {
    private String path;

    protected Chronicle chronicle;

    protected AbstractChronicleAppender(String name, Filter filter) {
        super(name, filter, null, true);

        this.path = null;
        this.chronicle = null;
    }

    // *************************************************************************
    // Custom logging options
    // *************************************************************************

    public void setPath(String path) {
        this.path = path;
    }

    public String getPath() {
        return this.path;
    }

    // *************************************************************************
    // Chronicle implementation
    // *************************************************************************

    protected abstract Chronicle createChronicle() throws IOException;

    protected abstract ExcerptAppender getAppender();

    // *************************************************************************
    //
    // *************************************************************************

    @Override
    public void start() {
        if(getPath() == null) {
            LOGGER.error("Appender " + getName() + " has configuration errors and is not started!");
        } else {
            try {
                this.chronicle = createChronicle();
            } catch(IOException e) {
                this.chronicle = null;
                LOGGER.error("Appender " + getName() + " " + e.getMessage());
            }

            super.start();
        }
    }

    @Override
    public void stop() {
        if(this.chronicle != null) {
            try {
                this.chronicle.close();
            } catch(IOException e) {
                LOGGER.error("Appender " + getName() + " " + e.getMessage());
            }
        }

        super.stop();
    }

    // *************************************************************************
    //
    // *************************************************************************

    public static ChronicleLogLevel toChronicleLogLevel(final Level level) {
        if(level.intLevel() == Level.DEBUG.intLevel()) {
            return ChronicleLogLevel.DEBUG;
        } else if(level.intLevel() == Level.TRACE.intLevel()) {
            return ChronicleLogLevel.TRACE;
        } else if(level.intLevel() == Level.INFO.intLevel()) {
            return ChronicleLogLevel.INFO;
        } else if(level.intLevel() == Level.WARN.intLevel()) {
            return ChronicleLogLevel.WARN;
        } else if(level.intLevel() == Level.ERROR.intLevel()) {
            return ChronicleLogLevel.ERROR;
        }

        throw new IllegalArgumentException(level.intLevel() + " not a valid level value");
    }
}
