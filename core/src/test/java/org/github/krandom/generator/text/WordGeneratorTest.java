/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package org.github.krandom.generator.text;

import org.github.krandom.generator.GeneratorConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("WordGenerator")
class WordGeneratorTest {

    @Test
    @DisplayName("constructors validate config")
    void constructors() {
        assertThrows(NullPointerException.class, () -> new WordGenerator(null));
        assertNotNull(new WordGenerator().generate());
        assertEquals(Locale.US, new WordGenerator().getLocale());
    }

    @Test
    @DisplayName("generate() returns lowercase alphabetic word")
    void generateDefault() {
        String word = new WordGenerator().generate();
        assertTrue(word.matches("[a-z]+"), "word should be lowercase alphabetic: " + word);
    }

    @Test
    @DisplayName("generate(syllables) validates input")
    void generateSyllablesValidation() {
        WordGenerator gen = new WordGenerator();
        assertThrows(IllegalArgumentException.class, () -> gen.generate(0));
        assertThrows(IllegalArgumentException.class, () -> gen.generate(-1));
    }

    @Test
    @DisplayName("generate(syllables) tends to produce longer words for more syllables")
    void generateSyllables() {
        WordGenerator gen = new WordGenerator(GeneratorConfig.builder().seed(42L).build());
        String one = gen.generate(1);
        String four = gen.generate(4);
        assertTrue(one.matches("[a-z]+"));
        assertTrue(four.matches("[a-z]+"));
        assertTrue(four.length() >= one.length());
    }

    @Test
    @DisplayName("generateByLength(length) returns exact length")
    void generateByLength() {
        WordGenerator gen = new WordGenerator(GeneratorConfig.builder().seed(5L).build());
        String word = gen.generateByLength(10);
        assertEquals(10, word.length());
        assertTrue(word.matches("[a-z]+"));
    }

    @Test
    @DisplayName("generateByLength(length) validates input")
    void generateByLengthValidation() {
        WordGenerator gen = new WordGenerator();
        assertThrows(IllegalArgumentException.class, () -> gen.generateByLength(0));
        assertThrows(IllegalArgumentException.class, () -> gen.generateByLength(-3));
    }

    @Test
    @DisplayName("generateWords(count) and generateWords(min,max) support range-style generation")
    void generateWordsRangeStyle() {
        WordGenerator gen = new WordGenerator(GeneratorConfig.builder().seed(31L).build());
        String fixed = gen.generateWords(4);
        assertEquals(4, fixed.split(" ").length);

        for (int i = 0; i < 20; i++) {
            String ranged = gen.generateWords(3, 6);
            int count = ranged.split(" ").length;
            assertTrue(count >= 3 && count <= 6);
        }

        assertThrows(IllegalArgumentException.class, () -> gen.generateWords(0));
        assertThrows(IllegalArgumentException.class, () -> gen.generateWords(0, 2));
        assertThrows(IllegalArgumentException.class, () -> gen.generateWords(5, 4));
    }

    @Test
    @DisplayName("generate(options) validates null")
    void optionsNullValidation() {
        WordGenerator gen = new WordGenerator();
        assertThrows(NullPointerException.class, () -> gen.generate((WordGenerator.WordOptions) null));
    }

    @Test
    @DisplayName("WordOptions validates values")
    void optionsValidation() {
        assertThrows(IllegalArgumentException.class, () -> new WordGenerator.WordOptions(0, null));
        assertThrows(IllegalArgumentException.class, () -> new WordGenerator.WordOptions(null, 0));
    }

    @Test
    @DisplayName("generate(options) supports syllables and length")
    void generateWithOptions() {
        WordGenerator gen = new WordGenerator(GeneratorConfig.builder().seed(8L).build());
        String bySyllables = gen.generate(WordGenerator.WordOptions.withSyllables(3));
        String byLength = gen.generate(WordGenerator.WordOptions.withLength(12));
        String defaultOptions = gen.generate(new WordGenerator.WordOptions(null, null));

        assertTrue(bySyllables.matches("[a-z]+"));
        assertEquals(12, byLength.length());
        assertTrue(defaultOptions.matches("[a-z]+"));
    }

    @Test
    @DisplayName("length takes precedence when both length and syllables are set")
    void optionsPrecedence() {
        WordGenerator gen = new WordGenerator(GeneratorConfig.builder().seed(13L).build());
        String word = gen.generate(new WordGenerator.WordOptions(2, 7));
        assertEquals(7, word.length());
    }

    @Test
    @DisplayName("seeded generators are reproducible")
    void seededReproducibility() {
        GeneratorConfig cfg = GeneratorConfig.builder().seed(123L).build();
        WordGenerator a = new WordGenerator(cfg);
        WordGenerator b = new WordGenerator(cfg);

        for (int i = 0; i < 30; i++) {
            assertEquals(a.generate(), b.generate());
            assertEquals(a.generate(3), b.generate(3));
            assertEquals(a.generateByLength(8), b.generateByLength(8));
        }
    }

    @Test
    @DisplayName("same seed with different locales produces different sequence")
    void localeAffectsOutput() {
        GeneratorConfig us = GeneratorConfig.builder().seed(7L).locale(Locale.US).build();
        GeneratorConfig de = GeneratorConfig.builder().seed(7L).locale(Locale.GERMANY).build();
        WordGenerator usGen = new WordGenerator(us);
        WordGenerator deGen = new WordGenerator(de);

        StringBuilder usSeq = new StringBuilder();
        StringBuilder deSeq = new StringBuilder();
        for (int i = 0; i < 8; i++) {
            usSeq.append(usGen.generate()).append('|');
            deSeq.append(deGen.generate()).append('|');
        }
        assertNotEquals(usSeq.toString(), deSeq.toString());
    }

    @Test
    @DisplayName("language fallback applies for unsupported country variant")
    void languageFallback() {
        GeneratorConfig esEs = GeneratorConfig.builder().seed(11L).locale(Locale.of("es", "ES")).build();
        GeneratorConfig esMx = GeneratorConfig.builder().seed(11L).locale(Locale.of("es", "MX")).build();
        WordGenerator a = new WordGenerator(esEs);
        WordGenerator b = new WordGenerator(esMx);

        for (int i = 0; i < 20; i++) {
            assertEquals(a.generate(), b.generate());
        }
    }

    @Test
    @DisplayName("unknown language falls back to default English profile")
    void unknownLanguageFallback() {
        GeneratorConfig unknown = GeneratorConfig.builder().seed(19L).locale(Locale.of("xx", "YY")).build();
        GeneratorConfig english = GeneratorConfig.builder().seed(19L).locale(Locale.US).build();
        WordGenerator a = new WordGenerator(unknown);
        WordGenerator b = new WordGenerator(english);

        for (int i = 0; i < 20; i++) {
            assertEquals(a.generate(), b.generate());
        }
    }

    @Test
    @DisplayName("language-only locale resolves without country branch")
    void languageOnlyLocale() {
        GeneratorConfig deLangOnly = GeneratorConfig.builder().seed(23L).locale(Locale.of("de")).build();
        GeneratorConfig deExact = GeneratorConfig.builder().seed(23L).locale(Locale.GERMANY).build();
        WordGenerator a = new WordGenerator(deLangOnly);
        WordGenerator b = new WordGenerator(deExact);

        for (int i = 0; i < 20; i++) {
            assertEquals(a.generate(), b.generate());
        }
    }
}
