/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.location;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.time.ZoneId;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

@DisplayName("Country calling-code, continent and time-zone tables")
class CountryDataTablesTest {

    private static final Pattern CALLING_CODE = Pattern.compile("^\\+[1-9][0-9]{0,3}$");
    private static final Set<String> CONTINENTS =
            Set.of("Africa", "Antarctica", "Asia", "Europe", "North America", "Oceania", "South America");
    private static final List<CountryCodes> ISO_COUNTRIES =
            CountryGenerator.parseIsoCodes(CountryResourceLoader.load("krandom/country_codes/iso_3166_1.txt"));

    private static CountryGenerator forCountry(String alpha2) {
        return new CountryGenerator(new Locale("en", alpha2));
    }

    @Test
    @DisplayName("every ISO 3166-1 country has a calling code, a continent of the documented set and a valid zone")
    void everyIsoCountryHasACallingCodeAContinentAndAZone() {
        assertEquals(249, ISO_COUNTRIES.size());
        Set<String> continentsUsed = new HashSet<>();
        for (CountryCodes country : ISO_COUNTRIES) {
            CountryGenerator generator = forCountry(country.alpha2());
            String callingCode = generator.currentCallingCode();
            String continent = generator.currentContinent();
            String zone = generator.currentTimezone();

            assertTrue(CALLING_CODE.matcher(callingCode).matches(), country + " calling code " + callingCode);
            assertTrue(CONTINENTS.contains(continent), country + " continent " + continent);
            assertEquals(zone, ZoneId.of(zone).getId(), country + " zone");
            continentsUsed.add(continent);
        }
        assertEquals(CONTINENTS, continentsUsed);
    }

    @ParameterizedTest
    @ValueSource(strings = {"calling_codes", "continents", "time_zones"})
    @DisplayName("each table lists every ISO country in ISO order and records its source, licence, date and checksum")
    void eachTableCoversTheIsoTableAndRecordsItsProvenance(String table) throws IOException {
        String path = "krandom/country_codes/" + table + ".txt";
        Map<String, String> values =
                CountryGenerator.parseCountryValues(CountryResourceLoader.load(path), ISO_COUNTRIES, path);
        assertEquals(ISO_COUNTRIES.stream().map(CountryCodes::alpha2).toList(), List.copyOf(values.keySet()));

        List<String> header = header(path);
        assertTrue(header.stream().anyMatch(line -> line.startsWith("# Source:")), path);
        assertTrue(header.stream().anyMatch(line -> line.startsWith("# Licence:")), path);
        assertTrue(header.contains("# Retrieved: 2026-11-04"), path);
        assertTrue(header.stream().anyMatch(line -> line.matches("# SHA-256: +[0-9a-f]{64}  \\S.*")), path);
    }

    @Test
    @DisplayName("the continent table carries the Unicode License v3 notice that its CLDR source requires")
    void continentTableCarriesTheUnicodeLicenceNotice() throws IOException {
        List<String> header = header("krandom/country_codes/continents.txt");

        assertTrue(header.contains("# Copyright © 1991-2026 Unicode, Inc."));
        assertTrue(header.contains("# Permission is hereby granted, free of charge, to any person obtaining a"));
        assertTrue(header.contains("# SPDX-License-Identifier: Unicode-3.0"));
    }

    private static List<String> header(String path) throws IOException {
        try (InputStream in = Objects.requireNonNull(CountryDataTablesTest.class.getResourceAsStream("/" + path))) {
            return new String(in.readAllBytes(), UTF_8)
                    .lines()
                    .takeWhile(line -> line.startsWith("#"))
                    .toList();
        }
    }

