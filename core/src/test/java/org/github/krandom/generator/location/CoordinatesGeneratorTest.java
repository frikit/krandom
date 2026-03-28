/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package org.github.krandom.generator.location;

import org.github.krandom.generator.GeneratorConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("CoordinatesGenerator")
class CoordinatesGeneratorTest {

    private static final double EPSILON = 0.0000001; // For floating point comparisons

    // ── Constructor tests ─────────────────────────────────────────────────────

    @Test
    @DisplayName("default constructor uses US locale")
    void defaultConstructorUsesUSLocale() {
        CoordinatesGenerator gen = new CoordinatesGenerator();
        assertEquals(Locale.US, gen.getLocale());
    }

    @Test
    @DisplayName("constructor with config accepts config")
    void constructorWithConfig() {
        GeneratorConfig config = GeneratorConfig.builder()
                                                .locale(Locale.GERMANY)
                                                .seed(42L)
                                                .build();

        CoordinatesGenerator gen = new CoordinatesGenerator(config);
        assertEquals(Locale.GERMANY, gen.getLocale());
    }

    @Test
    @DisplayName("constructor with locale accepts locale")
    void constructorWithLocale() {
        CoordinatesGenerator gen = new CoordinatesGenerator(Locale.JAPAN);
        assertEquals(Locale.JAPAN, gen.getLocale());
    }

    @Test
    @DisplayName("null config throws NullPointerException")
    void nullConfigThrows() {
        NullPointerException ex = assertThrows(
            NullPointerException.class,
            () -> new CoordinatesGenerator((GeneratorConfig) null)
        );
        assertTrue(ex.getMessage().contains("config must not be null"));
    }

    @Test
    @DisplayName("null locale throws NullPointerException")
    void nullLocaleThrows() {
        NullPointerException ex = assertThrows(
            NullPointerException.class,
            () -> new CoordinatesGenerator((Locale) null)
        );
        assertTrue(ex.getMessage().contains("locale must not be null"));
    }

    // ── US locale (en_US) tests ───────────────────────────────────────────────

    @Test
    @DisplayName("US locale has correct bounds")
    void usBounds() {
        CoordinatesGenerator gen = new CoordinatesGenerator(Locale.US);

        assertEquals(24.5, gen.getMinLatitude(), EPSILON);
        assertEquals(49.0, gen.getMaxLatitude(), EPSILON);
        assertEquals(-125.0, gen.getMinLongitude(), EPSILON);
        assertEquals(-66.0, gen.getMaxLongitude(), EPSILON);
    }

    @Test
    @DisplayName("US locale generates latitude within bounds")
    void usLatitudeWithinBounds() {
        CoordinatesGenerator gen = new CoordinatesGenerator(Locale.US);

        for (int i = 0; i < 100; i++) {
            double lat = gen.generateLatitude();
            assertTrue(lat >= 24.5 && lat <= 49.0,
                       "Latitude " + lat + " is outside US bounds");
        }
    }

    @Test
    @DisplayName("US locale generates longitude within bounds")
    void usLongitudeWithinBounds() {
        CoordinatesGenerator gen = new CoordinatesGenerator(Locale.US);

        for (int i = 0; i < 100; i++) {
            double lon = gen.generateLongitude();
            assertTrue(lon >= -125.0 && lon <= -66.0,
                       "Longitude " + lon + " is outside US bounds");
        }
    }

    @Test
    @DisplayName("US locale generates coordinate pairs")
    void usCoordinatePairs() {
        CoordinatesGenerator gen = new CoordinatesGenerator(Locale.US);
        String coords = gen.generate();

        assertNotNull(coords);
        assertTrue(coords.matches("-?\\d+\\.\\d+,-?\\d+\\.\\d+"),
                   "Expected 'lat,lon' format, got: " + coords);

        String[] parts = coords.split(",");
        double lat = Double.parseDouble(parts[0]);
        double lon = Double.parseDouble(parts[1]);

        assertTrue(lat >= 24.5 && lat <= 49.0);
        assertTrue(lon >= -125.0 && lon <= -66.0);
    }

