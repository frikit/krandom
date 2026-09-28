/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.location;

import io.github.frikit.krandom.generator.Generator;
import io.github.frikit.krandom.generator.GeneratorConfig;

import java.security.SecureRandom;
import java.util.HashSet;
import java.util.Locale;
import java.util.Objects;
import java.util.Random;
import java.util.Set;

/**
 * Generates locale-specific postal codes.
 *
 * <p>This generator creates realistic postal codes that match the format rules of the country
 * resolved from the configured locale. The formats are generated programmatically without
 * requiring resource files.
 *
 * <p>The format country is resolved once, when the generator is created:
 * <ol>
 *   <li>A locale whose country has a built-in format uses it. The language does not change
 *       the format: {@code en_CA} and {@code fr_CA} share the Canadian {@code A1A 1A1} format,
 *       and {@code ca_ES} uses the Spanish one.</li>
 *   <li>A locale without a country, or whose country has no built-in format, uses the country of
 *       the first {@link io.github.frikit.krandom.generator.locale.SupportedLocale} constant, in
 *       catalog order, with that language: {@code en} and {@code en_SG} resolve to the United
 *       States, {@code de_LI} to Germany, {@code pt} to Brazil, {@code zh} to China, and
 *       {@code ca} to Spain. The legacy code {@code no} resolves to Norway.</li>
 *   <li>A locale whose language is empty or not in the catalog, such as {@link Locale#ROOT} or
 *       {@code xx_YY}, uses the documented default {@code en_US}.</li>
 * </ol>
 *
 * <p>Every country of the built-in {@code SupportedLocale} catalog has a built-in format, so
 * supported locales never fall back to US ZIP codes.
 *
 * <p>Example usage:
 * <pre>{@code
 *   // Default US locale
 *   PostalCodeGenerator gen = new PostalCodeGenerator();
 *   String zip = gen.generate();  // "90210"
 *   String zipPlus4 = gen.generate(true);  // "90210-1234"
 *
 *   // UK locale
 *   PostalCodeGenerator ukGen = new PostalCodeGenerator(Locale.UK);
 *   String postcode = ukGen.generate();  // "SW1A 2AA"
 *
 *   // Seeded for reproducibility
 *   GeneratorConfig config = GeneratorConfig.builder()
 *       .locale(Locale.GERMANY)
 *       .seed(42L)
 *       .build();
 *   PostalCodeGenerator deGen = new PostalCodeGenerator(config);
 *   String plz = deGen.generate();  // Reproducible German postal code
 * }</pre>
 */
public final class PostalCodeGenerator implements Generator<String> {

    private static final String[] UK_AREA_CODES = {
        "SW", "EC", "N", "W", "E", "SE", "NW", "WC", "M", "B", "L", "G", "EH", "AB", "BD", "BS",
        "CB", "CF", "CR", "CV", "LE", "LS", "OX", "RG", "S", "SO", "TN", "YO"
    };

    /** Countries with a dedicated formatting method below; every other country uses a layout. */
    private static final Set<String> DEDICATED_FORMAT_COUNTRIES = Set.of(
        "US", "GB", "AU", "DE", "FR", "ES", "IT", "BR", "JP", "CN",
        "NL", "PL", "RU", "KR", "TR", "SE", "NO", "CZ", "SA", "IN"
    );

    private static final Set<String> SUPPORTED_COUNTRIES = supportedCountries();

    private final GeneratorConfig config;
    private final Random          random;
    private final Locale          locale;
    private final String          country;

    /**
     * Creates a generator using US locale with default config.
     */
    public PostalCodeGenerator() {
        this(GeneratorConfig.defaults());
    }

    /**
     * Creates a generator using the given config.
     *
     * @param config the generator configuration; must not be {@code null}
      * @throws NullPointerException if {@code config} is {@code null}
     */
    public PostalCodeGenerator(GeneratorConfig config) {
        this.config = Objects.requireNonNull(config, "config must not be null");
        this.locale = config.getLocale();
        this.country = FormatCountryResolver.resolveSupportedCountry(locale, SUPPORTED_COUNTRIES);
        this.random = config.createRandom();
    }

