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
 * configuration. With a seed, {@link GeneratorConfig#createRandom()} returns a fresh source at the
 * same initial state on every call, so legacy children start in the same state as each other and
 * as their parent and can produce correlated values.
 */
public enum ChildStreamPolicy {

    /**
     * Children receive the parent's configuration unchanged. This preserves every previously
     * published seeded value and is the default.
     */
    LEGACY,

    /**
     * Each child receives a configuration whose seed is derived from the parent seed and the
     * child's stable stream name with {@link GenerationRecipe#deriveChildSeed(long, String)}, so
     * sibling children draw from independent streams. Requires a numeric or textual seed.
     */
    INDEPENDENT
}
