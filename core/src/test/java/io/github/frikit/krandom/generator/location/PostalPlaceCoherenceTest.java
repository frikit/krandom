/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.location;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.frikit.krandom.generator.GeneratorConfig;
import io.github.frikit.krandom.generator.object.ObjectGenerator;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Over many seeds, every address of a locale with bundled postal places names a city, a region and a
 * postal code of one row of that locale's table, which this test reads on its own rather than through
 * the library's loader.
 */
@DisplayName("Postal places keep addresses coherent")
class PostalPlaceCoherenceTest {

    private static final int SEEDS = 1_000;

    /** One row of a bundled table, with the region code from the states file. */
    private record Row(String city, String region, String regionCode, List<String> postalCodes) {

        String regionLabel() {
            return regionCode.isEmpty() ? region : regionCode;
        }
    }

    /** A locale with bundled places, the table it resolves and its postal-code format. */
    private record Case(Locale locale, String table, Pattern postalFormat) {

        @Override
        public String toString() {
            return locale.toString();
        }
    }

    static Stream<Case> cases() {
        Pattern fiveDigits = Pattern.compile("[0-9]{5}");
        return Stream.of(
                new Case(Locale.US, "en_US", fiveDigits),
                new Case(Locale.UK, "en_GB", Pattern.compile("[A-Z]{1,2}[0-9][A-Z0-9]? [0-9][A-Z]{2}")),
                new Case(Locale.GERMANY, "de_DE", fiveDigits),
                new Case(Locale.FRANCE, "fr_FR", fiveDigits));
    }

    /** A locale that resolves another locale's table, and the country its addresses name. */
    static Stream<Arguments> borrowingLocales() {
        return Stream.of(
                Arguments.of(new Locale("es", "US"), "en_US", "Estados Unidos", "US"),
                Arguments.of(new Locale("en", "DE"), "de_DE", "Germany", "DE"),
                Arguments.of(Locale.ENGLISH, "en_US", "United States", "US"),
                Arguments.of(Locale.GERMAN, "de_DE", "Deutschland", "DE"),
                Arguments.of(Locale.FRENCH, "fr_FR", "France", "FR"));
    }

    static Stream<Arguments> tables() {
        return cases().map(c -> Arguments.of(c.table(), c.postalFormat()));
    }

    @ParameterizedTest
    @MethodSource("tables")
    @DisplayName("every bundled row is well formed and names a region of the locale")
    void bundledRowsAreWellFormed(String table, Pattern postalFormat) {
        List<Row> rows = rows(table);
        Set<String> keys = new HashSet<>();
        for (Row row : rows) {
            assertTrue(keys.add(row.city() + "|" + row.region()), "duplicate row " + row);
            assertFalse(row.postalCodes().isEmpty(), row.toString());
            assertTrue(row.postalCodes().size() <= 10, row.toString());
            assertEquals(row.postalCodes().size(), Set.copyOf(row.postalCodes()).size(), row.toString());
            for (String code : row.postalCodes()) {
                assertTrue(postalFormat.matcher(code).matches(), row.city() + " " + code);
            }
        }
        String header = header(table);
        for (String field : List.of("# Source:", "# Licence:", "# Retrieved:", "SHA-256")) {
            assertTrue(header.contains(field), table + " header lacks " + field);
        }
    }

    @ParameterizedTest
    @MethodSource("cases")
    @DisplayName("AddressInfo takes its city, region and postal code from one place")
    void addressInfoAgreesWithOnePlace(Case c) {
        List<Row> rows = rows(c.table());
        Set<String> cities = new HashSet<>();
        for (long seed = 0; seed < SEEDS; seed++) {
            AddressInfo info = new AddressInfoGenerator(config(c.locale(), seed)).generate();
            Row row = rowOf(rows, info.city(), info.state());
            assertEquals(row.regionCode(), info.stateAbbr(), info.toString());
            assertTrue(row.postalCodes().contains(info.zip()), info.toString());
            assertEquals(c.locale().getCountry(), info.countryAbbr(), info.toString());
            assertTrue(
                    info.address()
                            .endsWith(", " + info.city() + ", " + row.regionLabel() + " " + info.zip() + ", "
                                    + info.country()),
                    info.address());
            cities.add(info.city());
        }
        assertTrue(cities.size() > rows.size() / 2, "draws reach " + cities.size() + " of " + rows.size() + " places");
    }

    @ParameterizedTest
    @MethodSource("cases")
    @DisplayName("one seeded generator stays coherent draw after draw")
    void consecutiveDrawsStayCoherent(Case c) {
        List<Row> rows = rows(c.table());
        AddressInfoGenerator generator = new AddressInfoGenerator(config(c.locale(), 42L));
        for (int i = 0; i < SEEDS; i++) {
            AddressInfo info = generator.generate();
            assertTrue(rowOf(rows, info.city(), info.state()).postalCodes().contains(info.zip()), info.toString());
        }
    }

    @ParameterizedTest
    @MethodSource("cases")
    @DisplayName("a full street address names one place and the locale's country")
    void fullAddressAgreesWithOnePlace(Case c) {
        List<Row> rows = rows(c.table());
        for (long seed = 0; seed < SEEDS; seed++) {
            GeneratorConfig config = config(c.locale(), seed);
            String full = new StreetAddressGenerator(config).generateFullAddress();
            String country = new CountryGenerator(config).currentCountry();
            assertTrue(full.endsWith(", " + country), full);
            String locality = full.substring(0, full.length() - country.length() - 2);
            assertTrue(
                    rows.stream()
                            .anyMatch(row -> row.postalCodes().stream()
                                    .anyMatch(code -> locality.endsWith(
                                            ", " + row.city() + ", " + row.regionLabel() + " " + code))),
                    full);
        }
    }

