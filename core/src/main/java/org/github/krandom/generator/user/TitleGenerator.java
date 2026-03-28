/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package org.github.krandom.generator.user;

import org.github.krandom.generator.Generator;
import org.github.krandom.generator.GeneratorConfig;

import java.security.SecureRandom;
import java.util.Locale;
import java.util.Objects;
import java.util.Random;

/**
 * Generates locale-aware honorific titles.
 *
 * <p>Built-in support covers 10 locales (US, UK, AU, FR, DE, JA, ES, IT, PT, ZH). Additional
 * locales — and overrides of built-in ones — can be registered at runtime via
 * {@link TitleDataRegistry#register(TitleDataProvider)}.
 */
public final class TitleGenerator implements Generator<String> {

    private final GeneratorConfig config;
    private final Random          random;
    private final String[]        titles;

    public TitleGenerator() {
        this(GeneratorConfig.defaults());
    }

    public TitleGenerator(GeneratorConfig config) {
        this.config = Objects.requireNonNull(config, "config must not be null");

        Locale locale = config.getLocale();
        if (!TitleDataRegistry.isRegistered(locale)) {
            throw new UnsupportedOperationException(
                "Locale " + locale + " is not supported. Registered locales: " +
                TitleDataRegistry.registeredKeys());
        }

        this.random = config.getSeed().isPresent()
                      ? new Random(config.getSeed().getAsLong())
                      : new SecureRandom();

        this.titles = TitleDataRegistry.forLocale(locale).getTitles();
    }

    public TitleGenerator(Locale locale) {
        this(GeneratorConfig.builder().locale(locale).build());
    }

    @Override
    public String generate() {
        return titles[random.nextInt(titles.length)];
    }

    public Locale getLocale() {
        return config.getLocale();
    }

    public int getTitleCount() {
        return titles.length;
    }

    public boolean isLocaleExplicitlySupported() {
        return TitleDataRegistry.isRegistered(config.getLocale());
    }
}
