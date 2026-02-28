/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package org.github.krandom.generator.text;

import org.github.krandom.generator.Generator;
import org.github.krandom.generator.GeneratorConfig;

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

    private final Locale locale;
    private final Random random;
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
        this.random = config.getSeed().isPresent()
                ? new Random(config.getSeed().getAsLong())
                : new SecureRandom();
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
