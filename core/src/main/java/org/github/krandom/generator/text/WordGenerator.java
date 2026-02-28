/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package org.github.krandom.generator.text;

import org.github.krandom.generator.Generator;
import org.github.krandom.generator.GeneratorConfig;

import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Random;

/**
 * Generates natural-looking pseudo-words with Chance.js-style syllable and length controls.
 */
public final class WordGenerator implements Generator<String> {

    private static final String PHONETICS_PATH_PREFIX = "krandom/text/phonetics/";
    private static final WordPhonetics DEFAULT_EN = WordPhoneticsLoader.load(PHONETICS_PATH_PREFIX + "en.txt");
    private static final Map<String, WordPhonetics> LOCALE_PROFILES = localeProfiles();

    private final Random random;
    private final Locale locale;
    private final WordPhonetics phonetics;

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
        this.random = config.getSeed().isPresent()
                ? new Random(config.getSeed().getAsLong())
                : new SecureRandom();
        this.phonetics = forLocale(locale);
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
            sb.append(generateSyllable());
        }
        return sb.toString();
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
            sb.append(generateSyllable());
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

    private String generateSyllable() {
        String onset = phonetics.onsets()[random.nextInt(phonetics.onsets().length)];
        String nucleus = phonetics.nuclei()[random.nextInt(phonetics.nuclei().length)];
        String coda = phonetics.codas()[random.nextInt(phonetics.codas().length)];
        return onset + nucleus + coda;
    }

    /**
     * Returns the locale this generator is configured with.
     */
    public Locale getLocale() {
        return locale;
    }

    private static WordPhonetics forLocale(Locale locale) {
        String language = locale.getLanguage();
        String country = locale.getCountry();
        if (!country.isEmpty()) {
            WordPhonetics exact = LOCALE_PROFILES.get(language + "_" + country);
            if (exact != null) {
                return exact;
            }
        }
        WordPhonetics byLanguage = LOCALE_PROFILES.get(language);
        return byLanguage != null ? byLanguage : DEFAULT_EN;
    }

    private static Map<String, WordPhonetics> localeProfiles() {
        Map<String, WordPhonetics> map = new HashMap<>();

        WordPhonetics en = DEFAULT_EN;
        WordPhonetics enGb = WordPhoneticsLoader.load(PHONETICS_PATH_PREFIX + "en_GB.txt");
        WordPhonetics de = WordPhoneticsLoader.load(PHONETICS_PATH_PREFIX + "de.txt");
        WordPhonetics fr = WordPhoneticsLoader.load(PHONETICS_PATH_PREFIX + "fr.txt");
        WordPhonetics es = WordPhoneticsLoader.load(PHONETICS_PATH_PREFIX + "es.txt");
        WordPhonetics it = WordPhoneticsLoader.load(PHONETICS_PATH_PREFIX + "it.txt");
        WordPhonetics pt = WordPhoneticsLoader.load(PHONETICS_PATH_PREFIX + "pt.txt");
        WordPhonetics ja = WordPhoneticsLoader.load(PHONETICS_PATH_PREFIX + "ja.txt");
        WordPhonetics zh = WordPhoneticsLoader.load(PHONETICS_PATH_PREFIX + "zh.txt");

        map.put("en", en);
        map.put("en_US", en);
        map.put("en_GB", enGb);
        map.put("en_AU", en);

        map.put("de", de);
        map.put("de_DE", de);

        map.put("fr", fr);
        map.put("fr_FR", fr);

        map.put("es", es);
        map.put("es_ES", es);

        map.put("it", it);
        map.put("it_IT", it);

        map.put("pt", pt);
        map.put("pt_BR", pt);

        map.put("ja", ja);
        map.put("ja_JP", ja);

        map.put("zh", zh);
        map.put("zh_CN", zh);

        return Map.copyOf(map);
    }

    /**
     * Option bag for {@link #generate(WordOptions)}.
     *
     * @param syllables optional syllable count (positive)
     * @param length optional exact character length (positive)
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
