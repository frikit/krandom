/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package org.github.krandom.generator;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("GeneratorProfile")
class GeneratorProfileTest {

    @Test
    @DisplayName("STRICT preset creates deterministic tight config")
    void strictPreset() {
        GeneratorConfig config = GeneratorProfile.STRICT.createConfig(Locale.GERMANY);
        assertEquals(Locale.GERMANY, config.getLocale());
        assertTrue(config.getSeed().isPresent());
        assertEquals(1L, config.getSeed().getAsLong());
        assertEquals(3, config.getMinStringLength());
        assertEquals(12, config.getMaxStringLength());
        assertEquals(1, config.getMinCollectionSize());
        assertEquals(6, config.getMaxCollectionSize());
    }

    @Test
    @DisplayName("REALISTIC preset keeps unset seed and broader ranges")
    void realisticPreset() {
        GeneratorConfig config = GeneratorProfile.REALISTIC.createConfig();
        assertTrue(config.getSeed().isEmpty());
        assertEquals(5, config.getMinStringLength());
        assertEquals(24, config.getMaxStringLength());
        assertEquals(1, config.getMinCollectionSize());
        assertEquals(12, config.getMaxCollectionSize());
    }

    @Test
    @DisplayName("FAST preset derives from base config and overrides target fields")
    void fastApplyToBase() {
        GeneratorConfig base = GeneratorConfig.builder()
                                              .locale(Locale.ITALY)
                                              .seed(99L)
                                              .stringLength(20, 40)
                                              .collectionSize(5, 15)
                                              .build();

        GeneratorConfig derived = GeneratorProfile.FAST.applyTo(base);
        assertEquals(Locale.ITALY, derived.getLocale());
        assertTrue(derived.getSeed().isPresent());
        assertEquals(0L, derived.getSeed().getAsLong());
        assertEquals(3, derived.getMinStringLength());
        assertEquals(10, derived.getMaxStringLength());
        assertEquals(0, derived.getMinCollectionSize());
        assertEquals(5, derived.getMaxCollectionSize());
    }

    @Test
    @DisplayName("profile methods validate null arguments")
    void nullValidation() {
        assertThrows(NullPointerException.class, () -> GeneratorProfile.STRICT.applyToBuilder(null));
        assertThrows(NullPointerException.class, () -> GeneratorProfile.REALISTIC.applyTo(null));
    }
}
