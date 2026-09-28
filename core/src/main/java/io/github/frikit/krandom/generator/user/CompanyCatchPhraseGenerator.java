/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.user;

import io.github.frikit.krandom.generator.Generator;
import io.github.frikit.krandom.generator.GeneratorConfig;

import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Random;

/**
 * Generates locale-aware company catch phrases similar to Faker's {@code catch_phrase()}.
 *
 * <p>Vocabulary and word order are resolved through the configuration's
 * {@code DataRegistryContext}, which defaults to {@link CompanyCatchPhraseDataRegistry}; locales
 * without built-in data fall back to the bundled English vocabulary.
 */
public final class CompanyCatchPhraseGenerator implements Generator<String> {

    private static final CompanyCatchPhraseDataProvider DEFAULT_PROVIDER =
        new BuiltInCompanyCatchPhraseDataProvider(Locale.ROOT, "default");

    private final List<String> adjectives;
    private final List<String> nouns;
    private final List<String> taglines;
    private final String       format;
    private final Random       random;

    /**
     * Creates a catch-phrase generator with default configuration.
     */
    public CompanyCatchPhraseGenerator() {
        this(GeneratorConfig.defaults());
    }

    /**
     * Creates a catch-phrase generator for the given locale.
     *
     * @param locale locale whose vocabulary to use
     */
    public CompanyCatchPhraseGenerator(Locale locale) {
        this(GeneratorConfig.builder().locale(locale).build());
    }

    /**
     * Creates a catch-phrase generator with the specified configuration (locale + optional seed).
     *
     * @param config generator configuration; must not be {@code null}
     */
    public CompanyCatchPhraseGenerator(GeneratorConfig config) {
        GeneratorConfig effective = Objects.requireNonNull(config, "config must not be null");
        CompanyCatchPhraseDataProvider provider = effective.getRegistryContext().companyCatchPhraseProvider(effective.getLocale());
        CompanyCatchPhraseDataProvider resolved = provider != null ? provider : DEFAULT_PROVIDER;
        this.adjectives = resolved.getAdjectives();
        this.nouns = resolved.getNouns();
        this.taglines = resolved.getTaglines();
        this.format = resolved.getFormat();
        this.random = effective.createRandom();
    }

    @Override
    public String generate() {
        String adjective = pick(adjectives);
        String noun = pick(nouns);
        String tagline = pick(taglines);
        return format.replace("{adjective}", adjective).replace("{noun}", noun).replace("{tagline}", tagline);
    }

    private String pick(List<String> values) {
        return values.get(random.nextInt(values.size()));
    }
}
