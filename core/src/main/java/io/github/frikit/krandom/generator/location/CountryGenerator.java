/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.location;

import io.github.frikit.krandom.generator.DataRegistryContext;
import io.github.frikit.krandom.generator.Generator;
import io.github.frikit.krandom.generator.GeneratorConfig;
import io.github.frikit.krandom.generator.Vocabulary;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Generates locale-aware country names.
 *
 * <p>The locale controls the language of the returned country names — for example, Germany is
 * {@code "Germany"} in English, {@code "Deutschland"} in German, and {@code "ドイツ"} in Japanese.
 *
 * <p>Built-in country-name generation follows the full {@link io.github.frikit.krandom.generator.locale.SupportedLocale}
 * catalog. Additional locales — and overrides of built-in ones — can be registered at runtime
 * via {@link io.github.frikit.krandom.generator.DataRegistryContext.Builder}.
 *
 * <p>Country codes come from the ISO 3166-1 table bundled with kRandom: the alpha-2, alpha-3 and numeric
 * codes of all 249 officially assigned countries. Because the table ships with the library, seeded codes
 * are the same on every Java version.
 *
 * <p>Every one of those countries also has a calling code, a continent and a time zone, from tables
 * bundled beside the ISO table; each file records its source, licence, retrieval date and checksum:
 * <ul>
 *   <li>A calling code is the ITU-T E.164 country code alone, from libphonenumber, so countries that
 *       share one have the same value: {@code +1} for every North American Numbering Plan country,
 *       {@code +44} for the United Kingdom and the crown dependencies.</li>
 *   <li>A continent is the country's UN M49 region, with the Americas divided into North America and
 *       South America; a transcontinental country has its M49 region ({@code RU} is Europe, {@code TR}
 *       is Asia). The continents are {@code Africa}, {@code Antarctica}, {@code Asia}, {@code Europe},
 *       {@code North America}, {@code Oceania} and {@code South America}.</li>
 *   <li>A time zone is an IANA time zone ID from the tz database's {@code zone.tab}; a country with
 *       several zones has the zone of its seat of government ({@code America/New_York} for the United
 *       States, {@code Europe/Moscow} for Russia).</li>
 * </ul>
 */
public final class CountryGenerator implements Generator<String> {

    private static final String ISO_CODES_RESOURCE = "krandom/country_codes/iso_3166_1.txt";
    private static final String CALLING_CODES_RESOURCE = "krandom/country_codes/calling_codes.txt";
    private static final String CONTINENTS_RESOURCE = "krandom/country_codes/continents.txt";
    private static final String TIME_ZONES_RESOURCE = "krandom/country_codes/time_zones.txt";
    // Alpha-2 order, the same order as Locale.getISOCountries(), which seeded codes were drawn from before.
    private static final List<CountryCodes> ISO_CODES = parseIsoCodes(CountryResourceLoader.load(ISO_CODES_RESOURCE));
    private static final Map<String, CountryCodes> ISO_CODES_BY_ALPHA2 =
            ISO_CODES.stream().collect(Collectors.toUnmodifiableMap(CountryCodes::alpha2, Function.identity()));
    private static final Map<String, String> CALLING_BY_COUNTRY = loadCountryValues(CALLING_CODES_RESOURCE);
    private static final Map<String, String> CONTINENT_BY_COUNTRY = loadCountryValues(CONTINENTS_RESOURCE);
    private static final Map<String, String> TIMEZONE_BY_COUNTRY = loadCountryValues(TIME_ZONES_RESOURCE);
    // Random selection indexes these snapshots, one value per country in the ISO table's order, so a draw
    // picks a country as generateCodes() does; Map.of/Map.ofEntries iteration order is randomized per JVM
    // and must never feed seeded output.
    private static final List<String> CALLING_CODES = List.copyOf(CALLING_BY_COUNTRY.values());
    private static final List<String> CONTINENTS = List.copyOf(CONTINENT_BY_COUNTRY.values());
    private static final List<String> TIMEZONES = List.copyOf(TIMEZONE_BY_COUNTRY.values());

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
        DataRegistryContext registryContext = config.getRegistryContext();

        Locale locale = config.getLocale();
        if (!registryContext.isRegistered(Vocabulary.COUNTRY, locale)) {
            throw new UnsupportedOperationException("Locale " + locale + " is not supported. Registered locales: "
                    + registryContext.registeredKeys(Vocabulary.COUNTRY));
        }

        this.random = config.createRandom();

        this.countries = java.util.Objects.requireNonNull(registryContext.provider(Vocabulary.COUNTRY, locale))
                .getCountries();
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

    private static Map<String, String> loadCountryValues(String resourcePath) {
        return parseCountryValues(CountryResourceLoader.load(resourcePath), ISO_CODES, resourcePath);
    }

    /**
     * Parses a table that gives one value for each country of {@code countries}: each row is the country's
     * alpha-2 code, a space and the value, in the order of {@code countries}.
     *
     * @throws IllegalStateException if a country has no row, a row has no value, or the rows are in another order
     */
    static Map<String, String> parseCountryValues(String[] rows, List<CountryCodes> countries, String resourcePath) {
        if (rows.length != countries.size()) {
            throw new IllegalStateException(resourcePath + " must have " + countries.size()
                    + " rows, one for each ISO 3166-1 country, found: " + rows.length);
        }
        Map<String, String> values = new LinkedHashMap<>();
        for (int i = 0; i < rows.length; i++) {
            String alpha2 = countries.get(i).alpha2();
            String[] fields = rows[i].split(" ", 2);
            if (fields.length != 2 || !fields[0].equals(alpha2)) {
                throw new IllegalStateException(
                        resourcePath + " row " + (i + 1) + " must be " + alpha2 + " and its value, found: " + rows[i]);
            }
            values.put(alpha2, fields[1]);
        }
        return Collections.unmodifiableMap(values);
    }

