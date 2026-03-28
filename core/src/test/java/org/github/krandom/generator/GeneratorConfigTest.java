/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package org.github.krandom.generator;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("GeneratorConfig")
class GeneratorConfigTest {

    @Test
    @DisplayName("defaults() returns config with all default values")
    void defaultValues() {
        GeneratorConfig c = GeneratorConfig.defaults();
        assertTrue(c.getSeed().isEmpty());
        assertEquals(StandardCharsets.US_ASCII, c.getCharset());
        assertEquals(5, c.getMinStringLength());
        assertEquals(20, c.getMaxStringLength());
        assertEquals(1, c.getMinCollectionSize());
        assertEquals(10, c.getMaxCollectionSize());
        assertEquals(Locale.US, c.getLocale());
        assertSame(DataRegistryContext.globalDefault(), c.getRegistryContext());
    }

    @Test
    @DisplayName("seed() stores the seed value")
    void seedStored() {
        GeneratorConfig c = GeneratorConfig.builder().seed(42L).build();
        assertTrue(c.getSeed().isPresent());
        assertEquals(42L, c.getSeed().getAsLong());
    }

    @Test
    @DisplayName("charset() stores the charset")
    void charsetStored() {
        GeneratorConfig c = GeneratorConfig.builder().charset(StandardCharsets.UTF_8).build();
        assertEquals(StandardCharsets.UTF_8, c.getCharset());
    }

    @Test
    @DisplayName("charset(null) throws NullPointerException")
    void charsetNullThrows() {
        assertThrows(NullPointerException.class,
                     () -> GeneratorConfig.builder().charset(null));
    }

    @Test
    @DisplayName("stringLength(8, 32) stores the values")
    void stringLengthValid() {
        GeneratorConfig c = GeneratorConfig.builder().stringLength(8, 32).build();
        assertEquals(8, c.getMinStringLength());
        assertEquals(32, c.getMaxStringLength());
    }

    @Test
    @DisplayName("stringLength(0, 10) throws — min must be >= 1")
    void stringLengthMinBelowOneThrows() {
        assertThrows(IllegalArgumentException.class,
                     () -> GeneratorConfig.builder().stringLength(0, 10));
    }

    @Test
    @DisplayName("stringLength(10, 5) throws — max must be >= min")
    void stringLengthMaxBelowMinThrows() {
        assertThrows(IllegalArgumentException.class,
                     () -> GeneratorConfig.builder().stringLength(10, 5));
    }

    @Test
    @DisplayName("collectionSize(0, 20) stores the values")
    void collectionSizeValid() {
        GeneratorConfig c = GeneratorConfig.builder().collectionSize(0, 20).build();
        assertEquals(0, c.getMinCollectionSize());
        assertEquals(20, c.getMaxCollectionSize());
    }

    @Test
    @DisplayName("collectionSize(-1, 5) throws — min must be >= 0")
    void collectionSizeMinNegativeThrows() {
        assertThrows(IllegalArgumentException.class,
                     () -> GeneratorConfig.builder().collectionSize(-1, 5));
    }

    @Test
    @DisplayName("collectionSize(5, 3) throws — max must be >= min")
    void collectionSizeMaxBelowMinThrows() {
        assertThrows(IllegalArgumentException.class,
                     () -> GeneratorConfig.builder().collectionSize(5, 3));
    }

    @Test
    @DisplayName("locale() stores the locale")
    void localeStored() {
        GeneratorConfig c = GeneratorConfig.builder().locale(Locale.GERMANY).build();
        assertEquals(Locale.GERMANY, c.getLocale());
    }

    @Test
    @DisplayName("locale(null) throws NullPointerException")
    void localeNullThrows() {
        assertThrows(NullPointerException.class,
                     () -> GeneratorConfig.builder().locale(null));
    }

    @Test
    @DisplayName("registryContext() stores the context")
    void registryContextStored() {
        DataRegistryContext context = DataRegistryContext.builder().isolated().build();
        GeneratorConfig config = GeneratorConfig.builder().registryContext(context).build();
        assertSame(context, config.getRegistryContext());
    }

    @Test
    @DisplayName("registryContext(null) throws NullPointerException")
    void registryContextNullThrows() {
        assertThrows(NullPointerException.class,
                     () -> GeneratorConfig.builder().registryContext(null));
    }

    @Test
    @DisplayName("toBuilder() copies all fields and allows deriving new config")
    void toBuilderCopiesAndDerives() {
        DataRegistryContext context = DataRegistryContext.builder().isolated().build();
        GeneratorConfig base = GeneratorConfig.builder()
                                              .seed(123L)
                                              .charset(StandardCharsets.UTF_8)
                                              .stringLength(8, 16)
                                              .collectionSize(2, 4)
                                              .locale(Locale.FRANCE)
                                              .registryContext(context)
                                              .build();

        GeneratorConfig derived = base.toBuilder().locale(Locale.JAPAN).build();
        assertTrue(derived.getSeed().isPresent());
        assertEquals(123L, derived.getSeed().getAsLong());
        assertEquals(StandardCharsets.UTF_8, derived.getCharset());
        assertEquals(8, derived.getMinStringLength());
        assertEquals(16, derived.getMaxStringLength());
        assertEquals(2, derived.getMinCollectionSize());
        assertEquals(4, derived.getMaxCollectionSize());
        assertEquals(Locale.JAPAN, derived.getLocale());
        assertSame(context, derived.getRegistryContext());
    }

    @Test
    @DisplayName("locale() accepts various locales")
    void variousLocales() {
        GeneratorConfig japan = GeneratorConfig.builder().locale(Locale.JAPAN).build();
        assertEquals(Locale.JAPAN, japan.getLocale());

        GeneratorConfig custom = GeneratorConfig.builder()
                                                .locale(new Locale("es", "MX"))
                                                .build();
        assertEquals("es", custom.getLocale().getLanguage());
        assertEquals("MX", custom.getLocale().getCountry());
    }
}
