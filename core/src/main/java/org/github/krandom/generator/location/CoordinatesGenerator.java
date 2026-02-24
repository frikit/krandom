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
 * Generates locale-specific geographical coordinates (latitude and longitude).
 *
 * <p>This generator creates realistic coordinates within the geographical bounds of each
 * supported locale. Coordinates are generated with configurable precision (1-10 decimal places),
 * with a default of 6 decimal places providing approximately 0.1 meter accuracy.
 *
 * <p>Built-in support covers 10 locales with their geographical bounding boxes:
 * <ul>
 *   <li>{@code en_US}: Continental United States
 *       <ul>
 *         <li>Latitude: 24.5° to 49.0° N</li>
 *         <li>Longitude: -125.0° to -66.0° E</li>
 *         <li>Example: "40.712776,-74.005974" (New York City area)</li>
 *       </ul>
 *   </li>
 *   <li>{@code en_GB}: United Kingdom
 *       <ul>
 *         <li>Latitude: 49.9° to 60.8° N</li>
 *         <li>Longitude: -8.2° to 1.8° E</li>
 *         <li>Example: "51.507351,-0.127758" (London area)</li>
 *       </ul>
 *   </li>
 *   <li>{@code en_AU}: Australia
 *       <ul>
 *         <li>Latitude: -44.0° to -10.0° S</li>
 *         <li>Longitude: 113.0° to 154.0° E</li>
 *         <li>Example: "-33.868820,151.209290" (Sydney area)</li>
 *       </ul>
 *   </li>
 *   <li>{@code de_DE}: Germany
 *       <ul>
 *         <li>Latitude: 47.3° to 55.0° N</li>
 *         <li>Longitude: 5.9° to 15.0° E</li>
 *         <li>Example: "52.520008,13.404954" (Berlin area)</li>
 *       </ul>
 *   </li>
 *   <li>{@code fr_FR}: France
 *       <ul>
 *         <li>Latitude: 41.3° to 51.1° N</li>
 *         <li>Longitude: -5.2° to 9.6° E</li>
 *         <li>Example: "48.856613,2.352222" (Paris area)</li>
 *       </ul>
 *   </li>
 *   <li>{@code es_ES}: Spain
 *       <ul>
 *         <li>Latitude: 36.0° to 43.8° N</li>
 *         <li>Longitude: -9.3° to 4.3° E</li>
 *         <li>Example: "40.416775,-3.703790" (Madrid area)</li>
 *       </ul>
 *   </li>
 *   <li>{@code it_IT}: Italy
 *       <ul>
 *         <li>Latitude: 36.6° to 47.1° N</li>
 *         <li>Longitude: 6.6° to 18.5° E</li>
 *         <li>Example: "41.902782,12.496366" (Rome area)</li>
 *       </ul>
 *   </li>
 *   <li>{@code pt_BR}: Brazil
 *       <ul>
 *         <li>Latitude: -33.7° to 5.3° S/N</li>
 *         <li>Longitude: -74.0° to -34.8° W</li>
 *         <li>Example: "-23.550520,-46.633308" (São Paulo area)</li>
 *       </ul>
 *   </li>
 *   <li>{@code ja_JP}: Japan
 *       <ul>
 *         <li>Latitude: 24.0° to 45.5° N</li>
 *         <li>Longitude: 122.9° to 153.9° E</li>
 *         <li>Example: "35.689487,139.691711" (Tokyo area)</li>
 *       </ul>
 *   </li>
 *   <li>{@code zh_CN}: China
 *       <ul>
 *         <li>Latitude: 18.2° to 53.6° N</li>
 *         <li>Longitude: 73.5° to 135.0° E</li>
 *         <li>Example: "39.904202,116.407394" (Beijing area)</li>
 *       </ul>
 *   </li>
 * </ul>
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

    private final GeneratorConfig config;
    private final Random random;
    private final Locale locale;
    private final GeoBounds bounds;

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
        this.random = config.getSeed().isPresent()
                ? new Random(config.getSeed().getAsLong())
                : new SecureRandom();
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
        return lat + "," + lon;
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

    // ── Helper methods ────────────────────────────────────────────────────────

    private GeoBounds getBoundsForLocale(Locale loc) {
        String localeKey = getLocaleKey(loc);

        return switch (localeKey) {
            case "en_US" -> US_BOUNDS;
            case "en_GB" -> GB_BOUNDS;
            case "en_AU" -> AU_BOUNDS;
            case "de_DE" -> DE_BOUNDS;
            case "fr_FR" -> FR_BOUNDS;
            case "es_ES" -> ES_BOUNDS;
            case "it_IT" -> IT_BOUNDS;
            case "pt_BR" -> BR_BOUNDS;
            case "ja_JP" -> JP_BOUNDS;
            case "zh_CN" -> CN_BOUNDS;
            default -> US_BOUNDS; // Default to US bounds
        };
    }

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
}
