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

package net.openhft.chronicle.logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.management.ManagementFactory;
import java.util.*;

/**
 * @author lburgazzoli
 * @author dpisklov
 *
 * Configurationn example:
 *
 * # default
 * chronicle.logger.base = ${java.io.tmpdir}/chronicle/${pid}
 *
 * # logger : root
 * chronicle.logger.root.path      = ${chronicle.logger.base}/root
 * chronicle.logger.root.level     = debug
 * chronicle.logger.root.shortName = false
 * chronicle.logger.root.append    = false
 *
 * # logger : Logger1
 * chronicle.logger.Logger1.path = ${chronicle.logger.base}/logger_1
 * chronicle.logger.Logger1.level = info
 */
public class ChronicleLogConfig {
    public static final String KEY_LEVEL = "level";
    public static final String KEY_PATH = "path";
    public static final String KEY_APPEND = "append";
    public static final String PLACEHOLDER_START = "${";
    public static final String PLACEHOLDER_END = "}";

    private static final String KEY_PROPERTIES_FILE = "chronicle.logger.properties";
    private static final String KEY_PREFIX = "chronicle.logger.";
    private static final String KEY_PREFIX_ROOT = "chronicle.logger.root.";
    private static final String KEY_CFG_PREFIX = "chronicle.logger.root.cfg.";
    private static final String PLACEHOLDER_PID = "${pid}";

    private static final List<String> DEFAULT_CFG_LOCATIONS = Arrays.asList(
        "chronicle-logger.properties",
        "config/chronicle-logger.properties"
    );

    private static final String PID = ManagementFactory.getRuntimeMXBean().getName().split("@")[0];

    private final Properties properties;
    private final LogAppenderConfig appenderConfig;

    private ChronicleLogConfig(final Properties properties, final LogAppenderConfig appenderConfig) {
        this.properties = properties;
        this.appenderConfig = appenderConfig;
    }

    public static ChronicleLogConfig load(final Properties properties) {
        return new ChronicleLogConfig(
            properties,
            loadAppenderConfig(properties)
        );
    }

    /**
     * @param cfgPath   the configuration path
     * @return          the configuration object
     */
    public static ChronicleLogConfig load(String cfgPath) {
        try {
            return load(getConfigurationStream(cfgPath));
        } catch (Exception e) {
            // is printing stack trace and falling through really the right thing
            // to do here, or should it throw out?
            e.printStackTrace();
        }

        return null;
    }

    private static ChronicleLogConfig load(InputStream in) {
        if (in != null) {
            Properties properties = new Properties();

            try {
                properties.load(in);
                in.close();
            } catch (IOException ignored) {
            }

            return load(interpolate(properties));

        } else {
            System.err.printf(
                "Unable to configure chronicle-logger:"
                + " configuration file not found in default locations (%s)"
                + " or System property (%s) is not defined \n",
                DEFAULT_CFG_LOCATIONS.toString(),
                KEY_PROPERTIES_FILE);
        }

        return null;
    }

    /**
     * @return  the configuration object
     */
    public static ChronicleLogConfig load() {
        try {
            InputStream is = getConfigurationStream(System.getProperty(KEY_PROPERTIES_FILE));
            if(is == null) {
                for(String location : DEFAULT_CFG_LOCATIONS) {
                    is = getConfigurationStream(location);
                    if(is != null) {
                        break;
                    }
                }
            }

            if(is != null) {
                return load(is);
            }
        } catch (Exception e) {
            // is printing stack trace and falling through really the right thing
            // to do here, or should it throw out?
            e.printStackTrace();
        }

        return null;
    }

    private static InputStream getConfigurationStream(String cfgPath) throws IOException {
        if(cfgPath != null) {
            final File cfgFile = new File(cfgPath);
            if (!cfgFile.exists()) {
                return Thread.currentThread().getContextClassLoader().getResourceAsStream(cfgPath);

            } else if (cfgFile.canRead()) {
                return new FileInputStream(cfgFile);
            }
        }

        return null;
    }

