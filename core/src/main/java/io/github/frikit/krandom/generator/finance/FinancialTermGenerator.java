/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.finance;

import io.github.frikit.krandom.generator.Generator;
import io.github.frikit.krandom.generator.GeneratorConfig;
import io.github.frikit.krandom.generator.DataRegistryContext;

import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Random;

/**
 * Generates locale-aware financial-term names, e.g. {@code "Dividend"} for English or
 * {@code "Дивиденд"} for Russian.
 *
 * <p>Term names are resolved from the configured
 * {@link io.github.frikit.krandom.generator.DataRegistryContext}; its default view delegates to
 * {@link FinancialTermDataRegistry}. Locales without a built-in file fall back to bundled English
 * terms.
 *
 * <pre>{@code
 *   String en = new FinancialTermGenerator().generate();                     // e.g. "Asset"
 *   String ru = new FinancialTermGenerator(Locale.of("ru","RU")).generate(); // e.g. "Актив"
 * }</pre>
 */
public final class FinancialTermGenerator implements Generator<String> {

    private static final FinancialTermDataProvider DEFAULT_PROVIDER =
        new BuiltInFinancialTermDataProvider(Locale.ROOT, "krandom/financial_terms/default.txt");

    private final List<String> terms;
    private final Random random;

    /**
     * Creates a generator using the default configuration (and its locale).
     */
    public FinancialTermGenerator() {
        this(GeneratorConfig.defaults());
    }

    /**
     * Creates a generator for the given locale.
     *
     * @param locale the locale whose term names to use; falls back to English if no built-in file
     *               exists; must not be {@code null}
     */
    public FinancialTermGenerator(Locale locale) {
        this(GeneratorConfig.builder().locale(Objects.requireNonNull(locale, "locale must not be null")).build());
    }

    /**
     * Creates a generator from explicit configuration (locale + optional seed).
     *
     * @param config the generator configuration; must not be {@code null}
     */
    public FinancialTermGenerator(GeneratorConfig config) {
        Objects.requireNonNull(config, "config must not be null");
        DataRegistryContext registryContext = config.getRegistryContext();
        FinancialTermDataProvider provider = registryContext.financialTermProvider(config.getLocale());
        if (provider == null) {
            provider = DEFAULT_PROVIDER;
        }
        this.terms = provider.getTerms();
        this.random = config.createRandom();
    }

    /**
     * Generates a uniformly random financial-term name in the configured locale.
     *
     * @return a localized term such as {@code "Dividend"}; never {@code null}
     */
    @Override
    public String generate() {
        return terms.get(random.nextInt(terms.size()));
    }
}
