/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.location;

import io.github.frikit.krandom.generator.Generator;
import io.github.frikit.krandom.generator.GeneratorConfig;

import java.util.Objects;

/**
 * Generates geohashes from locale-aware coordinates.
 */
public final class GeohashGenerator implements Generator<String> {

    /**
     * Default geohash precision.
     */
    public static final int DEFAULT_PRECISION = 12;

    /**
     * Minimum supported geohash precision.
     */
    public static final int MIN_PRECISION = 1;

    /**
     * Maximum supported geohash precision.
     */
    public static final int MAX_PRECISION = 12;

    private static final char[] BASE32 = "0123456789bcdefghjkmnpqrstuvwxyz".toCharArray();

    private final CoordinatesGenerator coordinates;
    private final int                  precision;

    /**
     * Creates a geohash generator with default precision and configuration.
     */
    public GeohashGenerator() {
        this(DEFAULT_PRECISION, GeneratorConfig.defaults());
    }

    /**
     * Creates a geohash generator with explicit precision and default configuration.
     *
     * @param precision geohash length, 1-12
     */
    public GeohashGenerator(int precision) {
        this(precision, GeneratorConfig.defaults());
    }

    /**
     * Creates a deterministic geohash generator with default precision.
     *
     * @param seed deterministic seed
     */
    public GeohashGenerator(long seed) {
        this(DEFAULT_PRECISION, GeneratorConfig.builder().seed(seed).build());
    }

    /**
     * Creates a deterministic geohash generator with explicit precision.
     *
     * @param precision geohash length, 1-12
     * @param seed      deterministic seed
     */
    public GeohashGenerator(int precision, long seed) {
        this(precision, GeneratorConfig.builder().seed(seed).build());
    }

    /**
     * Creates a geohash generator with default precision and explicit configuration.
     *
     * @param config generator configuration
     */
    public GeohashGenerator(GeneratorConfig config) {
        this(DEFAULT_PRECISION, config);
    }

    /**
     * Creates a geohash generator with explicit precision and configuration.
     *
     * @param precision geohash length, 1-12
     * @param config    generator configuration
     */
    public GeohashGenerator(int precision, GeneratorConfig config) {
        validatePrecision(precision);
        this.precision = precision;
        this.coordinates = new CoordinatesGenerator(Objects.requireNonNull(config, "config must not be null"));
    }

    /**
     * Encodes generated coordinates as a geohash.
     *
     * @return generated geohash
     */
    @Override
    public String generate() {
        return encode(coordinates.generateLatitude(), coordinates.generateLongitude(), precision);
    }

    /**
     * Encodes latitude and longitude into a geohash with default precision.
     *
     * @param latitude  latitude in [-90, 90]
     * @param longitude longitude in [-180, 180]
     * @return geohash
     */
    public static String encode(double latitude, double longitude) {
        return encode(latitude, longitude, DEFAULT_PRECISION);
    }

    /**
     * Encodes latitude and longitude into a geohash.
     *
     * @param latitude  latitude in [-90, 90]
     * @param longitude longitude in [-180, 180]
     * @param precision geohash length, 1-12
     * @return geohash
     */
    public static String encode(double latitude, double longitude, int precision) {
        validateCoordinate(latitude, -90.0, 90.0, "latitude");
        validateCoordinate(longitude, -180.0, 180.0, "longitude");
        validatePrecision(precision);

        double minLat = -90.0;
        double maxLat = 90.0;
        double minLon = -180.0;
        double maxLon = 180.0;
        boolean evenBit = true;
        int bit = 0;
        int ch = 0;
        StringBuilder hash = new StringBuilder(precision);

        while (hash.length() < precision) {
            if (evenBit) {
                double mid = (minLon + maxLon) / 2.0;
                if (longitude >= mid) {
                    ch |= 1 << (4 - bit);
                    minLon = mid;
                } else {
                    maxLon = mid;
                }
            } else {
                double mid = (minLat + maxLat) / 2.0;
                if (latitude >= mid) {
                    ch |= 1 << (4 - bit);
                    minLat = mid;
                } else {
                    maxLat = mid;
                }
            }

            evenBit = !evenBit;
            if (bit < 4) {
                bit++;
            } else {
                hash.append(BASE32[ch]);
                bit = 0;
                ch = 0;
            }
        }
        return hash.toString();
    }

    private static void validatePrecision(int precision) {
        if (precision < MIN_PRECISION || precision > MAX_PRECISION) {
            throw new IllegalArgumentException(
                "precision must be between " + MIN_PRECISION + " and " + MAX_PRECISION + ", got: " + precision);
        }
    }

    private static void validateCoordinate(double value, double min, double max, String name) {
        if (!Double.isFinite(value) || value < min || value > max) {
            throw new IllegalArgumentException(name + " must be finite and between " + min + " and " + max
                                               + ", got: " + value);
        }
    }
}
