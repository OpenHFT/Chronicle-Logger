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
package net.openhft.chronicle.logger.jul;

import net.openhft.chronicle.logger.LogAppenderConfig;

import java.util.logging.*;

public class ChronicleHandlerConfig {
    private static final String PLACEHOLDER_START = "${";
    private static final String PLACEHOLDER_END = "}";

    private final LogManager manager;
    private final String prefix;

    public ChronicleHandlerConfig(final Class<?> type) {
        this.manager = LogManager.getLogManager();
        this.prefix = type.getName();
    }

    public String getString(String name, String defaultValue) {
        return getStringProperty(this.prefix + "." + name, defaultValue);
    }

    public Level getLevel(String name, Level defaultValue) {
        return getLevelProperty(this.prefix + "." + name, defaultValue);
    }

    public Filter getFilter(String name, Filter defaultValue) {
        return getFilterProperty(this.prefix + "." + name, defaultValue);
    }

    public Formatter getFormatter(String name, Formatter defaultValue) {
        return getFormatterProperty(this.prefix + "."  + name, defaultValue);
    }

    public LogAppenderConfig getAppenderConfig() {
        LogAppenderConfig cfg = new LogAppenderConfig();

        String s = this.prefix + ".cfg.";
        cfg.bufferCapacity = getIntProperty(s + "bufferCapacity", 128);
        cfg.blockSize = getIntProperty(s + "blockSize", 256);
        cfg.rollCycle = getStringProperty(s + "rollCycle", "");
        cfg.contentType = getStringProperty(s + "contentType", "");
        cfg.contentEncoding = getStringProperty(s + "contentEncoding", "");

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

    Formatter getFormatterProperty(String name, Formatter defaultValue) {
        String val = getStringProperty(name, null);

        try {
            if (val != null) {
                Class<?> clz = ClassLoader.getSystemClassLoader().loadClass(val);
                return (Formatter) clz.newInstance();
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