    // ── UK locale (en_GB) tests ───────────────────────────────────────────────

    @Test
    @DisplayName("UK locale has correct bounds")
    void ukBounds() {
        CoordinatesGenerator gen = new CoordinatesGenerator(Locale.UK);

        assertEquals(49.9, gen.getMinLatitude(), EPSILON);
        assertEquals(60.8, gen.getMaxLatitude(), EPSILON);
        assertEquals(-8.2, gen.getMinLongitude(), EPSILON);
        assertEquals(1.8, gen.getMaxLongitude(), EPSILON);
    }

    @Test
    @DisplayName("UK locale generates latitude within bounds")
    void ukLatitudeWithinBounds() {
        CoordinatesGenerator gen = new CoordinatesGenerator(Locale.UK);

        for (int i = 0; i < 100; i++) {
            double lat = gen.generateLatitude();
            assertTrue(lat >= 49.9 && lat <= 60.8,
                       "Latitude " + lat + " is outside UK bounds");
        }
    }

    @Test
    @DisplayName("UK locale generates longitude within bounds")
    void ukLongitudeWithinBounds() {
        CoordinatesGenerator gen = new CoordinatesGenerator(Locale.UK);

        for (int i = 0; i < 100; i++) {
            double lon = gen.generateLongitude();
            assertTrue(lon >= -8.2 && lon <= 1.8,
                       "Longitude " + lon + " is outside UK bounds");
        }
    }

    // ── Australia locale (en_AU) tests ────────────────────────────────────────

    @Test
    @DisplayName("Australia locale has correct bounds")
    void australiaBounds() {
        CoordinatesGenerator gen = new CoordinatesGenerator(
            new Locale("en", "AU")
        );

        assertEquals(-44.0, gen.getMinLatitude(), EPSILON);
        assertEquals(-10.0, gen.getMaxLatitude(), EPSILON);
        assertEquals(113.0, gen.getMinLongitude(), EPSILON);
        assertEquals(154.0, gen.getMaxLongitude(), EPSILON);
    }

    @Test
    @DisplayName("Australia locale generates latitude within bounds")
    void australiaLatitudeWithinBounds() {
        CoordinatesGenerator gen = new CoordinatesGenerator(new Locale("en", "AU"));

        for (int i = 0; i < 100; i++) {
            double lat = gen.generateLatitude();
            assertTrue(lat >= -44.0 && lat <= -10.0,
                       "Latitude " + lat + " is outside Australia bounds");
        }
    }

    @Test
    @DisplayName("Australia locale generates longitude within bounds")
    void australiaLongitudeWithinBounds() {
        CoordinatesGenerator gen = new CoordinatesGenerator(new Locale("en", "AU"));

        for (int i = 0; i < 100; i++) {
            double lon = gen.generateLongitude();
            assertTrue(lon >= 113.0 && lon <= 154.0,
                       "Longitude " + lon + " is outside Australia bounds");
        }
    }

    // ── Germany locale (de_DE) tests ──────────────────────────────────────────

    @Test
    @DisplayName("Germany locale has correct bounds")
    void germanyBounds() {
        CoordinatesGenerator gen = new CoordinatesGenerator(Locale.GERMANY);

        assertEquals(47.3, gen.getMinLatitude(), EPSILON);
        assertEquals(55.0, gen.getMaxLatitude(), EPSILON);
        assertEquals(5.9, gen.getMinLongitude(), EPSILON);
        assertEquals(15.0, gen.getMaxLongitude(), EPSILON);
    }

    @Test
    @DisplayName("Germany locale generates latitude within bounds")
    void germanyLatitudeWithinBounds() {
        CoordinatesGenerator gen = new CoordinatesGenerator(Locale.GERMANY);

        for (int i = 0; i < 100; i++) {
            double lat = gen.generateLatitude();
            assertTrue(lat >= 47.3 && lat <= 55.0,
                       "Latitude " + lat + " is outside Germany bounds");
        }
    }

