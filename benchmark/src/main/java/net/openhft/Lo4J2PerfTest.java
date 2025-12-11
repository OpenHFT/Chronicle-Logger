/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
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

import java.nio.file.FileSystems;
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
@SuppressWarnings("PMD.TestClassWithoutTestCases")
public class Lo4J2PerfTest {
    private static final int CHRONICLE_INDEX = 0;
    private static final int FILE_INDEX = 1;
    private final Logger[] loggers = {
            LoggerFactory.getLogger("perf-chro"),
            LoggerFactory.getLogger("perf-file")
    };

    /**
     * Default constructor required by JMH.
     */
    public Lo4J2PerfTest() {
    }

    /**
     * Executes the benchmark from the command line.
     *
     * @param args JVM arguments (unused)
     * @throws RunnerException if the benchmark fails
     */
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
        String sep = FileSystems.getDefault().getSeparator();

        if (!path.endsWith(sep)) {
            path += sep;
        }

        return path + "chronicle-log4j2-bench";
    }

    /**
     * Benchmarks Chronicle logging of a simple message.
     */
    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.NANOSECONDS)
    public void testChronicle() {
        loggers[CHRONICLE_INDEX].debug("Test {} {} " + 1, 2, 3);
    }

    /**
     * Benchmarks Chronicle logging when a throwable is attached.
     */
    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.NANOSECONDS)
    public void testChronicleException() {
        loggers[CHRONICLE_INDEX].debug("Throwable test 2", new UnsupportedOperationException("Exception message"));
    }

    /**
     * Benchmarks file-based logging of a simple message.
     */
    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.NANOSECONDS)
    public void testFile() {
        loggers[FILE_INDEX].debug("Test {} {} " + 1, 2, 3);
    }

    /**
     * Benchmarks file-based logging when a throwable is attached.
     */
    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.NANOSECONDS)
    public void testFileException() {
        loggers[FILE_INDEX].debug("Throwable test 2", new UnsupportedOperationException("Exception message"));
    }

    /**
     * Cleans up benchmark output directories after execution.
     */
    @TearDown
    public void tearDown() {
        IOTools.deleteDirWithFiles(rootPath());
    }
}
