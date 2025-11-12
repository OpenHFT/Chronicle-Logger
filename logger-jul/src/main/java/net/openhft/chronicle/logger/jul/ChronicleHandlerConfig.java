/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.logger.jul;

import net.openhft.chronicle.logger.LogAppenderConfig;

import java.util.logging.Filter;
import java.util.logging.Level;
import java.util.logging.LogManager;

import static net.openhft.chronicle.logger.ChronicleLogConfig.PLACEHOLDER_END;
import static net.openhft.chronicle.logger.ChronicleLogConfig.PLACEHOLDER_START;

/**
 * Reads handler specific properties from the {@link LogManager} using the
 * handler's class name as the property prefix. Values may contain
 * {@code ${key}} placeholders which are resolved from system properties.
 */
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

    /**
     * Look up a property by name.
     *
     * @param name         full property key
     * @param defaultValue value to return when the key is absent
     * @return trimmed value with placeholders resolved
     */
    String getStringProperty(String name, String defaultValue) {
        String val = this.manager.getProperty(name);
        if (val == null) {
            return defaultValue;
        }

        return resolvePlaceholder(val.trim());
    }

    /**
     * Parse an integer property.
     *
     * @param name         full property key
     * @param defaultValue value to return on error
     * @return parsed number or {@code defaultValue}
     */
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

    /**
     * Parse a boolean property. Recognises "true", "false", "1" and "0".
     *
     * @param name         full property key
     * @param defaultValue value to return on error
     * @return parsed flag or {@code defaultValue}
     */
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

    /**
     * Instantiate a {@link Filter} from a class name.
     *
     * @param name         full property key returning the class name
     * @param defaultValue filter to use when instantiation fails
     * @return new filter or {@code defaultValue}
     */
    Filter getFilterProperty(String name, Filter defaultValue) {
        String val = getStringProperty(name, null);

        try {
            if (val != null) {
                Class<?> clz = ClassLoader.getSystemClassLoader().loadClass(val);
                return (Filter) clz.getConstructor().newInstance();
            }
        } catch (Exception ex) {
            // ignore and return default
        }
        return defaultValue;
    }

    /**
     * Parse a {@link Level} from configuration.
     *
     * @param name         full property key
     * @param defaultValue value to return on error
     * @return parsed level or {@code defaultValue}
     */
    Level getLevelProperty(String name, Level defaultValue) {
        String val = getStringProperty(name, null);

        if (val == null) {
            return defaultValue;
        }
        Level l = Level.parse(val.trim());
        return l != null ? l : defaultValue;
    }

    /**
     * Replace {@code ${key}} tokens with matching system properties.
     *
     * @param placeholder raw value possibly containing tokens
     * @return value with substitutions applied
     */
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
