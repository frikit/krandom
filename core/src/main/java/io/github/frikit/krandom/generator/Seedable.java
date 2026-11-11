/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator;

/**
 * Contract for generators that support deterministic reseeding.
 *
 * <p>Implementing this interface signals that a generator holds mutable PRNG state that can be
 * reset to produce a deterministic sequence. This is the only reseeding contract in v2: callers
 * check {@code generator instanceof Seedable} and reseed through this interface; generators that
 * do not implement it are not reseedable.
 *
 * <p>A generator whose random source is a {@link java.security.SecureRandom}, from
 * {@link GeneratorConfig.Builder#secureRandom()} or one that the caller supplies, cannot be reset:
 * {@link java.security.SecureRandom#setSeed(long)} adds the seed to its state rather than replacing
 * it, so reseeding such a generator does not repeat a sequence.
 */
public interface Seedable {

    /**
     * Reseed the underlying PRNG with the given seed value.
     *
     * @param seed deterministic seed
     */
    void reseed(long seed);
}
