/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.object;

import io.github.frikit.krandom.generator.GeneratorConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Semantic providers are resolved on first use; one that has no data for the configured locale
 * falls back to generic generation instead of failing the object.
 */
class SemanticProviderFallbackTest {

    public static class Address {
        public String state;
    }

    public static class Person {
        public String firstName;
        public String email;
    }

    @Test
    @DisplayName("a semantic provider without data for the locale falls back to generic text")
    void unsupportedLocaleFallsBackToGenericText() {
        GeneratorConfig icelandic = GeneratorConfig.builder().seed(4L).locale(Locale.of("is", "IS")).build();
        Address address = new ObjectGenerator<>(Address.class, icelandic).generate();
        assertNotNull(address.state);
        assertFalse(address.state.isBlank());
    }

    @Test
    @DisplayName("independent member streams replay provider-backed semantic values")
    void independentStreamsReplaySemanticValues() {
        GeneratorConfig config = GeneratorConfig.builder()
                                                .seed(21L)
                                                .objectFieldStreamPolicy(ObjectFieldStreamPolicy.INDEPENDENT)
                                                .build();
        Person first = new ObjectGenerator<>(Person.class, config).generate();
        Person second = new ObjectGenerator<>(Person.class, config).generate();
        assertEquals(first.firstName, second.firstName);
        assertEquals(first.email, second.email);
        assertTrue(first.email.contains("@"), first.email);
    }
}
