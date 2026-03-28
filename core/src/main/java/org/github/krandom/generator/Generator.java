/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package org.github.krandom.generator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * Core abstraction for random value generation.
 *
 * <p>Being a {@code @FunctionalInterface} it can be expressed as a lambda wherever a
 * {@code Generator<T>} is expected:
 * <pre>{@code
 *   Generator<String> fixed = () -> "hello";
 *   Generator<Integer> delegating = Generators.ofInt(1, 100)::generate;
 * }</pre>
 *
 * <p>The default methods ({@link #generateList}, {@link #stream}, {@link #map},
 * {@link #filter}) let generators be composed without sub-classing.
 *
 * @param <T> the type of value produced
 */
@FunctionalInterface
public interface Generator<T> {

    /**
     * Produce one random value.
     */
    T generate();

    /**
     * Produce an immutable list of {@code count} random values.
     *
     * @param count number of values; must be &gt;= 0
     * @throws IllegalArgumentException if {@code count} is negative
     */
    default List<T> generateList(int count) {
        if (count < 0) {
            throw new IllegalArgumentException("count must be >= 0, was: " + count);
        }
        List<T> result = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            result.add(generate());
        }
        return Collections.unmodifiableList(result);
    }

    /**
     * Return an infinite {@link Stream} of generated values backed by this generator.
     * Callers are responsible for terminating the stream (e.g. {@code .limit(n)}).
     */
    default Stream<T> stream() {
        return Stream.generate(this::generate);
    }

    /**
     * Return a new {@code Generator<R>} that applies {@code mapper} to every value
     * produced by this generator.
     *
     * <pre>{@code
     *   Generator<String> intAsString = Generators.ofInt(1, 100).map(Object::toString);
     * }</pre>
     */
    default <R> Generator<R> map(Function<? super T, ? extends R> mapper) {
        Objects.requireNonNull(mapper, "mapper must not be null");
        return () -> mapper.apply(generate());
    }

    /**
     * Return a new {@code Generator<T>} that keeps generating until the produced value
     * satisfies {@code predicate}. Use with care — an unsatisfiable predicate loops forever.
     */
    default Generator<T> filter(Predicate<? super T> predicate) {
        Objects.requireNonNull(predicate, "predicate must not be null");
        return () -> {
            T value;
            do {
                value = generate();
            } while (!predicate.test(value));
            return value;
        };
    }
}
