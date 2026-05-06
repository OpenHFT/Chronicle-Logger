/*
 * Copyright 2013-2026 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.logger.slf4j2;

import org.junit.jupiter.api.Test;
import org.slf4j.spi.SLF4JServiceProvider;

import java.util.ServiceLoader;

import static org.junit.jupiter.api.Assertions.*;

class Slf4jProviderHealthCheckTest {

    @Test
    void chronicleProviderIsOnClasspath() {
        boolean found = false;
        for (SLF4JServiceProvider provider : ServiceLoader.load(SLF4JServiceProvider.class)) {
            provider.initialize();
            if (provider.getLoggerFactory() instanceof ChronicleLoggerFactory) {
                found = true;
            }
        }

        assertTrue(found, "Expected Chronicle SLF4J 2.x provider on the classpath");
    }
}
