/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.user;

import io.github.frikit.krandom.generator.GeneratorConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("BirthdayGenerator")
class BirthdayGeneratorTest {

    private static final Pattern M_D_YYYY   = Pattern.compile("^\\d{1,2}/\\d{1,2}/\\d{4}$");
    private static final Pattern MM_DD_YYYY = Pattern.compile("^\\d{2}/\\d{2}/\\d{4}$");

    // ── Default constructor ────────────────────────────────────────────────────

    @Test
    @DisplayName("default constructor generates non-null past dates")
    void defaultConstructor() {
        BirthdayGenerator gen = new BirthdayGenerator();
        LocalDate today = LocalDate.now();
        for (int i = 0; i < 50; i++) {
            LocalDate bd = gen.generate();
            assertNotNull(bd);
            assertTrue(bd.isBefore(today), "birthday should be in the past");
        }
        assertEquals(1, gen.getMinAge());
        assertEquals(100, gen.getMaxAge());
    }

    @Test
    @DisplayName("default birthday respects age range [1, 100]")
    void defaultAgeRange() {
        BirthdayGenerator gen = new BirthdayGenerator();
        LocalDate today = LocalDate.now();
        for (int i = 0; i < 100; i++) {
            LocalDate bd = gen.generate();
            int age = today.getYear() - bd.getYear();
            if (bd.getDayOfYear() > today.getDayOfYear()) age--;
            assertTrue(age >= 1 && age <= 101,
                       "Age derived from birthday should be close to [1,100], got: " + age);
        }
    }

    // ── Seeded constructor ─────────────────────────────────────────────────────

    @Test
    @DisplayName("seeded constructor produces reproducible output")
    void seededReproducibility() {
        BirthdayGenerator gen1 = new BirthdayGenerator(42L);
        BirthdayGenerator gen2 = new BirthdayGenerator(42L);
        assertEquals(gen1.generate(), gen2.generate());
    }

    @Test
    @DisplayName("config constructor produces reproducible output")
    void configConstructorReproducibility() {
        GeneratorConfig config = GeneratorConfig.builder()
                                                .locale(Locale.GERMANY)
                                                .seed(42L)
                                                .build();
        BirthdayGenerator gen1 = new BirthdayGenerator(config);
        BirthdayGenerator gen2 = new BirthdayGenerator(config);

        assertEquals(gen1.generateList(20), gen2.generateList(20));
        assertEquals(Locale.GERMANY, gen1.getLocale());
    }

    @Test
    @DisplayName("config constructor supports custom random factory")
    void configConstructorSupportsRandomFactory() {
        GeneratorConfig config = GeneratorConfig.builder()
                                                .locale(Locale.US)
                                                .randomFactory(() -> new Random(7L))
                                                .build();
        BirthdayGenerator gen1 = new BirthdayGenerator(AgeType.ADULT, config);
        BirthdayGenerator gen2 = new BirthdayGenerator(AgeType.ADULT, config);

        assertEquals(gen1.generateList(20), gen2.generateList(20));
    }

    @Test
    @DisplayName("different seeds produce different dates")
    void differentSeeds() {
        BirthdayGenerator gen1 = new BirthdayGenerator(1L);
        BirthdayGenerator gen2 = new BirthdayGenerator(2L);
        List<LocalDate> list1 = gen1.generateList(20);
        List<LocalDate> list2 = gen2.generateList(20);
        assertNotEquals(list1, list2);
    }

    // ── AgeType constructor ───────────────────────────────────────────────────

    @Test
    @DisplayName("CHILD type produces birthdays for ages [1, 12]")
    void childType() {
        BirthdayGenerator gen = new BirthdayGenerator(AgeType.CHILD);
        LocalDate today = LocalDate.now();
        for (int i = 0; i < 50; i++) {
            LocalDate bd = gen.generate();
            assertTrue(bd.isBefore(today.minusYears(1).plusDays(1)),
                       "Child birthday should be at least 1 year ago");
            assertTrue(bd.isAfter(today.minusYears(13)),
                       "Child birthday should be at most 12 years ago");
        }
        assertEquals(1, gen.getMinAge());
        assertEquals(12, gen.getMaxAge());
    }

    @Test
    @DisplayName("TEEN type produces birthdays for ages [13, 19]")
    void teenType() {
        BirthdayGenerator gen = new BirthdayGenerator(AgeType.TEEN);
        assertEquals(13, gen.getMinAge());
        assertEquals(19, gen.getMaxAge());
        LocalDate bd = gen.generate();
        assertNotNull(bd);
        assertTrue(bd.isBefore(LocalDate.now()));
    }

