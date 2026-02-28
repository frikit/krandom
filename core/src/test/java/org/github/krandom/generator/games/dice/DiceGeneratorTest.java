/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package org.github.krandom.generator.games.dice;

import org.github.krandom.generator.Generator;
import org.github.krandom.generator.games.dice.DiceGenerator;
import org.github.krandom.generator.games.dice.DiceType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("DiceGenerator")
class DiceGeneratorTest {

    // ── DiceType enum ─────────────────────────────────────────────────────────

    @Nested
    @DisplayName("DiceType")
    class DiceTypeTests {

        @Test
        @DisplayName("has exactly 6 constants")
        void sixConstants() {
            assertEquals(6, DiceType.values().length);
        }

        @ParameterizedTest(name = "{0} has correct side count")
        @EnumSource(DiceType.class)
        @DisplayName("sides() matches expected face counts")
        void sideCount(DiceType type) {
            int expected = switch (type) {
                case D4  -> 4;
                case D6  -> 6;
                case D8  -> 8;
                case D10 -> 10;
                case D12 -> 12;
                case D20 -> 20;
            };
            assertEquals(expected, type.sides());
        }
    }

    // ── generate() ───────────────────────────────────────────────────────────

    @Nested
    @DisplayName("generate()")
    class GenerateSingle {

        @ParameterizedTest(name = "{0} generate() in range [1, sides]")
        @EnumSource(DiceType.class)
        @DisplayName("result is within [1, sides]")
        void inRange(DiceType type) {
            DiceGenerator gen = new DiceGenerator(type);
            for (int i = 0; i < 200; i++) {
                int v = gen.generate();
                assertTrue(v >= 1 && v <= type.sides(),
                        () -> type + ": expected [1," + type.sides() + "] but got " + v);
            }
        }

        @ParameterizedTest(name = "{0} generate() covers all faces")
        @EnumSource(DiceType.class)
        @DisplayName("all faces appear over many rolls")
        void coversAllFaces(DiceType type) {
            DiceGenerator gen = new DiceGenerator(type);
            Set<Integer> seen = gen.stream()
                    .limit(type.sides() * 500L)
                    .collect(Collectors.toSet());
            for (int face = 1; face <= type.sides(); face++) {
                assertTrue(seen.contains(face),
                        type + ": face " + face + " never appeared");
            }
        }
    }

    // ── roll(n) ───────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("roll(n)")
    class RollN {

        @ParameterizedTest(name = "{0} roll(1) returns single result in range")
        @EnumSource(DiceType.class)
        @DisplayName("roll(1) returns one value in [1, sides]")
        void rollOne(DiceType type) {
            DiceGenerator gen = new DiceGenerator(type);
            for (int i = 0; i < 100; i++) {
                List<Integer> result = gen.roll(1);
                assertEquals(1, result.size());
                int v = result.getFirst();
                assertTrue(v >= 1 && v <= type.sides());
            }
        }

        @ParameterizedTest(name = "{0} roll(n < sides) returns exactly n results")
        @EnumSource(DiceType.class)
        @DisplayName("roll(n) where n < sides returns exactly n results, all in range")
        void rollLessThanSides(DiceType type) {
            DiceGenerator gen = new DiceGenerator(type);
            int n = Math.max(1, type.sides() - 1);
            List<Integer> results = gen.roll(n);
            assertEquals(n, results.size());
            results.forEach(v -> assertTrue(v >= 1 && v <= type.sides()));
        }

        @ParameterizedTest(name = "{0} roll(n > sides) guarantees all faces appear")
        @EnumSource(DiceType.class)
        @DisplayName("roll(n) where n > sides guarantees all faces present")
        void rollMoreThanSidesCoversAllFaces(DiceType type) {
            DiceGenerator gen = new DiceGenerator(type);
            int n = type.sides() * 3;
            List<Integer> results = gen.roll(n);

            assertEquals(n, results.size());
            results.forEach(v -> assertTrue(v >= 1 && v <= type.sides()));

            Set<Integer> seen = Set.copyOf(results);
            for (int face = 1; face <= type.sides(); face++) {
                assertTrue(seen.contains(face),
                        type + ": face " + face + " missing from roll(" + n + ")");
            }
        }

        @ParameterizedTest(name = "{0} roll(0) throws")
        @EnumSource(DiceType.class)
        @DisplayName("roll(0) throws IllegalArgumentException")
        void rollZeroThrows(DiceType type) {
            assertThrows(IllegalArgumentException.class,
                    () -> new DiceGenerator(type).roll(0));
        }

        @ParameterizedTest(name = "{0} roll(-1) throws")
        @EnumSource(DiceType.class)
        @DisplayName("roll(-1) throws IllegalArgumentException")
        void rollNegativeThrows(DiceType type) {
            assertThrows(IllegalArgumentException.class,
                    () -> new DiceGenerator(type).roll(-1));
        }
    }

    // ── rollSum(n) ────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("rollSum(n)")
    class RollSum {

        @ParameterizedTest(name = "{0} rollSum(1) equals a single roll in [1, sides]")
        @EnumSource(DiceType.class)
        @DisplayName("rollSum(1) is in [1, sides]")
        void rollSumOne(DiceType type) {
            DiceGenerator gen = new DiceGenerator(type);
            for (int i = 0; i < 200; i++) {
                int sum = gen.rollSum(1);
                assertTrue(sum >= 1 && sum <= type.sides(),
                        () -> type + ": rollSum(1) expected [1," + type.sides() + "] but got " + sum);
            }
        }

