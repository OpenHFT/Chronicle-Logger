/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.logger.tools;

import net.openhft.chronicle.core.OS;
import net.openhft.chronicle.core.util.Time;
import net.openhft.chronicle.wire.WireType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ChronicleLogReaderTest {
    @BeforeEach
    public void setup() {
        System.setProperty(
                "logback.configurationFile",
                System.getProperty("resources.path")
                        + "/logback-chronicle-binary-appender.xml");
    }

    @Test
    public void readTest() {
        final Logger logger = LoggerFactory.getLogger("binary-chronicle");
        logger.info("test {} {} {}", 1, 100L, 100.123D);
        logger.info("test {} {} {}", 2, 100L, 100.123D);
        logger.info("test {} {} {}", 3, 100L, 100.123D);

        ChronicleLogReader reader = new ChronicleLogReader(OS.getTarget() + "/chronicle-logback/binary-chronicle" + Time.uniqueId(), WireType.BINARY_LIGHT);
        reader.processLogs(ChronicleLogReader::printf, false);

        //ChroniCat.main(new String[] {OS.getTarget() + "/chronicle-logback/binary-chronicle"});
    }
}
