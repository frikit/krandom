/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.report;

/** Where a reported value's random stream comes from. */
public enum SeedSource {

    /** The configuration's own numeric or textual seed, drawn in sequence. */
    EXPLICIT_SEED,

    /**
     * A stream derived by name from the configuration's seed: each member of a seeded configuration with a
     * portable recipe, or with {@code ObjectFieldStreamPolicy.INDEPENDENT}, draws from its own named stream.
     */
    DERIVED_STREAM,

    /** The seed of a replay recipe, restored with {@code GenerationRecipe.toGeneratorConfig()}. */
    RECIPE,

    /** No seed: a secure, caller-owned or unseeded random source, which cannot be replayed. */
    UNSEEDED
}
