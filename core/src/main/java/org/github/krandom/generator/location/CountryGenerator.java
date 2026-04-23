/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package org.github.krandom.generator.location;

import org.github.krandom.generator.Generator;
import org.github.krandom.generator.GeneratorConfig;
import org.github.krandom.generator.DataRegistryContext;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.Objects;
import java.util.Random;

/**
 * Generates locale-aware country names.
 *
 * <p>The locale controls the language of the returned country names — for example, Germany is
 * {@code "Germany"} in English, {@code "Deutschland"} in German, and {@code "ドイツ"} in Japanese.
 *
 * <p>Built-in country-name generation follows the full {@link org.github.krandom.generator.locale.SupportedLocale}
 * catalog. Additional locales — and overrides of built-in ones — can be registered at runtime
 * via {@link CountryDataRegistry#register(CountryDataProvider)}.
 */
public final class CountryGenerator implements Generator<String> {

    private static final String[]            ISO_ALPHA2_CODES     = Locale.getISOCountries();
    private static final String[]            ISO_NUMERIC_CODES    = {
        "036", "076", "156", "203", "250", "276", "356", "380", "392", "410",
        "528", "578", "616", "643", "682", "724", "752", "792", "826", "840"
    };
    private static final Map<String, String> NUMERIC_BY_COUNTRY   = Map.ofEntries(
        Map.entry("US", "840"),
        Map.entry("GB", "826"),
        Map.entry("AU", "036"),
        Map.entry("DE", "276"),
        Map.entry("FR", "250"),
        Map.entry("ES", "724"),
        Map.entry("IT", "380"),
        Map.entry("BR", "076"),
        Map.entry("JP", "392"),
        Map.entry("CN", "156"),
        Map.entry("NL", "528"),
        Map.entry("PL", "616"),
        Map.entry("RU", "643"),
        Map.entry("KR", "410"),
        Map.entry("TR", "792"),
        Map.entry("SE", "752"),
        Map.entry("NO", "578"),
        Map.entry("CZ", "203"),
        Map.entry("SA", "682"),
        Map.entry("IN", "356")
    );
    private static final Map<String, String> CALLING_BY_COUNTRY   = Map.ofEntries(
        Map.entry("US", "+1"),
        Map.entry("GB", "+44"),
        Map.entry("AU", "+61"),
        Map.entry("DE", "+49"),
        Map.entry("FR", "+33"),
        Map.entry("ES", "+34"),
        Map.entry("IT", "+39"),
        Map.entry("BR", "+55"),
        Map.entry("JP", "+81"),
        Map.entry("CN", "+86"),
        Map.entry("NL", "+31"),
        Map.entry("PL", "+48"),
        Map.entry("RU", "+7"),
        Map.entry("KR", "+82"),
        Map.entry("TR", "+90"),
        Map.entry("SE", "+46"),
        Map.entry("NO", "+47"),
        Map.entry("CZ", "+420"),
        Map.entry("SA", "+966"),
        Map.entry("IN", "+91")
    );
    private static final Map<String, String> CONTINENT_BY_COUNTRY = Map.ofEntries(
        Map.entry("US", "North America"),
        Map.entry("GB", "Europe"),
        Map.entry("AU", "Oceania"),
        Map.entry("DE", "Europe"),
        Map.entry("FR", "Europe"),
        Map.entry("ES", "Europe"),
        Map.entry("IT", "Europe"),
        Map.entry("BR", "South America"),
        Map.entry("JP", "Asia"),
        Map.entry("CN", "Asia"),
        Map.entry("NL", "Europe"),
        Map.entry("PL", "Europe"),
        Map.entry("RU", "Europe"),
        Map.entry("KR", "Asia"),
        Map.entry("TR", "Asia"),
        Map.entry("SE", "Europe"),
        Map.entry("NO", "Europe"),
        Map.entry("CZ", "Europe"),
        Map.entry("SA", "Asia"),
        Map.entry("IN", "Asia")
    );
    private static final Map<String, String> TIMEZONE_BY_COUNTRY  = Map.ofEntries(
        Map.entry("US", "America/New_York"),
        Map.entry("GB", "Europe/London"),
        Map.entry("AU", "Australia/Sydney"),
        Map.entry("DE", "Europe/Berlin"),
        Map.entry("FR", "Europe/Paris"),
        Map.entry("ES", "Europe/Madrid"),
        Map.entry("IT", "Europe/Rome"),
        Map.entry("BR", "America/Sao_Paulo"),
        Map.entry("JP", "Asia/Tokyo"),
        Map.entry("CN", "Asia/Shanghai"),
        Map.entry("NL", "Europe/Amsterdam"),
        Map.entry("PL", "Europe/Warsaw"),
        Map.entry("RU", "Europe/Moscow"),
        Map.entry("KR", "Asia/Seoul"),
        Map.entry("TR", "Europe/Istanbul"),
        Map.entry("SE", "Europe/Stockholm"),
        Map.entry("NO", "Europe/Oslo"),
        Map.entry("CZ", "Europe/Prague"),
        Map.entry("SA", "Asia/Riyadh"),
        Map.entry("IN", "Asia/Kolkata")
    );

    private final GeneratorConfig config;
    private final Random          random;
    private final String[]        countries;

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
        DataRegistryContext registryContext = config.getRegistryContext();

