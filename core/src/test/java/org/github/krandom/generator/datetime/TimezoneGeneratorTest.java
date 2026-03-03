/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package org.github.krandom.generator.datetime;

import org.github.krandom.generator.GeneratorConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.ZoneId;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("TimezoneGenerator")
class TimezoneGeneratorTest {

    @Test
    @DisplayName("generate returns a valid zone id")
    void generate() {
        String zone = new TimezoneGenerator().generate();
        assertDoesNotThrow(() -> ZoneId.of(zone));
    }

    @Test
    @DisplayName("locale-aware generation prefers locale country zones")
    void localeAware() {
        TimezoneGenerator us = new TimezoneGenerator(Locale.US);
        assertTrue(us.localeZones().contains(us.generate()));

        TimezoneGenerator de = new TimezoneGenerator(Locale.GERMANY);
        assertTrue(de.localeZones().contains(de.generate()));
    }

    @Test
    @DisplayName("locale with no country falls back to global zones and empty locale zones")
    void localeWithoutCountryFallback() {
        TimezoneGenerator languageOnly = new TimezoneGenerator(Locale.ENGLISH);
        assertTrue(languageOnly.localeZones().isEmpty());
        assertDoesNotThrow(() -> ZoneId.of(languageOnly.generate()));
    }

    @Test
    @DisplayName("generateOffset returns offset id")
    void offset() {
        String offset = new TimezoneGenerator(Locale.UK).generateOffset();
        assertTrue(offset.equals("Z") || offset.matches("[+-]\\d{2}:\\d{2}"));
    }

    @Test
    @DisplayName("timezone aliases delegate to core methods")
    void aliases() {
        GeneratorConfig config = GeneratorConfig.builder().seed(11L).locale(Locale.US).build();
        TimezoneGenerator alias = new TimezoneGenerator(config);
        TimezoneGenerator core = new TimezoneGenerator(config);
        assertEquals(core.generate(), alias.generateTimezone());
        assertEquals(core.generateOffset(), alias.generateUtcOffset());
    }

    @Test
    @DisplayName("seeded generation is reproducible")
    void seeded() {
        GeneratorConfig config = GeneratorConfig.builder().seed(77L).locale(Locale.US).build();
        TimezoneGenerator a = new TimezoneGenerator(config);
        TimezoneGenerator b = new TimezoneGenerator(config);
        assertEquals(a.generate(), b.generate());
    }

    @Test
    @DisplayName("null config is rejected")
    void nullConfig() {
        assertThrows(NullPointerException.class, () -> new TimezoneGenerator((GeneratorConfig) null));
    }
}
