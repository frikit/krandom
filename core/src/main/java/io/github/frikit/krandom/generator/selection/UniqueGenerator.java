/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.selection;

import io.github.frikit.krandom.generator.Generator;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiPredicate;

/**
 * Decorates another generator and guarantees generated values are unique.
 *
 * <p><b>Memory:</b> every emitted value is remembered until {@link #reset()} is called, so
 * long-lived instances grow with the number of generated values. With the default
 * {@link Objects#equals(Object, Object)} equality, membership uses a hash set and generated
 * values must have consistent {@code equals}/{@code hashCode}; a custom comparator falls back to
 * a linear scan of the remembered values.
 *
 * <p><b>Exhaustion:</b> when the source cannot produce a fresh value within
 * {@code maxAttempts} draws (default {@value #DEFAULT_MAX_ATTEMPTS}), {@link #generate()} throws
 * {@link IllegalStateException} instead of looping forever.
 *
 * @param <T> element type
 */
public final class UniqueGenerator<T> implements Generator<T> {

    private static final int DEFAULT_MAX_ATTEMPTS = 10_000;
    private static final BiPredicate<Object, Object> DEFAULT_EQUALITY = Objects::equals;

    private final Generator<T>      source;
    private final BiPredicate<T, T> comparator;
    private final int               maxAttempts;
    private final Set<T>            seenSet;
    private final List<T>           seen;

    /**
     * Creates a unique generator using {@link Objects#equals(Object, Object)}.
     *
     * @param source source generator
     */
    @SuppressWarnings("unchecked")
    public UniqueGenerator(Generator<T> source) {
        this(source, (BiPredicate<T, T>) DEFAULT_EQUALITY, DEFAULT_MAX_ATTEMPTS);
    }

    /**
     * Creates a unique generator with bounded retry attempts.
     *
     * @param source      source generator
     * @param maxAttempts max attempts per generated value
     */
    @SuppressWarnings("unchecked")
    public UniqueGenerator(Generator<T> source, int maxAttempts) {
        this(source, (BiPredicate<T, T>) DEFAULT_EQUALITY, maxAttempts);
    }

    /**
     * Creates a unique generator with custom equality comparator.
     *
     * @param source     source generator
     * @param comparator equality comparator
     */
    public UniqueGenerator(Generator<T> source, BiPredicate<T, T> comparator) {
        this(source, comparator, DEFAULT_MAX_ATTEMPTS);
    }

    /**
     * Creates a unique generator with custom comparator and bounded retries.
     *
     * @param source      source generator
     * @param comparator  equality comparator
     * @param maxAttempts max attempts per generated value
     */
    public UniqueGenerator(Generator<T> source, BiPredicate<T, T> comparator, int maxAttempts) {
        this.source = Objects.requireNonNull(source, "source must not be null");
        this.comparator = Objects.requireNonNull(comparator, "comparator must not be null");
        if (maxAttempts <= 0) {
            throw new IllegalArgumentException("maxAttempts must be > 0, got: " + maxAttempts);
        }
        this.maxAttempts = maxAttempts;
        this.seenSet = new HashSet<>();
        this.seen = new ArrayList<>();
    }

    @Override
    public T generate() {
        boolean defaultEquality = comparator == DEFAULT_EQUALITY;
        for (int i = 0; i < maxAttempts; i++) {
            T candidate = source.generate();
            if (defaultEquality) {
                if (seenSet.add(candidate)) {
                    return candidate;
                }
            } else if (!isSeen(candidate)) {
                seen.add(candidate);
                return candidate;
            }
        }
        throw new IllegalStateException("Unable to generate a unique value after " + maxAttempts + " attempts");
    }

    /**
     * Clears values already emitted by this generator.
     *
     * <p>The wrapped source generator is not reset; only this uniqueness
     * filter's remembered values are cleared.
     */
    public void reset() {
        seenSet.clear();
        seen.clear();
    }

    private boolean isSeen(T candidate) {
        for (T existing : seen) {
            if (comparator.test(existing, candidate)) {
                return true;
            }
        }
        return false;
    }
}
