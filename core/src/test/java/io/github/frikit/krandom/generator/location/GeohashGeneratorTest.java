/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.location;

import io.github.frikit.krandom.generator.GeneratorConfig;
import io.github.frikit.krandom.generator.Generators;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("GeohashGenerator")
class GeohashGeneratorTest {

    @Test
    @DisplayName("encode matches known examples")
    void encodeMatchesKnownExamples() {
        assertEquals("ezs42", GeohashGenerator.encode(42.6, -5.6, 5));
        assertEquals("u4pruydqqvj", GeohashGenerator.encode(57.64911, 10.40744, 11));
        assertEquals(GeohashGenerator.DEFAULT_PRECISION, GeohashGenerator.encode(42.6, -5.6).length());
    }

    @Test
    @DisplayName("generate uses configured precision")
    void generateUsesConfiguredPrecision() {
        assertEquals(7, new GeohashGenerator(7, 123L).generate().length());
    }

    @Test
    @DisplayName("seeded generation is reproducible")
    void seededGenerationIsReproducible() {
        GeohashGenerator first = new GeohashGenerator(8, GeneratorConfig.builder()
            .locale(Locale.GERMANY)
            .seed(44L)
            .build());
        GeohashGenerator second = new GeohashGenerator(8, GeneratorConfig.builder()
            .locale(Locale.GERMANY)
            .seed(44L)
            .build());

        for (int i = 0; i < 20; i++) {
            assertEquals(first.generate(), second.generate());
        }
    }

    @Test
    @DisplayName("generated geohash contains only geohash alphabet")
    void generatedGeohashContainsOnlyGeohashAlphabet() {
        String value = new GeohashGenerator(GeneratorConfig.builder().seed(5L).build()).generate();

        assertTrue(value.matches("[0123456789bcdefghjkmnpqrstuvwxyz]{12}"));
    }

    @Test
    @DisplayName("facade exposes geohash generator")
    void facadeExposesGeohashGenerator() {
        assertNotNull(Generators.ofGeohash().generate());
        assertNotNull(Generators.ofGeohash(6).generate());
        assertNotNull(Generators.ofGeohash(1L).generate());
        assertNotNull(Generators.ofGeohash(6, 1L).generate());
        assertNotNull(Generators.ofGeohash(GeneratorConfig.defaults()).generate());
        assertNotNull(Generators.ofGeohash(6, GeneratorConfig.defaults()).generate());
    }

    @Test
    @DisplayName("invalid precision and coordinates fail fast")
    void invalidPrecisionAndCoordinatesFailFast() {
        assertThrows(IllegalArgumentException.class, () -> new GeohashGenerator(0));
        assertThrows(IllegalArgumentException.class, () -> new GeohashGenerator(13));
        assertThrows(IllegalArgumentException.class, () -> GeohashGenerator.encode(Double.NaN, 0.0));
        assertThrows(IllegalArgumentException.class, () -> GeohashGenerator.encode(91.0, 0.0));
        assertThrows(IllegalArgumentException.class, () -> GeohashGenerator.encode(-91.0, 0.0));
        assertThrows(IllegalArgumentException.class, () -> GeohashGenerator.encode(0.0, 181.0));
        assertThrows(IllegalArgumentException.class, () -> GeohashGenerator.encode(0.0, -181.0));
        assertThrows(NullPointerException.class, () -> new GeohashGenerator((GeneratorConfig) null));
    }
}
