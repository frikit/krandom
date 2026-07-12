/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator;

import io.github.frikit.krandom.generator.base.IntGenerator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Generator default methods")
class GeneratorTest {

    @Test
    @DisplayName("typed Seedable reseed replays a deterministic sequence")
    void typedSeedableReseedReplaysDeterministically() {
        IntGenerator generator = new IntGenerator(0, 1_000);

        ((Seedable) generator).reseed(24680L);
        List<Integer> first = generator.generateList(20);
        ((Seedable) generator).reseed(24680L);

        assertEquals(first, generator.generateList(20));
    }

    @Test
    @DisplayName("map preserves deterministic reseeding from a Seedable source")
    void mapPreservesSeedableReplay() {
        Generator<String> mapped = new IntGenerator(0, 1_000).map(value -> "value-" + value);

        assertTrue(mapped instanceof Seedable);
        ((Seedable) mapped).reseed(123456L);
        List<String> first = mapped.generateList(20);
        ((Seedable) mapped).reseed(123456L);

        assertEquals(first, mapped.generateList(20));
    }

    @Test
    @DisplayName("filter preserves deterministic reseeding from a Seedable source")
    void filterPreservesSeedableReplay() {
        Generator<Integer> filtered = new IntGenerator(0, 1_000).filter(value -> value % 7 == 0);

        assertTrue(filtered instanceof Seedable);
        ((Seedable) filtered).reseed(987654L);
        List<Integer> first = filtered.generateList(20);
        ((Seedable) filtered).reseed(987654L);

        assertEquals(first, filtered.generateList(20));
    }

    @Test
    @DisplayName("composed generators replay from a portable recipe")
    void composedGeneratorsReplayFromRecipe() {
        GeneratorConfig original = GeneratorConfig.builder().seed(24680L).build();
        String serialized = original.getGenerationRecipe().orElseThrow().serialize();
        GeneratorConfig replayed = GenerationRecipe.parse(serialized).toGeneratorConfig();

        Generator<String> first = new IntGenerator(0, 1_000, original.getSeed().getAsLong())
            .map(value -> "v-" + value)
            .filter(value -> !value.endsWith("7"));
        Generator<String> second = new IntGenerator(0, 1_000, replayed.getSeed().getAsLong())
            .map(value -> "v-" + value)
            .filter(value -> !value.endsWith("7"));

        assertEquals(first.generateList(25), second.generateList(25));
    }

    @Test
    @DisplayName("combinators do not claim Seedable for a non-Seedable source")
    void combinatorsDoNotInventSeedability() {
        Generator<Integer> source = () -> 7;

        assertFalse(source.map(value -> value * 2) instanceof Seedable);
        assertFalse(source.filter(value -> true) instanceof Seedable);
    }
}
