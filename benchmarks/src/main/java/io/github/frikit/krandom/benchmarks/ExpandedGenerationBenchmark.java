/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.benchmarks;

import io.github.frikit.krandom.generator.GeneratorConfig;
import io.github.frikit.krandom.generator.Generators;
import io.github.frikit.krandom.generator.object.ObjectGenerationSemanticMode;
import io.github.frikit.krandom.generator.object.ObjectGenerator;
import io.github.frikit.krandom.generator.selection.UniqueGenerator;
import io.github.frikit.krandom.generator.user.EmailGenerator;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.OperationsPerInvocation;
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

    private static final int UNIQUE_EMAIL_BATCH_SIZE = 1_000;

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

        String generateUniqueEmailBatch() {
            uniqueEmail.reset();
            String email = uniqueEmail.generate();
            for (int i = 1; i < UNIQUE_EMAIL_BATCH_SIZE; i++) {
                email = uniqueEmail.generate();
            }
            return email;
        }
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
    @OperationsPerInvocation(UNIQUE_EMAIL_BATCH_SIZE)
    public String uniquenessHeavyEmailGeneration(GeneratorState state) {
        return state.generateUniqueEmailBatch();
    }
}
