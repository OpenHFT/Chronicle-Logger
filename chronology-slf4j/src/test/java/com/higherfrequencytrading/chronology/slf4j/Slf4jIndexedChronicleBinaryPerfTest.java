package com.higherfrequencytrading.chronology.slf4j;

import net.openhft.lang.io.IOTools;
import org.apache.commons.lang3.StringUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Ignore
public class Slf4jIndexedChronicleBinaryPerfTest extends Slf4jTestBase {

    // *************************************************************************
    //
    // *************************************************************************

    @Before
    public void setUp() {
        System.setProperty(
            "slf4j.chronicle.properties",
            System.getProperty("slf4j.chronicle.indexed.perf.properties")
        );

        getChronicleLoggerFactory().relaod();
        getChronicleLoggerFactory().warmup();
    }

    @After
    public void tearDown() {
        getChronicleLoggerFactory().shutdown();

        IOTools.deleteDir(basePath(ChronicleLoggingConfig.TYPE_INDEXED));
    }

    // *************************************************************************
    //
    // *************************************************************************

    @Test
    public void testSingleThreadLogging() throws IOException {
        Logger l = LoggerFactory.getLogger(Slf4jIndexedChronicleBinaryPerfTest.class);

        for (int size : new int[]{64, 128, 256}) {
            String msg = StringUtils.rightPad("", size, 'X');

            {
                long start = System.nanoTime();

                int items = 1000000;
                for (int i = 1; i <= items; i++) {
                    l.trace("{} ({}}", msg, i);
                }

                long end = System.nanoTime();

                System.out.printf("Indexed.SingleThreadLogging (min size %d, level disabled): took an average of %.2f us to write %d items\n",
                        size,
                        (end - start) / items / 1e3,
                        items);
            }

            {
                long start = System.nanoTime();

                int items = 1000000;
                for (int i = 1; i <= items; i++) {
                    l.warn("{} ({}}", msg, i);
                }

                long end = System.nanoTime();

                System.out.printf("Indexed.SingleThreadLogging (min size %d, level enabled): took an average of %.2f us to write %d items\n",
                        size,
                        (end - start) / items / 1e3,
                        items);
            }
        }
    }

    @Test
    public void testSingleThreadLogging2() throws IOException {
        final Logger clogger = LoggerFactory.getLogger(Slf4jIndexedChronicleBinaryPerfTest.class);
        final long   items     = 10000;
        final String strFmt    = StringUtils.leftPad("> v1={}, v2={}, v3={}", 32, 'X');

        for(int n=0;n<10;n++) {

            long cStart1 = System.nanoTime();

            for (int i = 1; i <= items; i++) {
                clogger.info(strFmt, i, i * 10, i / 16);
            }

            long cEnd1 = System.nanoTime();

            System.out.printf("items=%03d chronology=%.3f\n",
                items,
                (cEnd1 - cStart1) / items / 1e3);
        }
    }

    @Test
    public void testMultiThreadLogging() throws IOException, InterruptedException {
        final int RUNS = 1000000;
        final int THREADS = 4;

        for (int size : new int[]{64, 128, 256}) {
            final long start = System.nanoTime();

            ExecutorService es = Executors.newFixedThreadPool(THREADS);
            for (int t = 0; t < THREADS; t++) {
                es.submit(new RunnableLogger(RUNS, size, "thread-" + t));
            }

            es.shutdown();
            es.awaitTermination(5, TimeUnit.SECONDS);

            final long time = System.nanoTime() - start;

            System.out.printf("Indexed.MultiThreadLogging (min size = %d): took an average of %.1f us per entry\n",
                    size,
                    time / 1e3 / (RUNS * THREADS)
            );
        }
    }
}
