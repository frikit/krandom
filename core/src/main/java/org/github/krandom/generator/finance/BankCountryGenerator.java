/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package org.github.krandom.generator.finance;

import org.github.krandom.generator.Generator;
import org.github.krandom.generator.GeneratorConfig;

import java.security.SecureRandom;
import java.util.Locale;
import java.util.Objects;
import java.util.Random;

/**
 * Generates bank-country ISO alpha-2 codes, preferring the configured locale country.
 */
public final class BankCountryGenerator implements Generator<String> {

    private static final String[] SUPPORTED = { "US", "GB", "DE", "FR", "ES", "IT", "BR", "JP", "CN", "AU" };

    private final Locale locale;
    private final Random random;

    public BankCountryGenerator() {
        this(GeneratorConfig.defaults());
    }

    public BankCountryGenerator(Locale locale) {
        this(GeneratorConfig.builder().locale(locale).build());
    }

    public BankCountryGenerator(GeneratorConfig config) {
        GeneratorConfig effective = Objects.requireNonNull(config, "config must not be null");
        this.locale = effective.getLocale();
        this.random = effective.getSeed().isPresent()
                      ? new Random(effective.getSeed().getAsLong())
                      : new SecureRandom();
    }

    @Override
    public String generate() {
        String localeCountry = locale.getCountry();
        if (!localeCountry.isBlank()) {
            for (String country : SUPPORTED) {
                if (country.equals(localeCountry)) {
                    return country;
                }
            }
        }
        return SUPPORTED[random.nextInt(SUPPORTED.length)];
    }
}
