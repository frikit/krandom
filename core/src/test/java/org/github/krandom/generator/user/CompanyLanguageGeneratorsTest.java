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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@DisplayName("Company language generators")
class CompanyLanguageGeneratorsTest {

    @Test
    @DisplayName("company buzzword generator returns non-empty text")
    void buzzword() {
        assertFalse(new CompanyBuzzwordGenerator(Locale.US).generate().isBlank());
        assertFalse(new CompanyBuzzwordGenerator(Locale.GERMANY).generate().isBlank());
        assertFalse(new CompanyBuzzwordGenerator(Locale.FRANCE).generate().isBlank());
    }

    @Test
    @DisplayName("company catch phrase generator returns non-empty text")
    void catchPhrase() {
        assertFalse(new CompanyCatchPhraseGenerator(Locale.US).generate().isBlank());
        assertFalse(new CompanyCatchPhraseGenerator(Locale.of("es", "ES")).generate().isBlank());
    }

    @Test
    @DisplayName("locale datasets produce different company language output")
    void localeDifferences() {
        GeneratorConfig usCfg = GeneratorConfig.builder().seed(19L).locale(Locale.US).build();
        GeneratorConfig esCfg = GeneratorConfig.builder().seed(19L).locale(Locale.of("es", "ES")).build();
        String usBuzz = new CompanyBuzzwordGenerator(usCfg).generate();
        String esBuzz = new CompanyBuzzwordGenerator(esCfg).generate();
        assertNotEquals(usBuzz, esBuzz);

        String usCatch = new CompanyCatchPhraseGenerator(usCfg).generate();
        String esCatch = new CompanyCatchPhraseGenerator(esCfg).generate();
        assertNotEquals(usCatch, esCatch);
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
