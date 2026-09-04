/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.object;

import io.github.frikit.krandom.generator.GeneratorConfig;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ObjectFieldStreamPlannerTest {

    @Test
    void usesNamedStreamsForPortableSeededConfiguration() {
        assertTrue(ObjectFieldStreamPlanner.usesNamedChildStreams(GeneratorConfig.builder().seed(42L).build(), 42L));
    }

    @Test
    void usesNamedStreamsForExplicitIndependentPolicyEvenWithCustomRules() {
        GeneratorConfig config = GeneratorConfig.builder().seed(42L)
            .objectFieldStreamPolicy(ObjectFieldStreamPolicy.INDEPENDENT)
            .objectOverride(String.class, () -> "fixed")
            .build();
        assertTrue(ObjectFieldStreamPlanner.usesNamedChildStreams(config, 42L));
    }

    @Test
    void keepsSequentialStreamsWithoutAnOwnedSeedOrPortableConfiguration() {
        assertFalse(ObjectFieldStreamPlanner.usesNamedChildStreams(GeneratorConfig.builder().build(), null));
        assertFalse(ObjectFieldStreamPlanner.usesNamedChildStreams(GeneratorConfig.builder().seed(42L)
            .objectOverride(String.class, () -> "fixed").build(), 42L));
    }
}
