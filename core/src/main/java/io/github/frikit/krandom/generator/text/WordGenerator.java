/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.text;

import io.github.frikit.krandom.generator.Generator;
import io.github.frikit.krandom.generator.GeneratorConfig;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Random;

/**
 * Generates natural-looking pseudo-words with Chance.js-style syllable and length controls.
 */
public final class WordGenerator implements Generator<String> {

    private final Random            random;
    private final Locale            locale;
    private final SyllableGenerator syllableGenerator;

    /**
     * Creates a generator with default configuration.
     */
    public WordGenerator() {
        this(GeneratorConfig.defaults());
    }

    /**
     * Creates a generator with explicit configuration.
     *
     * @param config generator configuration; must not be null
     */
    public WordGenerator(GeneratorConfig config) {
        Objects.requireNonNull(config, "config must not be null");
        this.locale = config.getLocale();
        this.random = config.createRandom();
        this.syllableGenerator = new SyllableGenerator(config);
    }

    /**
     * Generates a random pseudo-word using 2-4 syllables.
     */
    @Override
    public String generate() {
        return generate(random.nextInt(3) + 2);
    }

    /**
     * Generates a pseudo-word using exactly {@code syllables} generated syllables.
     *
     * @param syllables number of syllables; must be positive
     * @return generated lowercase word
     */
    public String generate(int syllables) {
        if (syllables <= 0) {
            throw new IllegalArgumentException("syllables must be positive, got: " + syllables);
        }
        StringBuilder sb = new StringBuilder(syllables * 4);
        for (int i = 0; i < syllables; i++) {
            sb.append(syllableGenerator.generate());
        }
        return sb.toString();
    }

    /**
     * Generates a space-separated sequence of pseudo-words with fixed count.
     *
     * @param count number of words; must be positive
     * @return words separated by single spaces
     */
    public String generateWords(int count) {
        if (count <= 0) {
            throw new IllegalArgumentException("count must be positive, got: " + count);
        }
        List<String> words = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            words.add(generate());
        }
        return String.join(" ", words);
    }

    /**
     * Generates a space-separated sequence of pseudo-words where the word count is sampled
     * from the inclusive range [{@code minWords}, {@code maxWords}].
     *
     * @param minWords minimum number of words (inclusive), must be positive
     * @param maxWords maximum number of words (inclusive), must be >= minWords
     * @return words separated by single spaces
     */
    public String generateWords(int minWords, int maxWords) {
        if (minWords <= 0) {
            throw new IllegalArgumentException("minWords must be positive, got: " + minWords);
        }
        if (maxWords < minWords) {
            throw new IllegalArgumentException("maxWords must be >= minWords, got: "
                                               + maxWords + " < " + minWords);
        }
        int count = minWords + random.nextInt(maxWords - minWords + 1);
        return generateWords(count);
    }

    /**
     * Generates a pseudo-word with exact character {@code length}.
     *
     * @param length desired word length; must be positive
     * @return generated lowercase word with exact length
     */
    public String generateByLength(int length) {
        if (length <= 0) {
            throw new IllegalArgumentException("length must be positive, got: " + length);
        }
        StringBuilder sb = new StringBuilder(length + 4);
        while (sb.length() < length) {
            sb.append(syllableGenerator.generate());
        }
        if (sb.length() > length) {
            sb.setLength(length);
        }
        return sb.toString();
    }

    /**
     * Generates a word with an option bag similar to Chance.js {@code word({syllables, length})}.
     *
     * <p>If both options are provided, {@code length} takes precedence.
     *
     * @param options option bag; must not be null
     * @return generated lowercase word
     */
    public String generate(WordOptions options) {
        Objects.requireNonNull(options, "options must not be null");
        if (options.length() != null) {
            return generateByLength(options.length());
        }
        if (options.syllables() != null) {
            return generate(options.syllables());
        }
        return generate();
    }

    /**
     * Generates a word.
     * Alias for {@link #generate()}.
     *
     * @return generated word
     */
    public String generateWord() {
        return generate();
    }

    /**
     * Returns the locale this generator is configured with.
     */
    public Locale getLocale() {
        return locale;
    }

    /**
     * Option bag for {@link #generate(WordOptions)}.
     *
     * @param syllables optional syllable count (positive)
     * @param length    optional exact character length (positive)
     */
    public record WordOptions(Integer syllables, Integer length) {

        public WordOptions {
            if (syllables != null && syllables <= 0) {
                throw new IllegalArgumentException("syllables must be positive, got: " + syllables);
            }
            if (length != null && length <= 0) {
                throw new IllegalArgumentException("length must be positive, got: " + length);
            }
        }

        /**
         * Creates options with only syllable control.
         */
        public static WordOptions withSyllables(int syllables) {
            return new WordOptions(syllables, null);
        }

        /**
         * Creates options with only length control.
         */
        public static WordOptions withLength(int length) {
            return new WordOptions(null, length);
        }
    }
}
