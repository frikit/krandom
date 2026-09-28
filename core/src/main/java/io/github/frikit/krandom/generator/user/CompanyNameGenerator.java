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
 * Generates locale-aware company names by combining a descriptor word, a business noun, and an
 * optional legal-form suffix.
 *
 * <p>Vocabulary and word order are resolved through the configuration's
 * {@code DataRegistryContext}, which defaults to {@link CompanyNameDataRegistry}: each locale
 * supplies descriptor words, business nouns, legal forms (e.g. {@code "Inc."}, {@code "GmbH"},
 * {@code "株式会社"}) and the formats that arrange them. Locales without built-in data fall back to the
 * bundled English vocabulary.
 *
 * <p>The default {@link #generate()} method includes the legal form.
 * Use {@link #generate(boolean)} to control whether it is appended.
 *
 * <pre>{@code
 * CompanyNameGenerator gen = new CompanyNameGenerator();
 * String name     = gen.generate();        // "Global Solutions Inc."
 * String noSuffix = gen.generate(false);   // "Dynamic Technologies"
 * String german   = new CompanyNameGenerator(Locale.GERMANY).generate(); // e.g. "Globale Lösungen GmbH"
 * }</pre>
 *
 * <p><strong>Seeded Generation:</strong>
 * <pre>{@code
 * GeneratorConfig config = GeneratorConfig.builder().seed(42L).build();
 * CompanyNameGenerator gen = new CompanyNameGenerator(config);
 * String name = gen.generate();  // Reproducible output
 * }</pre>
 */
public final class CompanyNameGenerator implements Generator<String> {

    private static final CompanyNameDataProvider DEFAULT_PROVIDER =
        new BuiltInCompanyNameDataProvider(Locale.ROOT, "default");

    private final List<String> prefixes;
    private final List<String> nouns;
    private final List<String> suffixes;
    private final String       nameFormat;
    private final String       legalNameFormat;
    private final Random       random;

    /**
     * Creates a company-name generator with default configuration.
     */
    public CompanyNameGenerator() {
        this(GeneratorConfig.defaults());
    }

    /**
     * Creates a company-name generator for the given locale.
     *
     * @param locale locale whose company-name vocabulary to use; must not be {@code null}
     */
    public CompanyNameGenerator(Locale locale) {
        this(GeneratorConfig.builder().locale(Objects.requireNonNull(locale, "locale must not be null")).build());
    }

    /**
     * Creates a company-name generator with the specified configuration.
     *
     * @param config the generator configuration; must not be {@code null}
     * @throws NullPointerException if {@code config} is {@code null}
     */
    public CompanyNameGenerator(GeneratorConfig config) {
        Objects.requireNonNull(config, "config must not be null");
        CompanyNameDataProvider provider = config.getRegistryContext().companyNameProvider(config.getLocale());
        CompanyNameDataProvider resolved = provider != null ? provider : DEFAULT_PROVIDER;
        this.prefixes = resolved.getPrefixes();
        this.nouns = resolved.getNouns();
        this.suffixes = resolved.getSuffixes();
        this.nameFormat = resolved.getNameFormat();
        this.legalNameFormat = resolved.getLegalNameFormat();
        this.random = config.createRandom();
    }

    /**
     * {@inheritDoc}
     *
     * <p>Generates a company name including a legal-form suffix.
     *
     * @return a company-name string; never {@code null}
     */
    @Override
    public String generate() {
        return generate(true);
    }

    /**
     * Generates a company name, optionally including a legal-form suffix.
     *
     * @param withSuffix {@code true} to include a legal form (e.g. {@code "Inc."}),
     *                   {@code false} for the bare two-word name
     * @return a company-name string; never {@code null}
     */
    public String generate(boolean withSuffix) {
        String prefix = pick(prefixes);
        String noun = pick(nouns);
        String name = nameFormat.replace("{prefix}", prefix).replace("{noun}", noun);
        return withSuffix ? legalNameFormat.replace("{name}", name).replace("{suffix}", generateSuffix()) : name;
    }

    /**
     * Generates a legal-form suffix.
     *
     * @return company suffix (for example, {@code "Inc."}, {@code "LLC"})
     */
    public String generateSuffix() {
        return pick(suffixes);
    }

    private String pick(List<String> values) {
        return values.get(random.nextInt(values.size()));
    }
}
