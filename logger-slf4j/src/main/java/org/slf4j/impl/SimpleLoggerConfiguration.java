/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package org.slf4j.impl;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.security.PrivilegedAction;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Properties;

import org.slf4j.helpers.Reporter;
import org.slf4j.impl.OutputChoice.OutputChoiceType;

/**
 * This class holds configuration values for {@link SimpleLogger}. The
 * values are computed at runtime. See {@link SimpleLogger} documentation for
 * more information.
 *
 *
 * @author Ceki G&uuml;lc&uuml;
 * @author Scott Sanders
 * @author Rod Waldhoff
 * @author Robert Burrell Donkin
 * @author C&eacute;drik LIME
 *
 * @since 1.7.25
 */
public class SimpleLoggerConfiguration {

    /** Name of the properties file searched on the class path. */
    private static final String CONFIGURATION_FILE = "simplelogger.properties";

    /** Default level used when no property is set (INFO). */
    static int DEFAULT_LOG_LEVEL_DEFAULT = SimpleLogger.LOG_LEVEL_INFO;
    /**
     * Runtime default log level as configured by
     * {@code org.slf4j.simpleLogger.defaultLogLevel}.
     */
    int defaultLogLevel = DEFAULT_LOG_LEVEL_DEFAULT;

    /** Default for {@code org.slf4j.simpleLogger.showDateTime} (false). */
    private static final boolean SHOW_DATE_TIME_DEFAULT = false;
    /** Whether to include a time stamp in log lines. */
    boolean showDateTime = SHOW_DATE_TIME_DEFAULT;

    /** Default for {@code org.slf4j.simpleLogger.dateTimeFormat}. */
    private static final String DATE_TIME_FORMAT_STR_DEFAULT = null;
    /** Format string used to render the date, or {@code null} for relative time. */
    private static String dateTimeFormatStr = DATE_TIME_FORMAT_STR_DEFAULT;

    /** Formatter built from {@link #dateTimeFormatStr} when initialised. */
    DateFormat dateFormatter = null;

    /** Default for {@code org.slf4j.simpleLogger.showThreadName} (true). */
    private static final boolean SHOW_THREAD_NAME_DEFAULT = true;
    /** Whether the thread name should appear in output. */
    boolean showThreadName = SHOW_THREAD_NAME_DEFAULT;

    /** Default for {@code org.slf4j.simpleLogger.showLogName} (true). */
    static final boolean SHOW_LOG_NAME_DEFAULT = true;
    /** Whether the logger name should appear in output. */
    boolean showLogName = SHOW_LOG_NAME_DEFAULT;

    /** Default for {@code org.slf4j.simpleLogger.showShortLogName} (false). */
    private static final boolean SHOW_SHORT_LOG_NAME_DEFAULT = false;
    /** If true only the last logger name component is printed. */
    boolean showShortLogName = SHOW_SHORT_LOG_NAME_DEFAULT;

    /** Default for {@code org.slf4j.simpleLogger.levelInBrackets} (false). */
    private static final boolean LEVEL_IN_BRACKETS_DEFAULT = false;
    /** Print the level surrounded by brackets. */
    boolean levelInBrackets = LEVEL_IN_BRACKETS_DEFAULT;

    /** Default for {@code org.slf4j.simpleLogger.logFile} (System.err). */
    private static String LOG_FILE_DEFAULT = "System.err";
    /** Destination for log output. */
    private String logFile = LOG_FILE_DEFAULT;
    /** Actual output target decided after initialisation. */
    OutputChoice outputChoice = null;

    /** Default for {@code org.slf4j.simpleLogger.cacheOutputStream} (false). */
    private static final boolean CACHE_OUTPUT_STREAM_DEFAULT = false;
    /**
     * When true the {@code System.out/err} stream is cached on start up rather
     * than looked up for every log entry.
     */
    private boolean cacheOutputStream = CACHE_OUTPUT_STREAM_DEFAULT;

    /** Default text used for the WARN level. */
    private static final String WARN_LEVELS_STRING_DEFAULT = "WARN";
    /** String to write for the warn level. Configured via {@code org.slf4j.simpleLogger.warnLevelString}. */
    String warnLevelString = WARN_LEVELS_STRING_DEFAULT;

    /** Properties loaded from {@link #CONFIGURATION_FILE}. */
    private final Properties properties = new Properties();

