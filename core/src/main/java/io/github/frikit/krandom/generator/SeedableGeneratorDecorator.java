/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator;

import org.jspecify.annotations.Nullable;

import java.util.Objects;

/**
 * Preserves a source generator's explicit {@link Seedable} contract across a decorator.
 */
final class SeedableGeneratorDecorator<T extends @Nullable Object> implements Generator<T>, Seedable {

    private final Generator<T> delegate;
    private final Seedable seedableSource;

    private SeedableGeneratorDecorator(Generator<T> delegate, Seedable seedableSource) {
        this.delegate = Objects.requireNonNull(delegate, "delegate must not be null");
        this.seedableSource = Objects.requireNonNull(seedableSource, "seedableSource must not be null");
    }

    static <T extends @Nullable Object> Generator<T> preserve(Generator<?> source, Generator<T> delegate) {
        if (source instanceof Seedable seedable) {
            return new SeedableGeneratorDecorator<>(delegate, seedable);
        }
        return delegate;
    }

    @Override
    public T generate() {
        return delegate.generate();
    }

    @Override
    public void reseed(long seed) {
        seedableSource.reseed(seed);
    }
}