    @Test
    @DisplayName("the 45 countries of the former tables keep their values; Argentina's zone has its current name")
    void formerTableValuesAreKept() {
        String[] rows = """
                US|+1|North America|America/New_York
                GB|+44|Europe|Europe/London
                AU|+61|Oceania|Australia/Sydney
                DE|+49|Europe|Europe/Berlin
                FR|+33|Europe|Europe/Paris
                ES|+34|Europe|Europe/Madrid
                IT|+39|Europe|Europe/Rome
                BR|+55|South America|America/Sao_Paulo
                JP|+81|Asia|Asia/Tokyo
                CN|+86|Asia|Asia/Shanghai
                NL|+31|Europe|Europe/Amsterdam
                PL|+48|Europe|Europe/Warsaw
                RU|+7|Europe|Europe/Moscow
                KR|+82|Asia|Asia/Seoul
                TR|+90|Asia|Europe/Istanbul
                SE|+46|Europe|Europe/Stockholm
                NO|+47|Europe|Europe/Oslo
                CZ|+420|Europe|Europe/Prague
                SA|+966|Asia|Asia/Riyadh
                IN|+91|Asia|Asia/Kolkata
                DK|+45|Europe|Europe/Copenhagen
                FI|+358|Europe|Europe/Helsinki
                HU|+36|Europe|Europe/Budapest
                RO|+40|Europe|Europe/Bucharest
                SK|+421|Europe|Europe/Bratislava
                UA|+380|Europe|Europe/Kyiv
                BG|+359|Europe|Europe/Sofia
                HR|+385|Europe|Europe/Zagreb
                GR|+30|Europe|Europe/Athens
                TH|+66|Asia|Asia/Bangkok
                VN|+84|Asia|Asia/Ho_Chi_Minh
                ID|+62|Asia|Asia/Jakarta
                MY|+60|Asia|Asia/Kuala_Lumpur
                IL|+972|Asia|Asia/Jerusalem
                CA|+1|North America|America/Toronto
                NZ|+64|Oceania|Pacific/Auckland
                IE|+353|Europe|Europe/Dublin
                ZA|+27|Africa|Africa/Johannesburg
                BE|+32|Europe|Europe/Brussels
                CH|+41|Europe|Europe/Zurich
                AT|+43|Europe|Europe/Vienna
                MX|+52|North America|America/Mexico_City
                AR|+54|South America|America/Argentina/Buenos_Aires
                PT|+351|Europe|Europe/Lisbon
                TW|+886|Asia|Asia/Taipei
                """.strip().split("\n");

        assertEquals(45, rows.length);
        for (String row : rows) {
            String[] fields = row.split("\\|");
            assertCountry(fields[0], fields[1], fields[2], fields[3]);
        }
    }

    @Test
    @DisplayName("countries that share a calling code have the code alone, without an area code")
    void sharedCallingCodes() {
        Set<String> nanp = new TreeSet<>();
        for (CountryCodes country : ISO_COUNTRIES) {
            if (forCountry(country.alpha2()).currentCallingCode().equals("+1")) {
                nanp.add(country.alpha2());
            }
        }
        // The NANP members, and UM, which takes the code of the United States.
        assertEquals(
                new TreeSet<>(List.of(
                        "AG", "AI", "AS", "BB", "BM", "BS", "CA", "DM", "DO", "GD", "GU", "JM", "KN", "KY", "LC", "MP",
                        "MS", "PR", "SX", "TC", "TT", "UM", "US", "VC", "VG", "VI")),
                nanp);
        assertCallingCode("+7", "RU", "KZ");
        assertCallingCode("+44", "GB", "GG", "IM", "JE");
        assertCallingCode("+61", "AU", "CC", "CX");
        assertCallingCode("+262", "RE", "YT");
        assertCallingCode("+590", "GP", "BL", "MF");
        assertCallingCode("+599", "CW", "BQ");
        assertCallingCode("+47", "NO", "SJ");
        assertCallingCode("+358", "FI", "AX");
        assertCallingCode("+212", "MA", "EH");
        assertCallingCode("+39", "IT", "VA");
    }

    @Test
    @DisplayName("territories without an entry of their own in a source follow the documented rules")
    void territoriesWithoutAnEntryOfTheirOwn() {
        // Not in libphonenumber: AQ and TF take the codes it names; the others the administering state's code.
        assertCountry("AQ", "+672", "Antarctica", "Antarctica/McMurdo");
        assertCountry("TF", "+262", "Africa", "Indian/Kerguelen");
        assertCountry("BV", "+47", "South America", "Europe/Oslo");
        assertCountry("GS", "+44", "South America", "Atlantic/South_Georgia");
        assertCountry("HM", "+61", "Oceania", "Etc/GMT-5");
        assertCountry("PN", "+44", "Oceania", "Pacific/Pitcairn");
        assertCountry("UM", "+1", "Oceania", "Pacific/Midway");
        // Not in M49.
        assertCountry("TW", "+886", "Asia", "Asia/Taipei");
    }