    /**
     * Creates an unseeded generator for the given locale.
     *
     * @param locale the locale determining the postal code format; must not be {@code null}
     * @throws NullPointerException if {@code locale} is {@code null}
     */
    public PostalCodeGenerator(Locale locale) {
        this(GeneratorConfig.builder().locale(
            Objects.requireNonNull(locale, "locale must not be null")
        ).build());
    }

    /**
     * {@inheritDoc}
     *
     * <p>Returns a postal code in the basic format of the configured locale.
     * For US, returns 5-digit ZIP. For Brazil and Japan, returns format without hyphen.
     */
    @Override
    public String generate() {
        return generate(false);
    }

    /**
     * Generates a postal code with optional extended format.
     *
     * <p>For the United States, the extended format includes the plus-4 digits (e.g., "90210-1234").
     * For Brazil and Japan, the extended format includes a hyphen separator. For Argentina, the
     * basic format is the 4-digit code and the extended format the full CPA (e.g., "C1425DKF").
     * For Taiwan, the basic format is the 3-digit code and the extended format the 5-digit code.
     * For other countries, this parameter has no effect.
     *
     * @param extended {@code true} for extended format where applicable
     * @return a postal code string
     */
    public String generate(boolean extended) {
        return switch (country) {
            case "US" -> generateUSZip(extended);
            case "GB" -> generateUKPostcode();
            case "AU" -> generateAustralianPostcode();
            case "DE" -> generateGermanPostcode();
            case "FR" -> generateFrenchPostcode();
            case "ES" -> generateSpanishPostcode();
            case "IT" -> generateItalianPostcode();
            case "BR" -> generateBrazilianCEP(extended);
            case "JP" -> generateJapanesePostcode(extended);
            case "CN" -> generateChinesePostcode();
            case "NL" -> generateDutchPostcode();
            case "PL" -> generatePolishPostcode();
            case "RU" -> generateRussianPostcode();
            case "KR" -> generateKoreanPostcode();
            case "TR" -> generateTurkishPostcode();
            case "SE" -> generateSwedishPostcode();
            case "NO" -> generateNorwegianPostcode();
            case "CZ" -> generateCzechPostcode();
            case "SA" -> generateSaudiPostcode();
            case "IN" -> generateIndianPin();
            default -> PostalCodeLayouts.forCountry(country).generate(random, extended);
        };
    }

    /**
     * Returns the locale this generator is configured with.
     *
     * @return the locale; never {@code null}
     */
    public Locale getLocale() {
        return locale;
    }

    // ── Format generators ─────────────────────────────────────────────────────

    private String generateUSZip(boolean extended) {
        String zip5 = String.format(Locale.ROOT, "%05d", random.nextInt(100000));
        if (extended) {
            String plus4 = String.format(Locale.ROOT, "%04d", random.nextInt(10000));
            return zip5 + "-" + plus4;
        }
        return zip5;
    }

    private String generateUKPostcode() {
        // UK postcode formats: A9 9AA, A99 9AA, AA9 9AA, AA99 9AA, A9A 9AA, AA9A 9AA
        // Simplified approach: use common area codes and generate valid format
        String areaCode = UK_AREA_CODES[random.nextInt(UK_AREA_CODES.length)];

        // Outward code: area code + district
        String outward;
        int formatType = random.nextInt(6);

        switch (formatType) {
        case 0: // A9
            outward = areaCode.substring(0, 1) + random.nextInt(10);
            break;
        case 1: // A99
            outward = areaCode.substring(0, 1) + (10 + random.nextInt(90));
            break;
        case 2: // AA9
            outward = areaCode + random.nextInt(10);
            break;
        case 3: // AA99
            outward = areaCode + (10 + random.nextInt(90));
            break;
        case 4: // A9A
            outward = areaCode.substring(0, 1) + random.nextInt(10) + randomLetter();
            break;
        default: // AA9A (most common with 2-letter area codes)
            outward = areaCode + random.nextInt(10) + randomLetter();
            break;
        }

        // Inward code: always format 9AA
        String inward = random.nextInt(10) + randomLetter() + randomLetter();

        return outward + " " + inward;
    }

