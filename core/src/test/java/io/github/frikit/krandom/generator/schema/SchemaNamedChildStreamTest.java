/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.schema;

import io.github.frikit.krandom.generator.GenerationRecipe;
import io.github.frikit.krandom.generator.GeneratorConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("Schema named child streams")
class SchemaNamedChildStreamTest {

    private static final GeneratorConfig CONFIG = GeneratorConfig.builder()
                                                               .seed(73L)
                                                               .clock(Clock.fixed(
                                                                   Instant.parse("2026-07-10T12:00:00Z"), ZoneOffset.UTC))
                                                               .build();

    @Test
    @DisplayName("unrelated columns do not perturb seeded values at existing columns")
    void unrelatedColumnsDoNotPerturbExistingColumns() {
        Schema original = new Schema(CONFIG, orderedFields("first", "second"));
        Schema expanded = new Schema(CONFIG, orderedFields("unrelated", "first", "second"));

        List<Map<String, Object>> originalRecords = original.generateBatch(3);
        List<Map<String, Object>> expandedRecords = expanded.generateBatch(3);

        for (int recordIndex = 0; recordIndex < originalRecords.size(); recordIndex++) {
            assertEquals(originalRecords.get(recordIndex).get("first"), expandedRecords.get(recordIndex).get("first"));
            assertEquals(originalRecords.get(recordIndex).get("second"), expandedRecords.get(recordIndex).get("second"));
        }
    }

    @Test
    @DisplayName("a recipe replays every schema column across record indexes")
    void recipeReplaysSchemaColumns() {
        GenerationRecipe recipe = CONFIG.getGenerationRecipe().orElseThrow();
        Schema original = new Schema(CONFIG, orderedFields("first", "second"));
        Schema replay = new Schema(recipe.toGeneratorConfig(), orderedFields("first", "second"));

        assertEquals(original.generateBatch(4), replay.generateBatch(4));
    }

    private static Map<String, SchemaValueProvider> orderedFields(String... names) {
        Map<String, SchemaValueProvider> fields = new LinkedHashMap<>();
        for (String name : names) {
            fields.put(name, context -> context.random().nextLong());
        }
        return fields;
    }
}
