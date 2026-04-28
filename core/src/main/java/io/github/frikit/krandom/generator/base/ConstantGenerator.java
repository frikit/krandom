/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.base;

import io.github.frikit.krandom.generator.Generator;

/**
 * Generator that always returns the same configured value.
 *
 * @param <T> generated value type
 */
public final class ConstantGenerator<T> implements Generator<T> {

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
