/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.commerce;

import io.github.frikit.krandom.generator.GeneratorConfig;
import io.github.frikit.krandom.generator.Generators;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.Locale;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("CommerceGenerator")
class CommerceGeneratorTest {

    private static Random fixedRandom(int value) {
        return new Random() {

            @Override
            public int nextInt(int bound) {
                return Math.min(value, bound - 1);
            }
        };
    }

    @Test
    @DisplayName("generates product fields and defaults")
    void basics() {
        CommerceGenerator generator = new CommerceGenerator();
        assertFalse(generator.generate().isBlank());
        assertFalse(generator.generateProductDescription().isBlank());
        assertFalse(generator.generateDepartment().isBlank());
        assertFalse(generator.generateCategory().isBlank());
        assertFalse(generator.generateMaterial().isBlank());
        assertFalse(generator.generateAdjective().isBlank());
        assertFalse(generator.generateColor().isBlank());
        assertFalse(generator.generateProduct().isBlank());
        assertTrue(generator.generatePrice().compareTo(BigDecimal.ZERO) > 0);
        assertTrue(generator.generateUpc().matches("\\d{12}"));
        assertTrue(generator.generateEan8().matches("\\d{8}"));
        assertTrue(generator.generateEan13().matches("\\d{13}"));
        assertTrue(generator.generateIsbn13().matches("\\d{13}"));
        assertEquals(10, generator.generateIsbn10().length());
        assertFalse(generator.generateProductCode().isBlank());
        ProductInfo info = generator.generateProductInfo();
        assertFalse(info.name().isBlank());
        assertFalse(info.description().isBlank());
        assertFalse(info.category().isBlank());
        assertFalse(info.material().isBlank());
        assertTrue(info.upc().matches("\\d{12}"));
        assertTrue(info.isbn().matches("\\d{13}"));
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

    @Test
    @DisplayName("generateProductCode covers all switch branches")
    void productCodeBranches() throws Exception {
        CommerceGenerator generator = new CommerceGenerator(Locale.US);
        Field randomField = CommerceGenerator.class.getDeclaredField("random");
        randomField.setAccessible(true);

        randomField.set(generator, fixedRandom(0));
        assertTrue(generator.generateProductCode().matches("\\d{12}"));

        randomField.set(generator, fixedRandom(1));
        assertTrue(generator.generateProductCode().matches("\\d{13}"));

        randomField.set(generator, fixedRandom(2));
        assertTrue(generator.generateProductCode().matches("\\d{13}"));
    }
}
