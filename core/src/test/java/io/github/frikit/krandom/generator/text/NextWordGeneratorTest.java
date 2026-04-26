/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.text;

import io.github.frikit.krandom.generator.GeneratorConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("NextWordGenerator")
class NextWordGeneratorTest {

    private static final String[] CORPUS = { "the", "quick", "brown", "fox", "jumps", "over", "the", "lazy", "dog" };

    @Test
    @DisplayName("constructors validate inputs")
    void constructorValidation() {
        assertThrows(NullPointerException.class, () -> new NextWordGenerator((String[]) null));
        assertThrows(NullPointerException.class, () -> new NextWordGenerator(null, CORPUS));
        assertThrows(IllegalArgumentException.class, () -> new NextWordGenerator(new String[] { "onlyone" }));
        assertThrows(IllegalArgumentException.class, () -> new NextWordGenerator(new String[] { "ok", " " }));
    }

    @Test
    @DisplayName("generate returns corpus word")
    void generateReturnsCorpusWord() {
        NextWordGenerator gen = new NextWordGenerator(GeneratorConfig.builder().seed(1L).build(), CORPUS);
        String word = gen.generate();
        assertTrue(new HashSet<>(List.of(CORPUS)).contains(word));
    }

    @Test
    @DisplayName("generateNext follows known transitions")
    void generateNextFollowsTransition() {
        NextWordGenerator gen = new NextWordGenerator(GeneratorConfig.builder().seed(2L).build(), CORPUS);
        // In this corpus, "quick" is followed only by "brown"
        assertEquals("brown", gen.generateNext("quick"));
    }

    @Test
    @DisplayName("generateNext unknown word falls back to starter")
    void generateNextUnknownFallback() {
        NextWordGenerator gen = new NextWordGenerator(GeneratorConfig.builder().seed(3L).build(), CORPUS);
        String next = gen.generateNext("unknown");
        assertTrue(new HashSet<>(List.of(CORPUS)).contains(next));
    }

    @Test
    @DisplayName("generateNext validates currentWord")
    void generateNextValidation() {
        NextWordGenerator gen = new NextWordGenerator(CORPUS);
        assertThrows(IllegalArgumentException.class, () -> gen.generateNext(null));
        assertThrows(IllegalArgumentException.class, () -> gen.generateNext(" "));
    }

    @Test
    @DisplayName("generateWordSequence returns requested size")
    void generateWordSequenceCount() {
        NextWordGenerator gen = new NextWordGenerator(GeneratorConfig.builder().seed(4L).build(), CORPUS);
        List<String> words = gen.generateWordSequence(7);
        assertEquals(7, words.size());
    }

    @Test
    @DisplayName("generateWordSequence validates count")
    void generateWordSequenceValidation() {
        NextWordGenerator gen = new NextWordGenerator(CORPUS);
        assertThrows(IllegalArgumentException.class, () -> gen.generateWordSequence(0));
    }

    @Test
    @DisplayName("generateSentence format and word count")
    void generateSentenceFormat() {
        NextWordGenerator gen = new NextWordGenerator(GeneratorConfig.builder().seed(5L).build(), CORPUS);
        String sentence = gen.generateSentence(5);
        assertTrue(Character.isUpperCase(sentence.charAt(0)));
        assertTrue(sentence.endsWith("."));
        assertEquals(5, sentence.substring(0, sentence.length() - 1).split(" ").length);
    }

    @Test
    @DisplayName("generateSentence validates count")
    void generateSentenceValidation() {
        NextWordGenerator gen = new NextWordGenerator(CORPUS);
        assertThrows(IllegalArgumentException.class, () -> gen.generateSentence(-1));
    }

    @Test
    @DisplayName("seeded generators are reproducible")
    void seededReproducibility() {
        GeneratorConfig cfg = GeneratorConfig.builder().seed(99L).build();
        NextWordGenerator a = new NextWordGenerator(cfg, CORPUS);
        NextWordGenerator b = new NextWordGenerator(cfg, CORPUS);
        for (int i = 0; i < 20; i++) {
            assertEquals(a.generate(), b.generate());
            assertEquals(a.generateNext("the"), b.generateNext("the"));
        }
    }

    @Test
    @DisplayName("metadata accessors return expected values")
    void metadataAccessors() {
        NextWordGenerator gen = new NextWordGenerator(CORPUS);
        assertEquals(CORPUS.length, gen.getCorpusWords().length);
        assertTrue(gen.getTransitionKeyCount() > 0);
    }

    @Test
    @DisplayName("fromText builds working generator")
    void fromText() {
        NextWordGenerator gen = NextWordGenerator.fromText("Hello, world! Hello data world.");
        assertNotNull(gen.generate());
    }

    @Test
    @DisplayName("fromText validation")
    void fromTextValidation() {
        assertThrows(IllegalArgumentException.class, () -> NextWordGenerator.fromText(null));
        assertThrows(IllegalArgumentException.class, () -> NextWordGenerator.fromText(" "));
    }
}