        @ParameterizedTest(name = "{0} rollSum(n) is in [n, n*sides]")
        @EnumSource(DiceType.class)
        @DisplayName("rollSum(n) is in [n, n*sides] for n=3")
        void rollSumNInRange(DiceType type) {
            DiceGenerator gen = new DiceGenerator(type);
            int n = 3;
            for (int i = 0; i < 100; i++) {
                int sum = gen.rollSum(n);
                assertTrue(sum >= n && sum <= n * type.sides(),
                        () -> type + ": rollSum(" + n + ") expected [" + n + "," + (n * type.sides()) + "] but got " + sum);
            }
        }

        @ParameterizedTest(name = "{0} rollSum(n) min/max bounds are reachable over many rolls")
        @EnumSource(DiceType.class)
        @DisplayName("rollSum(n) achieves values across the full range")
        void rollSumCoversRange(DiceType type) {
            DiceGenerator gen = new DiceGenerator(type);
            int min = 2;
            int max = min * type.sides();
            int observedMin = Integer.MAX_VALUE;
            int observedMax = Integer.MIN_VALUE;
            for (int i = 0; i < 2000; i++) {
                int sum = gen.rollSum(min);
                if (sum < observedMin) observedMin = sum;
                if (sum > observedMax) observedMax = sum;
            }
            assertTrue(observedMin <= min + 2, type + ": min sum never approached lower bound");
            assertTrue(observedMax >= max - 2, type + ": max sum never approached upper bound");
        }

        @ParameterizedTest(name = "{0} rollSum(0) throws")
        @EnumSource(DiceType.class)
        @DisplayName("rollSum(0) throws IllegalArgumentException")
        void rollSumZeroThrows(DiceType type) {
            assertThrows(IllegalArgumentException.class,
                    () -> new DiceGenerator(type).rollSum(0));
        }

        @ParameterizedTest(name = "{0} rollSum(-1) throws")
        @EnumSource(DiceType.class)
        @DisplayName("rollSum(-1) throws IllegalArgumentException")
        void rollSumNegativeThrows(DiceType type) {
            assertThrows(IllegalArgumentException.class,
                    () -> new DiceGenerator(type).rollSum(-1));
        }
    }

    // ── Static factories ──────────────────────────────────────────────────────

    @Nested
    @DisplayName("Static factories")
    class Factories {

        @Test @DisplayName("d4()  creates D4 generator")
        void d4()  { assertEquals(DiceType.D4,  DiceGenerator.d4().type()); }

        @Test @DisplayName("d6()  creates D6 generator")
        void d6()  { assertEquals(DiceType.D6,  DiceGenerator.d6().type()); }

        @Test @DisplayName("d8()  creates D8 generator")
        void d8()  { assertEquals(DiceType.D8,  DiceGenerator.d8().type()); }

        @Test @DisplayName("d10() creates D10 generator")
        void d10() { assertEquals(DiceType.D10, DiceGenerator.d10().type()); }

        @Test @DisplayName("d12() creates D12 generator")
        void d12() { assertEquals(DiceType.D12, DiceGenerator.d12().type()); }

        @Test @DisplayName("d20() creates D20 generator")
        void d20() { assertEquals(DiceType.D20, DiceGenerator.d20().type()); }

        @Test @DisplayName("null DiceType throws NullPointerException")
        void nullTypeThrows() {
            assertThrows(NullPointerException.class, () -> new DiceGenerator(null));
        }
    }

    // ── Generator<Integer> interface ──────────────────────────────────────────

    @Nested
    @DisplayName("Generator<Integer> interface")
    class GeneratorInterface {

        @ParameterizedTest(name = "{0} generateList(10) returns 10 in-range values")
        @EnumSource(DiceType.class)
        @DisplayName("generateList(n) returns n values all in range")
        void generateList(DiceType type) {
            List<Integer> list = new DiceGenerator(type).generateList(10);
            assertEquals(10, list.size());
            list.forEach(v -> assertTrue(v >= 1 && v <= type.sides()));
        }

        @ParameterizedTest(name = "{0} stream() produces in-range values")
        @EnumSource(DiceType.class)
        @DisplayName("stream() produces valid in-range values")
        void stream(DiceType type) {
            new DiceGenerator(type).stream().limit(50)
                    .forEach(v -> assertTrue(v >= 1 && v <= type.sides()));
        }

        @Test
        @DisplayName("map() transforms integer result to string")
        void map() {
            Generator<String> mapped = DiceGenerator.d6().map(Object::toString);
            String v = mapped.generate();
            assertTrue(v.matches("[1-6]"), "Expected 1-6 but got: " + v);
        }

        @Test
        @DisplayName("all DiceType constants produce distinct generators")
        void allTypesDistinct() {
            EnumSet<DiceType> covered = EnumSet.noneOf(DiceType.class);
            for (DiceType t : DiceType.values()) {
                covered.add(new DiceGenerator(t).type());
            }
            assertEquals(EnumSet.allOf(DiceType.class), covered);
        }
    }
}
