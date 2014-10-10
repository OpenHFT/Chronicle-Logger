package net.openhft.chronicle.logger;

import net.openhft.chronicle.Chronicle;
import net.openhft.chronicle.ExcerptAppender;
import net.openhft.chronicle.VanillaChronicle;
import net.openhft.chronicle.tools.ChronicleTools;
import org.apache.commons.lang3.StringUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Created by peter on 10/10/14.
 */
public class VanillChronicleQueuePerfTest {
    Chronicle chronicle;
    ExcerptAppender logger;

    @Before
    public void setUp() throws IOException {
        String baseDir = System.getProperty("java.io.tmpdir") + "/perf-binary-vanilla-chronicle";
        ChronicleTools.deleteDirOnExit(baseDir);
        chronicle = new VanillaChronicle(baseDir);
        logger = chronicle.createAppender();
    }

    @After
    public void tearDown() throws IOException {
        chronicle.close();
    }

    @Test
    public void testMultiThreadLogging() throws IOException, InterruptedException {

        final int RUNS = 2000000;
        final int THREADS = Runtime.getRuntime().availableProcessors();

        for (int size : new int[]{64, 128, 256}) {
            {
                final long start = System.nanoTime();

                ExecutorService es = Executors.newFixedThreadPool(THREADS);
                for (int t = 0; t < THREADS; t++) {
                    es.submit(new RunnableLogger(RUNS, size));
                }

                es.shutdown();
                es.awaitTermination(10, TimeUnit.SECONDS);

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

    protected final class RunnableLogger implements Runnable {
        private static final String fmtBase = " > val1={}, val2={}, val3={}";
        private final String fmt;
        private final int runs;

        public RunnableLogger(int runs, int pad) {
            this.runs = runs;
            fmt = StringUtils.rightPad(fmtBase, pad + fmtBase.length(), "X");
        }

        @Override
        public void run() {
            for (int i = 0; i < this.runs; i++) {
                logger.startExcerpt();
                logger.writeUTFΔ(fmt);
                logger.writeInt(i);
                logger.writeInt(i * 7);
                logger.writeInt(i / 16);
                logger.finish();
            }
        }
    }
}
