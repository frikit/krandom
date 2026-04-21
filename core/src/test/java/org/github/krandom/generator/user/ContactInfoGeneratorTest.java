/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package org.github.krandom.generator.user;

import org.github.krandom.generator.GeneratorConfig;
import org.github.krandom.generator.Generators;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.text.Normalizer;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("ContactInfoGenerator")
class ContactInfoGeneratorTest {

    @Test
    @DisplayName("generates a coherent structured contact payload")
    void generateContactInfo() {
        ContactInfo info = new ContactInfoGenerator(Locale.US).generate();

        assertNotNull(info);
        assertEquals(info.firstName() + " " + info.lastName(), info.name());
        assertTrue(info.age() >= 18 && info.age() <= 90);
        assertTrue(info.phone().matches("\\d+"));
        assertEquals(info.phone(), info.phoneFormatted().replaceAll("\\D", ""));
        assertTrue(info.email().contains("@"));
        assertFalse(info.gender().isBlank());

        String localPart = info.email().substring(0, info.email().indexOf('@'));
        assertTrue(localPart.contains(normalize(info.lastName())));
    }

    @Test
    @DisplayName("seeded generation is reproducible")
    void seededGeneration() {
        GeneratorConfig config = GeneratorConfig.builder()
                                                .locale(Locale.US)
                                                .seed(42L)
                                                .build();

        ContactInfoGenerator one = new ContactInfoGenerator(config);
        ContactInfoGenerator two = new ContactInfoGenerator(config);

        assertEquals(one.generate(), two.generate());
    }

    @Test
    @DisplayName("non latin locales fall back to generated email local parts")
    void nonLatinEmailFallback() {
        ContactInfo info = new ContactInfoGenerator(
            GeneratorConfig.builder().locale(Locale.JAPAN).seed(9L).build()
        ).generate();

        assertTrue(info.email().contains("@"));
        assertFalse(info.email().substring(0, info.email().indexOf('@')).isBlank());
    }

    @Test
    @DisplayName("email formats eventually cover dotted local parts")
    void dottedEmailFormatAppears() {
        ContactInfoGenerator generator = new ContactInfoGenerator(
            GeneratorConfig.builder().locale(Locale.US).seed(12L).build()
        );

        boolean sawDotted = false;
        for (int i = 0; i < 200 && !sawDotted; i++) {
            ContactInfo info = generator.generate();
            String localPart = info.email().substring(0, info.email().indexOf('@'));
            sawDotted |= localPart.contains(".");
        }

        assertTrue(sawDotted);
    }

    @Test
    @DisplayName("constructors and factories reject nulls and expose locale")
    void constructorValidation() {
        assertThrows(NullPointerException.class, () -> new ContactInfoGenerator((Locale) null));
        assertThrows(NullPointerException.class, () -> new ContactInfoGenerator((GeneratorConfig) null));
        assertEquals(Locale.US, new ContactInfoGenerator(Locale.US).getLocale());
        assertNotNull(Generators.ofContactInfo().generate());
        assertNotNull(Generators.ofContactInfo(Locale.US).generate());
        assertNotNull(Generators.ofContactInfo(GeneratorConfig.defaults()).generate());
    }

    private static String normalize(String value) {
        return Normalizer.normalize(value, Normalizer.Form.NFD)
                         .replaceAll("\\p{M}+", "")
                         .toLowerCase(Locale.ROOT)
                         .replaceAll("[^a-z0-9]", "");
    }
}
