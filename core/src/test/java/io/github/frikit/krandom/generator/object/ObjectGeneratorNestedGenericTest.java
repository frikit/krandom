/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.object;

import io.github.frikit.krandom.generator.GeneratorConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("ObjectGenerator — nested generic types")
class ObjectGeneratorNestedGenericTest {

    private static final GeneratorConfig CONFIG = GeneratorConfig.builder()
                                                                  .seed(2202L)
                                                                  .collectionSize(2, 2)
                                                                  .objectOptionalEmptyProbability(0.0)
                                                                  .build();

    @Test
    @DisplayName("mutable fields retain nested list, map-value, and optional types")
    void mutableFieldsRetainNestedTypes() {
        NestedFields value = new ObjectGenerator<>(NestedFields.class, CONFIG).generate();

        assertNestedValues(value.names, value.counts, value.label);
    }

    @Test
    @DisplayName("record components retain nested list, map-value, and optional types")
    void recordComponentsRetainNestedTypes() {
        NestedRecord value = new ObjectGenerator<>(NestedRecord.class, CONFIG).generate();

        assertNestedValues(value.names(), value.counts(), value.label());
    }

    private static void assertNestedValues(List<List<String>> names,
                                           Map<String, List<Integer>> counts,
                                           Optional<Optional<String>> label) {
        assertEquals(2, names.size());
        for (List<String> inner : names) {
            assertEquals(2, inner.size());
            for (String item : inner) {
                assertInstanceOf(String.class, item);
            }
        }

        assertEquals(2, counts.size());
        for (Map.Entry<String, List<Integer>> entry : counts.entrySet()) {
            assertInstanceOf(String.class, entry.getKey());
            assertEquals(2, entry.getValue().size());
            for (Integer item : entry.getValue()) {
                assertInstanceOf(Integer.class, item);
            }
        }

        assertTrue(label.isPresent());
        assertTrue(label.orElseThrow().isPresent());
        assertInstanceOf(String.class, label.orElseThrow().orElseThrow());
    }

    static final class NestedFields {

        List<List<String>> names;
        Map<String, List<Integer>> counts;
        Optional<Optional<String>> label;
    }

    record NestedRecord(
        List<List<String>> names,
        Map<String, List<Integer>> counts,
        Optional<Optional<String>> label
    ) {
    }
}