    private static Properties interpolate(final Properties props) {
        int amended;
        do {
            amended = 0;
            for (Map.Entry<Object, Object> entries : props.entrySet()) {
                String val = props.getProperty((String) entries.getKey());
                val = val.replace(PLACEHOLDER_PID, PID);

                int startIndex;
                int endIndex = 0;

                do {
                    startIndex = val.indexOf(PLACEHOLDER_START, endIndex);
                    if (startIndex != -1) {
                        endIndex = val.indexOf(PLACEHOLDER_END, startIndex);
                        if (endIndex != -1) {
                            String envKey = val.substring(startIndex + 2, endIndex);
                            String newVal = null;
                            if (props.containsKey(envKey)) {
                                newVal = props.getProperty(envKey);

                            } else if (System.getProperties().containsKey(envKey)) {
                                newVal = System.getProperties().getProperty(envKey);
                            }

                            if (newVal != null) {
                                val = val.replace(PLACEHOLDER_START + envKey + PLACEHOLDER_END, newVal);
                                endIndex += newVal.length() - envKey.length() + 3;

                                amended++;
                            }
                        }
                    }
                } while (startIndex != -1 && endIndex != -1 && endIndex < val.length());

                entries.setValue(val);
            }
        } while (amended > 0);

        return props;
    }

    private static LogAppenderConfig loadAppenderConfig(final Properties properties) {

        final LogAppenderConfig cfg = new LogAppenderConfig();
        cfg.setProperties(properties, KEY_CFG_PREFIX);

        return cfg;
    }

    // *************************************************************************
    //
    // *************************************************************************

    public LogAppenderConfig getAppenderConfig() {
        return this.appenderConfig;
    }

    public String getString(final String shortName) {
        String name = KEY_PREFIX_ROOT + shortName;
        return this.properties.getProperty(name);
    }

    public String getString(final String loggerName, final String shortName) {
        String name = KEY_PREFIX  + loggerName + "." + shortName;
        String val = this.properties.getProperty(name);

        if (val == null) {
            val = getString(shortName);
        }

        return val;
    }

    public Boolean getBoolean(final String shortName) {
        return getBoolean(shortName, null);
    }

    public Boolean getBoolean(final String shortName, boolean defval) {
        String prop = getString(shortName);
        return (prop != null) ? "true".equalsIgnoreCase(prop) : defval;
    }

    public Boolean getBoolean(final String loggerName, final String shortName) {
        String prop = getString(loggerName, shortName);
        return (prop != null) ? "true".equalsIgnoreCase(prop) : null;
    }

    public Boolean getBoolean(final String loggerName, final String shortName, boolean defval) {
        String prop = getString(loggerName, shortName);
        return (prop != null) ? "true".equalsIgnoreCase(prop) : defval;
    }

    public Integer getInteger(final String shortName) {
        String prop = getString(shortName);
        return (prop != null) ? Integer.parseInt(prop) : null;
    }

    public Integer getInteger(final String loggerName, final String shortName) {
        String prop = getString(loggerName, shortName);
        return (prop != null) ? Integer.parseInt(prop) : null;
    }

    public Long getLong(final String shortName) {
        String prop = getString(shortName);
        return (prop != null) ? Long.parseLong(prop) : null;
    }

    public Long getLong(final String loggerName, final String shortName) {
        String prop = getString(loggerName, shortName);
        return (prop != null) ? Long.parseLong(prop) : null;
    }

    public ChronicleLogLevel getLevel(final String loggerName) {
        return getLevel(loggerName, null);
    }

    public ChronicleLogLevel getLevel(final String loggerName, ChronicleLogLevel defVal) {
        String prop = getString(loggerName, KEY_LEVEL);
        return (prop != null) ? ChronicleLogLevel.fromStringLevel(prop) : defVal;
    }
}