    @ParameterizedTest
    @MethodSource("cases")
    @DisplayName("object members named city, state and postalCode come from one place")
    void objectAddressMembersAgreeWithOnePlace(Case c) {
        List<Row> rows = rows(c.table());
        for (long seed = 0; seed < 200; seed++) {
            Customer customer = new ObjectGenerator<>(Customer.class, config(c.locale(), seed)).generate();
            Row row = rowOf(rows, customer.city, customer.state);
            assertTrue(row.postalCodes().contains(customer.postalCode), customer.city + " " + customer.postalCode);
        }
    }

    @ParameterizedTest
    @MethodSource("borrowingLocales")
    @DisplayName("another language of a country, or a language without a country, uses the country's places")
    void borrowingLocalesUseTheCountrysPlaces(Locale locale, String table, String country, String countryCode) {
        List<Row> rows = rows(table);
        for (long seed = 0; seed < 200; seed++) {
            AddressInfo info = new AddressInfoGenerator(config(locale, seed)).generate();
            assertTrue(rowOf(rows, info.city(), info.state()).postalCodes().contains(info.zip()), info.toString());
            assertEquals(countryCode, info.countryAbbr());
            assertEquals(country, info.country());
        }
    }

    @Test
    @DisplayName("a country without places composes its addresses from independent draws")
    void countryWithoutPlacesDrawsIndependently() {
        Pattern canadianPostalCode = Pattern.compile("[A-Z][0-9][A-Z] [0-9][A-Z][0-9]");
        AddressInfo info = new AddressInfoGenerator(config(Locale.CANADA, 7L)).generate();

        assertTrue(canadianPostalCode.matcher(info.zip()).matches(), info.zip());
        assertEquals("CA", info.countryAbbr());
        for (String fallback : List.of("en_CA", "en_IE", "de_AT", "de_CH", "fr_BE", "fr_CA", "fr_CH")) {
            Locale locale = Locale.forLanguageTag(fallback.replace('_', '-'));
            assertEquals(null, PostalPlaceDataRegistry.forLocale(locale), fallback);
        }
    }

    @Test
    @DisplayName("the en_GB places leave out Northern Ireland, which OS Open Names does not cover")
    void britishPlacesLeaveOutNorthernIreland() {
        List<Row> rows = rows("en_GB");

        assertEquals(
                Set.of("England", "Scotland", "Wales"),
                Set.copyOf(rows.stream().map(Row::region).toList()));
        for (String city : List.of("Armagh", "Belfast", "Lisburn", "Londonderry", "Newry")) {
            assertTrue(rows.stream().noneMatch(row -> row.city().equals(city)), city);
        }
    }

    @Test
    @DisplayName("every de_DE place has the one postal code of its administrative seat")
    void germanPlacesHaveOnePostalCode() {
        for (Row row : rows("de_DE")) {
            assertEquals(1, row.postalCodes().size(), row.toString());
        }
    }

    /** A record whose members the object engine fills from one coherent address. */
    static final class Customer {

        String city;
        String state;
        String postalCode;
    }

    private static GeneratorConfig config(Locale locale, long seed) {
        return GeneratorConfig.builder().locale(locale).seed(seed).build();
    }

    private static Row rowOf(List<Row> rows, String city, String region) {
        return rows.stream()
                .filter(row -> row.city().equals(city) && row.region().equals(region))
                .findFirst()
                .orElseThrow(() -> new AssertionError("no place " + city + ", " + region));
    }

    private static List<Row> rows(String table) {
        Map<String, String> codes = regionCodes(table);
        List<Row> rows = new ArrayList<>();
        for (String line : dataLines("/krandom/postal_places/" + table + ".txt")) {
            String[] fields = line.split("\t");
            assertEquals(3, fields.length, line);
            String code = codes.get(fields[1]);
            assertNotNull(code, "unknown region in " + line);
            rows.add(new Row(fields[0], fields[1], code, List.of(fields[2].split(","))));
        }
        assertFalse(rows.isEmpty(), table);
        return rows;
    }

    /** The states file alternates a name and its code; a blank code line means none. */
    private static Map<String, String> regionCodes(String table) {
        List<String> lines = new ArrayList<>();
        for (String line : allLines("/krandom/states/" + table + "_states.txt")) {
            if (!line.startsWith("#")) {
                lines.add(line.trim());
            }
        }
        Map<String, String> codes = new HashMap<>();
        for (int i = 0; i < lines.size(); i++) {
            String name = lines.get(i);
            if (name.isEmpty()) {
                continue;
            }
            boolean hasCode = i + 1 < lines.size() && !lines.get(i + 1).isEmpty();
            codes.put(name, hasCode ? lines.get(i + 1) : "");
            if (hasCode) {
                i++;
            }
        }
        return codes;
    }

    private static String header(String table) {
        StringBuilder header = new StringBuilder();
        for (String line : allLines("/krandom/postal_places/" + table + ".txt")) {
            if (line.startsWith("#")) {
                header.append(line).append('\n');
            }
        }
        return header.toString();
    }

    private static List<String> dataLines(String resource) {
        return allLines(resource).stream()
                .filter(line -> !line.isBlank() && !line.startsWith("#"))
                .toList();
    }

    private static List<String> allLines(String resource) {
        try (InputStream in =
                        Objects.requireNonNull(PostalPlaceCoherenceTest.class.getResourceAsStream(resource), resource);
                BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
            return reader.lines().toList();
        } catch (IOException e) {
            throw new AssertionError(resource, e);
        }
    }
}
