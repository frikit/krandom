/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.object;

import io.github.frikit.krandom.generator.GeneratorConfig;

/** Selects the random-stream model used for one object-generation session. */
final class ObjectFieldStreamPlanner {

    private ObjectFieldStreamPlanner() {
    }

    static boolean usesNamedChildStreams(GeneratorConfig config, Long generationSeed) {
        return usesNamedChildStreams(
            config.getObjectFieldStreamPolicy(), generationSeed, config.getGenerationRecipe().isPresent());
    }

    /** Resolvers pass the recipe presence computed once per configuration. */
    static boolean usesNamedChildStreams(ObjectFieldStreamPolicy policy, Long generationSeed, boolean portableRecipe) {
        return generationSeed != null && (policy == ObjectFieldStreamPolicy.INDEPENDENT || portableRecipe);
    }
}
