/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator;

import io.github.frikit.krandom.generator.algorithms.FibonacciGenerator;
import io.github.frikit.krandom.generator.algorithms.LuhnGenerator;
import io.github.frikit.krandom.generator.games.coin.CoinGenerator;
import io.github.frikit.krandom.generator.games.coin.CoinResult;
import io.github.frikit.krandom.generator.games.dice.DiceGenerator;
import io.github.frikit.krandom.generator.games.dice.DiceType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Generators that previously owned a hard-wired {@code new Random()} must honour the
 * {@link GeneratorConfig} random source, so a recipe seed reproduces their output.
 */
@DisplayName("Seeding consistency for formerly unseedable generators")
class SeedingConsistencyTest {

    private static GeneratorConfig seeded(long seed) {
        return GeneratorConfig.builder().seed(seed).build();
    }

    @Nested
    @DisplayName("CoinGenerator")
    class Coin {

        @Test
        @DisplayName("same config seed reproduces the same flips")
        void reproducible() {
            List<CoinResult> first = new CoinGenerator(seeded(11L)).flip(64);
            List<CoinResult> second = new CoinGenerator(seeded(11L)).flip(64);
            assertEquals(first, second);
            assertTrue(first.contains(CoinResult.HEAD) && first.contains(CoinResult.TAIL));
        }

        @Test
        @DisplayName("uses the caller-owned random source of the configuration")
        void usesCallerOwnedRandom() {
            Random expected = new Random(5L);
            List<CoinResult> flips = new CoinGenerator(GeneratorConfig.builder().random(new Random(5L)).build())
                .flip(32);
            for (CoinResult flip : flips) {
                assertEquals(expected.nextBoolean() ? CoinResult.HEAD : CoinResult.TAIL, flip);
            }
        }

        @Test
        @DisplayName("rejects a null configuration")
        void rejectsNull() {
            assertThrows(NullPointerException.class, () -> new CoinGenerator(null));
        }

        @Test
        @DisplayName("facade overload honours the configuration")
        void facade() {
            assertEquals(new CoinGenerator(seeded(3L)).flip(20), Generators.ofCoin(seeded(3L)).flip(20));
        }
    }

    @Nested
    @DisplayName("DiceGenerator")
    class Dice {

        @Test
        @DisplayName("same config seed reproduces rolls, multi-rolls and sums")
        void reproducible() {
            DiceGenerator first = new DiceGenerator(DiceType.D20, seeded(21L));
            DiceGenerator second = new DiceGenerator(DiceType.D20, seeded(21L));
            assertEquals(first.generateList(30), second.generateList(30));
            assertEquals(first.roll(40), second.roll(40));
            assertEquals(first.rollSum(5), second.rollSum(5));
            assertEquals(DiceType.D20, first.type());
        }

        @Test
        @DisplayName("different seeds diverge")
        void differentSeedsDiverge() {
            assertNotEquals(new DiceGenerator(DiceType.D20, seeded(1L)).generateList(30),
                            new DiceGenerator(DiceType.D20, seeded(2L)).generateList(30));
        }

        @Test
        @DisplayName("rejects null arguments")
        void rejectsNulls() {
            assertThrows(NullPointerException.class, () -> new DiceGenerator(null, seeded(1L)));
            assertThrows(NullPointerException.class, () -> new DiceGenerator(DiceType.D6, null));
        }

        @Test
        @DisplayName("facade overload honours the configuration")
        void facade() {
            assertEquals(new DiceGenerator(DiceType.D8, seeded(4L)).generateList(25),
                         Generators.ofDice(DiceType.D8, seeded(4L)).generateList(25));
        }
    }

    @Nested
    @DisplayName("LuhnGenerator")
    class Luhn {

        @Test
        @DisplayName("same config seed reproduces Luhn-valid numbers")
        void reproducible() {
            List<String> first = new LuhnGenerator(seeded(8L)).generateList(25);
            assertEquals(first, new LuhnGenerator(seeded(8L)).generateList(25));
            for (String value : first) {
                assertEquals(10, value.length());
                assertTrue(luhnValid(value), value);
            }
        }

        @Test
        @DisplayName("rejects a null configuration")
        void rejectsNull() {
            assertThrows(NullPointerException.class, () -> new LuhnGenerator(null));
        }

        @Test
        @DisplayName("facade overload honours the configuration")
        void facade() {
            assertEquals(new LuhnGenerator(seeded(6L)).generateList(10), Generators.ofLuhn(seeded(6L)).generateList(10));
        }

        private boolean luhnValid(String number) {
            int sum = 0;
            boolean doubleIt = false;
            for (int i = number.length() - 1; i >= 0; i--) {
                int digit = number.charAt(i) - '0';
                if (doubleIt) {
                    digit *= 2;
                    if (digit > 9) {
                        digit -= 9;
                    }
                }
                sum += digit;
                doubleIt = !doubleIt;
            }
            return sum % 10 == 0;
        }
    }

    @Nested
    @DisplayName("FibonacciGenerator")
    class Fibonacci {

        @Test
        @DisplayName("same config seed reproduces the same Fibonacci draws")
        void reproducible() {
            List<Long> first = new FibonacciGenerator(seeded(13L)).generateList(40);
            assertEquals(first, new FibonacciGenerator(seeded(13L)).generateList(40));
            List<Long> sequence = new FibonacciGenerator(seeded(13L)).generateSequence();
            assertTrue(sequence.containsAll(first));
        }

        @Test
        @DisplayName("rejects a null configuration")
        void rejectsNull() {
            assertThrows(NullPointerException.class, () -> new FibonacciGenerator(null));
        }

        @Test
        @DisplayName("facade overload honours the configuration")
        void facade() {
            List<Long> expected = new ArrayList<>(new FibonacciGenerator(seeded(9L)).generateList(12));
            assertEquals(expected, Generators.ofFibonacci(seeded(9L)).generateList(12));
        }
    }
}