    @Test
    @DisplayName("Germany locale generates longitude within bounds")
    void germanyLongitudeWithinBounds() {
        CoordinatesGenerator gen = new CoordinatesGenerator(Locale.GERMANY);

        for (int i = 0; i < 100; i++) {
            double lon = gen.generateLongitude();
            assertTrue(lon >= 5.9 && lon <= 15.0,
                       "Longitude " + lon + " is outside Germany bounds");
        }
    }

    // ── France locale (fr_FR) tests ───────────────────────────────────────────

    @Test
    @DisplayName("France locale has correct bounds")
    void franceBounds() {
        CoordinatesGenerator gen = new CoordinatesGenerator(Locale.FRANCE);

        assertEquals(41.3, gen.getMinLatitude(), EPSILON);
        assertEquals(51.1, gen.getMaxLatitude(), EPSILON);
        assertEquals(-5.2, gen.getMinLongitude(), EPSILON);
        assertEquals(9.6, gen.getMaxLongitude(), EPSILON);
    }

    @Test
    @DisplayName("France locale generates latitude within bounds")
    void franceLatitudeWithinBounds() {
        CoordinatesGenerator gen = new CoordinatesGenerator(Locale.FRANCE);

        for (int i = 0; i < 100; i++) {
            double lat = gen.generateLatitude();
            assertTrue(lat >= 41.3 && lat <= 51.1,
                       "Latitude " + lat + " is outside France bounds");
        }
    }

    @Test
    @DisplayName("France locale generates longitude within bounds")
    void franceLongitudeWithinBounds() {
        CoordinatesGenerator gen = new CoordinatesGenerator(Locale.FRANCE);

        for (int i = 0; i < 100; i++) {
            double lon = gen.generateLongitude();
            assertTrue(lon >= -5.2 && lon <= 9.6,
                       "Longitude " + lon + " is outside France bounds");
        }
    }

    // ── Spain locale (es_ES) tests ────────────────────────────────────────────

    @Test
    @DisplayName("Spain locale has correct bounds")
    void spainBounds() {
        CoordinatesGenerator gen = new CoordinatesGenerator(
            new Locale("es", "ES")
        );

        assertEquals(36.0, gen.getMinLatitude(), EPSILON);
        assertEquals(43.8, gen.getMaxLatitude(), EPSILON);
        assertEquals(-9.3, gen.getMinLongitude(), EPSILON);
        assertEquals(4.3, gen.getMaxLongitude(), EPSILON);
    }

    @Test
    @DisplayName("Spain locale generates latitude within bounds")
    void spainLatitudeWithinBounds() {
        CoordinatesGenerator gen = new CoordinatesGenerator(new Locale("es", "ES"));

        for (int i = 0; i < 100; i++) {
            double lat = gen.generateLatitude();
            assertTrue(lat >= 36.0 && lat <= 43.8,
                       "Latitude " + lat + " is outside Spain bounds");
        }
    }

    @Test
    @DisplayName("Spain locale generates longitude within bounds")
    void spainLongitudeWithinBounds() {
        CoordinatesGenerator gen = new CoordinatesGenerator(new Locale("es", "ES"));

        for (int i = 0; i < 100; i++) {
            double lon = gen.generateLongitude();
            assertTrue(lon >= -9.3 && lon <= 4.3,
                       "Longitude " + lon + " is outside Spain bounds");
        }
    }

    // ── Italy locale (it_IT) tests ────────────────────────────────────────────

    @Test
    @DisplayName("Italy locale has correct bounds")
    void italyBounds() {
        CoordinatesGenerator gen = new CoordinatesGenerator(Locale.ITALY);

        assertEquals(36.6, gen.getMinLatitude(), EPSILON);
        assertEquals(47.1, gen.getMaxLatitude(), EPSILON);
        assertEquals(6.6, gen.getMinLongitude(), EPSILON);
        assertEquals(18.5, gen.getMaxLongitude(), EPSILON);
    }

