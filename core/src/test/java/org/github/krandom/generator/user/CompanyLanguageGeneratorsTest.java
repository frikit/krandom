/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package org.github.krandom.generator.user;

import org.github.krandom.generator.GeneratorConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Company language generators")
class CompanyLanguageGeneratorsTest {

    @Test
    @DisplayName("company buzzword generator returns non-empty text")
    void buzzword() {
        assertFalse(new CompanyBuzzwordGenerator(Locale.US).generate().isBlank());
        assertFalse(new CompanyBuzzwordGenerator(Locale.GERMANY).generate().isBlank());
        assertTrue(new CompanyBuzzwordGenerator(Locale.FRANCE).generate().startsWith("solutions "));
    }

    @Test
    @DisplayName("company catch phrase generator returns non-empty text")
    void catchPhrase() {
        assertFalse(new CompanyCatchPhraseGenerator(Locale.US).generate().isBlank());
        assertEquals("Innovacion confiable para equipos",
                new CompanyCatchPhraseGenerator(Locale.of("es", "ES")).generate());
    }

    @Test
    @DisplayName("company language generators support seeded config")
    void seededConfig() {
        GeneratorConfig cfg = GeneratorConfig.builder().seed(44L).locale(Locale.US).build();
        assertEquals(new CompanyBuzzwordGenerator(cfg).generate(), new CompanyBuzzwordGenerator(cfg).generate());
        assertEquals(new CompanyCatchPhraseGenerator(cfg).generate(), new CompanyCatchPhraseGenerator(cfg).generate());
    }

    @Test
    @DisplayName("last name gender variants are available")
    void lastNameGenderVariants() {
        LastNameGenerator gen = new LastNameGenerator(Locale.US);
        assertNotNull(gen.generateMale());
        assertNotNull(gen.generateFemale());
    }
}
