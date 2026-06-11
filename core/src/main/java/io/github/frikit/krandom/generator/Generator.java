/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator;

import org.jspecify.annotations.Nullable;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Random;
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
 * generator holds its own {@link java.util.Random} instance whose internal state mutates
 * on every call to {@link #generate()}. Sharing a generator across threads without
 * external synchronization will produce corrupted or duplicated output. Use
 * {@code Generators.threadLocal()} to obtain a thread-confined wrapper when
 * concurrent generation is needed.
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
     * Reseeds underlying mutable random sources for stateful generators.
     *
     * <p>If this generator implements {@link Seedable}, the seed is applied directly via
     * {@link Seedable#reseed(long)}. Otherwise, a reflection-based fallback discovers
     * non-static fields assignable to {@link Random} across the class hierarchy and applies
     * {@link Random#setSeed(long)}.
     *
     * <p>New generators should implement {@link Seedable} directly instead of relying on
     * the reflection fallback. The reflection fallback is a transitional mechanism and is
     * planned for deprecation and removal in a future major release.
     *
     * @param seed deterministic seed
     * @throws UnsupportedOperationException if no reseedable random field is present
     */
    default void reseed(long seed) {
        if (this instanceof Seedable seedable) {
            seedable.reseed(seed);
            return;
        }
        // Reflection fallback for generators that do not implement Seedable
        boolean reseeded = false;
        for (Class<?> type = getClass(); type != Object.class; type = type.getSuperclass()) {
            for (Field field : type.getDeclaredFields()) {
                if (Modifier.isStatic(field.getModifiers())) {
                    continue;
                }
                try {
                    field.setAccessible(true);
                    Object value = field.get(this);
                    if (value instanceof Random random) {
                        random.setSeed(seed);
                        reseeded = true;
                    }
                } catch (RuntimeException | IllegalAccessException e) {
                    throw new IllegalStateException("Unable to access field '" + field.getName() + "'", e);
                }
            }
        }
        if (!reseeded) {
            throw new UnsupportedOperationException(
                "Generator " + getClass().getName() + " does not expose a reseedable Random field");
        }
    }

    /**
     * Reseeds using the same string-to-seed derivation contract as {@link GeneratorConfig}.
     *
     * @param seedText textual seed
     */
    default void reseed(String seedText) {
        reseed(GeneratorConfig.deriveSeed(seedText));
    }

    /**
     * Return a new {@code Generator<R>} that applies {@code mapper} to every value
     * produced by this generator.
     *
     * <pre>{@code
     *   Generator<String> intAsString = Generators.ofInt(1, 100).map(Object::toString);
     * }</pre>
     */
    default <R extends @Nullable Object> Generator<R> map(Function<? super T, ? extends R> mapper) {
        Objects.requireNonNull(mapper, "mapper must not be null");
        return () -> mapper.apply(generate());
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
     * @param predicate predicate a generated value must satisfy
     * @param maxAttempts maximum generated values to try before failing
     * @throws IllegalArgumentException if {@code maxAttempts} is not positive
     */
    default Generator<T> filter(Predicate<? super T> predicate, int maxAttempts) {
        Objects.requireNonNull(predicate, "predicate must not be null");
        if (maxAttempts <= 0) {
            throw new IllegalArgumentException("maxAttempts must be > 0, was: " + maxAttempts);
        }
        return () -> {
            for (int attempt = 0; attempt < maxAttempts; attempt++) {
                T value = generate();
                if (predicate.test(value)) {
                    return value;
                }
            }
            throw new IllegalStateException(
                "Unable to generate a value matching the predicate after " + maxAttempts + " attempts");
        };
    }
}
