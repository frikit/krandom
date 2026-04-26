/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.user;

import io.github.frikit.krandom.generator.GeneratorConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.Locale;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

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

    @Test
    @DisplayName("local-part generator supports concatenated first+last branch")
    void localPartDefaultBranch() throws Exception {
        CompanyEmailGenerator gen = new CompanyEmailGenerator(
            GeneratorConfig.builder().seed(12L).locale(Locale.US).build());
        Field randomField = CompanyEmailGenerator.class.getDeclaredField("random");
        randomField.setAccessible(true);
        randomField.set(gen, new Random() {

            @Override
            public int nextInt(int bound) {
                if (bound == 3) {
                    return 2;
                }
                return super.nextInt(bound);
            }
        });

        String email = gen.generate("Acme");
        String localPart = email.substring(0, email.indexOf('@'));
        assertFalse(localPart.contains("."));
        assertTrue(localPart.matches("[a-z0-9]+"));
    }

    @Test
    @DisplayName("local-part generator supports dotted first.last branch")
    void localPartDottedBranch() throws Exception {
        CompanyEmailGenerator gen = new CompanyEmailGenerator(
            GeneratorConfig.builder().seed(12L).locale(Locale.US).build());
        Field randomField = CompanyEmailGenerator.class.getDeclaredField("random");
        randomField.setAccessible(true);
        randomField.set(gen, new Random() {

            @Override
            public int nextInt(int bound) {
                if (bound == 3) {
                    return 0;
                }
                return super.nextInt(bound);
            }
        });

        String email = gen.generate("Acme");
        String localPart = email.substring(0, email.indexOf('@'));
        assertTrue(localPart.contains("."));
        assertTrue(localPart.matches("[a-z0-9]+\\.[a-z0-9]+"));
    }

    @Test
    @DisplayName("non-latin names fall back to employee/user local-part defaults")
    void localPartBlankFallback() {
        CompanyEmailGenerator gen = new CompanyEmailGenerator(Locale.JAPAN);
        String email = gen.generate("Acme");
        String localPart = email.substring(0, email.indexOf('@'));
        assertTrue(localPart.contains("employee") || localPart.contains("user"));
    }
}