    /**
     * Returns the calling code of an ISO 3166-1 country, for the phone-number formats of this package.
     *
     * @param alpha2 an ISO 3166-1 alpha-2 code
     * @return the country's calling code, such as {@code +44}
     */
    static String callingCodeOf(String alpha2) {
        return Objects.requireNonNull(CALLING_BY_COUNTRY.get(alpha2), alpha2);
    }

    static List<CountryCodes> parseIsoCodes(String[] rows) {
        List<CountryCodes> codes = new ArrayList<>(rows.length);
        for (String row : rows) {
            String[] fields = row.split(" ");
            if (fields.length != 3) {
                throw new IllegalStateException("Malformed ISO 3166-1 row: " + row);
            }
            codes.add(new CountryCodes(fields[0], fields[1], fields[2]));
        }
        return List.copyOf(codes);
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
        return generateCodes().alpha2();
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
        return generateCodes().alpha3();
    }

    /**
     * Returns a random ISO 3166-1 numeric code (for example, {@code "840"}, {@code "276"}).
     *
     * @return a three-digit numeric country code; never {@code null}
     */
    public String generateCodeNumeric() {
        return generateCodes().numeric();
    }

    /**
     * Returns the alpha-2, alpha-3 and numeric ISO 3166-1 codes of one random country, for when the codes
     * must belong together.
     *
     * @return the codes of one of the 249 ISO 3166-1 countries; never {@code null}
     */
    public CountryCodes generateCodes() {
        return ISO_CODES.get(random.nextInt(ISO_CODES.size()));
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
     * @throws UnsupportedOperationException if configured locale has no country component, or its country
     *                                       is not an ISO 3166-1 country
     */
    public String currentCountryCodeAlpha3() {
        return currentCountryCodes().alpha3();
    }

    /**
     * Returns the configured locale-country ISO numeric code.
     *
     * @return numeric code
     * @throws UnsupportedOperationException if configured locale has no country component, or its country
     *                                       is not an ISO 3166-1 country
     */
    public String currentCountryCodeNumeric() {
        return currentCountryCodes().numeric();
    }

    /**
     * Returns the alpha-2, alpha-3 and numeric ISO 3166-1 codes of the configured locale country.
     *
     * @return the locale country's codes; never {@code null}
     * @throws UnsupportedOperationException if configured locale has no country component, or its country
     *                                       is not an ISO 3166-1 country
     */
    public CountryCodes currentCountryCodes() {
        String countryCode = requireLocaleCountry();
        CountryCodes codes = ISO_CODES_BY_ALPHA2.get(countryCode);
        if (codes == null) {
            throw new UnsupportedOperationException(
                    "ISO 3166-1 codes not available for locale country: " + countryCode);
        }
        return codes;
    }

    /**
     * Returns the calling code of a random ISO 3166-1 country. Each of the 249 countries is equally likely,
     * so a code that several countries share, such as {@code +1}, comes up more often.
     *
     * @return a calling code such as {@code +44}
     */
    public String generateCallingCode() {
        return CALLING_CODES.get(random.nextInt(CALLING_CODES.size()));
    }

    /**
     * Returns the calling code of the configured locale's country.
     *
     * @return a calling code such as {@code +44}
     * @throws UnsupportedOperationException if configured locale has no country component, or its country
     *                                       is not an ISO 3166-1 country
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
     * Returns the continent of a random ISO 3166-1 country. Each of the 249 countries is equally likely, so a
     * continent with more countries comes up more often.
     *
     * @return one of {@code Africa}, {@code Antarctica}, {@code Asia}, {@code Europe}, {@code North America},
     *         {@code Oceania} and {@code South America}
     */
    public String generateContinent() {
        return CONTINENTS.get(random.nextInt(CONTINENTS.size()));
    }

    /**
     * Returns the continent of the configured locale's country.
     *
     * @return one of {@code Africa}, {@code Antarctica}, {@code Asia}, {@code Europe}, {@code North America},
     *         {@code Oceania} and {@code South America}
     * @throws UnsupportedOperationException if configured locale has no country component, or its country
     *                                       is not an ISO 3166-1 country
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
     * Returns the time zone of a random ISO 3166-1 country, each of the 249 countries equally likely. A
     * country with several zones has the zone of its seat of government.
     *
     * @return an IANA time zone ID that {@link java.time.ZoneId#of(String)} accepts, such as
     *         {@code Europe/London}
     */
    public String generateTimezone() {
        return TIMEZONES.get(random.nextInt(TIMEZONES.size()));
    }

    /**
     * Returns the time zone of the configured locale's country; a country with several zones has the zone
     * of its seat of government.
     *
     * @return an IANA time zone ID that {@link java.time.ZoneId#of(String)} accepts, such as
     *         {@code Europe/London}
     * @throws UnsupportedOperationException if configured locale has no country component, or its country
     *                                       is not an ISO 3166-1 country
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
        return config.getRegistryContext().isRegistered(Vocabulary.COUNTRY, config.getLocale());
    }

    private String requireLocaleCountry() {
        String countryCode = config.getLocale().getCountry();
        if (countryCode.isBlank()) {
            throw new UnsupportedOperationException("Locale " + config.getLocale() + " has no country component");
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
}
