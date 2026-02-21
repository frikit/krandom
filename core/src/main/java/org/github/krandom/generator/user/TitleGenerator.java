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
 * <p>Supports 10 locales: US, UK, AU, FR, DE, JA, ES, IT, PT, ZH.
 */
public final class TitleGenerator implements Generator<String> {

    private final GeneratorConfig config;
    private final Random random;
    private final String[] titles;

    public TitleGenerator() {
        this(GeneratorConfig.defaults());
    }

    public TitleGenerator(GeneratorConfig config) {
        this.config = Objects.requireNonNull(config, "config must not be null");
        this.random = config.getSeed().isPresent()
                ? new Random(config.getSeed().getAsLong())
                : new SecureRandom();

        LocaleTitleData localeData = LocaleTitleData.forLocale(config.getLocale());
        this.titles = localeData.getTitles();
    }

    public TitleGenerator(Locale locale) {
        this(GeneratorConfig.builder().locale(locale).build());
    }

    @Override
    public String generate() {
        if (titles.length == 0) {
            return "";
        }
        return titles[random.nextInt(titles.length)];
    }

    public Locale getLocale() {
        return config.getLocale();
    }

    public int getTitleCount() {
        return titles.length;
    }

    public boolean isLocaleExplicitlySupported() {
        return LocaleTitleData.isSupported(config.getLocale());
    }
}
