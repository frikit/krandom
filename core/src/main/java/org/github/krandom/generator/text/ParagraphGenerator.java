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
 * Generates locale-aware pseudo-natural paragraphs.
 *
 * <p>Default paragraph size is 3-7 sentences to mirror Chance.js defaults.
 */
public final class ParagraphGenerator implements Generator<String> {

    private final Locale            locale;
    private final Random            random;
    private final SentenceGenerator sentenceGenerator;

    /**
     * Creates a paragraph generator with default configuration.
     */
    public ParagraphGenerator() {
        this(GeneratorConfig.defaults());
    }

    /**
     * Creates a paragraph generator with explicit configuration.
     *
     * @param config generator configuration; must not be null
     */
    public ParagraphGenerator(GeneratorConfig config) {
        Objects.requireNonNull(config, "config must not be null");
        this.locale = config.getLocale();
        this.random = config.createRandom();
        this.sentenceGenerator = new SentenceGenerator(config);
    }

    /**
     * Generates a paragraph with 3-7 sentences.
     */
    @Override
    public String generate() {
        return generate(random.nextInt(5) + 3); // [3, 7]
    }

    /**
     * Generates a paragraph with exactly {@code sentences} sentences.
     *
     * @param sentences sentence count; must be positive
     * @return paragraph containing the requested sentence count
     */
    public String generate(int sentences) {
        if (sentences <= 0) {
            throw new IllegalArgumentException("sentences must be positive, got: " + sentences);
        }
        StringBuilder sb = new StringBuilder(sentences * 96);
        for (int i = 0; i < sentences; i++) {
            if (i > 0) {
                sb.append(' ');
            }
            sb.append(sentenceGenerator.generate());
        }
        return sb.toString();
    }

    /**
     * Generates multiple paragraphs and joins them with blank lines.
     *
     * @param count number of paragraphs; must be positive
     * @return multi-paragraph text separated by {@code "\n\n"}
     */
    public String generateParagraphs(int count) {
        if (count <= 0) {
            throw new IllegalArgumentException("count must be positive, got: " + count);
        }
        StringBuilder sb = new StringBuilder(count * 480);
        for (int i = 0; i < count; i++) {
            if (i > 0) {
                sb.append("\n\n");
            }
            sb.append(generate());
        }
        return sb.toString();
    }

    /**
     * Generates multiple paragraphs where paragraph count is sampled from the inclusive range
     * [{@code minParagraphs}, {@code maxParagraphs}].
     *
     * @param minParagraphs minimum paragraph count (inclusive); must be positive
     * @param maxParagraphs maximum paragraph count (inclusive); must be >= minParagraphs
     * @return multi-paragraph text separated by {@code "\n\n"}
     */
    public String generateParagraphs(int minParagraphs, int maxParagraphs) {
        if (minParagraphs <= 0) {
            throw new IllegalArgumentException("minParagraphs must be positive, got: " + minParagraphs);
        }
        if (maxParagraphs < minParagraphs) {
            throw new IllegalArgumentException("maxParagraphs must be >= minParagraphs, got: "
                                               + maxParagraphs + " < " + minParagraphs);
        }
        int count = minParagraphs + random.nextInt(maxParagraphs - minParagraphs + 1);
        return generateParagraphs(count);
    }

    /**
     * Generates a paragraph with an option bag similar to Chance.js {@code paragraph({sentences})}.
     *
     * @param options option bag; must not be null
     * @return generated paragraph
     */
    public String generate(ParagraphOptions options) {
        Objects.requireNonNull(options, "options must not be null");
        return options.sentences() == null ? generate() : generate(options.sentences());
    }

    /**
     * Returns the configured locale.
     */
    public Locale getLocale() {
        return locale;
    }

    /**
     * Option bag for paragraph generation.
     *
     * @param sentences optional exact sentence count (positive)
     */
    public record ParagraphOptions(Integer sentences) {

        public ParagraphOptions {
            if (sentences != null && sentences <= 0) {
                throw new IllegalArgumentException("sentences must be positive, got: " + sentences);
            }
        }

        /**
         * Creates options with fixed sentence count.
         */
        public static ParagraphOptions withSentences(int sentences) {
            return new ParagraphOptions(sentences);
        }
    }
}
