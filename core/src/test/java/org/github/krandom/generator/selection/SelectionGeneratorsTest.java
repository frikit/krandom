/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package org.github.krandom.generator.selection;

import org.github.krandom.generator.Generator;
import org.github.krandom.generator.Generators;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;
import java.util.stream.IntStream;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Selection generators")
class SelectionGeneratorsTest {

    @Test
    @DisplayName("PickGenerator picks only from source values")
    void pickGeneratorPicksFromSource() {
        List<String> source = List.of("a", "b", "c");
        PickGenerator<String> gen = new PickGenerator<>(source);

        for (int i = 0; i < 200; i++) {
            assertTrue(source.contains(gen.generate()));
        }
    }

    @Test
    @DisplayName("PickGenerator seeded constructor is deterministic")
    void pickGeneratorSeededIsDeterministic() {
        List<String> source = List.of("x", "y", "z");
        PickGenerator<String> a = new PickGenerator<>(source, 123L);
        PickGenerator<String> b = new PickGenerator<>(source, 123L);

        for (int i = 0; i < 20; i++) {
            assertEquals(a.generate(), b.generate());
        }
    }

    @Test
    @DisplayName("PickSetGenerator returns distinct elements")
    void pickSetReturnsDistinct() {
        List<Integer> source = IntStream.rangeClosed(1, 10).boxed().toList();
        PickSetGenerator<Integer> gen = new PickSetGenerator<>(source, 5, 123L);

        List<Integer> picked = gen.generate();
        assertEquals(5, picked.size());
        assertEquals(5, Set.copyOf(picked).size());
        assertTrue(source.containsAll(picked));
    }

    @Test
    @DisplayName("PickSetGenerator returns empty list when count is zero")
    void pickSetZeroCountReturnsEmpty() {
        PickSetGenerator<Integer> gen = new PickSetGenerator<>(List.of(1, 2, 3), 0, 7L);
        assertEquals(List.of(), gen.generate());
    }

    @Test
    @DisplayName("ShuffleGenerator returns shuffled copy with same members")
    void shuffleReturnsPermutation() {
        List<Integer> source = List.of(1, 2, 3, 4, 5, 6);
        ShuffleGenerator<Integer> gen = new ShuffleGenerator<>(source, 42L);

        List<Integer> shuffled = gen.generate();
        assertEquals(source.size(), shuffled.size());
        assertEquals(source.stream().sorted().toList(), shuffled.stream().sorted().toList());
    }

    @Test
    @DisplayName("WeightedGenerator honors weight bias")
    void weightedHonorsBias() {
        WeightedGenerator<String> gen = new WeightedGenerator<>(
                List.of("heads", "tails"),
                List.of(7, 3),
                42L
        );

        int heads = 0;
        int trials = 10_000;
        for (int i = 0; i < trials; i++) {
            if ("heads".equals(gen.generate())) {
                heads++;
            }
        }

        double ratio = heads / (double) trials;
        assertTrue(ratio > 0.65 && ratio < 0.75, "heads ratio should be around 0.7, got: " + ratio);
    }

    @Test
    @DisplayName("WeightedGenerator rejects empty values")
    void weightedRejectsEmptyValues() {
        assertThrows(IllegalArgumentException.class, () -> new WeightedGenerator<>(List.of(), List.of()));
    }

    @Test
    @DisplayName("UniqueGenerator throws when source cannot produce enough distinct values")
    void uniqueThrowsWhenExhausted() {
        Generator<Integer> bounded = new Generator<>() {
            private int n = 0;

            @Override
            public Integer generate() {
                return n++ % 2;
            }
        };

        UniqueGenerator<Integer> unique = new UniqueGenerator<>(bounded, 5);
        assertDoesNotThrow(unique::generate);
        assertDoesNotThrow(unique::generate);
        IllegalStateException ex = assertThrows(IllegalStateException.class, unique::generate);
        assertTrue(ex.getMessage().contains("Unable to generate a unique value"));
    }

    @Test
    @DisplayName("UniqueGenerator supports custom comparator")
    void uniqueSupportsCustomComparator() {
        Generator<String> source = new Generator<>() {
            private int i = 0;

            @Override
            public String generate() {
                return (i++ % 2 == 0) ? "Alice" : "ALICE";
            }
        };

        UniqueGenerator<String> unique = new UniqueGenerator<>(
                source,
                (a, b) -> a.equalsIgnoreCase(b),
                5
        );

        assertEquals("Alice", unique.generate());
        assertThrows(IllegalStateException.class, unique::generate);
    }

    @Test
    @DisplayName("UniqueGenerator constructor with comparator overload works")
    void uniqueComparatorOverloadWorks() {
        UniqueGenerator<String> unique = new UniqueGenerator<>(() -> "a", String::equalsIgnoreCase);
        assertEquals("a", unique.generate());
    }

    @Test
    @DisplayName("Generators.uniqueValues alias wraps source generator")
    void generatorsUniqueValuesAliasWorks() {
        Generator<Integer> bounded = new Generator<>() {
            private int n = 0;

            @Override
            public Integer generate() {
                return n++ % 2;
            }
        };
        UniqueGenerator<Integer> unique = Generators.uniqueValues(bounded);
        assertDoesNotThrow(unique::generate);
        assertDoesNotThrow(unique::generate);
    }

    @Test
    @DisplayName("RepeatGenerator returns fixed-size list")
    void repeatReturnsFixedSizeList() {
        RepeatGenerator<Integer> repeat = new RepeatGenerator<>(() -> 7, 4);
        List<Integer> values = repeat.generate();
        assertEquals(List.of(7, 7, 7, 7), values);
    }

    @Test
    @DisplayName("Constructors validate inputs")
    void constructorsValidateInputs() {
        assertThrows(IllegalArgumentException.class, () -> new PickGenerator<>(List.of()));
        assertThrows(IllegalArgumentException.class, () -> new PickSetGenerator<>(List.of(1, 2), 3));
        assertThrows(IllegalArgumentException.class, () -> new PickSetGenerator<>(List.of(1, 2), -1));
        assertThrows(IllegalArgumentException.class, () -> new WeightedGenerator<>(List.of("a"), List.of(0)));
        assertThrows(IllegalArgumentException.class, () -> new WeightedGenerator<>(List.of("a"), List.of(1, 2)));
        assertThrows(IllegalArgumentException.class,
                () -> new WeightedGenerator<>(List.of("a"), Arrays.asList((Integer) null)));
        assertThrows(IllegalArgumentException.class,
                () -> new WeightedGenerator<>(List.of("a", "b"), List.of(Integer.MAX_VALUE, 1)));
        assertThrows(IllegalArgumentException.class, () -> new UniqueGenerator<>(() -> 1, 0));
        assertThrows(IllegalArgumentException.class, () -> new RepeatGenerator<>(() -> 1, -1));
    }
}
