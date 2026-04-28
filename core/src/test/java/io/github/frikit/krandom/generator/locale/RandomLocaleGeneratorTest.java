/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.locale;

import io.github.frikit.krandom.generator.GeneratorConfig;
import io.github.frikit.krandom.generator.Generators;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("RandomLocaleGenerator")
class RandomLocaleGeneratorTest {

    @Test
    @DisplayName("generate returns supported locales")
    void generateReturnsSupportedLocales() {
        Set<Locale> supported = new HashSet<>(SupportedLocale.locales());
        RandomLocaleGenerator generator = new RandomLocaleGenerator(GeneratorConfig.builder().seed(42L).build());

        for (int i = 0; i < 100; i++) {
            assertTrue(supported.contains(generator.generate()));
        }
    }

    @Test
    @DisplayName("generateSupportedLocale exposes enum value")
    void generateSupportedLocaleExposesEnumValue() {
        RandomLocaleGenerator generator = new RandomLocaleGenerator(7L);

        assertNotNull(generator.generateSupportedLocale().locale());
    }

    @Test
    @DisplayName("seeded generation is reproducible")
    void seededGenerationIsReproducible() {
        RandomLocaleGenerator first = new RandomLocaleGenerator(99L);
        RandomLocaleGenerator second = new RandomLocaleGenerator(99L);

        for (int i = 0; i < 20; i++) {
            assertEquals(first.generate(), second.generate());
        }
    }

    @Test
    @DisplayName("facade and forType expose locale generator")
    void facadeAndForTypeExposeLocaleGenerator() {
        assertNotNull(Generators.ofLocale().generate());
        assertNotNull(Generators.ofLocale(1L).generate());
        assertNotNull(Generators.ofLocale(GeneratorConfig.defaults()).generate());
        assertTrue(SupportedLocale.locales().contains(Generators.forType(Locale.class).generate()));
    }

    @Test
    @DisplayName("null config throws")
    void nullConfigThrows() {
        assertThrows(NullPointerException.class, () -> new RandomLocaleGenerator(null));
    }
}
