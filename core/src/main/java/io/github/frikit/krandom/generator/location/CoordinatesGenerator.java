/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.location;

import io.github.frikit.krandom.generator.Generator;
import io.github.frikit.krandom.generator.GeneratorConfig;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.util.Locale;
import java.util.Objects;
import java.util.Random;

/**
 * Generates locale-specific geographical coordinates (latitude and longitude).
 *
 * <p>This generator creates realistic coordinates within the geographical bounds of each
 * supported locale. Coordinates are generated with configurable precision (1-10 decimal places),
 * with a default of 6 decimal places providing approximately 0.1 meter accuracy.
 *
 * <p>Built-in support now follows the full locale catalog used by the locale-aware data
 * generators. Each built-in locale maps to one primary country bounding box so coordinate output
 * stays aligned with the configured locale instead of silently falling back to the original
 * US-only defaults for newly added locales.
 *
 * <p>Example usage:
 * <pre>{@code
 *   // Default US locale
 *   CoordinatesGenerator gen = new CoordinatesGenerator();
 *   String coords = gen.generate();  // "40.712776,-74.005974"
 *   double lat = gen.generateLatitude();  // 40.712776
 *   double lon = gen.generateLongitude();  // -74.005974
 *
 *   // Custom precision (3 decimal places)
 *   double preciseLat = gen.generateLatitude(3);  // 40.712
 *   double preciseLon = gen.generateLongitude(3);  // -74.005
 *
 *   // UK locale
 *   CoordinatesGenerator ukGen = new CoordinatesGenerator(Locale.UK);
 *   String ukCoords = ukGen.generate();  // "51.507351,-0.127758"
 *
 *   // Seeded for reproducibility
 *   GeneratorConfig config = GeneratorConfig.builder()
 *       .locale(Locale.JAPAN)
 *       .seed(42L)
 *       .build();
 *   CoordinatesGenerator jpGen = new CoordinatesGenerator(config);
 *   String reproducibleCoords = jpGen.generate();  // Always the same with seed 42
 * }</pre>
 */
public final class CoordinatesGenerator implements Generator<String> {

    /**
     * Default precision for coordinate generation (6 decimal places).
     * This provides approximately 0.1 meter accuracy.
     */
    public static final int DEFAULT_PRECISION = 6;

    /**
     * Minimum allowed precision (1 decimal place).
     */
    public static final int MIN_PRECISION = 1;

    /**
     * Maximum allowed precision (10 decimal places).
     */
    public static final int MAX_PRECISION = 10;
    // Continental United States
    private static final GeoBounds US_BOUNDS = new GeoBounds(24.5, 49.0, -125.0, -66.0);
    // United Kingdom
    private static final GeoBounds GB_BOUNDS = new GeoBounds(49.9, 60.8, -8.2, 1.8);
    // Australia
    private static final GeoBounds AU_BOUNDS = new GeoBounds(-44.0, -10.0, 113.0, 154.0);
    // Germany
    private static final GeoBounds DE_BOUNDS = new GeoBounds(47.3, 55.0, 5.9, 15.0);
    // France
    private static final GeoBounds FR_BOUNDS = new GeoBounds(41.3, 51.1, -5.2, 9.6);
    // Spain
    private static final GeoBounds ES_BOUNDS = new GeoBounds(36.0, 43.8, -9.3, 4.3);
    // Italy
    private static final GeoBounds IT_BOUNDS = new GeoBounds(36.6, 47.1, 6.6, 18.5);
    // Brazil
    private static final GeoBounds BR_BOUNDS = new GeoBounds(-33.7, 5.3, -74.0, -34.8);
    // Japan
    private static final GeoBounds JP_BOUNDS = new GeoBounds(24.0, 45.5, 122.9, 153.9);
    // China
    private static final GeoBounds CN_BOUNDS = new GeoBounds(18.2, 53.6, 73.5, 135.0);
    // Netherlands
    private static final GeoBounds NL_BOUNDS = new GeoBounds(50.7, 53.7, 3.3, 7.3);
    // Poland
    private static final GeoBounds PL_BOUNDS = new GeoBounds(49.0, 54.9, 14.1, 24.2);
    // Russia
    private static final GeoBounds RU_BOUNDS = new GeoBounds(41.2, 81.9, 19.6, 179.9);
    // South Korea
    private static final GeoBounds KR_BOUNDS = new GeoBounds(33.1, 38.7, 124.6, 131.9);
    // Turkey
    private static final GeoBounds TR_BOUNDS = new GeoBounds(36.0, 42.1, 26.0, 45.0);
    // Sweden
    private static final GeoBounds SE_BOUNDS = new GeoBounds(55.3, 69.1, 11.1, 24.2);
    // Norway
    private static final GeoBounds NO_BOUNDS = new GeoBounds(57.9, 71.2, 4.5, 31.3);
    // Czech Republic
    private static final GeoBounds CZ_BOUNDS = new GeoBounds(48.5, 51.1, 12.1, 18.9);
    // Saudi Arabia
    private static final GeoBounds SA_BOUNDS = new GeoBounds(16.3, 32.2, 34.5, 55.7);
    // India
    private static final GeoBounds IN_BOUNDS = new GeoBounds(6.5, 35.7, 68.1, 97.4);
    private final GeneratorConfig config;
    private final Random          random;
    private final Locale          locale;
    private final GeoBounds       bounds;
    /**
     * Creates a generator using US locale with default config.
     */
    public CoordinatesGenerator() {
        this(GeneratorConfig.defaults());
    }

