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
package java.util.logging;

import net.openhft.chronicle.logger.ChronicleLogAppenderConfig;
import net.openhft.chronicle.logger.IndexedLogAppenderConfig;
import net.openhft.chronicle.logger.VanillaLogAppenderConfig;

public class ChronicleHandlerConfig {
    private final LogManager manager;
    private final String prefix;

    public ChronicleHandlerConfig(final Class<?> type) {
        this.manager = LogManager.getLogManager();
        this.prefix  = type.getName();
    }

    public String getString(String name, String defaultValue) {
        return this.manager.getStringProperty(this.prefix + "." + name, defaultValue);
    }

    public int getInt(String name, int defaultValue) {
        return this.manager.getIntProperty(this.prefix + "." + name, defaultValue);
    }

    public boolean getBoolean(String name, boolean defaultValue) {
        return this.manager.getBooleanProperty(this.prefix + "." + name, defaultValue);
    }

    public Level getLevel(String name, Level defaultValue) {
        return this.manager.getLevelProperty(this.prefix + "." + name, defaultValue);
    }

    public Filter getFilter(String name, Filter defaultValue) {
        return this.manager.getFilterProperty(this.prefix + "." + name, defaultValue);
    }

    public ChronicleLogAppenderConfig getIndexedAppenderConfig() {
        IndexedLogAppenderConfig cfg = new IndexedLogAppenderConfig();
        for(final String key : cfg.keys()) {
            cfg.setProperty(
                key,
                this.manager.getStringProperty(this.prefix + ".cfg." + key, null)
            );
        }

        return cfg;
    }

    public ChronicleLogAppenderConfig getVanillaAppenderConfig() {
        VanillaLogAppenderConfig cfg = new VanillaLogAppenderConfig();
        for(final String key : cfg.keys()) {
            cfg.setProperty(
                key,
                this.manager.getStringProperty(this.prefix + ".cfg." + key, null)
            );
        }

        return cfg;
    }
}
