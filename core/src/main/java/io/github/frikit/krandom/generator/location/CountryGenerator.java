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
 */
public final class CountryGenerator implements Generator<String> {

    private static final String ISO_CODES_RESOURCE = "krandom/country_codes/iso_3166_1.txt";
    // Alpha-2 order, the same order as Locale.getISOCountries(), which seeded codes were drawn from before.
    private static final List<CountryCodes> ISO_CODES = parseIsoCodes(CountryResourceLoader.load(ISO_CODES_RESOURCE));
    private static final Map<String, CountryCodes> ISO_CODES_BY_ALPHA2 =
            ISO_CODES.stream().collect(Collectors.toUnmodifiableMap(CountryCodes::alpha2, Function.identity()));
    private static final Map<String, String> CALLING_BY_COUNTRY = orderedMap(
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
            Map.entry("IN", "+91"),
            Map.entry("DK", "+45"),
            Map.entry("FI", "+358"),
            Map.entry("HU", "+36"),
            Map.entry("RO", "+40"),
            Map.entry("SK", "+421"),
            Map.entry("UA", "+380"),
            Map.entry("BG", "+359"),
            Map.entry("HR", "+385"),
            Map.entry("GR", "+30"),
            Map.entry("TH", "+66"),
            Map.entry("VN", "+84"),
            Map.entry("ID", "+62"),
            Map.entry("MY", "+60"),
            Map.entry("IL", "+972"),
            Map.entry("CA", "+1"),
            Map.entry("NZ", "+64"),
            Map.entry("IE", "+353"),
            Map.entry("ZA", "+27"),
            Map.entry("BE", "+32"),
            Map.entry("CH", "+41"),
            Map.entry("AT", "+43"),
            Map.entry("MX", "+52"),
            Map.entry("AR", "+54"),
            Map.entry("PT", "+351"),
            Map.entry("TW", "+886"));
    private static final Map<String, String> CONTINENT_BY_COUNTRY = orderedMap(
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
            Map.entry("IN", "Asia"),
            Map.entry("DK", "Europe"),
            Map.entry("FI", "Europe"),
            Map.entry("HU", "Europe"),
            Map.entry("RO", "Europe"),
            Map.entry("SK", "Europe"),
            Map.entry("UA", "Europe"),
            Map.entry("BG", "Europe"),
            Map.entry("HR", "Europe"),
            Map.entry("GR", "Europe"),
            Map.entry("TH", "Asia"),
            Map.entry("VN", "Asia"),
            Map.entry("ID", "Asia"),
            Map.entry("MY", "Asia"),
            Map.entry("IL", "Asia"),
            Map.entry("CA", "North America"),
            Map.entry("NZ", "Oceania"),
            Map.entry("IE", "Europe"),
            Map.entry("ZA", "Africa"),
            Map.entry("BE", "Europe"),
            Map.entry("CH", "Europe"),
            Map.entry("AT", "Europe"),
            Map.entry("MX", "North America"),
            Map.entry("AR", "South America"),
            Map.entry("PT", "Europe"),
            Map.entry("TW", "Asia"));
    private static final Map<String, String> TIMEZONE_BY_COUNTRY = orderedMap(
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
            Map.entry("IN", "Asia/Kolkata"),
            Map.entry("DK", "Europe/Copenhagen"),
            Map.entry("FI", "Europe/Helsinki"),
            Map.entry("HU", "Europe/Budapest"),
            Map.entry("RO", "Europe/Bucharest"),
            Map.entry("SK", "Europe/Bratislava"),
            Map.entry("UA", "Europe/Kyiv"),
            Map.entry("BG", "Europe/Sofia"),
            Map.entry("HR", "Europe/Zagreb"),
            Map.entry("GR", "Europe/Athens"),
            Map.entry("TH", "Asia/Bangkok"),
            Map.entry("VN", "Asia/Ho_Chi_Minh"),
            Map.entry("ID", "Asia/Jakarta"),
            Map.entry("MY", "Asia/Kuala_Lumpur"),
            Map.entry("IL", "Asia/Jerusalem"),
            Map.entry("CA", "America/Toronto"),
            Map.entry("NZ", "Pacific/Auckland"),
            Map.entry("IE", "Europe/Dublin"),
            Map.entry("ZA", "Africa/Johannesburg"),
            Map.entry("BE", "Europe/Brussels"),
            Map.entry("CH", "Europe/Zurich"),
            Map.entry("AT", "Europe/Vienna"),
            Map.entry("MX", "America/Mexico_City"),
            Map.entry("AR", "America/Buenos_Aires"),
            Map.entry("PT", "Europe/Lisbon"),
            Map.entry("TW", "Asia/Taipei"));
    // Random selection indexes these declaration-ordered snapshots; Map.of/Map.ofEntries iteration
    // order is randomized per JVM and must never feed seeded output.
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

    @SafeVarargs
    private static Map<String, String> orderedMap(Map.Entry<String, String>... entries) {
        Map<String, String> ordered = new LinkedHashMap<>();
        for (Map.Entry<String, String> entry : entries) {
            ordered.put(entry.getKey(), entry.getValue());
        }
        return Collections.unmodifiableMap(ordered);
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
     * Returns a random calling code from the supported locale-country set.
     *
     * @return calling code such as {@code +44}
     */
    public String generateCallingCode() {
        return CALLING_CODES.get(random.nextInt(CALLING_CODES.size()));
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
        return CONTINENTS.get(random.nextInt(CONTINENTS.size()));
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
        return TIMEZONES.get(random.nextInt(TIMEZONES.size()));
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
