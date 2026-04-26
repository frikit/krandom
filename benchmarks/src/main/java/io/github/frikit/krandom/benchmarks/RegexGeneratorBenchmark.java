/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.benchmarks;

import io.github.frikit.krandom.generator.base.RegexGenerator;
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
public class RegexGeneratorBenchmark {

    private static final String SIMPLE_PATTERN = "\\d{3}-\\d{2}-\\d{4}";
    private static final String COMPLEX_PATTERN = "([A-Z]{2}|[A-Z]{3})-\\d{4}-(yes|no|maybe)-[a-z]{3,8}";

    @State(Scope.Benchmark)
    public static class RegexState {
        public final RegexGenerator simple = new RegexGenerator(SIMPLE_PATTERN, 7L);
        public final RegexGenerator complex = new RegexGenerator(COMPLEX_PATTERN, 7L);
    }

    @Benchmark
    public String simplePattern(RegexState state) {
        return state.simple.generate();
    }

    @Benchmark
    public String complexPattern(RegexState state) {
        return state.complex.generate();
    }

    @Benchmark
    public String constructorHotPathWithCachedParseTree() {
        return new RegexGenerator(COMPLEX_PATTERN, 7L).generate();
    }
}
