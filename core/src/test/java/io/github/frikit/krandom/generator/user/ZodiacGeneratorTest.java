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
import java.util.Locale;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Zodiac generators")
class ZodiacGeneratorTest {

    private static final Locale RU = Locale.of("ru", "RU");

    private static final Set<String> WESTERN_EN = Set.of(
        "Aries", "Taurus", "Gemini", "Cancer", "Leo", "Virgo",
        "Libra", "Scorpio", "Sagittarius", "Capricorn", "Aquarius", "Pisces");

    private static final Set<String> WESTERN_RU = Set.of(
        "Овен", "Телец", "Близнецы", "Рак", "Лев", "Дева",
        "Весы", "Скорпион", "Стрелец", "Козерог", "Водолей", "Рыбы");

    private static final Set<String> CHINESE = Set.of(
        "Monkey", "Rooster", "Dog", "Pig", "Rat", "Ox",
        "Tiger", "Rabbit", "Dragon", "Snake", "Horse", "Goat");

    @Nested
    @DisplayName("ZodiacGenerator (Western)")
    class Western {

        @RepeatedTest(200)
        @DisplayName("default generate() returns a valid English sign")
        void generateValid() {
            assertTrue(WESTERN_EN.contains(new ZodiacGenerator().generate()));
        }

        @Test
        @DisplayName("default generate() can surface every English sign over many draws")
        void generateCoversAll() {
            ZodiacGenerator gen = new ZodiacGenerator(GeneratorConfig.builder().seed(7L).build());
            Set<String> seen = new HashSet<>();
            for (int i = 0; i < 2000; i++) {
                seen.add(gen.generate());
            }
            assertEquals(WESTERN_EN, seen);
        }

        @Test
        @DisplayName("Russian locale generates Russian sign names")
        void russianGenerate() {
            ZodiacGenerator gen = new ZodiacGenerator(GeneratorConfig.builder().locale(RU).seed(7L).build());
            Set<String> seen = new HashSet<>();
            for (int i = 0; i < 2000; i++) {
                seen.add(gen.generate());
            }
            assertEquals(WESTERN_RU, seen);
        }

        @Test
        @DisplayName("signFor resolves the conventional sign in the configured locale")
        void signForLocalized() {
            ZodiacGenerator en = new ZodiacGenerator();
            assertEquals("Scorpio", en.signFor(LocalDate.of(1990, 11, 5)));
            assertEquals("Aries", en.signFor(LocalDate.of(2000, 3, 21)));   // exactly on cutoff
            assertEquals("Pisces", en.signFor(LocalDate.of(2000, 3, 20)));  // day before cutoff

            ZodiacGenerator ru = new ZodiacGenerator(RU);
            assertEquals("Скорпион", ru.signFor(LocalDate.of(1990, 11, 5)));
            assertEquals("Овен", ru.signFor(LocalDate.of(2000, 3, 21)));
            assertEquals("Рыбы", ru.signFor(LocalDate.of(2000, 3, 20)));
        }

        @Test
        @DisplayName("signFor handles the January wrap-around to Capricorn")
        void januaryWrap() {
            ZodiacGenerator gen = new ZodiacGenerator();
            assertEquals("Capricorn", gen.signFor(MonthDay.of(1, 10)));  // before Jan cutoff
            assertEquals("Aquarius", gen.signFor(MonthDay.of(1, 25)));   // on/after Jan cutoff
            assertEquals("Capricorn", gen.signFor(MonthDay.of(12, 25))); // December on/after cutoff
        }

        @Test
        @DisplayName("every day of the year maps to a valid sign")
        void everyDayValid() {
            ZodiacGenerator gen = new ZodiacGenerator();
            LocalDate d = LocalDate.of(2024, 1, 1); // leap year to include Feb 29
            for (int i = 0; i < 366; i++) {
                assertTrue(WESTERN_EN.contains(gen.signFor(d)), "no sign for " + d);
                d = d.plusDays(1);
            }
        }

