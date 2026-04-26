/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.text;

import io.github.frikit.krandom.generator.GeneratorConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("SyllableGenerator")
class SyllableGeneratorTest {

    @Test
    @DisplayName("constructors validate config")
    void constructors() {
        assertThrows(NullPointerException.class, () -> new SyllableGenerator(null));
        SyllableGenerator gen = new SyllableGenerator();
        assertNotNull(gen.generate());
        assertEquals(Locale.US, gen.getLocale());
    }

    @Test
    @DisplayName("generate returns lowercase alphabetic syllable")
    void generateDefault() {
        String value = new SyllableGenerator().generate();
        assertTrue(value.matches("[a-z]+"));
    }

    @Test
    @DisplayName("generate(length) validates input")
    void generateLengthValidation() {
        SyllableGenerator gen = new SyllableGenerator();
        assertThrows(IllegalArgumentException.class, () -> gen.generate(0));
        assertThrows(IllegalArgumentException.class, () -> gen.generate(-1));
    }

    @Test
    @DisplayName("generate(length) returns exact length")
    void generateLength() {
        SyllableGenerator gen = new SyllableGenerator(GeneratorConfig.builder().seed(5L).build());
        String value = gen.generate(6);
        assertEquals(6, value.length());
        assertTrue(value.matches("[a-z]+"));
    }

    @Test
    @DisplayName("generate(length) supports exact first syllable length path")
    void generateLengthEqualsFirstSyllable() {
        GeneratorConfig cfg = GeneratorConfig.builder().seed(123L).build();
        SyllableGenerator probe = new SyllableGenerator(cfg);
        int firstLength = probe.generate().length();

        SyllableGenerator gen = new SyllableGenerator(cfg);
        String value = gen.generate(firstLength);
        assertEquals(firstLength, value.length());
    }

    @Test
    @DisplayName("generate(length) covers exact-fit branch without truncation")
    void generateLengthExactFitNoTruncation() throws Exception {
        SyllableGenerator gen = new SyllableGenerator(GeneratorConfig.builder().seed(1L).build());
        Field phoneticsField = SyllableGenerator.class.getDeclaredField("phonetics");
        phoneticsField.setAccessible(true);
        phoneticsField.set(gen, new WordPhonetics(new String[] { "" }, new String[] { "a" }, new String[] { "" }));

        String value = gen.generate(3);
        assertEquals("aaa", value);
    }

    @Test
    @DisplayName("seeded generators are reproducible")
    void seededReproducibility() {
        GeneratorConfig cfg = GeneratorConfig.builder().seed(44L).build();
        SyllableGenerator a = new SyllableGenerator(cfg);
        SyllableGenerator b = new SyllableGenerator(cfg);

        for (int i = 0; i < 40; i++) {
            assertEquals(a.generate(), b.generate());
            assertEquals(a.generate(5), b.generate(5));
        }
    }

    @Test
    @DisplayName("same seed with different locales produces different sequence")
    void localeAffectsOutput() {
        SyllableGenerator us = new SyllableGenerator(
            GeneratorConfig.builder().seed(7L).locale(Locale.US).build()
        );
        SyllableGenerator de = new SyllableGenerator(
            GeneratorConfig.builder().seed(7L).locale(Locale.GERMANY).build()
        );

        StringBuilder usSeq = new StringBuilder();
        StringBuilder deSeq = new StringBuilder();
        for (int i = 0; i < 12; i++) {
            usSeq.append(us.generate()).append('|');
            deSeq.append(de.generate()).append('|');
        }
        assertNotEquals(usSeq.toString(), deSeq.toString());
    }

    @Test
    @DisplayName("language fallback applies for unsupported country variant")
    void languageFallback() {
        SyllableGenerator esEs = new SyllableGenerator(
            GeneratorConfig.builder().seed(11L).locale(Locale.of("es", "ES")).build()
        );
        SyllableGenerator esMx = new SyllableGenerator(
            GeneratorConfig.builder().seed(11L).locale(Locale.of("es", "MX")).build()
        );

        for (int i = 0; i < 20; i++) {
            assertEquals(esEs.generate(), esMx.generate());
        }
    }

    @Test
    @DisplayName("unknown language falls back to default English profile")
    void unknownLanguageFallback() {
        SyllableGenerator unknown = new SyllableGenerator(
            GeneratorConfig.builder().seed(19L).locale(Locale.of("xx", "YY")).build()
        );
        SyllableGenerator english = new SyllableGenerator(
            GeneratorConfig.builder().seed(19L).locale(Locale.US).build()
        );

        for (int i = 0; i < 20; i++) {
            assertEquals(unknown.generate(), english.generate());
        }
    }

    @Test
    @DisplayName("language-only locale resolves without country branch")
    void languageOnlyLocale() {
        SyllableGenerator deLangOnly = new SyllableGenerator(
            GeneratorConfig.builder().seed(23L).locale(Locale.of("de")).build()
        );
        SyllableGenerator deExact = new SyllableGenerator(
            GeneratorConfig.builder().seed(23L).locale(Locale.GERMANY).build()
        );

        for (int i = 0; i < 20; i++) {
            assertEquals(deLangOnly.generate(), deExact.generate());
        }
    }
}
