/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.benchmarks;

import io.github.frikit.krandom.generator.Generators;
import io.github.frikit.krandom.generator.object.ObjectGenerator;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;

import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.SECONDS)
@Warmup(iterations = 2, time = 1)
@Measurement(iterations = 3, time = 1)
@Fork(1)
public class ObjectFactoryBenchmark {

    @State(Scope.Thread)
    public static class GeneratorState {
        public final ObjectGenerator<BenchmarkFixtures.SimpleUser> directGenerator =
            new ObjectGenerator<>(BenchmarkFixtures.SimpleUser.class);
        public final ObjectGenerator<BenchmarkFixtures.SimpleUser> facadeGenerator =
            Generators.ofObject(BenchmarkFixtures.SimpleUser.class);
    }

    @Benchmark
    public BenchmarkFixtures.SimpleUser directObjectGenerator(GeneratorState state) {
        return state.directGenerator.generate();
    }

    @Benchmark
    public BenchmarkFixtures.SimpleUser generatorsFacadeObjectGenerator(GeneratorState state) {
        return state.facadeGenerator.generate();
    }

    @Benchmark
    public BenchmarkFixtures.SimpleUser manualConstruction() {
        BenchmarkFixtures.SimpleUser user = new BenchmarkFixtures.SimpleUser();
        int suffix = ThreadLocalRandom.current().nextInt(1, 1_000_000);
        user.firstName = "first" + suffix;
        user.lastName = "last" + suffix;
        user.email = "user" + suffix + "@example.com";
        user.age = ThreadLocalRandom.current().nextInt(18, 90);
        return user;
    }
}
