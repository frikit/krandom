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
 * Generates locale-aware pronounceable syllables.
 *
 * <p>Supports default random syllables and Chance.js-style length control via
 * {@link #generate(int)}.
 */
public final class SyllableGenerator implements Generator<String> {

    private static final String PHONETICS_PATH_PREFIX = "krandom/text/phonetics/";
    private static final WordPhonetics DEFAULT_EN = WordPhoneticsLoader.load(PHONETICS_PATH_PREFIX + "en.txt");
    private static final Map<String, WordPhonetics> LOCALE_PROFILES = localeProfiles();

    private final Locale locale;
    private final Random random;
    private final WordPhonetics phonetics;

    /**
     * Creates a generator with default configuration.
     */
    public SyllableGenerator() {
        this(GeneratorConfig.defaults());
    }

    /**
     * Creates a generator with explicit configuration.
     *
     * @param config generator configuration; must not be null
     */
    public SyllableGenerator(GeneratorConfig config) {
        Objects.requireNonNull(config, "config must not be null");
        this.locale = config.getLocale();
        this.random = config.getSeed().isPresent()
                ? new Random(config.getSeed().getAsLong())
                : new SecureRandom();
        this.phonetics = forLocale(locale);
    }

    /**
     * Generates a locale-aware syllable.
     */
    @Override
    public String generate() {
        String onset = phonetics.onsets()[random.nextInt(phonetics.onsets().length)];
        String nucleus = phonetics.nuclei()[random.nextInt(phonetics.nuclei().length)];
        String coda = phonetics.codas()[random.nextInt(phonetics.codas().length)];
        return onset + nucleus + coda;
    }

    /**
     * Generates a syllable with exact character {@code length}.
     *
     * @param length desired length; must be positive
     * @return lowercase syllable with exact length
     */
    public String generate(int length) {
        if (length <= 0) {
            throw new IllegalArgumentException("length must be positive, got: " + length);
        }
        String syllable = generate();
        if (syllable.length() > length) {
            return syllable.substring(0, length);
        }
        if (syllable.length() == length) {
            return syllable;
        }

        StringBuilder sb = new StringBuilder(length + 4);
        sb.append(syllable);
        while (sb.length() < length) {
            sb.append(generate());
        }
        if (sb.length() > length) {
            sb.setLength(length);
        }
        return sb.toString();
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
}
