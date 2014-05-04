package com.higherfrequencytrading.chronology.slf4j;

import com.higherfrequencytrading.chronology.Chronology;
import com.higherfrequencytrading.chronology.ChronologyLogLevel;
import org.junit.Test;

import java.io.File;

import static org.junit.Assert.assertEquals;

/**
 *
 */
public class Slf4jIndexedChronicleConfigurationTest extends Slf4jTestBase {

    @Test
    public void testLoadProperties() {
        String cfgPath = System.getProperty("slf4j.chronicle.indexed.properties");
        ChronicleLoggingConfig cfg = ChronicleLoggingConfig.load(cfgPath);

        assertEquals(
            new File(basePath(ChronicleLoggingConfig.TYPE_INDEXED, "root")),
            new File(cfg.getString(ChronicleLoggingConfig.KEY_PATH)));
        assertEquals(
            ChronicleLoggingConfig.TYPE_INDEXED,
            cfg.getString(ChronicleLoggingConfig.KEY_TYPE));
        assertEquals(
            ChronicleLoggingConfig.BINARY_MODE_FORMATTED,
            cfg.getString(ChronicleLoggingConfig.KEY_BINARY_MODE));
        assertEquals(
            Chronology.STR_FALSE,
            cfg.getString(ChronicleLoggingConfig.KEY_SYNCHRONOUS));
        assertEquals(
            ChronologyLogLevel.DEBUG.levelStr,
            cfg.getString(ChronicleLoggingConfig.KEY_LEVEL).toUpperCase());
        assertEquals(
            Chronology.STR_FALSE,
            cfg.getString(ChronicleLoggingConfig.KEY_SHORTNAME));
        assertEquals(
            Chronology.STR_FALSE,
            cfg.getString(ChronicleLoggingConfig.KEY_APPEND));
        assertEquals(
            new File(basePath(ChronicleLoggingConfig.TYPE_INDEXED, "logger_1")),
            new File(cfg.getString("logger_1", ChronicleLoggingConfig.KEY_PATH)));
        assertEquals(
            ChronologyLogLevel.INFO.levelStr,
            cfg.getString("logger_1", ChronicleLoggingConfig.KEY_LEVEL).toUpperCase());
        assertEquals(
            new File(basePath(ChronicleLoggingConfig.TYPE_INDEXED, "readwrite")),
            new File(cfg.getString("readwrite", ChronicleLoggingConfig.KEY_PATH)));
        assertEquals(
            ChronologyLogLevel.DEBUG.levelStr,
            cfg.getString("readwrite", ChronicleLoggingConfig.KEY_LEVEL).toUpperCase());
    }
}
