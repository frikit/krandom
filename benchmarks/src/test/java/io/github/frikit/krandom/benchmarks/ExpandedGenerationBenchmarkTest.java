/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.benchmarks;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@DisplayName("Expanded generation benchmark")
class ExpandedGenerationBenchmarkTest {

    @Test
    @DisplayName("Unique email batches can be repeated without exhausting benchmark state")
    void uniqueEmailBatchesCanBeRepeated() {
        ExpandedGenerationBenchmark.GeneratorState state = new ExpandedGenerationBenchmark.GeneratorState();

        String lastEmail = assertDoesNotThrow(() -> {
            String generated = null;
            for (int i = 0; i < 20; i++) {
                generated = state.generateUniqueEmailBatch();
            }
            return generated;
        });

        assertNotNull(lastEmail);
    }
}
