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

@DisplayName("CompanyEmailGenerator")
class CompanyEmailGeneratorTest {

    @Test
    @DisplayName("generate returns company-style email")
    void generate() {
        String email = new CompanyEmailGenerator(Locale.US).generate();
        assertTrue(email.matches("[a-z0-9.]+@[a-z0-9]+\\.[a-z]{2,}"));
    }

    @Test
    @DisplayName("generate with fixed company name uses normalized label")
    void generateWithCompany() {
        CompanyEmailGenerator gen = new CompanyEmailGenerator(Locale.GERMANY);
        String email = gen.generate("Muller & Sohne GmbH");
        assertTrue(email.contains("@mullersohnegmbh."));
    }

    @Test
    @DisplayName("blank company name falls back to generated domain")
    void blankCompanyName() {
        CompanyEmailGenerator gen = new CompanyEmailGenerator();
        String email = gen.generate("___");
        assertTrue(email.contains("@"));
        assertFalse(email.substring(email.indexOf('@') + 1).startsWith("."));
    }

    @Test
    @DisplayName("seeded generation is reproducible")
    void seeded() {
        GeneratorConfig cfg = GeneratorConfig.builder().seed(99L).locale(Locale.US).build();
        CompanyEmailGenerator a = new CompanyEmailGenerator(cfg);
        CompanyEmailGenerator b = new CompanyEmailGenerator(cfg);
        assertEquals(a.generate(), b.generate());
    }

    @Test
    @DisplayName("null inputs are rejected")
    void nullValidation() {
        CompanyEmailGenerator gen = new CompanyEmailGenerator();
        assertThrows(NullPointerException.class, () -> new CompanyEmailGenerator((GeneratorConfig) null));
        assertThrows(NullPointerException.class, () -> gen.generate(null));
    }
}
