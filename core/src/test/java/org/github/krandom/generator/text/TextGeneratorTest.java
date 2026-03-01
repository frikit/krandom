/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package org.github.krandom.generator.text;

import org.github.krandom.generator.GeneratorConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("TextGenerator")
class TextGeneratorTest {

    @Test
    @DisplayName("generate(maxChars) respects character limit")
    void maxChars() {
        String text = new TextGenerator(Locale.US).generate(80);
        assertTrue(text.length() <= 80);
        assertTrue(text.endsWith("."));
    }

    @Test
    @DisplayName("generateTexts returns requested count")
    void textsCount() {
        List<String> values = new TextGenerator().generateTexts(3, 60);
        assertEquals(3, values.size());
    }

    @Test
    @DisplayName("generateTexts rejects non-positive count")
    void invalidCount() {
        assertThrows(IllegalArgumentException.class, () -> new TextGenerator().generateTexts(0, 20));
    }

    @Test
    @DisplayName("options support ext word list and unique words")
    void options() {
        TextGenerator generator = new TextGenerator();
        TextGenerator.TextOptions options = new TextGenerator.TextOptions(
                120,
                List.of("apple", "banana", "cherry", "date"),
                true,
                false
        );
        String text = generator.generate(options);
        assertTrue(text.contains("apple") || text.contains("banana") || text.contains("cherry") || text.contains("date"));
    }

    @Test
    @DisplayName("seeded generation is reproducible")
    void seeded() {
        GeneratorConfig cfg = GeneratorConfig.builder().seed(555L).locale(Locale.US).build();
        TextGenerator a = new TextGenerator(cfg);
        TextGenerator b = new TextGenerator(cfg);
        assertEquals(a.generate(90), b.generate(90));
    }

    @Test
    @DisplayName("default locale vocabulary affects output")
    void localeVocabulary() {
        GeneratorConfig usCfg = GeneratorConfig.builder().seed(123L).locale(Locale.US).build();
        GeneratorConfig deCfg = GeneratorConfig.builder().seed(123L).locale(Locale.GERMANY).build();
        String us = new TextGenerator(usCfg).generate(80);
        String de = new TextGenerator(deCfg).generate(80);
        assertNotEquals(us, de);
    }

    @Test
    @DisplayName("unknown locale language falls back to default vocabulary")
    void unknownLocaleFallback() {
        GeneratorConfig cfg = GeneratorConfig.builder().seed(321L).locale(Locale.of("ru", "RU")).build();
        String text = new TextGenerator(cfg).generate(80);
        assertTrue(text.contains("alpha")
                || text.contains("beta")
                || text.contains("gamma")
                || text.contains("delta"));
    }

    @Test
    @DisplayName("invalid options throw")
    void invalid() {
        assertThrows(IllegalArgumentException.class, () -> new TextGenerator.TextOptions(0, null, false, true));
        assertThrows(NullPointerException.class, () -> new TextGenerator((GeneratorConfig) null));
        assertThrows(NullPointerException.class, () -> new TextGenerator().generate((TextGenerator.TextOptions) null));
    }

    @Test
    @DisplayName("empty external word list falls back to defaults")
    void emptyExtListFallsBack() {
        TextGenerator generator = new TextGenerator(GeneratorConfig.builder().seed(11L).locale(Locale.US).build());
        String text = generator.generate(new TextGenerator.TextOptions(40, List.of(), false, true));
        assertTrue(text.endsWith("."));
        assertFalse(text.isBlank());
    }

    @Test
    @DisplayName("locale fallback word is used when selected words are empty")
    void emptyWordFallbackByLocale() {
        TextGenerator de = new TextGenerator(Locale.GERMANY);
        TextGenerator us = new TextGenerator(Locale.US);
        TextGenerator.TextOptions options = new TextGenerator.TextOptions(5, List.of(""), true, false);

        assertEquals("text.", de.generate(options));
        assertEquals("lore.", us.generate(options));
    }

    @Test
    @DisplayName("text generation keeps existing trailing dot and supports unique completion modes")
    void trailingDotAndUniqueBranches() {
        TextGenerator generator = new TextGenerator();

        String dotted = generator.generate(new TextGenerator.TextOptions(40, List.of("."), true, false));
        assertTrue(dotted.endsWith("."));

        String enoughUniqueWords = generator.generate(new TextGenerator.TextOptions(
                120,
                List.of("w1", "w2", "w3", "w4", "w5", "w6", "w7", "w8", "w9", "w10"),
                true,
                false
        ));
        assertTrue(enoughUniqueWords.endsWith("."));
    }
}
