/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package org.github.krandom.generator.user;

import org.github.krandom.generator.Generator;
import org.github.krandom.generator.GeneratorConfig;

import java.security.SecureRandom;
import java.util.Objects;
import java.util.Random;

/**
 * Generates random company names by combining a prefix word, a business noun, and an
 * optional legal-form suffix.
 *
 * <p>The default {@link #generate()} method includes the suffix.
 * Use {@link #generate(boolean)} to control whether a suffix is appended.
 *
 * <pre>{@code
 * CompanyNameGenerator gen = new CompanyNameGenerator();
 * String name     = gen.generate();        // "Global Solutions Inc."
 * String noSuffix = gen.generate(false);   // "Dynamic Technologies"
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

    private static final String[] PREFIXES = {
        "Global", "National", "American", "United", "Premier", "Advanced",
        "Strategic", "Dynamic", "Innovative", "Professional", "Digital", "Next",
        "Alpha", "Summit", "Apex"
    };

    private static final String[] NOUNS = {
        "Solutions", "Systems", "Technologies", "Industries", "Services",
        "Group", "Partners", "Ventures", "Associates", "Consulting",
        "Networks", "Dynamics", "Resources", "Management", "Analytics"
    };

    private static final String[] SUFFIXES = {
        "Inc.", "LLC", "Corp.", "Ltd.", "Co.", "Group", "Holdings", "Enterprises"
    };

    private final GeneratorConfig config;
    private final Random random;

    /** Creates a company-name generator with default configuration. */
    public CompanyNameGenerator() {
        this(GeneratorConfig.defaults());
    }

    /**
     * Creates a company-name generator with the specified configuration.
     *
     * @param config the generator configuration; must not be {@code null}
     * @throws NullPointerException if {@code config} is {@code null}
     */
    public CompanyNameGenerator(GeneratorConfig config) {
        this.config = Objects.requireNonNull(config, "config must not be null");
        this.random = config.getSeed().isPresent()
                ? new Random(config.getSeed().getAsLong())
                : new SecureRandom();
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
     * Generates a company name, optionally appending a legal-form suffix.
     *
     * @param withSuffix {@code true} to append a suffix (e.g. {@code "Inc."}),
     *                   {@code false} for the bare two-word name
     * @return a company-name string; never {@code null}
     */
    public String generate(boolean withSuffix) {
        String name = PREFIXES[random.nextInt(PREFIXES.length)]
                + " " + NOUNS[random.nextInt(NOUNS.length)];
        return withSuffix ? name + " " + SUFFIXES[random.nextInt(SUFFIXES.length)] : name;
    }
}
