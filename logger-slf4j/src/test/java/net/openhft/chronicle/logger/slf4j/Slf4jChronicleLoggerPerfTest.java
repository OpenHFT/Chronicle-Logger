/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.logger.slf4j;

import net.openhft.chronicle.core.io.IOTools;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Disabled("Long running performance test")
public class Slf4jChronicleLoggerPerfTest extends Slf4jTestBase {

    @BeforeEach
    public void setUp() {
        System.setProperty(
                "chronicle.logger.properties",
                "chronicle.logger.perf.properties");

        reloadChronicleLoggerFactory();
    }

    @AfterEach
    public void tearDown() {
        IOTools.deleteDirWithFiles(basePath());
    }

    // Single Thread
    @Test
    public void testSingleThreadLogging1() {
        Thread.currentThread().setName("perf-plain");

        final String testId = "perf-chronicle";
        final Logger clogger = LoggerFactory.getLogger(testId);
        final long items = 1000000;

        assertNotNull(clogger, "logger instance should be created for performance test");
        assertTrue(clogger.isInfoEnabled(), "logger info enabled");

        warmup(clogger);

        for (int s = 64; s <= 1024; s += 64) {
            final String staticStr = StringUtils.leftPad("", s, 'X');

            long cStart1 = System.nanoTime();

            for (int i = 1; i <= items; i++) {
                clogger.info(staticStr);
            }

            long cEnd1 = System.nanoTime();

            System.out.printf("items=%03d size=%04d => chronology=%.3f ms, chronology-average=%.3f us\n",
                    items,
                    staticStr.length(),
                    (cEnd1 - cStart1) / 1e6,
                    (cEnd1 - cStart1) / items / 1e3);
        }
    }

    @Test
    public void testSingleThreadLogging2() {
        Thread.currentThread().setName("perf-plain");

        final String testId = "perf-chronicle";
        final Logger clogger = LoggerFactory.getLogger(testId);
        final long items = 1000000;
        final String strFmt = StringUtils.leftPad("> v1={}, v2={}, v3={}", 32, 'X');

        assertNotNull(clogger, "logger instance should be created for performance test");
        assertTrue(clogger.isInfoEnabled(), "logger info enabled");

        warmup(clogger);

        for (int n = 0; n < 10; n++) {
            long cStart1 = System.nanoTime();

            for (int i = 1; i <= items; i++) {
                clogger.info(strFmt, i, i * 10, i / 16);
            }

            long cEnd1 = System.nanoTime();

            System.out.printf("items=%03d => chronology=%.3f ms, chronology-average=%.3f us\n",
                    items,
                    (cEnd1 - cStart1) / 1e6,
                    (cEnd1 - cStart1) / items / 1e3);
        }
    }

    // Multi Thread
    @Test
    public void testMultiThreadLogging() throws InterruptedException {
        warmup(LoggerFactory.getLogger("perf-chronicle"));

        final int RUNS = 1000000;
        final int THREADS = 10;

        assertTrue(RUNS > 0, "runs > 0");
        assertTrue(THREADS > 0, "threads > 0");

        for (int size : new int[]{64, 128, 256}) {
            {
                final long start = System.nanoTime();

                ExecutorService es = Executors.newFixedThreadPool(THREADS);
                for (int t = 0; t < THREADS; t++) {
                    es.submit(new RunnableLogger(RUNS, size, "perf-chronicle"));
                }

                es.shutdown();
                assertTrue(es.awaitTermination(5, TimeUnit.SECONDS), "executor terminated");

                final long time = System.nanoTime() - start;

                System.out.printf("ChronicleLog.MT (runs=%d, min size=%03d, elapsed=%.3f ms) took an average of %.3f us per entry\n",
                        RUNS,
                        size,
                        time / 1e6,
                        time / 1e3 / (RUNS * THREADS)
                );
            }
        }
    }
}
