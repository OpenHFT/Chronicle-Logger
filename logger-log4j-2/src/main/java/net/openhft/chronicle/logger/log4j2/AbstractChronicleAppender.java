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

import net.openhft.chronicle.Chronicle;
import net.openhft.chronicle.ExcerptAppender;
import net.openhft.chronicle.logger.ChronicleLogLevel;
import net.openhft.chronicle.logger.IndexedLogAppenderConfig;
import net.openhft.chronicle.logger.VanillaLogAppenderConfig;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;

import java.io.IOException;

public abstract class AbstractChronicleAppender extends AbstractAppender {
    private String path;

    protected Chronicle chronicle;

    protected AbstractChronicleAppender(String name, Filter filter, String path) {
        super(name, filter, null, true);

        this.path = path;
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

    // *************************************************************************
    //
    // *************************************************************************

    @Plugin(
        name     = "indexedChronicleConfig",
        category = "Core")
    public static final class IndexedChronicleCfg extends IndexedLogAppenderConfig {

        protected IndexedChronicleCfg() {
        }

        @PluginFactory
        public static IndexedChronicleCfg create(
            @PluginAttribute("indexFileCapacity") final String indexFileCapacity,
            @PluginAttribute("indexFileExcerpts") final String indexFileExcerpts,
            @PluginAttribute("indexBlockSize") final String indexBlockSize,
            @PluginAttribute("useUnsafe") final String useUnsafe,
            @PluginAttribute("synchronousMode") final String synchronousMode,
            @PluginAttribute("cacheLineSize") final String cacheLineSize,
            @PluginAttribute("messageCapacity") final String messageCapacity,
            @PluginAttribute("minimiseFootprint") final String minimiseFootprint,
            @PluginAttribute("useCheckedExcerpt") final String useCheckedExcerpt,
            @PluginAttribute("dataBlockSize") final String dataBlockSize) {
            final IndexedChronicleCfg cfg = new IndexedChronicleCfg();
            cfg.setProperty("indexFileCapacity",indexFileCapacity);
            cfg.setProperty("useUnsafe",useUnsafe);
            cfg.setProperty("indexBlockSize",indexBlockSize);
            cfg.setProperty("synchronousMode",synchronousMode);
            cfg.setProperty("cacheLineSize",cacheLineSize);
            cfg.setProperty("messageCapacity",messageCapacity);
            cfg.setProperty("minimiseFootprint",minimiseFootprint);
            cfg.setProperty("useCheckedExcerpt",useCheckedExcerpt);
            cfg.setProperty("dataBlockSize",dataBlockSize);
            cfg.setProperty("indexFileExcerpts",indexFileExcerpts);

            return cfg;
        }
    }

    @Plugin(
        name     = "vanillaChronicleConfig",
        category = "Core")
    public static final class VanillaChronicleCfg extends VanillaLogAppenderConfig {

        protected VanillaChronicleCfg() {
        }

        @PluginFactory
        public static VanillaChronicleCfg create(
            @PluginAttribute("dataCacheCapacity") final String dataCacheCapacity,
            @PluginAttribute("cycleLength") final String cycleLength,
            @PluginAttribute("cleanupOnClose") final String cleanupOnClose,
            @PluginAttribute("synchronous") final String synchronous,
            @PluginAttribute("defaultMessageSize") final String defaultMessageSize,
            @PluginAttribute("useCheckedExcerpt") final String useCheckedExcerpt,
            @PluginAttribute("entriesPerCycle") final String entriesPerCycle,
            @PluginAttribute("indexCacheCapacity") final String indexCacheCapacity,
            @PluginAttribute("indexBlockSize") final String indexBlockSize) {
            final VanillaChronicleCfg cfg = new VanillaChronicleCfg();
            cfg.setProperty("dataCacheCapacity",dataCacheCapacity);
            cfg.setProperty("cycleLength",cycleLength);
            cfg.setProperty("cleanupOnClose",cleanupOnClose);
            cfg.setProperty("synchronous",synchronous);
            cfg.setProperty("defaultMessageSize",defaultMessageSize);
            cfg.setProperty("useCheckedExcerpt",useCheckedExcerpt);
            cfg.setProperty("entriesPerCycle",entriesPerCycle);
            cfg.setProperty("indexBlockSize",indexBlockSize);
            cfg.setProperty("indexCacheCapacity",indexCacheCapacity);

            return cfg;
        }
    }
}
