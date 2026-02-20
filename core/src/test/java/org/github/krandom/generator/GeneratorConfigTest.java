/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package org.github.krandom.generator;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("GeneratorConfig")
class GeneratorConfigTest {

    @Test
    @DisplayName("defaults() returns config with all default values")
    void defaultValues() {
        GeneratorConfig c = GeneratorConfig.defaults();
        assertTrue(c.getSeed().isEmpty());
        assertEquals(StandardCharsets.US_ASCII, c.getCharset());
        assertEquals(5,  c.getMinStringLength());
        assertEquals(20, c.getMaxStringLength());
        assertEquals(1,  c.getMinCollectionSize());
        assertEquals(10, c.getMaxCollectionSize());
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
        assertEquals(8,  c.getMinStringLength());
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
        assertEquals(0,  c.getMinCollectionSize());
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
}
