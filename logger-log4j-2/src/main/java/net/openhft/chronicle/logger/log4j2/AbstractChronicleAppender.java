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
package net.openhft.chronicle.logger.log4j2;

import net.openhft.chronicle.logger.ChronicleLogLevel;
import net.openhft.chronicle.logger.ChronicleLogWriter;
import net.openhft.chronicle.logger.LogAppenderConfig;
import net.openhft.lang.model.constraints.NotNull;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;

import java.io.IOException;

public abstract class AbstractChronicleAppender extends AbstractAppender {

    private String path;
    private String wireType;

    private ChronicleLogWriter writer;

    AbstractChronicleAppender(String name, Filter filter, String path, String wireType) {
        super(name, filter, null, true);

        this.path = path;
        this.wireType = wireType;
        this.writer = null;
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

    public String getWireType() {
        return wireType;
    }

    public void setWireType(String wireType) {
        this.wireType = wireType;
    }

    // *************************************************************************
    // Chronicle implementation
    // *************************************************************************

    protected abstract ChronicleLogWriter createWriter() throws IOException;

    protected abstract void doAppend(@NotNull final LogEvent event, @NotNull final ChronicleLogWriter writer);

    // *************************************************************************
    //
    // *************************************************************************

    @Override
    public void start() {
        if (getPath() == null) {
            LOGGER.error("Appender " + getName() + " has configuration errors and is not started!");

        } else {
            try {
                this.writer = createWriter();
            } catch (IOException e) {
                this.writer = null;
                LOGGER.error("Appender " + getName() + " " + e.getMessage());
            }

            super.start();
        }
    }

    @Override
    public void stop() {
        if (this.writer != null) {
            try {
                this.writer.close();
            } catch (IOException e) {
                LOGGER.error("Appender " + getName() + " " + e.getMessage());
            }
        }

        super.stop();
    }

    @Override
    public void append(final LogEvent event) {
        if (this.writer != null) {
            doAppend(event, writer);
        }
    }

    // *************************************************************************
    //
    // *************************************************************************

    static ChronicleLogLevel toChronicleLogLevel(final Level level) {
        if (level.intLevel() == Level.DEBUG.intLevel()) {
            return ChronicleLogLevel.DEBUG;

        } else if (level.intLevel() == Level.TRACE.intLevel()) {
            return ChronicleLogLevel.TRACE;

        } else if (level.intLevel() == Level.INFO.intLevel()) {
            return ChronicleLogLevel.INFO;

        } else if (level.intLevel() == Level.WARN.intLevel()) {
            return ChronicleLogLevel.WARN;

        } else if (level.intLevel() == Level.ERROR.intLevel()) {
            return ChronicleLogLevel.ERROR;
        }

        throw new IllegalArgumentException(level.intLevel() + " not a valid level value");
    }

    // *************************************************************************
    //
    // *************************************************************************

    @Plugin(
            name = "chronicleCfg",
            category = "Core")
    public static final class ChronicleCfg extends LogAppenderConfig {

        ChronicleCfg() {
        }

        @PluginFactory
        public static ChronicleCfg create(
                @PluginAttribute("blockSize") final String blockSize,
                @PluginAttribute("bufferCapacity") final String bufferCapacity) {

            final ChronicleCfg cfg = new ChronicleCfg();
            cfg.setProperty("blockSize", blockSize);
            cfg.setProperty("bufferCapacity", bufferCapacity);

            return cfg;
        }
    }

}
