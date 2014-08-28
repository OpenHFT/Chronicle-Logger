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
package net.openhft.chronicle.logger.log4j2.config;

import net.openhft.chronicle.logger.IndexedLogAppenderConfig;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;

@Plugin(
    name     = "indexedChronicleConfig",
    category = "Core")
public final class IndexedChronicleCfg extends IndexedLogAppenderConfig {

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

        final IndexedLogAppenderConfig cfg = new IndexedLogAppenderConfig();
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

        return new IndexedChronicleCfg();
    }
}
