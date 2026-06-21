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
 * Generates locale-aware nationality demonyms, e.g. {@code "French"} for English or
 * {@code "Француз"} for Russian.
 *
 * <p>Demonyms are resolved from {@link NationalityDataRegistry} for the configured locale; locales
 * without a built-in file fall back to the bundled English demonyms.
 *
 * <pre>{@code
 *   String en = new NationalityGenerator().generate();                     // e.g. "American"
 *   String ru = new NationalityGenerator(Locale.of("ru","RU")).generate(); // e.g. "Американец"
 * }</pre>
 */
public final class NationalityGenerator implements Generator<String> {

    private static final NationalityDataProvider DEFAULT_PROVIDER =
        new BuiltInNationalityDataProvider(Locale.ROOT, "krandom/nationalities/default.txt");

    private final List<String> nationalities;
    private final Random random;

    /**
     * Creates a generator using the default configuration (and its locale).
     */
    public NationalityGenerator() {
        this(GeneratorConfig.defaults());
    }

    /**
     * Creates a generator for the given locale.
     *
     * @param locale the locale whose demonyms to use; falls back to English if no built-in file
     *               exists; must not be {@code null}
     */
    public NationalityGenerator(Locale locale) {
        this(GeneratorConfig.builder().locale(Objects.requireNonNull(locale, "locale must not be null")).build());
    }

    /**
     * Creates a generator from explicit configuration (locale + optional seed).
     *
     * @param config the generator configuration; must not be {@code null}
     */
    public NationalityGenerator(GeneratorConfig config) {
        Objects.requireNonNull(config, "config must not be null");
        NationalityDataProvider provider = NationalityDataRegistry.forLocale(config.getLocale());
        if (provider == null) {
            provider = DEFAULT_PROVIDER;
        }
        this.nationalities = provider.getNationalities();
        this.random = config.createRandom();
    }

    /**
     * Generates a uniformly random nationality demonym in the configured locale.
     *
     * @return a localized demonym such as {@code "French"}; never {@code null}
     */
    @Override
    public String generate() {
        return nationalities.get(random.nextInt(nationalities.size()));
    }
}
