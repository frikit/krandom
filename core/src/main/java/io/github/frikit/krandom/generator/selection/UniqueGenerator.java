/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.selection;

import io.github.frikit.krandom.generator.Generator;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.BiPredicate;

/**
 * Decorates another generator and guarantees generated values are unique.
 *
 * @param <T> element type
 */
public final class UniqueGenerator<T> implements Generator<T> {

    private static final int DEFAULT_MAX_ATTEMPTS = 10_000;

    private final Generator<T>      source;
    private final BiPredicate<T, T> comparator;
    private final int               maxAttempts;
    private final List<T>           seen;

    /**
     * Creates a unique generator using {@link Objects#equals(Object, Object)}.
     *
     * @param source source generator
     */
    public UniqueGenerator(Generator<T> source) {
        this(source, Objects::equals, DEFAULT_MAX_ATTEMPTS);
    }

    /**
     * Creates a unique generator with bounded retry attempts.
     *
     * @param source      source generator
     * @param maxAttempts max attempts per generated value
     */
    public UniqueGenerator(Generator<T> source, int maxAttempts) {
        this(source, Objects::equals, maxAttempts);
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
        this.seen = new ArrayList<>();
    }

    @Override
    public T generate() {
        for (int i = 0; i < maxAttempts; i++) {
            T candidate = source.generate();
            if (!isSeen(candidate)) {
                seen.add(candidate);
                return candidate;
            }
        }
        throw new IllegalStateException("Unable to generate a unique value after " + maxAttempts + " attempts");
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

