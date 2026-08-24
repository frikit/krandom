/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.object;

import io.github.frikit.krandom.generator.GeneratorConfig;
import io.github.frikit.krandom.generator.user.FirstNameGenerator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("BuiltInProviderResolver")
class BuiltInProviderResolverTest {

    @Test
    @DisplayName("resolves canonical provider names and aliases without a mutable hub")
    void resolvesNamesAndAliases() {
        GeneratorConfig config = GeneratorConfig.builder().seed(7L).build();

        assertInstanceOf(
            FirstNameGenerator.class,
            BuiltInProviderResolver.provider("person.first_name", config, FirstNameGenerator.class));
        assertInstanceOf(
            FirstNameGenerator.class,
            BuiltInProviderResolver.provider("first_name", config, FirstNameGenerator.class));
    }

    @Test
    @DisplayName("seeded provider creation remains deterministic")
    void seededCreationRemainsDeterministic() {
        GeneratorConfig config = GeneratorConfig.builder().seed(7L).build();

        String first = BuiltInProviderResolver.generator("person.first_name", config).generate().toString();
        String second = BuiltInProviderResolver.generator("person.first_name", config).generate().toString();

        assertEquals(first, second);
    }

    @Test
    @DisplayName("unknown provider names fail with useful context")
    void unknownProviderFails() {
        assertThrows(
            IllegalArgumentException.class,
            () -> BuiltInProviderResolver.generator("missing.provider", GeneratorConfig.defaults()));
    }
}
