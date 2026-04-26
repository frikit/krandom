/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator;

/**
 * Marker interface for generators that support deterministic reseeding.
 *
 * <p>Implementing this interface signals that a generator holds mutable PRNG state
 * that can be reset to produce a deterministic sequence. Prefer implementing this
 * interface over relying on the reflection-based fallback in {@link Generator#reseed(long)}.
 *
 * @see Generator#reseed(long)
 */
public interface Seedable {

    /**
     * Reseed the underlying PRNG with the given seed value.
     *
     * @param seed deterministic seed
     */
    void reseed(long seed);
}
