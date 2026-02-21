/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package org.github.krandom.generator;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Example demonstrating locale usage with GeneratorConfig.
 * This shows how generators can be configured with different locales
 * for locale-aware data generation (names, addresses, phone numbers, etc.)
 */
@DisplayName("Locale Usage Examples")
class LocaleUsageExample {

    @Test
    @DisplayName("Create config with default US locale")
    void defaultLocale() {
        GeneratorConfig config = GeneratorConfig.defaults();
        
        assertEquals(Locale.US, config.getLocale());
        System.out.println("Default locale: " + config.getLocale().getDisplayName());
    }

    @Test
    @DisplayName("Create config with German locale")
    void germanLocale() {
        GeneratorConfig config = GeneratorConfig.builder()
                .locale(Locale.GERMANY)
                .seed(12345L)  // For reproducible results
                .build();
        
        assertEquals(Locale.GERMANY, config.getLocale());
        assertEquals("de", config.getLocale().getLanguage());
        assertEquals("DE", config.getLocale().getCountry());
        
        System.out.println("German locale: " + config.getLocale().getDisplayName());
        System.out.println("Language: " + config.getLocale().getLanguage());
        System.out.println("Country: " + config.getLocale().getCountry());
    }

    @Test
    @DisplayName("Create config with Japanese locale")
    void japaneseLocale() {
        GeneratorConfig config = GeneratorConfig.builder()
                .locale(Locale.JAPAN)
                .stringLength(3, 10)
                .build();
        
        assertEquals(Locale.JAPAN, config.getLocale());
        assertEquals(3, config.getMinStringLength());
        assertEquals(10, config.getMaxStringLength());
        
        System.out.println("Japanese locale: " + config.getLocale().getDisplayName());
    }

    @Test
    @DisplayName("Create config with custom locale (Spanish Mexico)")
    void customLocale() {
        Locale mexicanSpanish = new Locale("es", "MX");
        
        GeneratorConfig config = GeneratorConfig.builder()
                .locale(mexicanSpanish)
                .build();
        
        assertEquals("es", config.getLocale().getLanguage());
        assertEquals("MX", config.getLocale().getCountry());
        assertEquals("Spanish (Mexico)", config.getLocale().getDisplayName());
        
        System.out.println("Custom locale: " + config.getLocale().getDisplayName());
    }

    @Test
    @DisplayName("Multiple configs with different locales")
    void multipleLocales() {
        GeneratorConfig usConfig = GeneratorConfig.builder()
                .locale(Locale.US)
                .build();
        
        GeneratorConfig ukConfig = GeneratorConfig.builder()
                .locale(Locale.UK)
                .build();
        
        GeneratorConfig franceConfig = GeneratorConfig.builder()
                .locale(Locale.FRANCE)
                .build();
        
        // All have different locales
        assertEquals(Locale.US, usConfig.getLocale());
        assertEquals(Locale.UK, ukConfig.getLocale());
        assertEquals(Locale.FRANCE, franceConfig.getLocale());
        
        System.out.println("US: " + usConfig.getLocale());
        System.out.println("UK: " + ukConfig.getLocale());
        System.out.println("France: " + franceConfig.getLocale());
    }

    @Test
    @DisplayName("Config with all parameters including locale")
    void fullConfiguration() {
        GeneratorConfig config = GeneratorConfig.builder()
                .locale(Locale.GERMANY)
                .seed(42L)
                .stringLength(8, 16)
                .collectionSize(5, 20)
                .build();
        
        assertEquals(Locale.GERMANY, config.getLocale());
        assertEquals(42L, config.getSeed().getAsLong());
        assertEquals(8, config.getMinStringLength());
        assertEquals(16, config.getMaxStringLength());
        assertEquals(5, config.getMinCollectionSize());
        assertEquals(20, config.getMaxCollectionSize());
        
        System.out.println("Full config created with locale: " + config.getLocale().getDisplayName());
    }
}
