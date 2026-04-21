/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package org.github.krandom.benchmarks;

import org.github.krandom.generator.object.ObjectGenerator;
import org.github.krandom.generator.GeneratorConfig;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;

import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.SECONDS)
@Warmup(iterations = 2, time = 1)
@Measurement(iterations = 3, time = 1)
@Fork(1)
public class ObjectGeneratorBenchmark {

    @State(Scope.Benchmark)
    public static class GeneratorState {
        public final ObjectGenerator<BenchmarkFixtures.Depth2Root> depth2Generator =
            new ObjectGenerator<>(BenchmarkFixtures.Depth2Root.class,
                                  GeneratorConfig.builder().objectMaxDepth(2).build());

        public final ObjectGenerator<BenchmarkFixtures.Depth5Root> depth5Generator =
            new ObjectGenerator<>(BenchmarkFixtures.Depth5Root.class,
                                  GeneratorConfig.builder().objectMaxDepth(5).build());

        public final ObjectGenerator<BenchmarkFixtures.Depth10Root> depth10Generator =
            new ObjectGenerator<>(BenchmarkFixtures.Depth10Root.class,
                                  GeneratorConfig.builder().objectMaxDepth(10).build());
    }

    @Benchmark
    public BenchmarkFixtures.Depth2Root depth2ObjectGraph(GeneratorState state) {
        return state.depth2Generator.generate();
    }

    @Benchmark
    public BenchmarkFixtures.Depth5Root depth5ObjectGraph(GeneratorState state) {
        return state.depth5Generator.generate();
    }

    @Benchmark
    public BenchmarkFixtures.Depth10Root depth10ObjectGraph(GeneratorState state) {
        return state.depth10Generator.generate();
    }
}