    /**
     * Creates a generator using the given config.
     *
     * @param config the generator configuration; must not be {@code null}
     * @throws NullPointerException if {@code config} is {@code null}
     */
    public CoordinatesGenerator(GeneratorConfig config) {
        this.config = Objects.requireNonNull(config, "config must not be null");
        this.locale = config.getLocale();
        this.random = config.createRandom();
        this.bounds = getBoundsForLocale(locale);
    }

    /**
     * Creates an unseeded generator for the given locale.
     *
     * @param locale the locale determining the geographical bounds; must not be {@code null}
     * @throws NullPointerException if {@code locale} is {@code null}
     */
    public CoordinatesGenerator(Locale locale) {
        this(GeneratorConfig.builder().locale(
            Objects.requireNonNull(locale, "locale must not be null")
        ).build());
    }

    /**
     * {@inheritDoc}
     *
     * <p>Returns a coordinate pair in the format "latitude,longitude" with default precision
     * (6 decimal places). Example: "40.712776,-74.005974"
     */
    @Override
    public String generate() {
        double lat = generateLatitude();
        double lon = generateLongitude();
        return format(lat) + "," + format(lon);
    }

    /**
     * Formats a coordinate as a plain decimal string -- never scientific notation -- so near-zero
     * values such as {@code 0.00044} render as {@code "0.00044"} rather than {@code "4.4E-4"}.
     * {@code BigDecimal.valueOf} preserves the canonical shortest decimal representation and always
     * emits a {@code '.'} separator regardless of locale.
     */
    private static String format(double value) {
        return BigDecimal.valueOf(value).toPlainString();
    }

    /**
     * Generates a latitude value within the locale's geographical bounds.
     * Uses the default precision of 6 decimal places.
     *
     * @return a latitude value between the locale's minimum and maximum latitude
     */
    public double generateLatitude() {
        return generateLatitude(DEFAULT_PRECISION);
    }

    /**
     * Generates a latitude value within the locale's geographical bounds
     * with the specified precision.
     *
     * @param precision the number of decimal places (1-10)
     * @return a latitude value between the locale's minimum and maximum latitude
     * @throws IllegalArgumentException if precision is not between 1 and 10
     */
    public double generateLatitude(int precision) {
        validatePrecision(precision);
        double lat = bounds.minLat + (bounds.maxLat - bounds.minLat) * random.nextDouble();
        return roundToPrecision(lat, precision);
    }

    /**
     * Generates a longitude value within the locale's geographical bounds.
     * Uses the default precision of 6 decimal places.
     *
     * @return a longitude value between the locale's minimum and maximum longitude
     */
    public double generateLongitude() {
        return generateLongitude(DEFAULT_PRECISION);
    }

