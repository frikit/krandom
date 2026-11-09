/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator;

/**
 * Controls how composite generators obtain random streams for their child generators.
 *
 * <p>A composite generator such as {@code PaymentInfoGenerator} builds child generators from its
 * configuration through {@link GeneratorConfig#forChildStream(String)}. With a seed,
 * {@link GeneratorConfig#createRandom()} returns a fresh source at the same initial state on every
 * call, so children given the parent's configuration unchanged start in the same state as each
 * other and as their parent and can produce correlated values. Portable recipes record the policy
 * as {@code child-stream-policy}, and replay requires it.
 */
public enum ChildStreamPolicy {

    /**
     * Children receive the parent's configuration unchanged, so the children of a seeded composite
     * start in the same state. This reproduces the seeded output of the releases in which it was
     * the default, before {@link #INDEPENDENT} replaced it.
     */
    LEGACY,

    /**
     * Each child of a configuration with a numeric or textual seed receives a configuration whose
     * seed is derived from the parent seed and the child's stable stream name with
     * {@link GenerationRecipe#deriveChildSeed(long, String)}, so sibling children draw from
     * independent streams that the seed reproduces. This is the default. Without a seed nothing is
     * derived: each child takes its own source from {@link GeneratorConfig#createRandom()}, which
     * is a fresh unseeded or secure source unless the caller supplies the randomness.
     */
    INDEPENDENT
}
