/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package org.github.krandom.generator.location;

import org.github.krandom.generator.Generator;
import org.github.krandom.generator.GeneratorConfig;

import java.security.SecureRandom;
import java.util.Locale;
import java.util.Objects;
import java.util.Random;

/**
 * Generates locale-specific postal codes.
 *
 * <p>This generator creates realistic postal codes that match the format rules of each supported
 * locale. The formats are generated programmatically without requiring resource files.
 *
 * <p>Built-in support now follows the expanded locale catalog used across the locale-aware
 * generators, instead of silently falling back to US ZIP codes for newer built-in locales.
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

    private final GeneratorConfig config;
    private final Random          random;
    private final Locale          locale;

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
     * <p>For {@code en_US}, the extended format includes the plus-4 digits (e.g., "90210-1234").
     * For {@code pt_BR} and {@code ja_JP}, the extended format includes a hyphen separator.
     * For other locales, this parameter has no effect.
     *
     * @param extended {@code true} for extended format where applicable
     * @return a postal code string
     */
    public String generate(boolean extended) {
        String localeKey = getLocaleKey(locale);

        return switch (localeKey) {
            case "en_US" -> generateUSZip(extended);
            case "en" -> generateUSZip(extended);
            case "en_GB" -> generateUKPostcode();
            case "en_AU" -> generateAustralianPostcode();
            case "de_DE" -> generateGermanPostcode();
            case "de" -> generateGermanPostcode();
            case "fr_FR" -> generateFrenchPostcode();
            case "fr" -> generateFrenchPostcode();
            case "es_ES" -> generateSpanishPostcode();
            case "es" -> generateSpanishPostcode();
            case "it_IT" -> generateItalianPostcode();
            case "it" -> generateItalianPostcode();
            case "pt_BR" -> generateBrazilianCEP(extended);
            case "pt" -> generateBrazilianCEP(extended);
            case "ja_JP" -> generateJapanesePostcode(extended);
            case "ja" -> generateJapanesePostcode(extended);
            case "zh_CN" -> generateChinesePostcode();
            case "zh" -> generateChinesePostcode();
            case "nl_NL", "nl" -> generateDutchPostcode();
            case "pl_PL", "pl" -> generatePolishPostcode();
            case "ru_RU", "ru" -> generateRussianPostcode();
            case "ko_KR", "ko" -> generateKoreanPostcode();
            case "tr_TR", "tr" -> generateTurkishPostcode();
            case "sv_SE", "sv" -> generateSwedishPostcode();
            case "nb_NO", "nb", "no_NO", "no" -> generateNorwegianPostcode();
            case "cs_CZ", "cs" -> generateCzechPostcode();
            case "ar_SA", "ar" -> generateSaudiPostcode();
            case "hi_IN", "hi" -> generateIndianPin();
            default -> generateUSZip(extended); // Default to US format
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
        String zip5 = String.format("%05d", random.nextInt(100000));
        if (extended) {
            String plus4 = String.format("%04d", random.nextInt(10000));
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
        return String.format("%04d", 200 + random.nextInt(8800)); // Range 0200-8999
    }

    private String generateGermanPostcode() {
        // German postcodes: 01xxx to 99xxx
        return String.format("%05d", 1000 + random.nextInt(98999));
    }

    private String generateFrenchPostcode() {
        // French postcodes: 01xxx to 95xxx (departments 01-95)
        int dept = 1 + random.nextInt(95);
        int suffix = random.nextInt(1000);
        return String.format("%02d%03d", dept, suffix);
    }

    private String generateSpanishPostcode() {
        // Spanish postcodes: 01xxx to 52xxx (provinces 01-52)
        int province = 1 + random.nextInt(52);
        int suffix = random.nextInt(1000);
        return String.format("%02d%03d", province, suffix);
    }

    private String generateItalianPostcode() {
        // Italian postcodes: 00xxx to 98xxx
        return String.format("%05d", random.nextInt(99000));
    }

    private String generateBrazilianCEP(boolean withHyphen) {
        // Brazilian CEP: 00000-000 or 00000000
        int first = random.nextInt(100000);
        int second = random.nextInt(1000);

        if (withHyphen) {
            return String.format("%05d-%03d", first, second);
        }
        return String.format("%05d%03d", first, second);
    }

    private String generateJapanesePostcode(boolean withHyphen) {
        // Japanese postcode: 000-0000 or 0000000
        int first = random.nextInt(1000);
        int second = random.nextInt(10000);

        if (withHyphen) {
            return String.format("%03d-%04d", first, second);
        }
        return String.format("%03d%04d", first, second);
    }

    private String generateChinesePostcode() {
        // Chinese postcodes: 6 digits, 100000-999999
        return String.format("%06d", 100000 + random.nextInt(900000));
    }

    private String generateDutchPostcode() {
        int digits = 1000 + random.nextInt(9000);
        return String.format("%04d %s%s", digits, randomLetter(), randomLetter());
    }

    private String generatePolishPostcode() {
        return String.format("%02d-%03d", 10 + random.nextInt(90), random.nextInt(1000));
    }

    private String generateRussianPostcode() {
        return String.format("%06d", 100000 + random.nextInt(900000));
    }

    private String generateKoreanPostcode() {
        return String.format("%05d", 10000 + random.nextInt(90000));
    }

    private String generateTurkishPostcode() {
        return String.format("%05d", 1000 + random.nextInt(90000));
    }

    private String generateSwedishPostcode() {
        return String.format("%03d %02d", 100 + random.nextInt(900), random.nextInt(100));
    }

    private String generateNorwegianPostcode() {
        return String.format("%04d", random.nextInt(10000));
    }

    private String generateCzechPostcode() {
        return String.format("%03d %02d", 100 + random.nextInt(900), random.nextInt(100));
    }

    private String generateSaudiPostcode() {
        return String.format("%05d", 10000 + random.nextInt(90000));
    }

    private String generateIndianPin() {
        return String.format("%d%05d", 1 + random.nextInt(9), random.nextInt(100000));
    }

    // ── Helper methods ────────────────────────────────────────────────────────

    private String randomLetter() {
        // UK postcodes use A-Z excluding C, I, K, M, O, V to avoid confusion
        String validLetters = "ABDEFGHJLNPQRSTUWXYZ";
        return String.valueOf(validLetters.charAt(random.nextInt(validLetters.length())));
    }

    private String getLocaleKey(Locale loc) {
        String language = loc.getLanguage();
        String country = loc.getCountry();

        if (!country.isEmpty()) {
            return language + "_" + country;
        }
        return language;
    }
}
