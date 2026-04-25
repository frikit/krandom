/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package org.github.krandom.benchmarks;

import org.github.krandom.generator.GeneratorConfig;
import org.github.krandom.generator.object.ObjectGenerator;
import org.instancio.Instancio;
import org.jeasy.random.EasyRandom;
import org.jeasy.random.EasyRandomParameters;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Head-to-head bulk generation: produce N objects per invocation.
 *
 * <p>Parameterized by batch size (100 and 1000) so the results show how each library
 * scales with volume.
 */
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.SECONDS)
@Warmup(iterations = 2, time = 1)
@Measurement(iterations = 3, time = 1)
@Fork(1)
public class CompetitorBulkBenchmark {

    // ── shared batch size parameter ──────────────────────────────────────────

    @State(Scope.Benchmark)
    public static class BatchParams {
        @Param({"100", "1000"})
        public int batchSize;
    }

    // ── krandom state ────────────────────────────────────────────────────────

    @State(Scope.Thread)
    public static class KrandomState {
        public final ObjectGenerator<BenchmarkFixtures.ComparableUser> generator =
            new ObjectGenerator<>(BenchmarkFixtures.ComparableUser.class,
                                  GeneratorConfig.builder()
                                                 .locale(Locale.US)
                                                 .seed(7L)
                                                 .objectMaxDepth(2)
                                                 .build());
    }

    // ── DataFaker state ──────────────────────────────────────────────────────

    @State(Scope.Thread)
    public static class DataFakerState {
        public final net.datafaker.Faker faker = new net.datafaker.Faker(
            Locale.US, new java.util.Random(7L));
    }

    // ── EasyRandom state ─────────────────────────────────────────────────────

    @State(Scope.Thread)
    public static class EasyRandomState {
        public final EasyRandom easyRandom = new EasyRandom(
            new EasyRandomParameters().seed(7L));
    }

    // ── Benchmarks ───────────────────────────────────────────────────────────

    @Benchmark
    public List<BenchmarkFixtures.ComparableUser> krandomBulk(KrandomState state, BatchParams params) {
        return state.generator.generateList(params.batchSize);
    }

    @Benchmark
    public List<BenchmarkFixtures.ComparableUser> dataFakerBulk(DataFakerState state, BatchParams params) {
        List<BenchmarkFixtures.ComparableUser> result = new ArrayList<>(params.batchSize);
        for (int i = 0; i < params.batchSize; i++) {
            BenchmarkFixtures.ComparableUser user = new BenchmarkFixtures.ComparableUser();
            user.firstName = state.faker.name().firstName();
            user.lastName = state.faker.name().lastName();
            user.email = state.faker.internet().emailAddress();
            user.age = state.faker.number().numberBetween(18, 90);
            user.city = state.faker.address().city();
            user.country = state.faker.address().country();
            result.add(user);
        }
        return result;
    }

    @Benchmark
    public List<BenchmarkFixtures.ComparableUser> easyRandomBulk(EasyRandomState state, BatchParams params) {
        return state.easyRandom.objects(BenchmarkFixtures.ComparableUser.class, params.batchSize)
                               .collect(Collectors.toList());
    }

    @Benchmark
    public List<BenchmarkFixtures.ComparableUser> instancioBulk(BatchParams params) {
        return Instancio.ofList(BenchmarkFixtures.ComparableUser.class)
                        .size(params.batchSize)
                        .create();
    }
}
