/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.benchmarks;

import io.github.frikit.krandom.generator.GeneratorConfig;
import io.github.frikit.krandom.generator.Generators;
import io.github.frikit.krandom.generator.user.EmailGenerator;
import io.github.frikit.krandom.generator.user.FirstNameGenerator;
import io.github.frikit.krandom.generator.location.StreetAddressGenerator;
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
 * Head-to-head scalar value generation: krandom vs DataFaker vs JavaFaker.
 *
 * <p>EasyRandom and Instancio are excluded — they have no built-in faker-style
 * scalar providers (names, emails, addresses).
 */
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.SECONDS)
@Warmup(iterations = 2, time = 1)
@Measurement(iterations = 3, time = 1)
@Fork(1)
public class CompetitorScalarBenchmark {

    // ── krandom state ────────────────────────────────────────────────────────

    @State(Scope.Thread)
    public static class KrandomState {
        private final GeneratorConfig config = GeneratorConfig.builder()
                                                              .locale(Locale.US)
                                                              .seed(7L)
                                                              .build();

        public final FirstNameGenerator firstName = new FirstNameGenerator(config);
        public final EmailGenerator email = new EmailGenerator(config);
        public final StreetAddressGenerator streetAddress = Generators.ofStreetAddress(config);
    }

    // ── DataFaker state ──────────────────────────────────────────────────────

    @State(Scope.Thread)
    public static class DataFakerState {
        public final net.datafaker.Faker faker = new net.datafaker.Faker(
            Locale.US, new java.util.Random(7L));
    }

    // ── JavaFaker state ──────────────────────────────────────────────────────

    @State(Scope.Thread)
    public static class JavaFakerState {
        public final com.github.javafaker.Faker faker = new com.github.javafaker.Faker(
            Locale.US, new java.util.Random(7L));
    }

    // ── First name ───────────────────────────────────────────────────────────

    @Benchmark
    public String krandomFirstName(KrandomState state) {
        return state.firstName.generate();
    }

    @Benchmark
    public String dataFakerFirstName(DataFakerState state) {
        return state.faker.name().firstName();
    }

    @Benchmark
    public String javaFakerFirstName(JavaFakerState state) {
        return state.faker.name().firstName();
    }

    // ── Email ────────────────────────────────────────────────────────────────

    @Benchmark
    public String krandomEmail(KrandomState state) {
        return state.email.generate();
    }

    @Benchmark
    public String dataFakerEmail(DataFakerState state) {
        return state.faker.internet().emailAddress();
    }

    @Benchmark
    public String javaFakerEmail(JavaFakerState state) {
        return state.faker.internet().emailAddress();
    }

    // ── Street address ───────────────────────────────────────────────────────

    @Benchmark
    public String krandomStreetAddress(KrandomState state) {
        return state.streetAddress.generate();
    }

    @Benchmark
    public String dataFakerStreetAddress(DataFakerState state) {
        return state.faker.address().streetAddress();
    }

    @Benchmark
    public String javaFakerStreetAddress(JavaFakerState state) {
        return state.faker.address().streetAddress();
    }
}