    @Test
    @DisplayName("Italy locale generates latitude within bounds")
    void italyLatitudeWithinBounds() {
        CoordinatesGenerator gen = new CoordinatesGenerator(Locale.ITALY);

        for (int i = 0; i < 100; i++) {
            double lat = gen.generateLatitude();
            assertTrue(lat >= 36.6 && lat <= 47.1,
                       "Latitude " + lat + " is outside Italy bounds");
        }
    }

    @Test
    @DisplayName("Italy locale generates longitude within bounds")
    void italyLongitudeWithinBounds() {
        CoordinatesGenerator gen = new CoordinatesGenerator(Locale.ITALY);

        for (int i = 0; i < 100; i++) {
            double lon = gen.generateLongitude();
            assertTrue(lon >= 6.6 && lon <= 18.5,
                       "Longitude " + lon + " is outside Italy bounds");
        }
    }

    // ── Brazil locale (pt_BR) tests ───────────────────────────────────────────

    @Test
    @DisplayName("Brazil locale has correct bounds")
    void brazilBounds() {
        CoordinatesGenerator gen = new CoordinatesGenerator(
            new Locale("pt", "BR")
        );

        assertEquals(-33.7, gen.getMinLatitude(), EPSILON);
        assertEquals(5.3, gen.getMaxLatitude(), EPSILON);
        assertEquals(-74.0, gen.getMinLongitude(), EPSILON);
        assertEquals(-34.8, gen.getMaxLongitude(), EPSILON);
    }

    @Test
    @DisplayName("Brazil locale generates latitude within bounds")
    void brazilLatitudeWithinBounds() {
        CoordinatesGenerator gen = new CoordinatesGenerator(new Locale("pt", "BR"));

        for (int i = 0; i < 100; i++) {
            double lat = gen.generateLatitude();
            assertTrue(lat >= -33.7 && lat <= 5.3,
                       "Latitude " + lat + " is outside Brazil bounds");
        }
    }

    @Test
    @DisplayName("Brazil locale generates longitude within bounds")
    void brazilLongitudeWithinBounds() {
        CoordinatesGenerator gen = new CoordinatesGenerator(new Locale("pt", "BR"));

        for (int i = 0; i < 100; i++) {
            double lon = gen.generateLongitude();
            assertTrue(lon >= -74.0 && lon <= -34.8,
                       "Longitude " + lon + " is outside Brazil bounds");
        }
    }

    // ── Japan locale (ja_JP) tests ────────────────────────────────────────────

    @Test
    @DisplayName("Japan locale has correct bounds")
    void japanBounds() {
        CoordinatesGenerator gen = new CoordinatesGenerator(Locale.JAPAN);

        assertEquals(24.0, gen.getMinLatitude(), EPSILON);
        assertEquals(45.5, gen.getMaxLatitude(), EPSILON);
        assertEquals(122.9, gen.getMinLongitude(), EPSILON);
        assertEquals(153.9, gen.getMaxLongitude(), EPSILON);
    }

    @Test
    @DisplayName("Japan locale generates latitude within bounds")
    void japanLatitudeWithinBounds() {
        CoordinatesGenerator gen = new CoordinatesGenerator(Locale.JAPAN);

        for (int i = 0; i < 100; i++) {
            double lat = gen.generateLatitude();
            assertTrue(lat >= 24.0 && lat <= 45.5,
                       "Latitude " + lat + " is outside Japan bounds");
        }
    }

    @Test
    @DisplayName("Japan locale generates longitude within bounds")
    void japanLongitudeWithinBounds() {
        CoordinatesGenerator gen = new CoordinatesGenerator(Locale.JAPAN);

        for (int i = 0; i < 100; i++) {
            double lon = gen.generateLongitude();
            assertTrue(lon >= 122.9 && lon <= 153.9,
                       "Longitude " + lon + " is outside Japan bounds");
        }
    }

    // ── China locale (zh_CN) tests ────────────────────────────────────────────

