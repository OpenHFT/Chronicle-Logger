/*
 * Copyright 2014-2020 chronicle.software
 *
 *       https://chronicle.software
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.openhft.chronicle.logger.jul;

import net.openhft.chronicle.logger.LogAppenderConfig;

import java.util.logging.Filter;
import java.util.logging.Level;
import java.util.logging.LogManager;

import static net.openhft.chronicle.logger.ChronicleLogConfig.PLACEHOLDER_END;
import static net.openhft.chronicle.logger.ChronicleLogConfig.PLACEHOLDER_START;

public class ChronicleHandlerConfig {
    private final LogManager manager;
    private final String prefix;

    public ChronicleHandlerConfig(final Class<?> type) {
        this.manager = LogManager.getLogManager();
        this.prefix = type.getName();
    }

    public String getString(String name, String defaultValue) {
        return getStringProperty(this.prefix + "." + name, defaultValue);
    }

    public int getInt(String name, int defaultValue) {
        return getIntProperty(this.prefix + "." + name, defaultValue);
    }

    public boolean getBoolean(String name, boolean defaultValue) {
        return getBooleanProperty(this.prefix + "." + name, defaultValue);
    }

    public Level getLevel(String name, Level defaultValue) {
        return getLevelProperty(this.prefix + "." + name, defaultValue);
    }

    public Filter getFilter(String name, Filter defaultValue) {
        return getFilterProperty(this.prefix + "." + name, defaultValue);
    }

    public LogAppenderConfig getAppenderConfig() {
        LogAppenderConfig cfg = new LogAppenderConfig();
        for (final String key : cfg.keys()) {
            cfg.setProperty(
                    key,
                    getStringProperty(this.prefix + ".cfg." + key, null)
            );
        }

        return cfg;
    }

    // *************************************************************************
    //
    // *************************************************************************

    String getStringProperty(String name, String defaultValue) {
        String val = this.manager.getProperty(name);
        if (val == null) {
            return defaultValue;
        }

        return resolvePlaceholder(val.trim());
    }

    int getIntProperty(String name, int defaultValue) {
        String val = getStringProperty(name, null);
        if (val == null) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(val.trim());
        } catch (Exception ex) {
            return defaultValue;
        }
    }

    boolean getBooleanProperty(String name, boolean defaultValue) {
        String val = getStringProperty(name, null);
        if (val == null) {
            return defaultValue;
        }

        val = val.toLowerCase();
        if (val.equals("true") || val.equals("1")) {
            return true;

        } else if (val.equals("false") || val.equals("0")) {
            return false;
        }

        return defaultValue;
    }

    Filter getFilterProperty(String name, Filter defaultValue) {
        String val = getStringProperty(name, null);

        try {
            if (val != null) {
                Class<?> clz = ClassLoader.getSystemClassLoader().loadClass(val);
                return (Filter) clz.newInstance();
            }
        } catch (Exception ex) {
            // We got one of a variety of exceptions in creating the
            // class or creating an instance.
            // Drop through.
        }
        // We got an exception.  Return the defaultValue.
        return defaultValue;
    }

    Level getLevelProperty(String name, Level defaultValue) {
        String val = getStringProperty(name, null);

        if (val == null) {
            return defaultValue;
        }
        Level l = Level.parse(val.trim());
        return l != null ? l : defaultValue;
    }

    private String resolvePlaceholder(String placeholder) {
        int startIndex = 0;
        int endIndex = 0;

        do {
            startIndex = placeholder.indexOf(PLACEHOLDER_START, endIndex);
            if (startIndex != -1) {
                endIndex = placeholder.indexOf(PLACEHOLDER_END, startIndex);
                if (endIndex != -1) {
                    String envKey = placeholder.substring(startIndex + 2, endIndex);
                    String newVal = null;
                    if (System.getProperties().containsKey(envKey)) {
                        newVal = System.getProperties().getProperty(envKey);
                    }

                    if (newVal != null) {
                        placeholder = placeholder.replace(
                                PLACEHOLDER_START + envKey + PLACEHOLDER_END, newVal
                        );

                        endIndex += newVal.length() - envKey.length() + 3;
                    }
                }
            }
        } while (startIndex != -1 && endIndex != -1 && endIndex < placeholder.length());

        return placeholder;
    }
}
