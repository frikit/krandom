/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator;

import org.jspecify.annotations.Nullable;

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
 * <p><strong>Thread safety:</strong> Instances of {@code Generator} are <em>not thread-safe</em>. Each
 * generator usually holds a mutable random source whose state advances on every call to
 * {@link #generate()}. The JDK's {@link java.util.Random} is memory-safe when shared, but concurrent
 * calls contend and interleave its sequence, so call order and seeded replay are no longer
 * deterministic. Other generator state may not be safe to share. Use
 * {@code Generators.threadLocal()} to obtain a thread-confined generator when concurrent
 * generation is needed.
 *
 * @param <T> the type of value produced
 */
@FunctionalInterface
public interface Generator<T extends @Nullable Object> {

    /**
     * Default retry cap for {@link #filter(Predicate)}.
     */
    int DEFAULT_FILTER_MAX_ATTEMPTS = 10_000;

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
     *
     * <p>If this generator implements {@link Seedable}, the returned generator also implements
     * {@code Seedable} and forwards reseeding to this generator.
     */
    default <R extends @Nullable Object> Generator<R> map(Function<? super T, ? extends R> mapper) {
        Objects.requireNonNull(mapper, "mapper must not be null");
        Generator<R> mapped = () -> mapper.apply(generate());
        return SeedableGeneratorDecorator.preserve(this, mapped);
    }

    /**
     * Return a new {@code Generator<T>} that keeps generating until the produced value
     * satisfies {@code predicate}.
     *
     * <p>The returned generator fails after {@link #DEFAULT_FILTER_MAX_ATTEMPTS} attempts
     * to avoid hanging forever when the predicate cannot be satisfied.
     */
    default Generator<T> filter(Predicate<? super T> predicate) {
        return filter(predicate, DEFAULT_FILTER_MAX_ATTEMPTS);
    }

    /**
     * Return a new {@code Generator<T>} that keeps generating until the produced value
     * satisfies {@code predicate}, failing after {@code maxAttempts}.
     *
     * <p>If this generator implements {@link Seedable}, the returned generator also implements
     * {@code Seedable} and forwards reseeding to this generator.
     *
     * @param predicate predicate a generated value must satisfy
     * @param maxAttempts maximum generated values to try before failing
     * @throws IllegalArgumentException if {@code maxAttempts} is not positive
     */
    default Generator<T> filter(Predicate<? super T> predicate, int maxAttempts) {
        Objects.requireNonNull(predicate, "predicate must not be null");
        if (maxAttempts <= 0) {
            throw new IllegalArgumentException("maxAttempts must be > 0, was: " + maxAttempts);
        }
        Generator<T> filtered = () -> {
            for (int attempt = 0; attempt < maxAttempts; attempt++) {
                T value = generate();
                if (predicate.test(value)) {
                    return value;
                }
            }
            throw new IllegalStateException(
                "Unable to generate a value matching the predicate after " + maxAttempts + " attempts");
        };
        return SeedableGeneratorDecorator.preserve(this, filtered);
    }
}