        Locale locale = config.getLocale();
        if (!registryContext.isCountryRegistered(locale)) {
            throw new UnsupportedOperationException(
                "Locale " + locale + " is not supported. Registered locales: " +
                registryContext.countryRegisteredKeys());
        }

        this.random = config.createRandom();

        this.countries = registryContext.countryProvider(locale).getCountries();
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

    private static String[] loadIsoAlpha3Codes() {
        List<String> alpha3 = new ArrayList<>();
        for (String alpha2 : ISO_ALPHA2_CODES) {
            alpha3.add(Locale.of("", alpha2).getISO3Country());
        }
        return alpha3.toArray(String[]::new);
    }

    private static String[] isoAlpha3Codes() {
        return IsoAlpha3Holder.CODES;
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
     * Returns a random country code using the requested format.
     *
     * @param format code format; must not be {@code null}
     * @return formatted code
     */
    public String generateCode(CountryCodeFormat format) {
        Objects.requireNonNull(format, "format must not be null");
        return switch (format) {
            case ALPHA2 -> generateCode();
            case ALPHA3 -> generateCodeAlpha3();
            case NUMERIC -> generateCodeNumeric();
        };
    }

    /**
     * Returns a random ISO 3166-1 alpha-3 country code (for example, {@code "USA"}, {@code "DEU"}).
     *
     * @return an upper-case alpha-3 code; never {@code null}
     */
    public String generateCodeAlpha3() {
        String[] codes = isoAlpha3Codes();
        return codes[random.nextInt(codes.length)];
    }

    /**
     * Returns a random ISO 3166-1 numeric code (for example, {@code "840"}, {@code "276"}).
     *
     * @return numeric country code
     */
    public String generateCodeNumeric() {
        return ISO_NUMERIC_CODES[random.nextInt(ISO_NUMERIC_CODES.length)];
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
     * Returns the configured locale-country code in the requested format.
     *
     * @param format code format; must not be {@code null}
     * @return formatted code
     */
    public String currentCountryCode(CountryCodeFormat format) {
        Objects.requireNonNull(format, "format must not be null");
        return switch (format) {
            case ALPHA2 -> currentCountryCode();
            case ALPHA3 -> currentCountryCodeAlpha3();
            case NUMERIC -> currentCountryCodeNumeric();
        };
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
     * Returns the configured locale-country ISO numeric code.
     *
     * @return numeric code
     */
    public String currentCountryCodeNumeric() {
        String countryCode = requireLocaleCountry();
        String numeric = NUMERIC_BY_COUNTRY.get(countryCode);
        if (numeric == null) {
            throw new UnsupportedOperationException("Numeric country code not available for locale country: " + countryCode);
        }
        return numeric;
    }

    /**
     * Returns a random calling code from the supported locale-country set.
     *
     * @return calling code such as {@code +44}
     */
    public String generateCallingCode() {
        List<String> values = List.copyOf(CALLING_BY_COUNTRY.values());
        return values.get(random.nextInt(values.size()));
    }

    /**
     * Returns the configured locale-country calling code.
     *
     * @return calling code
     */
    public String currentCallingCode() {
        String countryCode = requireLocaleCountry();
        String calling = CALLING_BY_COUNTRY.get(countryCode);
        if (calling == null) {
            throw new UnsupportedOperationException("Calling code not available for locale country: " + countryCode);
        }
        return calling;
    }

    /**
     * Returns a random continent from the supported locale-country set.
     *
     * @return continent label
     */
    public String generateContinent() {
        List<String> values = List.copyOf(CONTINENT_BY_COUNTRY.values());
        return values.get(random.nextInt(values.size()));
    }

    /**
     * Returns the configured locale-country continent label.
     *
     * @return continent label
     */
    public String currentContinent() {
        String countryCode = requireLocaleCountry();
        String continent = CONTINENT_BY_COUNTRY.get(countryCode);
        if (continent == null) {
            throw new UnsupportedOperationException("Continent not available for locale country: " + countryCode);
        }
        return continent;
    }

    /**
     * Returns a random primary timezone from the supported locale-country set.
     *
     * @return timezone id
     */
    public String generateTimezone() {
        List<String> values = List.copyOf(TIMEZONE_BY_COUNTRY.values());
        return values.get(random.nextInt(values.size()));
    }

    /**
     * Returns the configured locale-country primary timezone id.
     *
     * @return timezone id
     */
    public String currentTimezone() {
        String countryCode = requireLocaleCountry();
        String timezone = TIMEZONE_BY_COUNTRY.get(countryCode);
        if (timezone == null) {
            throw new UnsupportedOperationException("Timezone not available for locale country: " + countryCode);
        }
        return timezone;
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
        return config.getRegistryContext().isCountryRegistered(config.getLocale());
    }

    private String requireLocaleCountry() {
        String countryCode = config.getLocale().getCountry();
        if (countryCode.isBlank()) {
            throw new UnsupportedOperationException(
                "Locale " + config.getLocale() + " has no country component");
        }
        return countryCode;
    }

    /**
     * Country-code output format.
     */
    public enum CountryCodeFormat {
        ALPHA2,
        ALPHA3,
        NUMERIC
    }

    private static final class IsoAlpha3Holder {
        private static final String[] CODES = loadIsoAlpha3Codes();
    }
}