    @Test
    @DisplayName("a transcontinental country has its M49 region, and the Americas divide into North and South America")
    void continentsFollowM49() {
        assertContinent("Europe", "RU");
        assertContinent("Asia", "TR", "CY", "GE", "AM", "AZ", "KZ");
        assertContinent("Africa", "EG");
        assertContinent("North America", "US", "CA", "GL", "BM", "PM", "MX", "GT", "PA", "CU", "JM", "TT", "AW");
        assertContinent("South America", "BR", "AR", "CO", "VE", "FK", "GF");
    }

    @Test
    @DisplayName("a country with several zones has the zone of its seat of government")
    void multiZoneCountriesHaveTheZoneOfTheirSeatOfGovernment() {
        // The seat of government is not in the country's first zone.tab row.
        assertZone("AU", "Australia/Sydney");
        assertZone("BR", "America/Sao_Paulo");
        assertZone("CA", "America/Toronto");
        assertZone("FM", "Pacific/Pohnpei");
        assertZone("PS", "Asia/Hebron");
        assertZone("RU", "Europe/Moscow");
        assertZone("UA", "Europe/Kyiv");
        assertZone("UZ", "Asia/Tashkent");
        // The seat of government is in the first row.
        assertZone("AR", "America/Argentina/Buenos_Aires");
        assertZone("CD", "Africa/Kinshasa");
        assertZone("CL", "America/Santiago");
        assertZone("CN", "Asia/Shanghai");
        assertZone("CY", "Asia/Nicosia");
        assertZone("DE", "Europe/Berlin");
        assertZone("EC", "America/Guayaquil");
        assertZone("ES", "Europe/Madrid");
        assertZone("GL", "America/Nuuk");
        assertZone("ID", "Asia/Jakarta");
        assertZone("KI", "Pacific/Tarawa");
        assertZone("KZ", "Asia/Almaty");
        assertZone("MH", "Pacific/Majuro");
        assertZone("MN", "Asia/Ulaanbaatar");
        assertZone("MX", "America/Mexico_City");
        assertZone("MY", "Asia/Kuala_Lumpur");
        assertZone("NZ", "Pacific/Auckland");
        assertZone("PF", "Pacific/Tahiti");
        assertZone("PG", "Pacific/Port_Moresby");
        assertZone("PT", "Europe/Lisbon");
        assertZone("US", "America/New_York");
    }

    @Test
    @DisplayName("current-country helpers cover countries outside the built-in locale catalog")
    void currentCountryHelpersCoverEveryIsoCountry() {
        assertCountry("NG", "+234", "Africa", "Africa/Lagos");
        assertCountry("LI", "+423", "Europe", "Europe/Vaduz");
        assertCountry("SG", "+65", "Asia", "Asia/Singapore");
        assertEquals("+423", new CountryGenerator(new Locale("de", "LI")).currentCallingCode());
    }

    private static void assertCountry(String alpha2, String callingCode, String continent, String zone) {
        CountryGenerator generator = forCountry(alpha2);
        assertEquals(callingCode, generator.currentCallingCode(), alpha2 + " calling code");
        assertEquals(continent, generator.currentContinent(), alpha2 + " continent");
        assertEquals(zone, generator.currentTimezone(), alpha2 + " zone");
    }

    private static void assertCallingCode(String callingCode, String... countries) {
        for (String alpha2 : countries) {
            assertEquals(callingCode, forCountry(alpha2).currentCallingCode(), alpha2);
        }
    }

    private static void assertContinent(String continent, String... countries) {
        for (String alpha2 : countries) {
            assertEquals(continent, forCountry(alpha2).currentContinent(), alpha2);
        }
    }

    private static void assertZone(String alpha2, String zone) {
        assertEquals(zone, forCountry(alpha2).currentTimezone(), alpha2);
    }
}
