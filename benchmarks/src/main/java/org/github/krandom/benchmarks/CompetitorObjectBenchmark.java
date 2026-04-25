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
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;

import java.util.Locale;
import java.util.concurrent.TimeUnit;

/**
 * Head-to-head POJO population: krandom vs DataFaker vs EasyRandom vs Instancio.
 *
 * <p>All libraries populate the same {@link BenchmarkFixtures.ComparableUser} POJO so the
 * comparison measures library overhead, not model complexity. JavaFaker is excluded because
 * it has no object-population API. DataFaker's {@code populate()} requires {@code @Fake}
 * annotations, so it uses manual field assignment instead — the idiomatic way DataFaker
 * users build test fixtures.
 */
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.SECONDS)
@Warmup(iterations = 2, time = 1)
@Measurement(iterations = 3, time = 1)
@Fork(1)
public class CompetitorObjectBenchmark {

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

    // ── Instancio state ──────────────────────────────────────────────────────
    // Instancio uses a stateless API — no pre-created state needed.

    // ── Benchmarks ───────────────────────────────────────────────────────────

    @Benchmark
    public BenchmarkFixtures.ComparableUser krandomObject(KrandomState state) {
        return state.generator.generate();
    }

    @Benchmark
    public BenchmarkFixtures.ComparableUser dataFakerObject(DataFakerState state) {
        BenchmarkFixtures.ComparableUser user = new BenchmarkFixtures.ComparableUser();
        user.firstName = state.faker.name().firstName();
        user.lastName = state.faker.name().lastName();
        user.email = state.faker.internet().emailAddress();
        user.age = state.faker.number().numberBetween(18, 90);
        user.city = state.faker.address().city();
        user.country = state.faker.address().country();
        return user;
    }

    @Benchmark
    public BenchmarkFixtures.ComparableUser easyRandomObject(EasyRandomState state) {
        return state.easyRandom.nextObject(BenchmarkFixtures.ComparableUser.class);
    }

    @Benchmark
    public BenchmarkFixtures.ComparableUser instancioObject() {
        return Instancio.create(BenchmarkFixtures.ComparableUser.class);
    }
}
