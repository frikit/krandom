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
 * Generates locale-aware honorific titles.
 *
 * <p>Built-in support follows the locale catalog in
 * {@link org.github.krandom.generator.locale.SupportedLocale}. Additional locales — and overrides
 * of built-in ones — can be registered at runtime via
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
        DataRegistryContext registryContext = config.getRegistryContext();

        Locale locale = config.getLocale();
        if (!registryContext.isTitleRegistered(locale)) {
            throw new UnsupportedOperationException(
                "Locale " + locale + " is not supported. Registered locales: " +
                registryContext.titleRegisteredKeys());
        }

        this.random = config.createRandom();

        this.titles = registryContext.titleProvider(locale).getTitles();
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
        return config.getRegistryContext().isTitleRegistered(config.getLocale());
    }
}
