/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.benchmarks;

import io.github.frikit.krandom.generator.GeneratorConfig;
import io.github.frikit.krandom.generator.object.ObjectFieldStreamPolicy;
import io.github.frikit.krandom.generator.object.ObjectGenerationSemanticMode;
import io.github.frikit.krandom.generator.object.ObjectGenerator;
import org.openjdk.jmh.annotations.*;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.concurrent.TimeUnit;

/** Compare legacy output and opt-in field streams on the same structural workload. */
@State(Scope.Thread)
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.SECONDS)
@Warmup(iterations = 3, time = 1)
@Measurement(iterations = 5, time = 1)
@Fork(2)
public class FieldStreamsBenchmark {
    @Param({"LEGACY", "INDEPENDENT"})
    public String policy;
    @Param({"false", "true"})
    public boolean customized;
    private ObjectGenerator<Fixture> generator;

    @Setup
    public void setup() {
        GeneratorConfig.Builder config = GeneratorConfig.builder().seed(42)
            .clock(Clock.fixed(Instant.EPOCH, ZoneOffset.UTC))
            .objectSemanticMode(ObjectGenerationSemanticMode.STRUCTURAL_ONLY);
        // The legacy case also runs against 2.2.0 bytecode, which has no explicit policy API.
        if (!policy.equals("LEGACY")) config.objectFieldStreamPolicy(ObjectFieldStreamPolicy.valueOf(policy));
        if (customized) config.objectOverride(Fixture.class, "name", () -> "fixed");
        generator = new ObjectGenerator<>(Fixture.class, config.build());
    }

    @Benchmark
    public Fixture generate() { return generator.generate(); }

    public record Fixture(String name, int age, List<Integer> values) {}
}
