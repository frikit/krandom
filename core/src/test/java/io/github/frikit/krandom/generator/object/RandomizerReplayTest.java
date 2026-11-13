/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.object;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.frikit.krandom.generator.GenerationRecipe;
import io.github.frikit.krandom.generator.Generator;
import io.github.frikit.krandom.generator.GeneratorConfig;
import io.github.frikit.krandom.generator.base.ByteGenerator;
import io.github.frikit.krandom.generator.identifier.UUIDGenerator;
import io.github.frikit.krandom.generator.user.EmailGenerator;
import io.github.frikit.krandom.generator.user.FirstNameGenerator;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

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

    public static class BuiltIns {
        @Randomizer(ByteGenerator.class)
        public byte seedable;

        @Randomizer(UUIDGenerator.class)
        public UUID id;

        @Randomizer(FirstNameGenerator.class)
        public String firstName;

        @Randomizer(EmailGenerator.class)
        public String contact;

        @Randomizer(Counter.class)
        public long sequence;

        public int plain;
        public String text;
    }

    private static GeneratorConfig reseedingBuiltIns(long seed) {
        return GeneratorConfig.builder()
                .seed(seed)
                .clock(FIXED)
                .objectReseedBuiltInRandomizers(true)
                .build();
    }

    private static List<String> builtIns(GeneratorConfig config, int count, boolean randomizers) {
        ObjectGenerator<BuiltIns> generator = new ObjectGenerator<>(BuiltIns.class, config);
        List<String> values = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            BuiltIns value = generator.generate();
            values.add(value.seedable + " " + value.sequence + " " + value.plain + " " + value.text
                    + (randomizers ? " " + value.id + " " + value.firstName + " " + value.contact : ""));
        }
        return values;
    }

    @Test
    @DisplayName("with the opt-in, a built-in randomizer that is not Seedable replays from the seed and the recipe")
    void builtInRandomizersReplayWithTheOptIn() {
        GeneratorConfig config = reseedingBuiltIns(42L);
        List<String> original = builtIns(config, 6, true);

        assertEquals(original, builtIns(reseedingBuiltIns(42L), 6, true));
        GeneratorConfig replay = GenerationRecipe.parse(
                        config.getGenerationRecipe().orElseThrow().serialize())
                .toGeneratorConfig();
        assertEquals(original, builtIns(replay, 6, true));
        assertNotEquals(original, builtIns(reseedingBuiltIns(43L), 6, true));
        assertTrue(new HashSet<>(original).size() > 1, "values should vary between objects: " + original);
    }

    @Test
    @DisplayName("the opt-in reseeds a built-in randomizer as a new build with the object's randomizer seed is")
    void builtInRandomizerTakesTheObjectsRandomizerSeed() {
        GeneratorConfig config = reseedingBuiltIns(42L);
        ObjectGenerator<BuiltIns> generator = new ObjectGenerator<>(BuiltIns.class, config);
        List<UUID> ids = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            ids.add(generator.generate().id);
        }
        // Each object takes its own randomizer seed, so the UUID generator gives the first value of a new
        // build seeded with it: not the same value for every object.
        assertEquals(3, new HashSet<>(ids).size(), ids::toString);
    }

    @Test
    @DisplayName("the opt-in leaves the other fields, Seedable and stateful randomizers included, as they were")
    void builtInRandomizerOptInKeepsOtherFields() {
        GeneratorConfig off = seeded(42L);
        assertEquals(builtIns(off, 6, false), builtIns(reseedingBuiltIns(42L), 6, false));
        // Off, the built-in randomizers draw from their own unseeded sources.
        assertNotEquals(builtIns(off, 3, true), builtIns(seeded(42L), 3, true));
    }

    @Test
    @DisplayName("without a seed the opt-in reseeds nothing")
    void builtInRandomizerOptInNeedsASeed() {
        GeneratorConfig unseeded =
                GeneratorConfig.builder().objectReseedBuiltInRandomizers(true).build();
        assertNotEquals(builtIns(unseeded, 3, true), builtIns(unseeded, 3, true));
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
        GeneratorConfig replay = GenerationRecipe.parse(
                        config.getGenerationRecipe().orElseThrow().serialize())
                .toGeneratorConfig();
        assertEquals(original, values(replay, 8));
        assertTrue(new HashSet<>(original).size() > 1, "values should vary between objects: " + original);
    }

    @Test
    @DisplayName("nested seedable randomizers replay too")
    void nestedSeedableRandomizersReplay() {
        Holder first = new ObjectGenerator<>(Holder.class, seeded(7L)).generate();
        Holder second = new ObjectGenerator<>(Holder.class, seeded(7L)).generate();
        assertEquals(
                first.items.stream().map(item -> item.value).toList(),
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