    void init() {
        loadProperties();

        String defaultLogLevelString = getStringProperty(SimpleLogger.DEFAULT_LOG_LEVEL_KEY, null);
        if (defaultLogLevelString != null)
            defaultLogLevel = stringToLevel(defaultLogLevelString);

        showLogName = getBooleanProperty(SimpleLogger.SHOW_LOG_NAME_KEY, SimpleLoggerConfiguration.SHOW_LOG_NAME_DEFAULT);
        showShortLogName = getBooleanProperty(SimpleLogger.SHOW_SHORT_LOG_NAME_KEY, SHOW_SHORT_LOG_NAME_DEFAULT);
        showDateTime = getBooleanProperty(SimpleLogger.SHOW_DATE_TIME_KEY, SHOW_DATE_TIME_DEFAULT);
        showThreadName = getBooleanProperty(SimpleLogger.SHOW_THREAD_NAME_KEY, SHOW_THREAD_NAME_DEFAULT);
        dateTimeFormatStr = getStringProperty(SimpleLogger.DATE_TIME_FORMAT_KEY, DATE_TIME_FORMAT_STR_DEFAULT);
        levelInBrackets = getBooleanProperty(SimpleLogger.LEVEL_IN_BRACKETS_KEY, LEVEL_IN_BRACKETS_DEFAULT);
        warnLevelString = getStringProperty(SimpleLogger.WARN_LEVEL_STRING_KEY, WARN_LEVELS_STRING_DEFAULT);

        logFile = getStringProperty(SimpleLogger.LOG_FILE_KEY, logFile);

        cacheOutputStream = getBooleanProperty(SimpleLogger.CACHE_OUTPUT_STREAM_STRING_KEY, CACHE_OUTPUT_STREAM_DEFAULT);
        outputChoice = computeOutputChoice(logFile, cacheOutputStream);

        if (dateTimeFormatStr != null) {
            try {
                dateFormatter = new SimpleDateFormat(dateTimeFormatStr);
            } catch (IllegalArgumentException e) {
                Reporter.error("Bad date format in " + CONFIGURATION_FILE + "; will output relative time", e);
            }
        }
    }

    private void loadProperties() {
        // Add props from the resource simplelogger.properties
        @SuppressWarnings({"deprecation", "removal"})
        InputStream in = java.security.AccessController.doPrivileged((PrivilegedAction<InputStream>) () -> {
            ClassLoader threadCL = Thread.currentThread().getContextClassLoader();
            if (threadCL != null) {
                return threadCL.getResourceAsStream(CONFIGURATION_FILE);
            } else {
                return ClassLoader.getSystemResourceAsStream(CONFIGURATION_FILE);
            }
        });
        if (null != in) {
            try {
                properties.load(in);
            } catch (java.io.IOException e) {
                // ignored
            } finally {
                try {
                    in.close();
                } catch (java.io.IOException e) {
                    // ignored
                }
            }
        }
    }

    String getStringProperty(String name, String defaultValue) {
        String prop = getStringProperty(name);
        return (prop == null) ? defaultValue : prop;
    }

    boolean getBooleanProperty(String name, boolean defaultValue) {
        String prop = getStringProperty(name);
        return (prop == null) ? defaultValue : "true".equalsIgnoreCase(prop);
    }

    String getStringProperty(String name) {
        String prop = null;
        try {
            prop = System.getProperty(name);
        } catch (SecurityException e) {
    ; // none // Ignore
        }
        return (prop == null) ? properties.getProperty(name) : prop;
    }

    static int stringToLevel(String levelStr) {
        if ("trace".equalsIgnoreCase(levelStr)) {
            return SimpleLogger.LOG_LEVEL_TRACE;
        } else if ("debug".equalsIgnoreCase(levelStr)) {
            return SimpleLogger.LOG_LEVEL_DEBUG;
        } else if ("info".equalsIgnoreCase(levelStr)) {
            return SimpleLogger.LOG_LEVEL_INFO;
        } else if ("warn".equalsIgnoreCase(levelStr)) {
            return SimpleLogger.LOG_LEVEL_WARN;
        } else if ("error".equalsIgnoreCase(levelStr)) {
            return SimpleLogger.LOG_LEVEL_ERROR;
        } else if ("off".equalsIgnoreCase(levelStr)) {
            return SimpleLogger.LOG_LEVEL_OFF;
        }
        // assume INFO by default
        return SimpleLogger.LOG_LEVEL_INFO;
    }

    private static OutputChoice computeOutputChoice(String logFile, boolean cacheOutputStream) {
        if ("System.err".equalsIgnoreCase(logFile))
            if (cacheOutputStream)
                return new OutputChoice(OutputChoiceType.CACHED_SYS_ERR);
            else
                return new OutputChoice(OutputChoiceType.SYS_ERR);
        else if ("System.out".equalsIgnoreCase(logFile)) {
            if (cacheOutputStream)
                return new OutputChoice(OutputChoiceType.CACHED_SYS_OUT);
            else
                return new OutputChoice(OutputChoiceType.SYS_OUT);
        } else {
            try {
                FileOutputStream fos = new FileOutputStream(logFile);
                PrintStream printStream = new PrintStream(fos);
                return new OutputChoice(printStream);
            } catch (FileNotFoundException e) {
                Reporter.error("Could not open [" + logFile + "]. Defaulting to System.err", e);
                return new OutputChoice(OutputChoiceType.SYS_ERR);
            }
        }
    }
}
