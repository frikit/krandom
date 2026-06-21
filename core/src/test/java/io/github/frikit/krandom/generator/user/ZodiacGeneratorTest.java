/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.user;

import io.github.frikit.krandom.generator.GeneratorConfig;
import io.github.frikit.krandom.generator.Generators;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.MonthDay;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Zodiac generators")
class ZodiacGeneratorTest {

    private static final Set<String> WESTERN = Set.of(
        "Aries", "Taurus", "Gemini", "Cancer", "Leo", "Virgo",
        "Libra", "Scorpio", "Sagittarius", "Capricorn", "Aquarius", "Pisces");

    private static final Set<String> CHINESE = Set.of(
        "Monkey", "Rooster", "Dog", "Pig", "Rat", "Ox",
        "Tiger", "Rabbit", "Dragon", "Snake", "Horse", "Goat");

    @Nested
    @DisplayName("ZodiacGenerator (Western)")
    class Western {

        @RepeatedTest(200)
        @DisplayName("generate() returns a valid sign")
        void generateValid() {
            assertTrue(WESTERN.contains(new ZodiacGenerator().generate()));
        }

        @Test
        @DisplayName("generate() can surface every sign over many draws")
        void generateCoversAll() {
            ZodiacGenerator gen = new ZodiacGenerator(GeneratorConfig.builder().seed(7L).build());
            Set<String> seen = new HashSet<>();
            for (int i = 0; i < 2000; i++) {
                seen.add(gen.generate());
            }
            assertEquals(WESTERN, seen);
        }

        @Test
        @DisplayName("same seed is reproducible")
        void reproducible() {
            List<String> a = new ZodiacGenerator(GeneratorConfig.builder().seed(42L).build()).generateList(30);
            List<String> b = new ZodiacGenerator(GeneratorConfig.builder().seed(42L).build()).generateList(30);
            assertEquals(a, b);
        }

        @Test
        @DisplayName("signFor(LocalDate) resolves the conventional sign")
        void signForDate() {
            ZodiacGenerator gen = new ZodiacGenerator();
            assertEquals("Scorpio", gen.signFor(LocalDate.of(1990, 11, 5)));
            assertEquals("Aries", gen.signFor(LocalDate.of(2000, 3, 21)));   // exactly on cutoff
            assertEquals("Pisces", gen.signFor(LocalDate.of(2000, 3, 20)));  // day before cutoff
        }

        @Test
        @DisplayName("signFor handles the January wrap-around to Capricorn")
        void januaryWrap() {
            ZodiacGenerator gen = new ZodiacGenerator();
            assertEquals("Capricorn", gen.signFor(MonthDay.of(1, 10)));  // before Jan cutoff -> previous-year sign
            assertEquals("Aquarius", gen.signFor(MonthDay.of(1, 25)));   // on/after Jan cutoff
            assertEquals("Capricorn", gen.signFor(MonthDay.of(12, 25))); // December on/after cutoff
        }

        @Test
        @DisplayName("every day of the year maps to a valid sign")
        void everyDayValid() {
            ZodiacGenerator gen = new ZodiacGenerator();
            LocalDate d = LocalDate.of(2024, 1, 1); // leap year to include Feb 29
            for (int i = 0; i < 366; i++) {
                assertTrue(WESTERN.contains(gen.signFor(d)), "no sign for " + d);
                d = d.plusDays(1);
            }
        }

        @Test
        @DisplayName("null arguments are rejected")
        void nullsRejected() {
            ZodiacGenerator gen = new ZodiacGenerator();
            assertThrows(NullPointerException.class, () -> new ZodiacGenerator(null));
            assertThrows(NullPointerException.class, () -> gen.signFor((LocalDate) null));
            assertThrows(NullPointerException.class, () -> gen.signFor((MonthDay) null));
        }
    }

    @Nested
    @DisplayName("ChineseZodiacGenerator")
    class Chinese {

        @RepeatedTest(200)
        @DisplayName("generate() returns a valid animal")
        void generateValid() {
            assertTrue(CHINESE.contains(new ChineseZodiacGenerator().generate()));
        }

        @Test
        @DisplayName("generate() can surface every animal over many draws")
        void generateCoversAll() {
            ChineseZodiacGenerator gen =
                new ChineseZodiacGenerator(GeneratorConfig.builder().seed(3L).build());
            Set<String> seen = new HashSet<>();
            for (int i = 0; i < 2000; i++) {
                seen.add(gen.generate());
            }
            assertEquals(CHINESE, seen);
        }

        @Test
        @DisplayName("animalFor(year) matches known years")
        void animalForKnownYears() {
            ChineseZodiacGenerator gen = new ChineseZodiacGenerator();
            assertEquals("Rat", gen.animalFor(2020));
            assertEquals("Dragon", gen.animalFor(2024));
            assertEquals("Snake", gen.animalFor(2025));
        }

        @Test
        @DisplayName("animalFor handles negative years via floorMod")
        void animalForNegativeYear() {
            ChineseZodiacGenerator gen = new ChineseZodiacGenerator();
            assertTrue(CHINESE.contains(gen.animalFor(-4)));
            assertEquals(gen.animalFor(2020), gen.animalFor(2020 - 12)); // same animal one cycle earlier
        }

        @Test
        @DisplayName("animalFor(LocalDate) uses the Gregorian year")
        void animalForDate() {
            ChineseZodiacGenerator gen = new ChineseZodiacGenerator();
            assertEquals("Dragon", gen.animalFor(LocalDate.of(2024, 6, 1)));
        }

        @Test
        @DisplayName("same seed is reproducible")
        void reproducible() {
            List<String> a =
                new ChineseZodiacGenerator(GeneratorConfig.builder().seed(9L).build()).generateList(30);
            List<String> b =
                new ChineseZodiacGenerator(GeneratorConfig.builder().seed(9L).build()).generateList(30);
            assertEquals(a, b);
        }

        @Test
        @DisplayName("null arguments are rejected")
        void nullsRejected() {
            ChineseZodiacGenerator gen = new ChineseZodiacGenerator();
            assertThrows(NullPointerException.class, () -> new ChineseZodiacGenerator(null));
            assertThrows(NullPointerException.class, () -> gen.animalFor((LocalDate) null));
        }
    }

    @Nested
    @DisplayName("Generators facade")
    class Facade {

        @Test
        @DisplayName("ofZodiac / ofChineseZodiac (with and without config) produce valid values")
        void facadeFactories() {
            assertTrue(WESTERN.contains(Generators.ofZodiac().generate()));
            assertTrue(WESTERN.contains(
                Generators.ofZodiac(GeneratorConfig.builder().seed(1L).build()).generate()));
            assertTrue(CHINESE.contains(Generators.ofChineseZodiac().generate()));
            assertTrue(CHINESE.contains(
                Generators.ofChineseZodiac(GeneratorConfig.builder().seed(1L).build()).generate()));
        }
    }
}
