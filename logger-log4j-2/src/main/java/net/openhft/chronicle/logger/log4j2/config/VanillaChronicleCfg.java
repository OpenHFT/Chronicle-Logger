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


import net.openhft.chronicle.logger.VanillaLogAppenderConfig;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;

@Plugin(
    name     = "vanillaChronicleConfig",
    category = "Core")
public final class VanillaChronicleCfg extends VanillaLogAppenderConfig {

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

        final VanillaLogAppenderConfig cfg = new VanillaLogAppenderConfig();
        cfg.setProperty("dataCacheCapacity",dataCacheCapacity);
        cfg.setProperty("cycleLength",cycleLength);
        cfg.setProperty("cleanupOnClose",cleanupOnClose);
        cfg.setProperty("synchronous",synchronous);
        cfg.setProperty("defaultMessageSize",defaultMessageSize);
        cfg.setProperty("useCheckedExcerpt",useCheckedExcerpt);
        cfg.setProperty("entriesPerCycle",entriesPerCycle);
        cfg.setProperty("indexBlockSize",indexBlockSize);
        cfg.setProperty("indexCacheCapacity",indexCacheCapacity);

        return new VanillaChronicleCfg();
    }
}
