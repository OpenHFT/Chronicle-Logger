package com.higherfrequencytrading.chronology.logback;

import com.higherfrequencytrading.chronology.Chronology;
import com.higherfrequencytrading.chronology.ChronologyLogEvent;
import com.higherfrequencytrading.chronology.ChronologyLogHelper;
import com.higherfrequencytrading.chronology.ChronologyLogLevel;
import net.openhft.chronicle.Chronicle;
import net.openhft.chronicle.ExcerptTailer;
import net.openhft.lang.io.IOTools;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

public class LogbackIndexedChronicleTest extends LogbackTestBase {

    // *************************************************************************
    //
    // *************************************************************************

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
        //IOTools.deleteDir(rootPath());
    }

    // *************************************************************************
    // BINARY
    // *************************************************************************

    @Test
    public void testBinaryAppender1() throws IOException {
        final String testId    = "binary-indexed-chronicle";
        final String threadId  = testId + "-th";
        final long   timestamp = System.currentTimeMillis();
        final Logger logger    = LoggerFactory.getLogger(testId);

        Thread.currentThread().setName(threadId);

        for(ChronologyLogLevel level : LOG_LEVELS) {
            log(logger,level,"level is {}",level.levelStr);
        }

        Chronicle          chronicle = getIndexedChronicle(testId);
        ExcerptTailer      tailer    = chronicle.createTailer().toStart();
        ChronologyLogEvent evt       = null;

        for(ChronologyLogLevel level : LOG_LEVELS) {
            assertTrue(tailer.nextIndex());

            evt = ChronologyLogHelper.decodeBinary(tailer);
            assertNotNull(evt);
            assertEquals(evt.getVersion(), Chronology.VERSION);
            assertEquals(evt.getType(), Chronology.TYPE_LOGBACK);
            assertTrue(evt.getTimeStamp() >= timestamp);
            assertEquals(level,evt.getLevel());
            assertEquals(threadId, evt.getThreadName());
            assertEquals(testId, evt.getLoggerName());
            assertEquals("level is {}", evt.getMessage());
            assertNotNull(evt.getArgumentArray());
            assertEquals(1, evt.getArgumentArray().length);
            assertEquals(level.levelStr , evt.getArgumentArray()[0]);

            tailer.finish();
        }

        tailer.close();
        chronicle.close();

        IOTools.deleteDir(basePath(testId));
    }

    @Test
    public void testBinaryAppender2() throws IOException {
        final String testId    = "binary-indexed-chronicle-fmt";
        final String threadId  = testId + "-th";
        final long   timestamp = System.currentTimeMillis();
        final Logger logger    = LoggerFactory.getLogger(testId);

        Thread.currentThread().setName(threadId);

        for(ChronologyLogLevel level : LOG_LEVELS) {
            log(logger,level,"level is {}",level.levelStr);
        }

        Chronicle          chronicle = getIndexedChronicle(testId);
        ExcerptTailer      tailer    = chronicle.createTailer().toStart();
        ChronologyLogEvent evt       = null;

        for(ChronologyLogLevel level : LOG_LEVELS) {
            assertTrue(tailer.nextIndex());

            evt = ChronologyLogHelper.decodeBinary(tailer);
            assertNotNull(evt);
            assertEquals(evt.getVersion(), Chronology.VERSION);
            assertEquals(evt.getType(), Chronology.TYPE_LOGBACK);
            assertTrue(evt.getTimeStamp() >= timestamp);
            assertEquals(level,evt.getLevel());
            assertEquals(threadId, evt.getThreadName());
            assertEquals(testId, evt.getLoggerName());
            assertEquals("level is " + level.levelStr, evt.getMessage());
            assertNotNull(evt.getArgumentArray());
            assertEquals(0, evt.getArgumentArray().length);

            tailer.finish();
        }

        tailer.close();
        chronicle.close();

        IOTools.deleteDir(basePath(testId));
    }

    @Test
    public void testMultiThreadLogging() throws IOException, InterruptedException {
        final int RUNS = 1000000;
        final int THREADS = 4;

        for (int size : new int[]{64, 128, 256}) {
            {
                final long start = System.nanoTime();

                ExecutorService es = Executors.newFixedThreadPool(THREADS);
                for (int t = 0; t < THREADS; t++) {
                    es.submit(new RunnableChronicle(RUNS, size, "perf-binary-indexed-chronicle"));
                }

                es.shutdown();
                es.awaitTermination(5, TimeUnit.SECONDS);

                final long time = System.nanoTime() - start;

                System.out.printf("Indexed.MultiThreadLogging (runs=%d, min size=%03d): took an average of %.3f us per entry\n",
                    RUNS,
                    size,
                    time / 1e3 / (RUNS * THREADS)
                );
            }

            {
                final long start = System.nanoTime();

                ExecutorService es = Executors.newFixedThreadPool(THREADS);
                for (int t = 0; t < THREADS; t++) {
                    es.submit(new RunnableChronicle(RUNS, size, "perf-plain-vanilla"));
                }

                es.shutdown();
                es.awaitTermination(5, TimeUnit.SECONDS);

                final long time = System.nanoTime() - start;

                System.out.printf("Plain.MultiThreadLogging (runs=%d, min size=%03d): took an average of %.3f us per entry\n",
                    RUNS,
                    size,
                    time / 1e3 / (RUNS * THREADS)
                );
            }
        }
    }
}
