/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.object;

import io.github.frikit.krandom.generator.GenerationRecipe;
import io.github.frikit.krandom.generator.GeneratorConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("ObjectGenerator named child streams")
class ObjectGeneratorNamedChildStreamTest {

    private static final GeneratorConfig CONFIG = GeneratorConfig.builder()
                                                               .seed(91L)
                                                               .clock(Clock.fixed(
                                                                   Instant.parse("2026-07-10T12:00:00Z"), ZoneOffset.UTC))
                                                               .stringLength(8, 8)
                                                               .collectionSize(2, 2)
                                                               .build();

    @Test
    @DisplayName("an unrelated inherited field does not perturb existing seeded members")
    void unrelatedFieldDoesNotPerturbExistingMembers() {
        BaseFixture original = new ObjectGenerator<>(BaseFixture.class, CONFIG).generate();
        ExpandedFixture expanded = new ObjectGenerator<>(ExpandedFixture.class, CONFIG).generate();

        assertBaseMembersEqual(original, expanded);
    }

    @Test
    @DisplayName("a recipe replays scalar, nested, collection, array, and map members")
    void recipeReplaysObjectMembers() {
        GenerationRecipe recipe = CONFIG.getGenerationRecipe().orElseThrow();
        BaseFixture original = new ObjectGenerator<>(BaseFixture.class, CONFIG).generate();
        BaseFixture replay = new ObjectGenerator<>(BaseFixture.class, recipe.toGeneratorConfig()).generate();

        assertBaseMembersEqual(original, replay);
    }

    private static void assertBaseMembersEqual(BaseFixture expected, BaseFixture actual) {
        assertEquals(expected.first, actual.first);
        assertEquals(expected.second, actual.second);
        assertEquals(expected.labels, actual.labels);
        assertEquals(expected.attributes, actual.attributes);
        assertArrayEquals(expected.codes, actual.codes);
        assertEquals(expected.nested.label, actual.nested.label);
        assertEquals(expected.nested.score, actual.nested.score);
    }

    static class BaseFixture {

        String first;
        String second;
        List<String> labels;
        Map<String, Long> attributes;
        String[] codes;
        NestedFixture nested;
    }

    static final class ExpandedFixture extends BaseFixture {

        String unrelated;
    }

    static class NestedFixture {

        String label;
        int score;
    }
}
