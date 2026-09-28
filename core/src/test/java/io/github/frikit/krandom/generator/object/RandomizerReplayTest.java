/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.object;

import io.github.frikit.krandom.generator.GenerationRecipe;
import io.github.frikit.krandom.generator.Generator;
import io.github.frikit.krandom.generator.GeneratorConfig;
import io.github.frikit.krandom.generator.base.ByteGenerator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link Randomizer} generators follow the seeded replay contract when they are {@link
 * io.github.frikit.krandom.generator.Seedable}, and keep their own state across generated objects.
 */
class RandomizerReplayTest {

    private static final Clock FIXED = Clock.fixed(Instant.parse("2026-09-26T10:00:00Z"), ZoneOffset.UTC);

    public static class SeedableField {
        @Randomizer(ByteGenerator.class)
        public byte value;
        public int plain;
    }

    public static class Holder {
        public List<SeedableField> items;
    }

    public static class Counter implements Generator<Long> {
        private long next = 1;

        @Override
        public Long generate() {
            return next++;
        }
    }

    public static class Sequenced {
        @Randomizer(Counter.class)
        public long sequence;
    }

    private static GeneratorConfig seeded(long seed) {
        return GeneratorConfig.builder().seed(seed).clock(FIXED).build();
    }

    private static List<Byte> values(GeneratorConfig config, int count) {
        ObjectGenerator<SeedableField> generator = new ObjectGenerator<>(SeedableField.class, config);
        List<Byte> values = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            values.add(generator.generate().value);
        }
        return values;
    }

    @Test
    @DisplayName("a seedable randomizer replays from the same seed and from the recipe")
    void seedableRandomizerReplays() {
        GeneratorConfig config = seeded(42L);
        List<Byte> original = values(config, 8);
        assertEquals(original, values(seeded(42L), 8));
        GeneratorConfig replay = GenerationRecipe.parse(config.getGenerationRecipe().orElseThrow().serialize())
                                                 .toGeneratorConfig();
        assertEquals(original, values(replay, 8));
        assertTrue(new HashSet<>(original).size() > 1, "values should vary between objects: " + original);
    }

    @Test
    @DisplayName("nested seedable randomizers replay too")
    void nestedSeedableRandomizersReplay() {
        Holder first = new ObjectGenerator<>(Holder.class, seeded(7L)).generate();
        Holder second = new ObjectGenerator<>(Holder.class, seeded(7L)).generate();
        assertEquals(first.items.stream().map(item -> item.value).toList(),
                     second.items.stream().map(item -> item.value).toList());
    }

    @Test
    @DisplayName("a stateful randomizer keeps its state across objects from one generator")
    void statefulRandomizerKeepsState() {
        ObjectGenerator<Sequenced> generator = new ObjectGenerator<>(Sequenced.class, seeded(1L));
        List<Long> sequence = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            sequence.add(generator.generate().sequence);
        }
        assertEquals(List.of(1L, 2L, 3L, 4L, 5L), sequence);
    }

    @Test
    @DisplayName("an unseeded configuration leaves seedable randomizers on their own randomness")
    void unseededRandomizerStillGenerates() {
        ObjectGenerator<SeedableField> generator = new ObjectGenerator<>(SeedableField.class);
        for (int i = 0; i < 3; i++) {
            assertNotNull(generator.generate());
        }
    }
}
