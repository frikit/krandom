/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.benchmarks;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.openjdk.jmh.annotations.Param;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;

@DisplayName("Benchmark workloads")
class BenchmarkWorkloadTest {

    @Test
    @DisplayName("Instancio reuses one prebuilt model held in benchmark state")
    void instancioUsesPrebuiltModel() {
        CompetitorObjectBenchmark benchmark = new CompetitorObjectBenchmark();
        CompetitorObjectBenchmark.InstancioState state = new CompetitorObjectBenchmark.InstancioState();

        assertNotNull(state.model);
        Object model = state.model;
        BenchmarkFixtures.ComparableUser user = benchmark.instancioObject(state);

        assertNotNull(user.firstName);
        assertSame(model, state.model, "the model must be built once in @State, not per invocation");
    }

    @Test
    @DisplayName("Field-stream workloads cover the default RELAXED semantic mode")
    void fieldStreamsCoverRelaxedSemantics() throws NoSuchFieldException {
        Param semanticModes = FieldStreamsBenchmark.class.getField("semanticMode").getAnnotation(Param.class);

        assertEquals(List.of("STRUCTURAL_ONLY", "RELAXED"), List.of(semanticModes.value()));
        for (String mode : semanticModes.value()) {
            FieldStreamsBenchmark benchmark = new FieldStreamsBenchmark();
            benchmark.policy = "INDEPENDENT";
            benchmark.customized = false;
            benchmark.semanticMode = mode;
            benchmark.setup();
            assertNotNull(benchmark.generate());
        }
    }
}