    @Test
    @DisplayName("China locale has correct bounds")
    void chinaBounds() {
        CoordinatesGenerator gen = new CoordinatesGenerator(Locale.CHINA);

        assertEquals(18.2, gen.getMinLatitude(), EPSILON);
        assertEquals(53.6, gen.getMaxLatitude(), EPSILON);
        assertEquals(73.5, gen.getMinLongitude(), EPSILON);
        assertEquals(135.0, gen.getMaxLongitude(), EPSILON);
    }

    @Test
    @DisplayName("China locale generates latitude within bounds")
    void chinaLatitudeWithinBounds() {
        CoordinatesGenerator gen = new CoordinatesGenerator(Locale.CHINA);

        for (int i = 0; i < 100; i++) {
            double lat = gen.generateLatitude();
            assertTrue(lat >= 18.2 && lat <= 53.6,
                       "Latitude " + lat + " is outside China bounds");
        }
    }

    @Test
    @DisplayName("China locale generates longitude within bounds")
    void chinaLongitudeWithinBounds() {
        CoordinatesGenerator gen = new CoordinatesGenerator(Locale.CHINA);

        for (int i = 0; i < 100; i++) {
            double lon = gen.generateLongitude();
            assertTrue(lon >= 73.5 && lon <= 135.0,
                       "Longitude " + lon + " is outside China bounds");
        }
    }

    // ── Precision tests ───────────────────────────────────────────────────────

    @Test
    @DisplayName("default precision is 6 decimal places")
    void defaultPrecision() {
        CoordinatesGenerator gen = new CoordinatesGenerator(Locale.US);
        double lat = gen.generateLatitude();

        String latStr = String.valueOf(lat);
        int decimalPlaces = latStr.length() - latStr.indexOf('.') - 1;
        assertTrue(decimalPlaces <= 6,
                   "Expected at most 6 decimal places, got: " + decimalPlaces);
    }

    @Test
    @DisplayName("precision=1 generates 1 decimal place")
    void precision1() {
        CoordinatesGenerator gen = new CoordinatesGenerator(Locale.US);
        double lat = gen.generateLatitude(1);

        String latStr = String.valueOf(lat);
        int decimalPlaces = latStr.length() - latStr.indexOf('.') - 1;
        assertTrue(decimalPlaces <= 1,
                   "Expected at most 1 decimal place, got: " + decimalPlaces);
    }

    @Test
    @DisplayName("precision=3 generates 3 decimal places")
    void precision3() {
        CoordinatesGenerator gen = new CoordinatesGenerator(Locale.US);
        double lat = gen.generateLatitude(3);

        String latStr = String.valueOf(lat);
        int decimalPlaces = latStr.length() - latStr.indexOf('.') - 1;
        assertTrue(decimalPlaces <= 3,
                   "Expected at most 3 decimal places, got: " + decimalPlaces);
    }

    @Test
    @DisplayName("precision=10 generates 10 decimal places")
    void precision10() {
        CoordinatesGenerator gen = new CoordinatesGenerator(Locale.US);
        double lat = gen.generateLatitude(10);

        String latStr = String.valueOf(lat);
        int decimalPlaces = latStr.length() - latStr.indexOf('.') - 1;
        assertTrue(decimalPlaces <= 10,
                   "Expected at most 10 decimal places, got: " + decimalPlaces);
    }

    @Test
    @DisplayName("longitude respects precision")
    void longitudePrecision() {
        CoordinatesGenerator gen = new CoordinatesGenerator(Locale.US);
        double lon = gen.generateLongitude(2);

        String lonStr = String.valueOf(lon);
        int decimalPlaces = lonStr.length() - lonStr.indexOf('.') - 1;
        assertTrue(decimalPlaces <= 2,
                   "Expected at most 2 decimal places, got: " + decimalPlaces);
    }

    @Test
    @DisplayName("precision < 1 throws IllegalArgumentException")
    void precisionTooLow() {
        CoordinatesGenerator gen = new CoordinatesGenerator(Locale.US);

        IllegalArgumentException ex = assertThrows(
            IllegalArgumentException.class,
            () -> gen.generateLatitude(0)
        );
        assertTrue(ex.getMessage().contains("precision must be between 1 and 10"));
    }

