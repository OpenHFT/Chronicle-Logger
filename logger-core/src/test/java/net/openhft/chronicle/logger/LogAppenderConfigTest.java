package net.openhft.chronicle.logger;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class LogAppenderConfigTest {

    @Test
    public void testConfig() {
        String tomlString = "contentEncoding = \"zstd\"\n" +
                "contentType = \"application/json\"";

        LogAppenderConfig config = LogAppenderConfig.parse(tomlString);
        assertEquals(config.getContentEncoding(), "zstd");
        assertEquals(config.getContentType(), "application/json");
    }

    @Test
    public void testDefaults() {
        String tomlString = "contentEncoding = \"zstd\"";
        LogAppenderConfig config = LogAppenderConfig.parse(tomlString);
        assertEquals(config.getContentEncoding(), "zstd");
        assertEquals(config.getContentType(), "application/octet-stream");
    }
}
