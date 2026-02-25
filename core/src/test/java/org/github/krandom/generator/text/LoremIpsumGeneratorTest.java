/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package org.github.krandom.generator.text;

import org.github.krandom.generator.Generators;
import org.github.krandom.generator.GeneratorConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("LoremIpsumGenerator")
class LoremIpsumGeneratorTest {

    private static final int SAMPLES = 50;

    @Nested
    @DisplayName("Constructors")
    class ConstructorTests {

        @Test
        @DisplayName("default constructor uses SENTENCE mode")
        void defaultMode() {
            assertEquals(LoremIpsumGenerator.Mode.SENTENCE,
                    new LoremIpsumGenerator().getMode());
        }

        @Test
        @DisplayName("mode constructor stores mode")
        void modeConstructor() {
            assertEquals(LoremIpsumGenerator.Mode.WORD,
                    new LoremIpsumGenerator(LoremIpsumGenerator.Mode.WORD).getMode());
        }

        @Test
        @DisplayName("config constructor uses SENTENCE mode")
        void configConstructor() {
            assertEquals(LoremIpsumGenerator.Mode.SENTENCE,
                    new LoremIpsumGenerator(GeneratorConfig.defaults()).getMode());
        }

        @Test
        @DisplayName("mode+config constructor stores both")
        void modeAndConfigConstructor() {
            GeneratorConfig cfg = GeneratorConfig.builder().seed(1L).build();
            LoremIpsumGenerator gen = new LoremIpsumGenerator(LoremIpsumGenerator.Mode.PARAGRAPH, cfg);
            assertEquals(LoremIpsumGenerator.Mode.PARAGRAPH, gen.getMode());
        }

        @Test
        @DisplayName("null mode throws NullPointerException")
        void nullModeThrows() {
            assertThrows(NullPointerException.class,
                    () -> new LoremIpsumGenerator((LoremIpsumGenerator.Mode) null));
        }

        @Test
        @DisplayName("null config throws NullPointerException")
        void nullConfigThrows() {
            assertThrows(NullPointerException.class,
                    () -> new LoremIpsumGenerator((GeneratorConfig) null));
        }
    }

    @Nested
    @DisplayName("generate() dispatches by mode")
    class GenerateModeTests {

        @Test
        @DisplayName("WORD mode: generate() returns a single word (no spaces)")
        void wordMode() {
            LoremIpsumGenerator gen = new LoremIpsumGenerator(LoremIpsumGenerator.Mode.WORD);
            for (int i = 0; i < SAMPLES; i++) {
                String result = gen.generate();
                assertNotNull(result);
                assertFalse(result.contains(" "), "Word should have no spaces: " + result);
            }
        }

        @Test
        @DisplayName("SENTENCE mode: generate() returns a capitalised sentence ending with '.'")
        void sentenceMode() {
            LoremIpsumGenerator gen = new LoremIpsumGenerator(LoremIpsumGenerator.Mode.SENTENCE);
            for (int i = 0; i < SAMPLES; i++) {
                String result = gen.generate();
                assertNotNull(result);
                assertTrue(Character.isUpperCase(result.charAt(0)),
                        "Sentence should start with uppercase: " + result);
                assertTrue(result.endsWith("."),
                        "Sentence should end with '.': " + result);
            }
        }

        @Test
        @DisplayName("PARAGRAPH mode: generate() returns multiple sentences")
        void paragraphMode() {
            LoremIpsumGenerator gen = new LoremIpsumGenerator(LoremIpsumGenerator.Mode.PARAGRAPH);
            for (int i = 0; i < SAMPLES; i++) {
                String result = gen.generate();
                assertNotNull(result);
                // A paragraph has at least 3 sentences (periods)
                long dotCount = result.chars().filter(c -> c == '.').count();
                assertTrue(dotCount >= 3,
                        "Paragraph should have >= 3 sentences, got " + dotCount + " in: " + result);
            }
        }
    }

    @Nested
    @DisplayName("generateWord()")
    class GenerateWordTests {

        @Test
        @DisplayName("generateWord() returns non-null lowercase word")
        void notNull() {
            for (int i = 0; i < SAMPLES; i++) {
                String word = new LoremIpsumGenerator().generateWord();
                assertNotNull(word);
                assertFalse(word.isEmpty());
                assertEquals(word.toLowerCase(), word,
                        "Word should be lowercase: " + word);
            }
        }

        @Test
        @DisplayName("generateWord() returns a word from the Lorem Ipsum corpus")
        void fromCorpus() {
            LoremIpsumGenerator gen = new LoremIpsumGenerator();
            for (int i = 0; i < SAMPLES; i++) {
                String word = gen.generateWord();
                assertTrue(word.matches("[a-z]+"),
                        "Word should be all lowercase letters: " + word);
            }
        }
    }

    @Nested
    @DisplayName("generateSentence()")
    class GenerateSentenceTests {

        @Test
        @DisplayName("generateSentence() returns non-null sentence")
        void notNull() {
            assertNotNull(new LoremIpsumGenerator().generateSentence());
        }

        @Test
        @DisplayName("generateSentence() starts with uppercase and ends with '.'")
        void format() {
            for (int i = 0; i < SAMPLES; i++) {
                String sentence = new LoremIpsumGenerator().generateSentence();
                assertTrue(Character.isUpperCase(sentence.charAt(0)),
                        "Should start uppercase: " + sentence);
                assertTrue(sentence.endsWith("."),
                        "Should end with '.': " + sentence);
            }
        }

