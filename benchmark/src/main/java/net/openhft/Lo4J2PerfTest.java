/*
 * Copyright 2013-2026 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft;

import net.openhft.chronicle.core.io.IOTools;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

/**
 * JMH benchmark comparing Chronicle logging via Log4j 2 with
 * ordinary file based logging.
 *
 * <p>Build the benchmark jar and run it with:
 *
 * <pre>{@code
 * mvn -pl benchmark package
 * java -jar benchmark/target/benchmarks.jar
 * }</pre>
 */
@State(Scope.Thread)
public class Lo4J2PerfTest {
    private final Logger chronicleLogger = LoggerFactory.getLogger("perf-chro");
    private final Logger fileLogger = LoggerFactory.getLogger("perf-file");

    public static void main(String[] args) throws RunnerException {

        Options opt = new OptionsBuilder()
                .include(Lo4J2PerfTest.class.getSimpleName())
                .warmupIterations(5)
                .measurementIterations(5)
                .forks(1)
                .build();

        new Runner(opt).run();

    }

    private static String rootPath() {
        // NB:
        // Be careful if using /tmp - quite often it's mounted as TMPFS, e.g. as RAM disk.
        // This will nullify the benefits of Chronicle Logger
        String path = System.getenv("HOME");
        String sep = System.getProperty("file.separator");

        if (!path.endsWith(sep)) {
            path += sep;
        }

        return path + "chronicle-log4j2-bench";
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.NANOSECONDS)
    public void testChronicle() {
        chronicleLogger.debug("Test {} {} " + 1, 2, 3);
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.NANOSECONDS)
    public void testChronicleException() {
        chronicleLogger.debug("Throwable test 2", new UnsupportedOperationException("Exception message"));
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.NANOSECONDS)
    public void testFile() {
        fileLogger.debug("Test {} {} " + 1, 2, 3);
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.NANOSECONDS)
    public void testFileException() {
        fileLogger.debug("Throwable test 2", new UnsupportedOperationException("Exception message"));
    }

    @TearDown
    public void tearDown() {
        IOTools.deleteDirWithFiles(rootPath());
    }
}
