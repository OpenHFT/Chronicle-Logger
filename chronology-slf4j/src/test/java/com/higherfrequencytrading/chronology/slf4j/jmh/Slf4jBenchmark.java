package com.higherfrequencytrading.chronology.slf4j.jmh;

import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OperationsPerInvocation;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.openjdk.jmh.logic.BlackHole;

import java.util.concurrent.TimeUnit;

@State(Scope.Thread)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
public class Slf4jBenchmark {

    @BenchmarkMode(Mode.AverageTime)
    @Warmup(iterations = 1000)
    @Measurement(iterations = 1000)
    public void measureX() {
        BlackHole.consumeCPU(100);
    }

    // *************************************************************************
    //
    // *************************************************************************

    public static void main(String... args) throws Exception {
        Options opt = new OptionsBuilder()
            .include(".*" + Slf4jBenchmark.class.getSimpleName() + ".*")
            .forks(1)
            .verbosity(VerboseMode.EXTRA)
            .build();

        new Runner(opt).run();
    }
}