    @Test
    @DisplayName("precision > 10 throws IllegalArgumentException")
    void precisionTooHigh() {
        CoordinatesGenerator gen = new CoordinatesGenerator(Locale.US);

        IllegalArgumentException ex = assertThrows(
            IllegalArgumentException.class,
            () -> gen.generateLatitude(11)
        );
        assertTrue(ex.getMessage().contains("precision must be between 1 and 10"));
    }

    @Test
    @DisplayName("negative precision throws IllegalArgumentException")
    void negativePrecision() {
        CoordinatesGenerator gen = new CoordinatesGenerator(Locale.US);

        assertThrows(
            IllegalArgumentException.class,
            () -> gen.generateLongitude(-1)
        );
    }

    // ── Seeding and reproducibility tests ─────────────────────────────────────

    @Test
    @DisplayName("seeded generator produces reproducible results")
    void seededReproducibility() {
        GeneratorConfig config1 = GeneratorConfig.builder()
                                                 .locale(Locale.US)
                                                 .seed(42L)
                                                 .build();
        GeneratorConfig config2 = GeneratorConfig.builder()
                                                 .locale(Locale.US)
                                                 .seed(42L)
                                                 .build();

        CoordinatesGenerator gen1 = new CoordinatesGenerator(config1);
        CoordinatesGenerator gen2 = new CoordinatesGenerator(config2);

        for (int i = 0; i < 10; i++) {
            assertEquals(gen1.generate(), gen2.generate());
        }
    }

    @Test
    @DisplayName("different seeds produce different results")
    void differentSeeds() {
        GeneratorConfig config1 = GeneratorConfig.builder()
                                                 .locale(Locale.US)
                                                 .seed(42L)
                                                 .build();
        GeneratorConfig config2 = GeneratorConfig.builder()
                                                 .locale(Locale.US)
                                                 .seed(123L)
                                                 .build();

        CoordinatesGenerator gen1 = new CoordinatesGenerator(config1);
        CoordinatesGenerator gen2 = new CoordinatesGenerator(config2);

        assertNotEquals(gen1.generate(), gen2.generate());
    }

    @Test
    @DisplayName("unseeded generators produce variety")
    void unseededVariety() {
        CoordinatesGenerator gen = new CoordinatesGenerator(Locale.US);

        Set<String> seen = new HashSet<>();
        for (int i = 0; i < 50; i++) {
            seen.add(gen.generate());
        }

        assertTrue(seen.size() > 45,
                   "Expected high variety, got only " + seen.size() + " unique values");
    }

    // ── generateList() and stream() tests ─────────────────────────────────────

    @Test
    @DisplayName("generateList produces correct count")
    void generateListCount() {
        CoordinatesGenerator gen = new CoordinatesGenerator(Locale.US);
        List<String> coords = gen.generateList(10);

        assertEquals(10, coords.size());
    }

    @Test
    @DisplayName("generateList produces valid coordinates")
    void generateListValid() {
        CoordinatesGenerator gen = new CoordinatesGenerator(Locale.US);
        List<String> coords = gen.generateList(5);

        for (String coord : coords) {
            assertTrue(coord.matches("-?\\d+\\.\\d+,-?\\d+\\.\\d+"),
                       "Invalid coordinate format: " + coord);
        }
    }

    @Test
    @DisplayName("stream produces correct count")
    void streamCount() {
        CoordinatesGenerator gen = new CoordinatesGenerator(Locale.US);
        long count = gen.stream().limit(20).count();

        assertEquals(20, count);
    }

    @Test
    @DisplayName("stream produces valid coordinates")
    void streamValid() {
        CoordinatesGenerator gen = new CoordinatesGenerator(Locale.UK);

        gen.stream().limit(10).forEach(coord -> {
            assertTrue(coord.matches("-?\\d+\\.\\d+,-?\\d+\\.\\d+"),
                       "Invalid coordinate format: " + coord);
        });
    }

    // ── Edge case tests ───────────────────────────────────────────────────────