        @Test
        @DisplayName("unmapped locale falls back to English names")
        void unmappedLocaleFallsBackToEnglish() {
            ZodiacGenerator gen = new ZodiacGenerator(Locale.of("is", "IS")); // Icelandic: no built-in file
            assertTrue(WESTERN_EN.contains(gen.generate()));
            assertEquals("Scorpio", gen.signFor(LocalDate.of(1990, 11, 5)));
        }

        @Test
        @DisplayName("German is now a built-in locale")
        void germanLocalized() {
            assertEquals("Skorpion", new ZodiacGenerator(Locale.GERMANY).signFor(LocalDate.of(1990, 11, 5)));
        }

        @Test
        @DisplayName("null arguments are rejected")
        void nullsRejected() {
            ZodiacGenerator gen = new ZodiacGenerator();
            assertThrows(NullPointerException.class, () -> new ZodiacGenerator((GeneratorConfig) null));
            assertThrows(NullPointerException.class, () -> new ZodiacGenerator((Locale) null));
            assertThrows(NullPointerException.class, () -> gen.signFor((LocalDate) null));
            assertThrows(NullPointerException.class, () -> gen.signFor((MonthDay) null));
        }
    }

    @Nested
    @DisplayName("ZodiacDataRegistry")
    class Registry {

        @Test
        @DisplayName("isRegistered / forLocale honor built-ins and reject null/unknown")
        void registryLookups() {
            assertTrue(ZodiacDataRegistry.isRegistered(RU));
            assertTrue(ZodiacDataRegistry.isRegistered(Locale.of("ru")));
            assertFalse(ZodiacDataRegistry.isRegistered(Locale.of("is", "IS"))); // Icelandic: no file
            assertFalse(ZodiacDataRegistry.isRegistered(null));

            assertNotNull(ZodiacDataRegistry.forLocale(RU));
            assertNotNull(ZodiacDataRegistry.forLocale(Locale.of("ru", "XX"))); // language fallback
            assertNull(ZodiacDataRegistry.forLocale(Locale.of("is"))); // Icelandic: no file
            assertNull(ZodiacDataRegistry.forLocale(null));

            assertTrue(ZodiacDataRegistry.registeredKeys().contains("ru_RU"));
        }

        @Test
        @DisplayName("register adds a custom provider; null is rejected")
        void registerCustom() {
            List<String> signs = List.of(
                "A1", "A2", "A3", "A4", "A5", "A6", "A7", "A8", "A9", "A10", "A11", "A12");
            ZodiacDataRegistry.register(new ZodiacDataProvider() {
                @Override
                public Locale getLocale() {
                    return Locale.of("zz");
                }

                @Override
                public List<String> getSigns() {
                    return signs;
                }
            });
            assertTrue(ZodiacDataRegistry.isRegistered(Locale.of("zz")));
            assertTrue(signs.contains(new ZodiacGenerator(Locale.of("zz")).generate()));
            assertThrows(NullPointerException.class, () -> ZodiacDataRegistry.register(null));
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
            assertEquals(gen.animalFor(2020), gen.animalFor(2020 - 12));
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
        @DisplayName("ofZodiac (default / locale / config) and ofChineseZodiac produce valid values")
        void facadeFactories() {
            assertTrue(WESTERN_EN.contains(Generators.ofZodiac().generate()));
            assertTrue(WESTERN_RU.contains(Generators.ofZodiac(RU).generate()));
            assertTrue(WESTERN_EN.contains(
                Generators.ofZodiac(GeneratorConfig.builder().seed(1L).build()).generate()));
            assertTrue(CHINESE.contains(Generators.ofChineseZodiac().generate()));
            assertTrue(CHINESE.contains(
                Generators.ofChineseZodiac(GeneratorConfig.builder().seed(1L).build()).generate()));
        }
    }
}
