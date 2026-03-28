/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package org.github.krandom.generator.text;

import org.github.krandom.generator.Generator;
import org.github.krandom.generator.GeneratorConfig;

import java.security.SecureRandom;
import java.util.Objects;
import java.util.Random;

/**
 * Generates Lorem Ipsum placeholder text as individual words, sentences, or paragraphs.
 *
 * <p>The generation mode is controlled by {@link Mode}. The default mode is
 * {@link Mode#SENTENCE}: {@link #generate()} returns a randomly assembled sentence
 * from the classic Lorem Ipsum corpus.
 *
 * <pre>{@code
 * LoremIpsumGenerator words      = new LoremIpsumGenerator(LoremIpsumGenerator.Mode.WORD);
 * LoremIpsumGenerator sentences  = new LoremIpsumGenerator();        // default: SENTENCE
 * LoremIpsumGenerator paragraphs = new LoremIpsumGenerator(LoremIpsumGenerator.Mode.PARAGRAPH);
 *
 * String word  = words.generate();       // "lorem"
 * String sent  = sentences.generate();   // "Lorem ipsum dolor sit amet..."
 * String para  = paragraphs.generate();  // Multiple sentences joined by spaces.
 *
 * // Explicit helpers (mode-independent):
 * String w     = sentences.generateWord();
 * String s3    = sentences.generateSentence(3);    // exactly 3 words
 * String p4    = sentences.generateParagraph(4);   // 4 sentences
 * }</pre>
 *
 * <p><strong>Seeded Generation:</strong>
 * <pre>{@code
 * GeneratorConfig config = GeneratorConfig.builder().seed(42L).build();
 * LoremIpsumGenerator gen = new LoremIpsumGenerator(config);
 * String sentence = gen.generate();  // Reproducible output
 * }</pre>
 */
public final class LoremIpsumGenerator implements Generator<String> {

    private static final String[] WORDS           = {
        "lorem", "ipsum", "dolor", "sit", "amet", "consectetur", "adipiscing", "elit",
        "sed", "do", "eiusmod", "tempor", "incididunt", "ut", "labore", "dolore",
        "magna", "aliqua", "enim", "ad", "minim", "veniam", "quis", "nostrud",
        "exercitation", "ullamco", "laboris", "nisi", "aliquip", "ex", "ea", "commodo",
        "consequat", "duis", "aute", "irure", "reprehenderit", "voluptate", "velit",
        "esse", "cillum", "fugiat", "nulla", "pariatur", "excepteur", "sint", "occaecat",
        "cupidatat", "proident", "sunt", "culpa", "qui", "officia", "deserunt", "mollit",
        "anim", "id", "est", "laborum"
    };
    private static final String[] CORPUS_SEQUENCE = (
        "lorem ipsum dolor sit amet consectetur adipiscing elit sed do eiusmod tempor incididunt ut labore et dolore " +
        "magna aliqua ut enim ad minim veniam quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat " +
        "duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur excepteur sint occaecat " +
        "cupidatat non proident sunt in culpa qui officia deserunt mollit anim id est laborum"
    ).split("\\s+");
    private final Mode              mode;
    private final GeneratorConfig   config;
    private final Random            random;
    private final NextWordGenerator nextWordGenerator;
    /**
     * Creates a generator in {@link Mode#SENTENCE} mode with default configuration.
     */
    public LoremIpsumGenerator() {
        this(Mode.SENTENCE, GeneratorConfig.defaults());
    }

    /**
     * Creates a generator with the specified mode and default configuration.
     *
     * @param mode generation mode; must not be {@code null}
     * @throws NullPointerException if {@code mode} is {@code null}
     */
    public LoremIpsumGenerator(Mode mode) {
        this(mode, GeneratorConfig.defaults());
    }

    /**
     * Creates a generator in {@link Mode#SENTENCE} mode with the specified configuration.
     *
     * @param config the generator configuration; must not be {@code null}
     * @throws NullPointerException if {@code config} is {@code null}
     */
    public LoremIpsumGenerator(GeneratorConfig config) {
        this(Mode.SENTENCE, config);
    }

    /**
     * Creates a generator with the specified mode and configuration.
     *
     * @param mode   generation mode; must not be {@code null}
     * @param config the generator configuration; must not be {@code null}
     * @throws NullPointerException if either argument is {@code null}
     */
    public LoremIpsumGenerator(Mode mode, GeneratorConfig config) {
        this.mode = Objects.requireNonNull(mode, "mode must not be null");
        this.config = Objects.requireNonNull(config, "config must not be null");
        this.random = config.getSeed().isPresent()
                      ? new Random(config.getSeed().getAsLong())
                      : new SecureRandom();
        this.nextWordGenerator = new NextWordGenerator(config, CORPUS_SEQUENCE);
    }

    /**
     * {@inheritDoc}
     *
     * <p>Generates text according to the configured {@link Mode}.
     *
     * @return generated text; never {@code null}
     */
    @Override
    public String generate() {
        return switch (mode) {
            case WORD -> generateWord();
            case SENTENCE -> generateSentence();
            case PARAGRAPH -> generateParagraph();
        };
    }

    /**
     * Returns a single Lorem Ipsum word (always lowercase).
     *
     * @return a word from the Lorem Ipsum corpus; never {@code null}
     */
    public String generateWord() {
        return WORDS[random.nextInt(WORDS.length)];
    }

    /**
     * Returns a sentence of 5–15 randomly-chosen Lorem Ipsum words,
     * capitalised at the start and terminated with a period.
     *
     * @return a sentence string; never {@code null}
     */
    public String generateSentence() {
        return generateSentence(random.nextInt(11) + 5);  // [5, 15]
    }

    /**
     * Returns a sentence of exactly {@code wordCount} Lorem Ipsum words,
     * capitalised at the start and terminated with a period.
     *
     * @param wordCount number of words; must be positive
     * @return a sentence string; never {@code null}
     * @throws IllegalArgumentException if {@code wordCount} is not positive
     */
    public String generateSentence(int wordCount) {
        return nextWordGenerator.generateSentence(wordCount);
    }

    /**
     * Returns a paragraph of 3–6 sentences.
     *
     * @return a paragraph string; never {@code null}
     */
    public String generateParagraph() {
        return generateParagraph(random.nextInt(4) + 3);  // [3, 6]
    }

    /**
     * Returns a paragraph of exactly {@code sentenceCount} sentences.
     *
     * @param sentenceCount number of sentences; must be positive
     * @return a paragraph string; never {@code null}
     * @throws IllegalArgumentException if {@code sentenceCount} is not positive
     */
    public String generateParagraph(int sentenceCount) {
        if (sentenceCount <= 0) {
            throw new IllegalArgumentException("sentenceCount must be positive, got: " + sentenceCount);
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < sentenceCount; i++) {
            if (i > 0) sb.append(' ');
            sb.append(generateSentence());
        }
        return sb.toString();
    }

    /**
     * Returns the {@link Mode} this generator was configured with.
     */
    public Mode getMode() {
        return mode;
    }

    /**
     * Controls what {@link #generate()} produces.
     */
    public enum Mode {WORD, SENTENCE, PARAGRAPH}
}