    @Test
    @DisplayName("coordinates are within valid global ranges")
    void withinGlobalRanges() {
        CoordinatesGenerator gen = new CoordinatesGenerator(Locale.US);

        for (int i = 0; i < 100; i++) {
            double lat = gen.generateLatitude();
            double lon = gen.generateLongitude();

            assertTrue(lat >= -90 && lat <= 90,
                       "Latitude " + lat + " is outside valid range [-90, 90]");
            assertTrue(lon >= -180 && lon <= 180,
                       "Longitude " + lon + " is outside valid range [-180, 180]");
        }
    }

    @Test
    @DisplayName("all locales produce coordinates within global bounds")
    void allLocalesWithinGlobalBounds() {
        Locale[] locales = {
            Locale.US, Locale.UK, new Locale("en", "AU"),
            Locale.GERMANY, Locale.FRANCE, new Locale("es", "ES"),
            Locale.ITALY, new Locale("pt", "BR"), Locale.JAPAN, Locale.CHINA
        };

        for (Locale locale : locales) {
            CoordinatesGenerator gen = new CoordinatesGenerator(locale);

            for (int i = 0; i < 10; i++) {
                double lat = gen.generateLatitude();
                double lon = gen.generateLongitude();

                assertTrue(lat >= -90 && lat <= 90,
                           locale + ": Latitude out of range: " + lat);
                assertTrue(lon >= -180 && lon <= 180,
                           locale + ": Longitude out of range: " + lon);
            }
        }
    }

    @Test
    @DisplayName("generate returns non-null")
    void generateNonNull() {
        CoordinatesGenerator gen = new CoordinatesGenerator(Locale.US);
        assertNotNull(gen.generate());
    }

    @Test
    @DisplayName("generate returns properly formatted string")
    void generateProperFormat() {
        CoordinatesGenerator gen = new CoordinatesGenerator(Locale.GERMANY);
        String coords = gen.generate();

        assertNotNull(coords);
        assertTrue(coords.contains(","));
        assertEquals(2, coords.split(",").length);
    }

    @Test
    @DisplayName("coordinates vary within locale bounds")
    void coordinatesVary() {
        CoordinatesGenerator gen = new CoordinatesGenerator(Locale.FRANCE);

        Set<Double> latitudes = new HashSet<>();
        Set<Double> longitudes = new HashSet<>();

        for (int i = 0; i < 50; i++) {
            latitudes.add(gen.generateLatitude());
            longitudes.add(gen.generateLongitude());
        }

        assertTrue(latitudes.size() > 45, "Expected variety in latitudes");
        assertTrue(longitudes.size() > 45, "Expected variety in longitudes");
    }

    @Test
    @DisplayName("unknown locale defaults to US bounds")
    void unknownLocaleDefaultsToUS() {
        CoordinatesGenerator gen = new CoordinatesGenerator(new Locale("xx", "XX"));

        // Should use US bounds
        assertEquals(24.5, gen.getMinLatitude(), EPSILON);
        assertEquals(49.0, gen.getMaxLatitude(), EPSILON);
        assertEquals(-125.0, gen.getMinLongitude(), EPSILON);
        assertEquals(-66.0, gen.getMaxLongitude(), EPSILON);
    }

    @Test
    @DisplayName("precision constants are correct")
    void precisionConstants() {
        assertEquals(6, CoordinatesGenerator.DEFAULT_PRECISION);
        assertEquals(1, CoordinatesGenerator.MIN_PRECISION);
        assertEquals(10, CoordinatesGenerator.MAX_PRECISION);
    }

    @Test
    @DisplayName("locale with language only defaults to US bounds")
    void localeLanguageOnlyDefaultsToUS() {
        CoordinatesGenerator gen = new CoordinatesGenerator(new Locale("en"));

        // Should use US bounds
        assertEquals(24.5, gen.getMinLatitude(), EPSILON);
        assertEquals(49.0, gen.getMaxLatitude(), EPSILON);
        assertEquals(-125.0, gen.getMinLongitude(), EPSILON);
        assertEquals(-66.0, gen.getMaxLongitude(), EPSILON);
    }
}
