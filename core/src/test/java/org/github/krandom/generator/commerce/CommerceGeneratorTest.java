/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package org.github.krandom.generator.commerce;

import org.github.krandom.generator.GeneratorConfig;
import org.github.krandom.generator.Generators;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("CommerceGenerator")
class CommerceGeneratorTest {

    @Test
    @DisplayName("generates product fields and defaults")
    void basics() {
        CommerceGenerator generator = new CommerceGenerator();
        assertFalse(generator.generate().isBlank());
        assertFalse(generator.generateProductDescription().isBlank());
        assertFalse(generator.generateDepartment().isBlank());
        assertFalse(generator.generateMaterial().isBlank());
        assertFalse(generator.generateAdjective().isBlank());
        assertFalse(generator.generateColor().isBlank());
        assertFalse(generator.generateProduct().isBlank());
        assertTrue(generator.generatePrice().compareTo(BigDecimal.ZERO) > 0);
    }

    @Test
    @DisplayName("locale affects generated vocabulary")
    void localeSupport() {
        CommerceGenerator en = new CommerceGenerator(GeneratorConfig.builder().seed(4L).locale(Locale.US).build());
        CommerceGenerator de = new CommerceGenerator(GeneratorConfig.builder().seed(4L).locale(Locale.GERMANY).build());
        assertNotEquals(en.generateProductName(), de.generateProductName());
        assertNotEquals(en.generateDepartment(), de.generateDepartment());
    }

    @Test
    @DisplayName("covers locale switch branches for vocabulary providers")
    void localeSwitchCoverage() {
        Locale[] locales = {
                Locale.US,
                Locale.GERMANY,
                Locale.FRANCE,
                Locale.of("es", "ES"),
                Locale.ITALY
        };
        for (Locale locale : locales) {
            CommerceGenerator generator = new CommerceGenerator(
                    GeneratorConfig.builder().seed(10L).locale(locale).build()
            );
            assertFalse(generator.generateAdjective().isBlank());
            assertFalse(generator.generateMaterial().isBlank());
            assertFalse(generator.generateProduct().isBlank());
            assertFalse(generator.generateDepartment().isBlank());
            assertFalse(generator.generateColor().isBlank());
        }
    }

    @Test
    @DisplayName("price helper validates range")
    void priceValidation() {
        CommerceGenerator generator = new CommerceGenerator(Locale.US);
        assertThrows(NullPointerException.class, () -> generator.generatePrice(null, BigDecimal.TEN));
        assertThrows(NullPointerException.class, () -> generator.generatePrice(BigDecimal.ONE, null));
        assertThrows(IllegalArgumentException.class,
                () -> generator.generatePrice(BigDecimal.valueOf(-1), BigDecimal.ONE));
        assertThrows(IllegalArgumentException.class,
                () -> generator.generatePrice(BigDecimal.TEN, BigDecimal.ONE));
    }

    @Test
    @DisplayName("constructor and factory validations")
    void constructorAndFactory() {
        assertThrows(NullPointerException.class, () -> new CommerceGenerator((Locale) null));
        assertThrows(NullPointerException.class, () -> new CommerceGenerator((GeneratorConfig) null));
        assertEquals(Locale.US, new CommerceGenerator(Locale.US).getLocale());
        assertNotNull(Generators.ofCommerce().generateProductName());
    }
}
