/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.games.dice;

import io.github.frikit.krandom.generator.Generator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Random;

/**
 * Generates random integer results for any standard polyhedral die.
 *
 * <p>Results are always in the range {@code [1, sides]} (inclusive).
 *
 * <p>When rolling more times than the die has sides, the fairness guarantee from the
 * original algorithm applies: every face is guaranteed to appear at least once in the
 * result list (the full face set is mixed in and the whole list is shuffled).
 *
 * <pre>{@code
 *   // Specific die via constructor
 *   DiceGenerator d6 = new DiceGenerator(DiceType.D6);
 *   int           pip = d6.generate();           // 1–6
 *   List<Integer> three = d6.roll(3);            // 3 results, each 1–6
 *
 *   // Convenience static factories
 *   int roll = DiceGenerator.d20().generate();   // 1–20
 *
 *   // Via the Generators facade
 *   int result = Generators.ofDice(DiceType.D8).generate();
 * }</pre>
 */
public final class DiceGenerator implements Generator<Integer> {

    private final DiceType type;
    private final Random   random = new Random();

    public DiceGenerator(DiceType type) {
        this.type = Objects.requireNonNull(type, "type must not be null");
    }

    public static DiceGenerator d4() {
        return new DiceGenerator(DiceType.D4);
    }

    public static DiceGenerator d6() {
        return new DiceGenerator(DiceType.D6);
    }

    public static DiceGenerator d8() {
        return new DiceGenerator(DiceType.D8);
    }

    public static DiceGenerator d10() {
        return new DiceGenerator(DiceType.D10);
    }

    public static DiceGenerator d12() {
        return new DiceGenerator(DiceType.D12);
    }

    // ── Static factories ──────────────────────────────────────────────────────

    public static DiceGenerator d20() {
        return new DiceGenerator(DiceType.D20);
    }

    /**
     * Returns the {@link DiceType} this generator was created for.
     */
    public DiceType type() {
        return type;
    }

    /**
     * Roll once and return a value in {@code [1, sides]}.
     * Implements {@link Generator#generate()}.
     */
    @Override
    public Integer generate() {
        return randomFace();
    }

    /**
     * Roll {@code n} times and return the sum of all results.
     *
     * <p>The returned value is always in {@code [n, n * sides]} (inclusive).
     * Equivalent to Chance.js {@code rpg('3d6', {sum: true})}.
     *
     * @param n number of rolls; must be &gt; 0
     * @return sum of {@code n} independent rolls
     * @throws IllegalArgumentException if {@code n <= 0}
     */
    public int rollSum(int n) {
        return roll(n).stream().mapToInt(Integer::intValue).sum();
    }

    /**
     * Roll {@code n} times and return the results as an immutable list.
     *
     * <p>Fairness guarantee: if {@code n > sides}, every face value appears at least
     * once in the returned list (the full face set is included and the list is shuffled).
     *
     * @param n number of rolls; must be &gt; 0
     * @throws IllegalArgumentException if {@code n <= 0}
     */
    public List<Integer> roll(int n) {
        if (n <= 0) {
            throw new IllegalArgumentException("n must be > 0, was: " + n);
        }
        int sides = type.sides();
        List<Integer> result = new ArrayList<>(n);
        if (n > sides) {
            // guarantee every face appears at least once
            for (int i = 0; i < n - sides; i++) {
                result.add(randomFace());
            }
            for (int face = 1; face <= sides; face++) {
                result.add(face);
            }
            Collections.shuffle(result, random);
        } else {
            for (int i = 0; i < n; i++) {
                result.add(randomFace());
            }
        }
        return Collections.unmodifiableList(result);
    }

    private int randomFace() {
        return random.nextInt(type.sides()) + 1;
    }
}
