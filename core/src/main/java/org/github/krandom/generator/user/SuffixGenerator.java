/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package org.github.krandom.generator.user;

import org.github.krandom.generator.Generator;
import org.github.krandom.generator.GeneratorConfig;
import org.github.krandom.generator.DataRegistryContext;

import java.security.SecureRandom;
import java.util.Locale;
import java.util.Objects;
import java.util.Random;

/**
 * Generates locale-aware name suffixes (e.g. "Jr.", "Sr.", "PhD", "MD").
 *
 * <p>Built-in support covers 10 locales (US, UK, AU, FR, DE, JA, ES, IT, PT, ZH). Additional
 * locales — and overrides of built-in ones — can be registered at runtime via
 * {@link SuffixDataRegistry#register(SuffixDataProvider)}.
 *
 * <pre>{@code
 * // Default — uses Locale.US
 * SuffixGenerator gen = new SuffixGenerator();
 * String suffix = gen.generate(); // "Jr.", "Sr.", "PhD", ...
 *
 * // Specific locale
 * SuffixGenerator jaGen = new SuffixGenerator(Locale.JAPAN);
 * String jaSuffix = jaGen.generate(); // "博士", "学士", "修士"
 * }</pre>
 */
public final class SuffixGenerator implements Generator<String> {

    private final GeneratorConfig config;
    private final Random          random;
    private final String[]        suffixes;

    /**
     * Uses {@link GeneratorConfig#defaults()} — locale defaults to {@link Locale#US}.
     */
    public SuffixGenerator() {
        this(GeneratorConfig.defaults());
    }

    /**
     * Constructs a generator for the given locale.
     */
    public SuffixGenerator(Locale locale) {
        this(GeneratorConfig.builder().locale(locale).build());
    }

    /**
     * Full constructor using a {@link GeneratorConfig} (locale + optional seed).
     */
    public SuffixGenerator(GeneratorConfig config) {
        this.config = Objects.requireNonNull(config, "config must not be null");
        DataRegistryContext registryContext = config.getRegistryContext();

        Locale locale = config.getLocale();
        if (!registryContext.isSuffixRegistered(locale)) {
            throw new UnsupportedOperationException(
                "Locale " + locale + " is not supported. Registered locales: "
                + registryContext.suffixRegisteredKeys());
        }

        this.random = config.getSeed().isPresent()
                      ? new Random(config.getSeed().getAsLong())
                      : new SecureRandom();
        this.suffixes = registryContext.suffixProvider(locale).getSuffixes();
    }

    @Override
    public String generate() {
        return suffixes[random.nextInt(suffixes.length)];
    }

    /**
     * Returns the locale this generator was configured with.
     */
    public Locale getLocale() {
        return config.getLocale();
    }

    /**
     * Returns the number of distinct suffixes available for the configured locale.
     */
    public int getSuffixCount() {
        return suffixes.length;
    }

    /**
     * Returns {@code true} if the configured locale has a registered suffix provider.
     */
    public boolean isLocaleExplicitlySupported() {
        return config.getRegistryContext().isSuffixRegistered(config.getLocale());
    }
}
