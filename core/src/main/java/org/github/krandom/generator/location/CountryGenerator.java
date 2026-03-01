/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package org.github.krandom.generator.location;

import org.github.krandom.generator.Generator;
import org.github.krandom.generator.GeneratorConfig;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.Objects;
import java.util.Random;

/**
 * Generates locale-aware country names.
 *
 * <p>The locale controls the language of the returned country names — for example, Germany is
 * {@code "Germany"} in English, {@code "Deutschland"} in German, and {@code "ドイツ"} in Japanese.
 *
 * <p>Built-in support covers 10 locales: {@code en_US}, {@code en_GB}, {@code en_AU},
 * {@code fr_FR}, {@code de_DE}, {@code ja_JP}, {@code es_ES}, {@code it_IT}, {@code pt_BR}, and
 * {@code zh_CN}. Additional locales — and overrides of built-in ones — can be registered at
 * runtime via {@link CountryDataRegistry#register(CountryDataProvider)}.
 */
public final class CountryGenerator implements Generator<String> {

    private static final String[] ISO_ALPHA2_CODES = Locale.getISOCountries();
    private static final String[] ISO_ALPHA3_CODES = loadIsoAlpha3Codes();

    private final GeneratorConfig config;
    private final Random random;
    private final String[] countries;

    /**
     * Creates a generator using default configuration ({@link Locale#US}).
     */
    public CountryGenerator() {
        this(GeneratorConfig.defaults());
    }

    /**
     * Creates a generator using the given config.
     *
     * @param config the generator configuration; must not be {@code null}; the locale in the
     *               config must be registered in {@link CountryDataRegistry}
     * @throws NullPointerException          if {@code config} is {@code null}
     * @throws UnsupportedOperationException if the configured locale has no registered data
     */
    public CountryGenerator(GeneratorConfig config) {
        this.config = Objects.requireNonNull(config, "config must not be null");

        Locale locale = config.getLocale();
        if (!CountryDataRegistry.isRegistered(locale)) {
            throw new UnsupportedOperationException(
                "Locale " + locale + " is not supported. Registered locales: " +
                CountryDataRegistry.registeredKeys());
        }

        this.random = config.getSeed().isPresent()
                ? new Random(config.getSeed().getAsLong())
                : new SecureRandom();

        this.countries = CountryDataRegistry.forLocale(locale).getCountries();
    }

    /**
     * Creates an unseeded generator for the given locale.
     *
     * @param locale the locale determining the language of country names; must not be {@code null}
     * @throws NullPointerException          if {@code locale} is {@code null}
     * @throws UnsupportedOperationException if the locale has no registered data
     */
    public CountryGenerator(Locale locale) {
        this(GeneratorConfig.builder().locale(locale).build());
    }

    /**
     * {@inheritDoc}
     *
     * <p>Returns a country name in the language of the configured locale.
     */
    @Override
    public String generate() {
        return countries[random.nextInt(countries.length)];
    }

    /**
     * Returns a random ISO 3166-1 alpha-2 country code (for example, {@code "US"}, {@code "DE"}).
     *
     * @return an upper-case country code; never {@code null}
     */
    public String generateCode() {
        return ISO_ALPHA2_CODES[random.nextInt(ISO_ALPHA2_CODES.length)];
    }

    /**
     * Returns a random ISO 3166-1 alpha-3 country code (for example, {@code "USA"}, {@code "DEU"}).
     *
     * @return an upper-case alpha-3 code; never {@code null}
     */
    public String generateCodeAlpha3() {
        return ISO_ALPHA3_CODES[random.nextInt(ISO_ALPHA3_CODES.length)];
    }

    /**
     * Returns the display name of the currently configured locale country in the configured locale language.
     *
     * @return current locale country display name; never {@code null}
     * @throws UnsupportedOperationException if configured locale has no country component
     */
    public String currentCountry() {
        String countryCode = requireLocaleCountry();
        Locale countryLocale = Locale.of("", countryCode);
        return countryLocale.getDisplayCountry(config.getLocale());
    }

    /**
     * Returns the ISO 3166-1 alpha-2 code for the configured locale country.
     *
     * @return alpha-2 country code
     * @throws UnsupportedOperationException if configured locale has no country component
     */
    public String currentCountryCode() {
        return requireLocaleCountry();
    }

    /**
     * Returns the ISO 3166-1 alpha-3 code for the configured locale country.
     *
     * @return alpha-3 country code
     * @throws UnsupportedOperationException if configured locale has no country component
     */
    public String currentCountryCodeAlpha3() {
        String countryCode = requireLocaleCountry();
        try {
            return Locale.of("", countryCode).getISO3Country();
        } catch (MissingResourceException ex) {
            throw new UnsupportedOperationException("Alpha-3 country code not available for locale country: " + countryCode, ex);
        }
    }

    /**
     * Returns the locale this generator is configured with.
     *
     * @return the locale; never {@code null}
     */
    public Locale getLocale() {
        return config.getLocale();
    }

    /**
     * Returns the number of distinct country names available for the configured locale.
     *
     * @return the country count; always positive
     */
    public int getCountryCount() {
        return countries.length;
    }

    /**
     * Returns {@code true} if the configured locale has a registered data provider.
     *
     * @return {@code true} for all locales accepted by the constructor
     */
    public boolean isLocaleExplicitlySupported() {
        return CountryDataRegistry.isRegistered(config.getLocale());
    }

    private String requireLocaleCountry() {
        String countryCode = config.getLocale().getCountry();
        if (countryCode == null || countryCode.isBlank()) {
            throw new UnsupportedOperationException(
                    "Locale " + config.getLocale() + " has no country component");
        }
        return countryCode;
    }

    private static String[] loadIsoAlpha3Codes() {
        List<String> alpha3 = new ArrayList<>();
        for (String alpha2 : ISO_ALPHA2_CODES) {
            try {
                alpha3.add(Locale.of("", alpha2).getISO3Country());
            } catch (MissingResourceException ignored) {
                // Skip deprecated/unsupported country entries where ISO3 is not available.
            }
        }
        return alpha3.toArray(String[]::new);
    }
}
