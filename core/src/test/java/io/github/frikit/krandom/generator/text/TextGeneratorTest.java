/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.text;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.frikit.krandom.generator.GeneratorConfig;
import java.time.Duration;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

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
    @DisplayName("default generate uses standard maxChars")
    void defaultGenerate() {
        String text = new TextGenerator(Locale.US).generate();
        assertTrue(text.length() > 0 && text.length() <= 200);
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
        TextGenerator.TextOptions options =
                new TextGenerator.TextOptions(120, List.of("apple", "banana", "cherry", "date"), true, false);
        String text = generator.generate(options);
        assertTrue(
                text.contains("apple") || text.contains("banana") || text.contains("cherry") || text.contains("date"));
    }

    @Test
    @DisplayName("seeded generation is reproducible")
    void seeded() {
        GeneratorConfig cfg =
                GeneratorConfig.builder().seed(555L).locale(Locale.US).build();
        TextGenerator a = new TextGenerator(cfg);
        TextGenerator b = new TextGenerator(cfg);
        assertEquals(a.generate(90), b.generate(90));
    }

    @Test
    @DisplayName("default locale vocabulary affects output")
    void localeVocabulary() {
        GeneratorConfig usCfg =
                GeneratorConfig.builder().seed(123L).locale(Locale.US).build();
        GeneratorConfig deCfg =
                GeneratorConfig.builder().seed(123L).locale(Locale.GERMANY).build();
        String us = new TextGenerator(usCfg).generate(80);
        String de = new TextGenerator(deCfg).generate(80);
        assertNotEquals(us, de);
    }

    @Test
    @DisplayName("unknown locale language falls back to default vocabulary")
    void unknownLocaleFallback() {
        GeneratorConfig cfg = GeneratorConfig.builder()
                .seed(321L)
                .locale(new Locale("is", "IS"))
                .build();
        String text = new TextGenerator(cfg).generate(80);
        assertTrue(text.contains("alpha") || text.contains("beta") || text.contains("gamma") || text.contains("delta"));
    }

    @Test
    @DisplayName("Russian locale uses Russian vocabulary")
    void russianVocabulary() {
        GeneratorConfig cfg = GeneratorConfig.builder()
                .seed(321L)
                .locale(new Locale("ru", "RU"))
                .build();
        String text = new TextGenerator(cfg).generate(80);
        assertTrue(
                text.codePoints().anyMatch(ch -> Character.UnicodeScript.of(ch) == Character.UnicodeScript.CYRILLIC),
                text);
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
        TextGenerator generator = new TextGenerator(
                GeneratorConfig.builder().seed(11L).locale(Locale.US).build());
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
                120, List.of("w1", "w2", "w3", "w4", "w5", "w6", "w7", "w8", "w9", "w10"), true, false));
        assertTrue(enoughUniqueWords.endsWith("."));
    }

    @Test
    @DisplayName("unique words from a vocabulary that repeats words end after its distinct words")
    void uniqueWordsFromRepeatedVocabulary() {
        TextGenerator generator = new TextGenerator(
                GeneratorConfig.builder().seed(1L).locale(Locale.US).build());

        // Two entries but one distinct word: the text used to wait forever for a second one.
        String single = assertTimeoutPreemptively(
                Duration.ofSeconds(10),
                () -> generator.generate(new TextGenerator.TextOptions(20, List.of("one", "one"), true, false)));
        assertEquals("one.", single);

        // Three distinct words among five entries, for texts of 8 and of 4 to 13 words: each word once.
        for (boolean variableWordCount : List.of(false, true)) {
            TextGenerator.TextOptions options =
                    new TextGenerator.TextOptions(120, List.of("a", "b", "a", "b", "c"), true, variableWordCount);
            String text = assertTimeoutPreemptively(Duration.ofSeconds(10), () -> generator.generate(options));
            List<String> words = List.of(text.substring(0, text.length() - 1).split(" "));
            assertEquals(Set.of("a", "b", "c"), Set.copyOf(words), text);
            assertEquals(3, words.size(), text);
        }
    }

    @Test
    @DisplayName("unique words keep their seeded output where the vocabulary has enough distinct words")
    void uniqueWordsKeepSeededOutput() {
        TextGenerator generator =
                new TextGenerator(GeneratorConfig.builder().seed(9L).build());
        List<String> words = List.of("w1", "w2", "w3", "w4", "w5", "w6", "w7", "w8", "w9", "w10");

        assertEquals(
                "w10 w7 w9 w6 w8 w4 w2 w5.",
                generator.generate(new TextGenerator.TextOptions(120, words, true, false)));
        assertEquals("w7 w3 w2 w5 w9.", generator.generate(new TextGenerator.TextOptions(120, words, true, true)));
        // Repeated words with at least as many distinct ones as the text needs worked before, and still do.
        assertEquals(
                "d h g a c f b e.",
                generator.generate(new TextGenerator.TextOptions(
                        120, List.of("a", "b", "c", "d", "e", "f", "g", "h", "a", "b"), true, false)));
    }

    @Test
    @DisplayName("the closing dot replaces a whole surrogate pair rather than its low half")
    void closingDotKeepsSurrogatePairsWhole() {
        TextGenerator generator =
                new TextGenerator(GeneratorConfig.builder().seed(9L).build());
        List<String> words = List.of("a\uD83D\uDE00");

        // The text fills maxChars and ends in a pair, which the dot replaces whole.
        assertEquals("a.", generator.generate(new TextGenerator.TextOptions(3, words, false, false)));
        // maxChars cuts the pair, and the dot replaces its high half.
        assertEquals("a.", generator.generate(new TextGenerator.TextOptions(2, words, false, false)));
        // The text ends in a pair with room to spare, and the dot follows it.
        assertEquals(
                String.join(" ", java.util.Collections.nCopies(8, "a\uD83D\uDE00")) + ".",
                generator.generate(new TextGenerator.TextOptions(40, words, false, false)));
        // A low surrogate on its own is replaced alone, as is any other last character.
        assertEquals("a.", generator.generate(new TextGenerator.TextOptions(2, List.of("a\uDE00"), false, false)));
        assertEquals(".", generator.generate(new TextGenerator.TextOptions(1, words, false, false)));
    }

    @Test
    @DisplayName("generateText alias delegates to generate")
    void generateTextAlias() {
        GeneratorConfig cfg =
                GeneratorConfig.builder().seed(202L).locale(Locale.US).build();
        TextGenerator alias = new TextGenerator(cfg);
        TextGenerator core = new TextGenerator(cfg);
        assertEquals(core.generate(), alias.generateText());
    }
}
