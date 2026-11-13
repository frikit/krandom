/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.base;

import io.github.frikit.krandom.generator.Generator;
import org.jspecify.annotations.Nullable;

/**
 * Generator that always returns the same configured value.
 *
 * <p>The value may be {@code null}, so {@code T} may be a nullable type, such as
 * {@code ConstantGenerator<@Nullable String>}, and the generator composes with the other generators
 * that accept nullable values.
 *
 * @param <T> generated value type, which may be nullable
 */
public final class ConstantGenerator<T extends @Nullable Object> implements Generator<T> {

    private final T value;

    /**
     * Creates a constant generator.
     *
     * @param value value returned by every {@link #generate()} call; may be {@code null}
     */
    public ConstantGenerator(T value) {
        this.value = value;
    }

    /**
     * Returns the configured constant value.
     *
     * @return constant value; may be {@code null}
     */
    @Override
    public T generate() {
        return value;
    }

    /**
     * Returns the configured value without advancing any state.
     *
     * @return constant value; may be {@code null}
     */
    public T value() {
        return value;
    }
}
