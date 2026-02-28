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

@DisplayName("SentenceGenerator")
class SentenceGeneratorTest {

    @Test
    @DisplayName("constructors validate config")
    void constructors() {
        assertThrows(NullPointerException.class, () -> new SentenceGenerator(null));
        SentenceGenerator gen = new SentenceGenerator();
        assertNotNull(gen.generate());
        assertEquals(Locale.US, gen.getLocale());
    }

    @Test
    @DisplayName("generate() produces sentence with 12-18 words")
    void generateDefaultRange() {
        SentenceGenerator gen = new SentenceGenerator(GeneratorConfig.builder().seed(2L).build());
        String sentence = gen.generate();
        assertTrue(Character.isUpperCase(sentence.charAt(0)));
        assertTrue(sentence.endsWith("."));
        int words = sentence.substring(0, sentence.length() - 1).split(" ").length;
        assertTrue(words >= 12 && words <= 18);
    }

    @Test
    @DisplayName("generate(words) validates input")
    void generateWordsValidation() {
        SentenceGenerator gen = new SentenceGenerator();
        assertThrows(IllegalArgumentException.class, () -> gen.generate(0));
        assertThrows(IllegalArgumentException.class, () -> gen.generate(-1));
    }

    @Test
    @DisplayName("generate(words) respects exact count")
    void generateWordsCount() {
        SentenceGenerator gen = new SentenceGenerator(GeneratorConfig.builder().seed(5L).build());
        String sentence = gen.generate(5);
        assertTrue(sentence.endsWith("."));
        assertEquals(5, sentence.substring(0, sentence.length() - 1).split(" ").length);
    }

    @Test
    @DisplayName("generate(options) supports words option")
    void generateOptions() {
        SentenceGenerator gen = new SentenceGenerator(GeneratorConfig.builder().seed(7L).build());
        String fixed = gen.generate(SentenceGenerator.SentenceOptions.withWords(4));
        assertEquals(4, fixed.substring(0, fixed.length() - 1).split(" ").length);
        assertNotNull(gen.generate(new SentenceGenerator.SentenceOptions(null)));
    }

    @Test
    @DisplayName("generate(options) validates null")
    void generateOptionsNullValidation() {
        SentenceGenerator gen = new SentenceGenerator();
        assertThrows(NullPointerException.class, () -> gen.generate((SentenceGenerator.SentenceOptions) null));
    }

    @Test
    @DisplayName("SentenceOptions validates values")
    void optionsValidation() {
        assertThrows(IllegalArgumentException.class, () -> new SentenceGenerator.SentenceOptions(0));
    }

    @Test
    @DisplayName("seeded generators are reproducible")
    void seededReproducibility() {
        GeneratorConfig cfg = GeneratorConfig.builder().seed(17L).locale(Locale.GERMANY).build();
        SentenceGenerator a = new SentenceGenerator(cfg);
        SentenceGenerator b = new SentenceGenerator(cfg);
        for (int i = 0; i < 20; i++) {
            assertEquals(a.generate(), b.generate());
            assertEquals(a.generate(6), b.generate(6));
        }
    }
}