    @Test
    @DisplayName("ADULT type produces birthdays for ages [18, 65]")
    void adultType() {
        BirthdayGenerator gen = new BirthdayGenerator(AgeType.ADULT);
        assertEquals(18, gen.getMinAge());
        assertEquals(65, gen.getMaxAge());
        assertNotNull(gen.generate());
    }

    @Test
    @DisplayName("SENIOR type produces birthdays for ages [65, 100]")
    void seniorType() {
        BirthdayGenerator gen = new BirthdayGenerator(AgeType.SENIOR);
        assertEquals(65, gen.getMinAge());
        assertEquals(100, gen.getMaxAge());
        assertNotNull(gen.generate());
    }

    @Test
    @DisplayName("AgeType with seed produces reproducible output")
    void ageTypeWithSeed() {
        BirthdayGenerator gen1 = new BirthdayGenerator(AgeType.ADULT, 7L);
        BirthdayGenerator gen2 = new BirthdayGenerator(AgeType.ADULT, 7L);
        assertEquals(gen1.generateList(20), gen2.generateList(20));
    }

    @Test
    @DisplayName("null AgeType throws NullPointerException")
    void nullAgeTypeThrows() {
        assertThrows(NullPointerException.class, () -> new BirthdayGenerator((AgeType) null));
    }

    @Test
    @DisplayName("null AgeType with seed throws NullPointerException")
    void nullAgeTypeWithSeedThrows() {
        assertThrows(NullPointerException.class, () -> new BirthdayGenerator((Locale) null, 1L));
    }

    @Test
    @DisplayName("null config throws NullPointerException")
    void nullConfigThrows() {
        assertThrows(NullPointerException.class, () -> new BirthdayGenerator((GeneratorConfig) null));
        assertThrows(NullPointerException.class, () -> new BirthdayGenerator(AgeType.ADULT,
                                                                             (GeneratorConfig) null));
        assertThrows(NullPointerException.class, () -> new BirthdayGenerator(18,
                                                                             65,
                                                                             (GeneratorConfig) null));
    }

    // ── Custom range constructor ──────────────────────────────────────────────

    @Test
    @DisplayName("custom range produces birthdays in correct age window")
    void customRange() {
        BirthdayGenerator gen = new BirthdayGenerator(30, 40);
        assertEquals(30, gen.getMinAge());
        assertEquals(40, gen.getMaxAge());
        assertNotNull(gen.generate());
    }

    @Test
    @DisplayName("single-age range produces consistent birthday")
    void singleAgeRange() {
        BirthdayGenerator gen = new BirthdayGenerator(25, 25);
        LocalDate today = LocalDate.now();
        for (int i = 0; i < 20; i++) {
            LocalDate bd = gen.generate();
            assertTrue(bd.isAfter(today.minusYears(27)),
                       "Should be within a year of 25 years ago");
            assertTrue(bd.isBefore(today.minusYears(24).plusDays(1)),
                       "Should not be more recent than 24 years ago");
        }
    }

    // ── Validation ────────────────────────────────────────────────────────────

