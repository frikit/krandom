/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.base;

import io.github.frikit.krandom.generator.BoundedGenerator;
import io.github.frikit.krandom.generator.Seedable;

import java.util.Objects;
import java.util.Random;
import java.util.random.RandomGenerator;

/**
 * Skeletal implementation of {@link BoundedGenerator} for numeric types.
 *
 * <p>Responsibilities:
 * <ul>
 *   <li>Owns the {@link RandomGenerator} instance ({@link Random}).</li>
 *   <li>Stores the default {@code min} / {@code max} bounds.</li>
 *   <li>Delegates no-arg {@link #generate()} to {@link #generate(Comparable, Comparable)}.</li>
 *   <li>Provides {@link #validate(Number, Number)}, {@link #lo(Number, Number)}, and
 *       {@link #hi(Number, Number)} helpers to concrete subclasses.</li>
 * </ul>
 *
 * @param <T> numeric type; must extend {@link Number} and implement {@link Comparable}
 */
public abstract class AbstractBoundedGenerator<T extends Number & Comparable<T>>
    implements BoundedGenerator<T>, Seedable {

    /**
     * The underlying PRNG — {@code RandomGenerator} is the Java-17+ interface.
     */
    protected final RandomGenerator random;

    private final T min;
    private final T max;

    /**
     * @param min  lower bound (inclusive) used by the no-arg {@link #generate()}
     * @param max  upper bound (exclusive) used by the no-arg {@link #generate()}
     * @param seed optional seed for reproducibility
     */
    protected AbstractBoundedGenerator(T min, T max, Long seed) {
        this.min = Objects.requireNonNull(min, "min must not be null");
        this.max = Objects.requireNonNull(max, "max must not be null");
        this.random = seed != null ? new Random(seed) : new Random();
    }

    // ── BoundedGenerator ──────────────────────────────────────────────────────

    @Override
    public final T getMin() {
        return min;
    }

    @Override
    public final T getMax() {
        return max;
    }

    /**
     * Delegates to {@link #generate(Comparable, Comparable)} using the configured bounds.
     */
    @Override
    public T generate() {
        return generate(min, max);
    }

    // ── Seedable ──────────────────────────────────────────────────────────────

    @Override
    public void reseed(long seed) {
        // Both Random and SecureRandom extend Random, so this always succeeds
        // for generators created by this library.
        ((Random) random).setSeed(seed);
    }

    // ── Helpers for subclasses ────────────────────────────────────────────────

    /**
     * Assert that {@code a} and {@code b} are not equal.
     * Called inside each {@code generate(min, max)} implementation.
     */
    protected void validate(T a, T b) {
        Objects.requireNonNull(a, "min must not be null");
        Objects.requireNonNull(b, "max must not be null");
        if (a.compareTo(b) == 0) {
            throw new IllegalArgumentException(
                "min and max must differ, both were: " + a);
        }
    }

    /**
     * Return the lesser of two comparable values.
     */
    protected T lo(T a, T b) {
        return a.compareTo(b) <= 0 ? a : b;
    }

    /**
     * Return the greater of two comparable values.
     */
    protected T hi(T a, T b) {
        return a.compareTo(b) >= 0 ? a : b;
    }
}
