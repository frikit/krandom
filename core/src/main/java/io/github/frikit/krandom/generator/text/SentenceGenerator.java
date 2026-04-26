/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.text;

import io.github.frikit.krandom.generator.Generator;
import io.github.frikit.krandom.generator.GeneratorConfig;

import java.security.SecureRandom;
import java.util.Locale;
import java.util.Objects;
import java.util.Random;

/**
 * Generates locale-aware pseudo-natural sentences.
 *
 * <p>Default sentence length is 12-18 words to mirror Chance.js defaults.
 */
public final class SentenceGenerator implements Generator<String> {

    private final Locale        locale;
    private final Random        random;
    private final WordGenerator wordGenerator;

    /**
     * Creates a sentence generator with default configuration.
     */
    public SentenceGenerator() {
        this(GeneratorConfig.defaults());
    }

    /**
     * Creates a sentence generator with explicit configuration.
     *
     * @param config generator configuration; must not be null
     */
    public SentenceGenerator(GeneratorConfig config) {
        Objects.requireNonNull(config, "config must not be null");
        this.locale = config.getLocale();
        this.random = config.createRandom();
        this.wordGenerator = new WordGenerator(config);
    }

    /**
     * Generates a sentence with 12-18 words.
     */
    @Override
    public String generate() {
        return generate(random.nextInt(7) + 12); // [12, 18]
    }

    /**
     * Generates a sentence with exactly {@code words} words.
     *
     * @param words word count; must be positive
     * @return sentence with capitalization and trailing period
     */
    public String generate(int words) {
        if (words <= 0) {
            throw new IllegalArgumentException("words must be positive, got: " + words);
        }
        StringBuilder sb = new StringBuilder(words * 8);
        for (int i = 0; i < words; i++) {
            if (i > 0) {
                sb.append(' ');
            }
            sb.append(wordGenerator.generate());
        }
        sb.setCharAt(0, Character.toUpperCase(sb.charAt(0)));
        sb.append('.');
        return sb.toString();
    }

    /**
     * Generates multiple sentences and joins them with a single space.
     *
     * @param count number of sentences; must be positive
     * @return multi-sentence text
     */
    public String generateSentences(int count) {
        if (count <= 0) {
            throw new IllegalArgumentException("count must be positive, got: " + count);
        }
        StringBuilder sb = new StringBuilder(count * 96);
        for (int i = 0; i < count; i++) {
            if (i > 0) {
                sb.append(' ');
            }
            sb.append(generate());
        }
        return sb.toString();
    }

    /**
     * Generates multiple sentences where sentence count is sampled from the inclusive range
     * [{@code minSentences}, {@code maxSentences}].
     *
     * @param minSentences minimum sentence count (inclusive); must be positive
     * @param maxSentences maximum sentence count (inclusive); must be >= minSentences
     * @return multi-sentence text
     */
    public String generateSentences(int minSentences, int maxSentences) {
        if (minSentences <= 0) {
            throw new IllegalArgumentException("minSentences must be positive, got: " + minSentences);
        }
        if (maxSentences < minSentences) {
            throw new IllegalArgumentException("maxSentences must be >= minSentences, got: "
                                               + maxSentences + " < " + minSentences);
        }
        int count = minSentences + random.nextInt(maxSentences - minSentences + 1);
        return generateSentences(count);
    }

    /**
     * Generates a sentence with an option bag similar to Chance.js {@code sentence({words})}.
     *
     * @param options option bag; must not be null
     * @return generated sentence
     */
    public String generate(SentenceOptions options) {
        Objects.requireNonNull(options, "options must not be null");
        return options.words() == null ? generate() : generate(options.words());
    }

    /**
     * Generates a sentence.
     * Alias for {@link #generate()}.
     *
     * @return generated sentence
     */
    public String generateSentence() {
        return generate();
    }

    /**
     * Returns the configured locale.
     */
    public Locale getLocale() {
        return locale;
    }

    /**
     * Option bag for sentence generation.
     *
     * @param words optional exact word count (positive)
     */
    public record SentenceOptions(Integer words) {

        public SentenceOptions {
            if (words != null && words <= 0) {
                throw new IllegalArgumentException("words must be positive, got: " + words);
            }
        }

        /**
         * Creates options with fixed word count.
         */
        public static SentenceOptions withWords(int words) {
            return new SentenceOptions(words);
        }
    }
}
