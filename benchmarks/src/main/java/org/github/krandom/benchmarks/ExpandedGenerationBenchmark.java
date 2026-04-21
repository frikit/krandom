/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package org.github.krandom.benchmarks;

import org.github.krandom.generator.GeneratorConfig;
import org.github.krandom.generator.Generators;
import org.github.krandom.generator.object.ObjectGenerationSemanticMode;
import org.github.krandom.generator.object.ObjectGenerator;
import org.github.krandom.generator.selection.UniqueGenerator;
import org.github.krandom.generator.user.EmailGenerator;
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

@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.SECONDS)
@Warmup(iterations = 2, time = 1)
@Measurement(iterations = 3, time = 1)
@Fork(1)
public class ExpandedGenerationBenchmark {

    @State(Scope.Thread)
    public static class GeneratorState {
        private final GeneratorConfig structuralConfig = GeneratorConfig.builder()
                                                                       .locale(Locale.US)
                                                                       .seed(7L)
                                                                       .objectMaxDepth(4)
                                                                       .objectSemanticMode(ObjectGenerationSemanticMode.STRUCTURAL_ONLY)
                                                                       .objectNullProbability(0.0)
                                                                       .objectOptionalEmptyProbability(0.0)
                                                                       .build();

        private final GeneratorConfig relaxedConfig = GeneratorConfig.builder()
                                                                    .locale(Locale.US)
                                                                    .seed(7L)
                                                                    .objectMaxDepth(4)
                                                                    .objectSemanticMode(ObjectGenerationSemanticMode.RELAXED)
                                                                    .objectNullProbability(0.0)
                                                                    .objectOptionalEmptyProbability(0.0)
                                                                    .build();

        private final GeneratorConfig semanticConfig = GeneratorConfig.builder()
                                                                     .locale(Locale.US)
                                                                     .seed(7L)
                                                                     .objectMaxDepth(4)
                                                                     .objectSemanticMode(ObjectGenerationSemanticMode.STRICT)
                                                                     .objectNullProbability(0.0)
                                                                     .objectOptionalEmptyProbability(0.0)
                                                                     .build();

        private final GeneratorConfig uniqueObjectConfig = GeneratorConfig.builder()
                                                                         .locale(Locale.US)
                                                                         .seed(7L)
                                                                         .objectMaxDepth(4)
                                                                         .objectSemanticMode(ObjectGenerationSemanticMode.STRICT)
                                                                         .objectNullProbability(0.0)
                                                                         .objectOptionalEmptyProbability(0.0)
                                                                         .objectUniqueFields("id", "email", "username")
                                                                         .objectUniquenessMaxAttempts(2_000)
                                                                         .build();

        public final ObjectGenerator<BenchmarkFixtures.SemanticCustomer> structuralCustomer =
            Generators.ofObject(BenchmarkFixtures.SemanticCustomer.class, structuralConfig);

        public final ObjectGenerator<BenchmarkFixtures.SemanticCustomer> relaxedCustomer =
            Generators.ofObject(BenchmarkFixtures.SemanticCustomer.class, relaxedConfig);

        public final ObjectGenerator<BenchmarkFixtures.SemanticCustomer> semanticCustomer =
            Generators.ofObject(BenchmarkFixtures.SemanticCustomer.class, semanticConfig);

        public final ObjectGenerator<BenchmarkFixtures.SemanticCustomer> semanticCustomerWithUniqueness =
            Generators.ofObject(BenchmarkFixtures.SemanticCustomer.class, uniqueObjectConfig);

        public final UniqueGenerator<String> uniqueEmail =
            Generators.unique(new EmailGenerator(GeneratorConfig.builder()
                                                                .locale(Locale.US)
                                                                .seed(7L)
                                                                .build()),
                              2_000);
    }

    @Benchmark
    public BenchmarkFixtures.SemanticCustomer structuralOnlyObjectGraph(GeneratorState state) {
        return state.structuralCustomer.generate();
    }

    @Benchmark
    public BenchmarkFixtures.SemanticCustomer relaxedSemanticObjectGraph(GeneratorState state) {
        return state.relaxedCustomer.generate();
    }

    @Benchmark
    public BenchmarkFixtures.SemanticCustomer semanticObjectGraph(GeneratorState state) {
        return state.semanticCustomer.generate();
    }

    @Benchmark
    public BenchmarkFixtures.SemanticCustomer semanticObjectGraphWithUniqueFields(GeneratorState state) {
        return state.semanticCustomerWithUniqueness.generate();
    }

    @Benchmark
    public String uniquenessHeavyEmailGeneration(GeneratorState state) {
        return state.uniqueEmail.generate();
    }
}