    private String generateAustralianPostcode() {
        return String.format(Locale.ROOT, "%04d", 200 + random.nextInt(8800)); // Range 0200-8999
    }

    private String generateGermanPostcode() {
        // German postcodes: 01xxx to 99xxx
        return String.format(Locale.ROOT, "%05d", 1000 + random.nextInt(98999));
    }

    private String generateFrenchPostcode() {
        // French postcodes: 01xxx to 95xxx (departments 01-95)
        int dept = 1 + random.nextInt(95);
        int suffix = random.nextInt(1000);
        return String.format(Locale.ROOT, "%02d%03d", dept, suffix);
    }

    private String generateSpanishPostcode() {
        // Spanish postcodes: 01xxx to 52xxx (provinces 01-52)
        int province = 1 + random.nextInt(52);
        int suffix = random.nextInt(1000);
        return String.format(Locale.ROOT, "%02d%03d", province, suffix);
    }

    private String generateItalianPostcode() {
        // Italian postcodes: 00xxx to 98xxx
        return String.format(Locale.ROOT, "%05d", random.nextInt(99000));
    }

    private String generateBrazilianCEP(boolean withHyphen) {
        // Brazilian CEP: 00000-000 or 00000000
        int first = random.nextInt(100000);
        int second = random.nextInt(1000);

        if (withHyphen) {
            return String.format(Locale.ROOT, "%05d-%03d", first, second);
        }
        return String.format(Locale.ROOT, "%05d%03d", first, second);
    }

    private String generateJapanesePostcode(boolean withHyphen) {
        // Japanese postcode: 000-0000 or 0000000
        int first = random.nextInt(1000);
        int second = random.nextInt(10000);

        if (withHyphen) {
            return String.format(Locale.ROOT, "%03d-%04d", first, second);
        }
        return String.format(Locale.ROOT, "%03d%04d", first, second);
    }

    private String generateChinesePostcode() {
        // Chinese postcodes: 6 digits, 100000-999999
        return String.format(Locale.ROOT, "%06d", 100000 + random.nextInt(900000));
    }

    private String generateDutchPostcode() {
        int digits = 1000 + random.nextInt(9000);
        return String.format(Locale.ROOT, "%04d %s%s", digits, randomLetter(), randomLetter());
    }

    private String generatePolishPostcode() {
        return String.format(Locale.ROOT, "%02d-%03d", 10 + random.nextInt(90), random.nextInt(1000));
    }

    private String generateRussianPostcode() {
        return String.format(Locale.ROOT, "%06d", 100000 + random.nextInt(900000));
    }

    private String generateKoreanPostcode() {
        return String.format(Locale.ROOT, "%05d", 10000 + random.nextInt(90000));
    }

    private String generateTurkishPostcode() {
        return String.format(Locale.ROOT, "%05d", 1000 + random.nextInt(90000));
    }

    private String generateSwedishPostcode() {
        return String.format(Locale.ROOT, "%03d %02d", 100 + random.nextInt(900), random.nextInt(100));
    }

    private String generateNorwegianPostcode() {
        return String.format(Locale.ROOT, "%04d", random.nextInt(10000));
    }

    private String generateCzechPostcode() {
        return String.format(Locale.ROOT, "%03d %02d", 100 + random.nextInt(900), random.nextInt(100));
    }

    private String generateSaudiPostcode() {
        return String.format(Locale.ROOT, "%05d", 10000 + random.nextInt(90000));
    }

    private String generateIndianPin() {
        return String.format(Locale.ROOT, "%d%05d", 1 + random.nextInt(9), random.nextInt(100000));
    }

    // ── Helper methods ────────────────────────────────────────────────────────

    private String randomLetter() {
        // UK postcodes use A-Z excluding C, I, K, M, O, V to avoid confusion
        String validLetters = "ABDEFGHJLNPQRSTUWXYZ";
        return String.valueOf(validLetters.charAt(random.nextInt(validLetters.length())));
    }

    private static Set<String> supportedCountries() {
        Set<String> countries = new HashSet<>(DEDICATED_FORMAT_COUNTRIES);
        countries.addAll(PostalCodeLayouts.countries());
        return Set.copyOf(countries);
    }
}