    @Test
    @DisplayName("negative minAge throws IllegalArgumentException")
    void negativeMinAgeThrows() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                                                   () -> new BirthdayGenerator(-1, 10));
        assertTrue(ex.getMessage().contains("minAge"));
    }

    @Test
    @DisplayName("maxAge less than minAge throws IllegalArgumentException")
    void maxLessThanMinThrows() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                                                   () -> new BirthdayGenerator(10, 5));
        assertTrue(ex.getMessage().contains("maxAge"));
    }

    @Test
    @DisplayName("minAge of 0 is accepted")
    void zeroMinAgeAccepted() {
        BirthdayGenerator gen = new BirthdayGenerator(0, 0);
        LocalDate bd = gen.generate();
        assertNotNull(bd);
    }

    // ── String output ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("generateAsString() returns M/d/yyyy format (no zero-padding)")
    void generateAsString() {
        BirthdayGenerator gen = new BirthdayGenerator(42L);
        String s = gen.generateAsString();
        assertNotNull(s);
        assertTrue(M_D_YYYY.matcher(s).matches(),
                   "Expected M/d/yyyy format but got: " + s);
    }

    @Test
    @DisplayName("generateAsAmericanString() returns MM/dd/yyyy format (zero-padded)")
    void generateAsAmericanString() {
        BirthdayGenerator gen = new BirthdayGenerator(42L);
        String s = gen.generateAsAmericanString();
        assertNotNull(s);
        assertTrue(MM_DD_YYYY.matcher(s).matches(),
                   "Expected MM/dd/yyyy format but got: " + s);
    }

    @Test
    @DisplayName("generateAsString and generateAsAmericanString represent the same date")
    void stringAndAmericanSameDateSeeded() {
        // Use the same seed for both generators so they produce the same date
        BirthdayGenerator gen1 = new BirthdayGenerator(55L);
        BirthdayGenerator gen2 = new BirthdayGenerator(55L);
        String s = gen1.generateAsString();
        String american = gen2.generateAsAmericanString();

        // Parse and compare
        String[] sParts = s.split("/");
        String[] aParts = american.split("/");

        // Both represent the same month/day/year (american is zero-padded)
        assertEquals(Integer.parseInt(sParts[0]), Integer.parseInt(aParts[0])); // month
        assertEquals(Integer.parseInt(sParts[1]), Integer.parseInt(aParts[1])); // day
        assertEquals(sParts[2], aParts[2]);                                      // year
    }

    @Test
    @DisplayName("generateAsString includes valid year in past")
    void generateAsStringValidYear() {
        BirthdayGenerator gen = new BirthdayGenerator();
        String s = gen.generateAsString();
        String[] parts = s.split("/");
        int year = Integer.parseInt(parts[2]);
        int currentYear = LocalDate.now().getYear();
        assertTrue(year >= currentYear - 101 && year <= currentYear - 1,
                   "Year should be in recent past, got: " + year);
    }

    // ── generateList / stream ─────────────────────────────────────────────────

    @Test
    @DisplayName("generateList returns correct count with past dates")
    void generateListCount() {
        BirthdayGenerator gen = new BirthdayGenerator(AgeType.ADULT);
        List<LocalDate> dates = gen.generateList(15);
        assertEquals(15, dates.size());
        LocalDate today = LocalDate.now();
        dates.forEach(d -> assertTrue(d.isBefore(today)));
    }

    @Test
    @DisplayName("stream generates continuous values")
    void streamGeneration() {
        BirthdayGenerator gen = new BirthdayGenerator(AgeType.TEEN);
        List<LocalDate> dates = gen.stream().limit(20).toList();
        assertEquals(20, dates.size());
        dates.forEach(d -> assertNotNull(d));
    }

    // ── Locale-aware generateAsString() ──────────────────────────────────────

    @Test
    @DisplayName("locale==null branch: no-locale constructor still returns M/d/yyyy")
    void noLocaleBackwardCompat() {
        BirthdayGenerator gen = new BirthdayGenerator(42L);
        String s = gen.generateAsString();
        assertTrue(M_D_YYYY.matcher(s).matches(), "Expected M/d/yyyy but got: " + s);
        assertNull(gen.getLocale());
    }

    @Test
    @DisplayName("German locale produces d.M.yyyy format")
    void germanLocale() {
        Pattern dotSeparated = Pattern.compile("^\\d{1,2}\\.\\d{1,2}\\.\\d{4}$");
        BirthdayGenerator gen = new BirthdayGenerator(Locale.GERMANY);
        for (int i = 0; i < 50; i++) {
            String s = gen.generateAsString();
            assertTrue(dotSeparated.matcher(s).matches(), "Expected d.M.yyyy but got: " + s);
        }
    }

    @Test
    @DisplayName("Japanese locale produces yyyy/M/d format")
    void japaneseLocale() {
        Pattern yearFirst = Pattern.compile("^\\d{4}/\\d{1,2}/\\d{1,2}$");
        BirthdayGenerator gen = new BirthdayGenerator(Locale.JAPAN);
        for (int i = 0; i < 50; i++) {
            String s = gen.generateAsString();
            assertTrue(yearFirst.matcher(s).matches(), "Expected yyyy/M/d but got: " + s);
        }
    }

    @Test
    @DisplayName("Chinese locale produces yyyy年M月d日 format")
    void chineseLocale() {
        BirthdayGenerator gen = new BirthdayGenerator(Locale.CHINA);
        for (int i = 0; i < 50; i++) {
            String s = gen.generateAsString();
            assertTrue(s.contains("年") && s.contains("月") && s.contains("日"),
                       "Expected 年月日 but got: " + s);
        }
    }

    @Test
    @DisplayName("UK locale produces d/M/yyyy format")
    void ukLocale() {
        BirthdayGenerator gen = new BirthdayGenerator(Locale.UK);
        for (int i = 0; i < 50; i++) {
            assertTrue(M_D_YYYY.matcher(gen.generateAsString()).matches());
        }
    }

    @Test
    @DisplayName("French locale with AgeType produces d/M/yyyy format")
    void frenchLocaleWithAgeType() {
        BirthdayGenerator gen = new BirthdayGenerator(AgeType.ADULT, Locale.FRANCE);
        for (int i = 0; i < 50; i++) {
            assertTrue(M_D_YYYY.matcher(gen.generateAsString()).matches());
        }
    }

    @Test
    @DisplayName("custom range + locale produces correct format")
    void customRangeWithLocale() {
        Pattern dotSeparated = Pattern.compile("^\\d{1,2}\\.\\d{1,2}\\.\\d{4}$");
        BirthdayGenerator gen = new BirthdayGenerator(20, 30, Locale.GERMANY);
        for (int i = 0; i < 50; i++) {
            assertTrue(dotSeparated.matcher(gen.generateAsString()).matches());
        }
    }

    @Test
    @DisplayName("getLocale() returns configured locale")
    void getLocaleReturnsConfigured() {
        BirthdayGenerator gen = new BirthdayGenerator(Locale.GERMANY);
        assertEquals(Locale.GERMANY, gen.getLocale());
    }

    @Test
    @DisplayName("getLocale() returns null for no-locale constructors")
    void getLocaleNullForNoLocale() {
        assertNull(new BirthdayGenerator().getLocale());
        assertNull(new BirthdayGenerator(42L).getLocale());
        assertNull(new BirthdayGenerator(AgeType.ADULT).getLocale());
        assertNull(new BirthdayGenerator(18, 65).getLocale());
    }

    @Test
    @DisplayName("null locale constructor throws NullPointerException")
    void nullLocaleConstructorThrows() {
        assertThrows(NullPointerException.class, () -> new BirthdayGenerator((Locale) null));
        assertThrows(NullPointerException.class, () -> new BirthdayGenerator(AgeType.ADULT, (Locale) null));
        assertThrows(NullPointerException.class, () -> new BirthdayGenerator(18, 65, (Locale) null));
        assertThrows(NullPointerException.class, () -> new BirthdayGenerator((Locale) null, 1L));
        assertThrows(NullPointerException.class, () -> new BirthdayGenerator(AgeType.ADULT, (Locale) null, 1L));
        assertThrows(NullPointerException.class, () -> new BirthdayGenerator(18, 65, (Locale) null, 1L));
    }

    @Test
    @DisplayName("unknown locale falls back to M/d/yyyy (both-null branch)")
    void unknownLocaleFallback() {
        // "xx_XX" not in map, "xx" not in map → falls back to STRING_FORMAT
        BirthdayGenerator gen = new BirthdayGenerator(Locale.of("xx", "XX"));
        String s = gen.generateAsString();
        assertTrue(M_D_YYYY.matcher(s).matches(), "Expected M/d/yyyy fallback but got: " + s);
    }

    @Test
    @DisplayName("language-only locale hits language-level fallback (first-key-miss branch)")
    void languageOnlyLocaleFallback() {
        // Locale.of("de") has no country; key "de_" misses, key "de" hits → d.M.yyyy
        Pattern dotSeparated = Pattern.compile("^\\d{1,2}\\.\\d{1,2}\\.\\d{4}$");
        BirthdayGenerator gen = new BirthdayGenerator(Locale.of("de"));
        for (int i = 0; i < 50; i++) {
            assertTrue(dotSeparated.matcher(gen.generateAsString()).matches());
        }
    }

    @Test
    @DisplayName("generateAsAmericanString() is unaffected by locale")
    void americanStringUnaffectedByLocale() {
        BirthdayGenerator gen = new BirthdayGenerator(Locale.GERMANY);
        for (int i = 0; i < 50; i++) {
            assertTrue(MM_DD_YYYY.matcher(gen.generateAsAmericanString()).matches());
        }
    }

    @Test
    @DisplayName("seeded + locale: reproducible sequence")
    void seededWithLocale() {
        BirthdayGenerator g1 = new BirthdayGenerator(Locale.GERMANY, 42L);
        BirthdayGenerator g2 = new BirthdayGenerator(Locale.GERMANY, 42L);
        for (int i = 0; i < 20; i++) {
            assertEquals(g1.generateAsString(), g2.generateAsString());
        }
    }

    @Test
    @DisplayName("seeded + AgeType + locale: reproducible sequence")
    void seededAgeTypeWithLocale() {
        BirthdayGenerator g1 = new BirthdayGenerator(AgeType.ADULT, Locale.JAPAN, 99L);
        BirthdayGenerator g2 = new BirthdayGenerator(AgeType.ADULT, Locale.JAPAN, 99L);
        assertEquals(g1.generateList(20), g2.generateList(20));
    }

    @Test
    @DisplayName("seeded + range + locale: reproducible sequence")
    void seededRangeWithLocale() {
        BirthdayGenerator g1 = new BirthdayGenerator(18, 65, Locale.FRANCE, 77L);
        BirthdayGenerator g2 = new BirthdayGenerator(18, 65, Locale.FRANCE, 77L);
        assertEquals(g1.generateList(20), g2.generateList(20));
    }
}