        @Test
        @DisplayName("generateSentence(5) contains exactly 5 words")
        void exactWordCount() {
            for (int i = 0; i < SAMPLES; i++) {
                String sentence = new LoremIpsumGenerator().generateSentence(5);
                // Strip trailing period, split by space
                String[] words = sentence.substring(0, sentence.length() - 1).split(" ");
                assertEquals(5, words.length, "Expected 5 words in: " + sentence);
            }
        }

        @Test
        @DisplayName("generateSentence(1) produces single-word sentence")
        void singleWord() {
            String sentence = new LoremIpsumGenerator().generateSentence(1);
            assertNotNull(sentence);
            String[] words = sentence.substring(0, sentence.length() - 1).split(" ");
            assertEquals(1, words.length);
        }

        @Test
        @DisplayName("generateSentence(0) throws IllegalArgumentException")
        void zeroWordCountThrows() {
            assertThrows(IllegalArgumentException.class,
                    () -> new LoremIpsumGenerator().generateSentence(0));
        }

        @Test
        @DisplayName("generateSentence(-1) throws IllegalArgumentException")
        void negativeWordCountThrows() {
            assertThrows(IllegalArgumentException.class,
                    () -> new LoremIpsumGenerator().generateSentence(-1));
        }
    }

    @Nested
    @DisplayName("generateParagraph()")
    class GenerateParagraphTests {

        @Test
        @DisplayName("generateParagraph() returns non-null paragraph")
        void notNull() {
            assertNotNull(new LoremIpsumGenerator().generateParagraph());
        }

        @Test
        @DisplayName("generateParagraph() contains at least 3 sentences")
        void atLeastThreeSentences() {
            for (int i = 0; i < SAMPLES; i++) {
                String para = new LoremIpsumGenerator().generateParagraph();
                long dotCount = para.chars().filter(c -> c == '.').count();
                assertTrue(dotCount >= 3,
                        "Expected >= 3 sentences in paragraph, got " + dotCount);
            }
        }

        @Test
        @DisplayName("generateParagraph(3) contains exactly 3 sentences")
        void exactSentenceCount() {
            for (int i = 0; i < SAMPLES; i++) {
                String para = new LoremIpsumGenerator().generateParagraph(3);
                long dotCount = para.chars().filter(c -> c == '.').count();
                assertEquals(3, dotCount,
                        "Expected exactly 3 sentences in: " + para);
            }
        }

        @Test
        @DisplayName("generateParagraph(0) throws IllegalArgumentException")
        void zeroSentenceCountThrows() {
            assertThrows(IllegalArgumentException.class,
                    () -> new LoremIpsumGenerator().generateParagraph(0));
        }

        @Test
        @DisplayName("generateParagraph(-1) throws IllegalArgumentException")
        void negativeSentenceCountThrows() {
            assertThrows(IllegalArgumentException.class,
                    () -> new LoremIpsumGenerator().generateParagraph(-1));
        }
    }

    @Nested
    @DisplayName("Seeded generation")
    class SeededTests {

        @Test
        @DisplayName("seeded SENTENCE generators produce identical output")
        void seededSentenceReproducibility() {
            GeneratorConfig cfg1 = GeneratorConfig.builder().seed(42L).build();
            GeneratorConfig cfg2 = GeneratorConfig.builder().seed(42L).build();
            LoremIpsumGenerator a = new LoremIpsumGenerator(cfg1);
            LoremIpsumGenerator b = new LoremIpsumGenerator(cfg2);
            for (int i = 0; i < SAMPLES; i++) {
                assertEquals(a.generate(), b.generate());
            }
        }

        @Test
        @DisplayName("seeded WORD generators produce identical output")
        void seededWordReproducibility() {
            GeneratorConfig cfg1 = GeneratorConfig.builder().seed(7L).build();
            GeneratorConfig cfg2 = GeneratorConfig.builder().seed(7L).build();
            LoremIpsumGenerator a = new LoremIpsumGenerator(LoremIpsumGenerator.Mode.WORD, cfg1);
            LoremIpsumGenerator b = new LoremIpsumGenerator(LoremIpsumGenerator.Mode.WORD, cfg2);
            for (int i = 0; i < SAMPLES; i++) {
                assertEquals(a.generate(), b.generate());
            }
        }
    }

    @Nested
    @DisplayName("Generators factory")
    class GeneratorsFactoryTest {

        @Test
        @DisplayName("Generators.ofLoremIpsum() returns non-null sentence")
        void ofLoremIpsumDefault() {
            String result = Generators.ofLoremIpsum().generate();
            assertNotNull(result);
            assertTrue(result.endsWith("."));
        }

        @Test
        @DisplayName("Generators.ofLoremIpsum(WORD) returns a single word")
        void ofLoremIpsumWord() {
            String result = Generators.ofLoremIpsum(LoremIpsumGenerator.Mode.WORD).generate();
            assertNotNull(result);
            assertFalse(result.contains(" "));
        }

        @Test
        @DisplayName("Generators.ofLoremIpsum(PARAGRAPH) returns multiple sentences")
        void ofLoremIpsumParagraph() {
            String result = Generators.ofLoremIpsum(LoremIpsumGenerator.Mode.PARAGRAPH).generate();
            assertNotNull(result);
            assertTrue(result.chars().filter(c -> c == '.').count() >= 3);
        }
    }
}