    /**
     * Generates a longitude value within the locale's geographical bounds
     * with the specified precision.
     *
     * @param precision the number of decimal places (1-10)
     * @return a longitude value between the locale's minimum and maximum longitude
     * @throws IllegalArgumentException if precision is not between 1 and 10
     */
    public double generateLongitude(int precision) {
        validatePrecision(precision);
        double lon = bounds.minLon + (bounds.maxLon - bounds.minLon) * random.nextDouble();
        return roundToPrecision(lon, precision);
    }

    /**
     * Returns the locale this generator is configured with.
     *
     * @return the locale; never {@code null}
     */
    public Locale getLocale() {
        return locale;
    }

    /**
     * Returns the minimum latitude for this locale.
     *
     * @return the minimum latitude in degrees
     */
    public double getMinLatitude() {
        return bounds.minLat;
    }

    /**
     * Returns the maximum latitude for this locale.
     *
     * @return the maximum latitude in degrees
     */
    public double getMaxLatitude() {
        return bounds.maxLat;
    }

    /**
     * Returns the minimum longitude for this locale.
     *
     * @return the minimum longitude in degrees
     */
    public double getMinLongitude() {
        return bounds.minLon;
    }

    /**
     * Returns the maximum longitude for this locale.
     *
     * @return the maximum longitude in degrees
     */
    public double getMaxLongitude() {
        return bounds.maxLon;
    }

    private GeoBounds getBoundsForLocale(Locale loc) {
        String localeKey = getLocaleKey(loc);

        return switch (localeKey) {
            case "en_US" -> US_BOUNDS;
            case "en" -> US_BOUNDS;
            case "en_GB" -> GB_BOUNDS;
            case "en_AU" -> AU_BOUNDS;
            case "de_DE" -> DE_BOUNDS;
            case "de" -> DE_BOUNDS;
            case "fr_FR" -> FR_BOUNDS;
            case "fr" -> FR_BOUNDS;
            case "es_ES" -> ES_BOUNDS;
            case "es" -> ES_BOUNDS;
            case "it_IT" -> IT_BOUNDS;
            case "it" -> IT_BOUNDS;
            case "pt_BR" -> BR_BOUNDS;
            case "pt" -> BR_BOUNDS;
            case "ja_JP" -> JP_BOUNDS;
            case "ja" -> JP_BOUNDS;
            case "zh_CN" -> CN_BOUNDS;
            case "zh" -> CN_BOUNDS;
            case "nl_NL", "nl" -> NL_BOUNDS;
            case "pl_PL", "pl" -> PL_BOUNDS;
            case "ru_RU", "ru" -> RU_BOUNDS;
            case "ko_KR", "ko" -> KR_BOUNDS;
            case "tr_TR", "tr" -> TR_BOUNDS;
            case "sv_SE", "sv" -> SE_BOUNDS;
            case "nb_NO", "nb", "no_NO", "no" -> NO_BOUNDS;
            case "cs_CZ", "cs" -> CZ_BOUNDS;
            case "ar_SA", "ar" -> SA_BOUNDS;
            case "hi_IN", "hi" -> IN_BOUNDS;
            default -> US_BOUNDS; // Default to US bounds
        };
    }

    // ── Helper methods ────────────────────────────────────────────────────────

    private String getLocaleKey(Locale loc) {
        String language = loc.getLanguage();
        String country = loc.getCountry();

        if (!country.isEmpty()) {
            return language + "_" + country;
        }
        return language;
    }

    private void validatePrecision(int precision) {
        if (precision < MIN_PRECISION || precision > MAX_PRECISION) {
            throw new IllegalArgumentException(
                "precision must be between " + MIN_PRECISION + " and " + MAX_PRECISION +
                ", got: " + precision
            );
        }
    }

    private double roundToPrecision(double value, int precision) {
        double multiplier = Math.pow(10, precision);
        return Math.round(value * multiplier) / multiplier;
    }


    // Geographical bounds for each locale
    private static class GeoBounds {

        final double minLat;
        final double maxLat;
        final double minLon;
        final double maxLon;

        GeoBounds(double minLat, double maxLat, double minLon, double maxLon) {
            this.minLat = minLat;
            this.maxLat = maxLat;
            this.minLon = minLon;
            this.maxLon = maxLon;
        }
    }
}
