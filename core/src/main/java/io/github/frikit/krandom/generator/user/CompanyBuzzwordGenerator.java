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
 * Generates locale-aware company buzzword phrases similar to Faker's {@code bs()}.
 *
 * <p>Vocabulary and word order are resolved through the configuration's
 * {@code DataRegistryContext}, which defaults to {@link CompanyBuzzwordDataRegistry}; locales
 * without built-in data fall back to the bundled English vocabulary.
 */
public final class CompanyBuzzwordGenerator implements Generator<String> {

    private static final CompanyBuzzwordDataProvider DEFAULT_PROVIDER =
        new BuiltInCompanyBuzzwordDataProvider(Locale.ROOT, "default");

    private final List<String> verbs;
    private final List<String> adjectives;
    private final List<String> nouns;
    private final String       format;
    private final Random       random;

    /**
     * Creates a buzzword generator with default configuration.
     */
    public CompanyBuzzwordGenerator() {
        this(GeneratorConfig.defaults());
    }

    /**
     * Creates a buzzword generator for the given locale.
     *
     * @param locale locale whose vocabulary to use
     */
    public CompanyBuzzwordGenerator(Locale locale) {
        this(GeneratorConfig.builder().locale(locale).build());
    }

    /**
     * Creates a buzzword generator with the specified configuration (locale + optional seed).
     *
     * @param config generator configuration; must not be {@code null}
     */
    public CompanyBuzzwordGenerator(GeneratorConfig config) {
        GeneratorConfig effective = Objects.requireNonNull(config, "config must not be null");
        CompanyBuzzwordDataProvider provider = effective.getRegistryContext().companyBuzzwordProvider(effective.getLocale());
        CompanyBuzzwordDataProvider resolved = provider != null ? provider : DEFAULT_PROVIDER;
        this.verbs = resolved.getVerbs();
        this.adjectives = resolved.getAdjectives();
        this.nouns = resolved.getNouns();
        this.format = resolved.getFormat();
        this.random = effective.createRandom();
    }

    @Override
    public String generate() {
        String verb = pick(verbs);
        String adjective = pick(adjectives);
        String noun = pick(nouns);
        return format.replace("{verb}", verb).replace("{adjective}", adjective).replace("{noun}", noun);
    }

    private String pick(List<String> values) {
        return values.get(random.nextInt(values.size()));
    }
}
