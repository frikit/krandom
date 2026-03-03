/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package org.github.krandom.generator.user;

import org.github.krandom.generator.Generators;
import org.github.krandom.generator.GeneratorConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("FullNameGenerator")
class FullNameGeneratorTest {

    private static final int SAMPLES = 50;

    @Nested
    @DisplayName("Constructors")
    class ConstructorTests {

        @Test
        @DisplayName("default constructor uses Locale.US")
        void defaultLocale() {
            assertEquals(Locale.US, new FullNameGenerator().getLocale());
        }

        @Test
        @DisplayName("locale constructor stores locale")
        void localeConstructor() {
            assertEquals(Locale.GERMANY, new FullNameGenerator(Locale.GERMANY).getLocale());
        }

        @Test
        @DisplayName("config constructor stores locale from config")
        void configConstructor() {
            GeneratorConfig cfg = GeneratorConfig.builder().locale(Locale.FRANCE).build();
            assertEquals(Locale.FRANCE, new FullNameGenerator(cfg).getLocale());
        }

        @Test
        @DisplayName("null config throws NullPointerException")
        void nullConfigThrows() {
            assertThrows(NullPointerException.class,
                    () -> new FullNameGenerator((GeneratorConfig) null));
        }
    }

    @Nested
    @DisplayName("generate()")
    class GenerateTests {

        @Test
        @DisplayName("generate() returns non-null")
        void notNull() {
            assertNotNull(new FullNameGenerator().generate());
        }

        @Test
        @DisplayName("generate() contains a space separating first and last name")
        void containsSpace() {
            for (int i = 0; i < SAMPLES; i++) {
                String name = new FullNameGenerator().generate();
                assertTrue(name.contains(" "), "Expected space in full name: " + name);
            }
        }

        @Test
        @DisplayName("generate() has exactly two parts")
        void twoPartName() {
            for (int i = 0; i < SAMPLES; i++) {
                String name = new FullNameGenerator().generate();
                assertEquals(2, name.split(" ").length,
                        "Expected two-part name: " + name);
            }
        }

        @Test
        @DisplayName("generate(MALE) returns non-null two-part name")
        void generateMale() {
            for (int i = 0; i < SAMPLES; i++) {
                String name = new FullNameGenerator().generate(Gender.MALE);
                assertNotNull(name);
                assertEquals(2, name.split(" ").length);
            }
        }

        @Test
        @DisplayName("generate(FEMALE) returns non-null two-part name")
        void generateFemale() {
            for (int i = 0; i < SAMPLES; i++) {
                String name = new FullNameGenerator().generate(Gender.FEMALE);
                assertNotNull(name);
                assertEquals(2, name.split(" ").length);
            }
        }

        @Test
        @DisplayName("generateFemale/generateMale convenience methods return two-part names")
        void genderConvenienceMethods() {
            FullNameGenerator gen = new FullNameGenerator();
            String female = gen.generateFemale();
            String male = gen.generateMale();
            assertEquals(2, female.split(" ").length);
            assertEquals(2, male.split(" ").length);
        }

        @Test
        @DisplayName("null gender throws NullPointerException")
        void nullGenderThrows() {
            assertThrows(NullPointerException.class,
                    () -> new FullNameGenerator().generate((Gender) null));
        }

        @Test
        @DisplayName("generateWithMiddleName() returns three-part name in supported locale")
        void generateWithMiddleName() {
            FullNameGenerator gen = new FullNameGenerator(Locale.US);
            for (int i = 0; i < SAMPLES; i++) {
                String[] parts = gen.generateWithMiddleName().split(" ", 3);
                assertEquals(3, parts.length);
                assertFalse(parts[0].isBlank());
                assertFalse(parts[1].isBlank());
                assertFalse(parts[2].isBlank());
            }
        }

        @Test
        @DisplayName("generateWithMiddleName(gender) returns three-part name in supported locale")
        void generateWithMiddleNameGender() {
            FullNameGenerator gen = new FullNameGenerator(Locale.GERMANY);
            for (int i = 0; i < SAMPLES; i++) {
                String[] parts = gen.generateWithMiddleName(Gender.FEMALE).split(" ", 3);
                assertEquals(3, parts.length);
                assertFalse(parts[0].isBlank());
                assertFalse(parts[1].isBlank());
                assertFalse(parts[2].isBlank());
            }
        }

        @Test
        @DisplayName("generateWithMiddleInitial() uses middle initial format")
        void generateWithMiddleInitial() {
            FullNameGenerator gen = new FullNameGenerator(Locale.US);
            for (int i = 0; i < SAMPLES; i++) {
                String[] parts = gen.generateWithMiddleInitial().split(" ", 3);
                assertEquals(3, parts.length);
                assertTrue(parts[1].matches(".\\."), "Expected middle initial, got: " + parts[1]);
            }
        }

        @Test
        @DisplayName("generateWithMiddleInitial(gender) uses middle initial format")
        void generateWithMiddleInitialGender() {
            FullNameGenerator gen = new FullNameGenerator(Locale.ITALY);
            for (int i = 0; i < SAMPLES; i++) {
                String[] parts = gen.generateWithMiddleInitial(Gender.MALE).split(" ", 3);
                assertEquals(3, parts.length);
                assertTrue(parts[1].matches(".\\."), "Expected middle initial, got: " + parts[1]);
            }
        }

        @Test
        @DisplayName("generateWithMiddleName throws UnsupportedOperationException for unsupported locale")
        void generateWithMiddleNameUnsupportedLocale() {
            FullNameGenerator gen = new FullNameGenerator(Locale.of("es", "ES"));
            assertThrows(UnsupportedOperationException.class, gen::generateWithMiddleName);
        }

        @Test
        @DisplayName("generateWithMiddleInitial throws UnsupportedOperationException for unsupported locale")
        void generateWithMiddleInitialUnsupportedLocale() {
            FullNameGenerator gen = new FullNameGenerator(Locale.JAPAN);
            assertThrows(UnsupportedOperationException.class, gen::generateWithMiddleInitial);
        }

        @Test
        @DisplayName("generate() produces variety across calls")
        void producesVariety() {
            Set<String> names = new HashSet<>();
            FullNameGenerator gen = new FullNameGenerator();
            for (int i = 0; i < 100; i++) names.add(gen.generate());
            assertTrue(names.size() >= 5, "Expected variety in generated names");
        }
    }

    @Nested
    @DisplayName("NameOptions")
    class NameOptionsTests {

        @Test
        @DisplayName("generate(NameOptions) validates null options")
        void nullOptionsThrows() {
            FullNameGenerator gen = new FullNameGenerator();
            assertThrows(NullPointerException.class, () -> gen.generate((FullNameGenerator.NameOptions) null));
        }

        @Test
        @DisplayName("middle option adds third name component")
        void middleOptionAddsMiddleName() {
            FullNameGenerator gen = new FullNameGenerator(Locale.US);
            String name = gen.generate(new FullNameGenerator.NameOptions(true, false, false, false, null, null));
            assertEquals(3, name.split(" ").length);
        }

        @Test
        @DisplayName("middleInitial option formats initial with dot")
        void middleInitialOption() {
            FullNameGenerator gen = new FullNameGenerator(Locale.US);
            String name = gen.generate(new FullNameGenerator.NameOptions(false, true, false, false, null, null));
            String[] parts = name.split(" ");
            assertEquals(3, parts.length);
            assertTrue(parts[1].matches(".\\."));
        }

        @Test
        @DisplayName("middleInitial option supports gender-specific generation")
        void middleInitialWithGender() {
            FullNameGenerator gen = new FullNameGenerator(Locale.US);
            String name = gen.generate(new FullNameGenerator.NameOptions(false, true, false, false, Gender.MALE, null));
            String[] parts = name.split(" ");
            assertEquals(3, parts.length);
            assertTrue(parts[1].matches(".\\."));
        }

        @Test
        @DisplayName("middleInitial takes precedence when middle and middleInitial are both true")
        void middleInitialTakesPrecedence() {
            FullNameGenerator gen = new FullNameGenerator(Locale.US);
            String name = gen.generate(new FullNameGenerator.NameOptions(true, true, false, false, null, null));
            String[] parts = name.split(" ");
            assertEquals(3, parts.length);
            assertTrue(parts[1].matches(".\\."), "Expected middle initial precedence");
        }

        @Test
        @DisplayName("prefix and suffix options add extra components")
        void prefixAndSuffixOptions() {
            FullNameGenerator gen = new FullNameGenerator(Locale.US);
            String name = gen.generate(new FullNameGenerator.NameOptions(false, false, true, true, null, null));
            assertEquals(4, name.split(" ").length);
        }

        @Test
        @DisplayName("repeated option generation reuses cached locale helpers")
        void repeatedGenerationReusesCachedLocaleHelpers() {
            FullNameGenerator gen = new FullNameGenerator(Locale.US);
            FullNameGenerator.NameOptions opts =
                    new FullNameGenerator.NameOptions(true, false, true, true, Gender.FEMALE, "en");

            String first = gen.generate(opts);
            String second = gen.generate(opts);

            assertEquals(5, first.split(" ").length);
            assertEquals(5, second.split(" ").length);
        }

        @Test
        @DisplayName("gender option produces valid two-part name")
        void genderOption() {
            FullNameGenerator gen = new FullNameGenerator(Locale.US);
            String male = gen.generate(new FullNameGenerator.NameOptions(false, false, false, false, Gender.MALE, null));
            String female = gen.generate(new FullNameGenerator.NameOptions(false, false, false, false, Gender.FEMALE, null));
            assertEquals(2, male.split(" ").length);
            assertEquals(2, female.split(" ").length);
        }

        @Test
        @DisplayName("nationality option supports language token mapping")
        void nationalityLanguageToken() {
            FullNameGenerator gen = new FullNameGenerator();
            String us = gen.generate(new FullNameGenerator.NameOptions(false, false, false, false, null, "en"));
            String it = gen.generate(new FullNameGenerator.NameOptions(false, false, false, false, null, "it"));
            assertTrue(us.split(" ").length >= 2);
            assertTrue(it.split(" ").length >= 2);
        }

        @Test
        @DisplayName("nationality option supports country token mapping")
        void nationalityCountryToken() {
            FullNameGenerator gen = new FullNameGenerator();
            String uk = gen.generate(new FullNameGenerator.NameOptions(false, false, false, false, null, "uk"));
            String jp = gen.generate(new FullNameGenerator.NameOptions(false, false, false, false, null, "jp"));
            assertTrue(uk.split(" ").length >= 2);
            assertTrue(jp.split(" ").length >= 2);
        }

        @Test
        @DisplayName("seeded config supports nationality mapping through locale-specific config")
        void seededNationalityMapping() {
            GeneratorConfig cfg = GeneratorConfig.builder().locale(Locale.US).seed(123L).build();
            FullNameGenerator gen = new FullNameGenerator(cfg);

            String de = gen.generate(new FullNameGenerator.NameOptions(false, false, false, false, null, "de"));
            String fr = gen.generate(new FullNameGenerator.NameOptions(false, false, false, false, null, "fr"));

            assertTrue(de.split(" ").length >= 2);
            assertTrue(fr.split(" ").length >= 2);
        }

        @Test
        @DisplayName("blank nationality throws IllegalArgumentException")
        void blankNationalityThrows() {
            FullNameGenerator gen = new FullNameGenerator();
            IllegalArgumentException ex = assertThrows(
                    IllegalArgumentException.class,
                    () -> gen.generate(new FullNameGenerator.NameOptions(false, false, false, false, null, " "))
            );
            assertTrue(ex.getMessage().contains("nationality"));
        }

        @Test
        @DisplayName("unsupported nationality throws UnsupportedOperationException")
        void unsupportedNationalityThrows() {
            FullNameGenerator gen = new FullNameGenerator();
            assertThrows(
                    UnsupportedOperationException.class,
                    () -> gen.generate(new FullNameGenerator.NameOptions(false, false, false, false, null, "hu"))
            );
        }

        @Test
        @DisplayName("middle option with locale that does not support middle names throws")
        void middleWithUnsupportedLocaleThrows() {
            FullNameGenerator gen = new FullNameGenerator();
            assertThrows(
                    UnsupportedOperationException.class,
                    () -> gen.generate(new FullNameGenerator.NameOptions(true, false, false, false, null, "es"))
            );
        }

        @Test
        @DisplayName("reverse option places last name before first name")
        void reverseOption() {
            GeneratorConfig config = GeneratorConfig.builder().locale(Locale.US).seed(77L).build();
            FullNameGenerator base = new FullNameGenerator(config);
            FullNameGenerator reversed = new FullNameGenerator(config);

            String normal = base.generate(new FullNameGenerator.NameOptions(false, false, false, false, false, null, null));
            String reverse = reversed.generate(new FullNameGenerator.NameOptions(false, false, false, false, true, null, null));

            String[] normalParts = normal.split(" ");
            String[] reverseParts = reverse.split(" ");
            assertEquals(normalParts[0], reverseParts[1]);
            assertEquals(normalParts[1], reverseParts[0]);
        }

        @Test
        @DisplayName("reverse works with middle names")
        void reverseWithMiddleName() {
            GeneratorConfig config = GeneratorConfig.builder().locale(Locale.US).seed(91L).build();
            FullNameGenerator generator = new FullNameGenerator(config);
            String reverse = generator.generate(new FullNameGenerator.NameOptions(true, false, false, false, true, null, null));
            String[] parts = reverse.split(" ");
            assertEquals(3, parts.length);
            assertFalse(parts[0].isBlank());
            assertFalse(parts[1].isBlank());
            assertFalse(parts[2].isBlank());
            assertFalse(parts[2].endsWith("."), "Expected full middle name, not middle initial");
        }
    }

    @Nested
    @DisplayName("Locale support")
    class LocaleTests {

        @Test
        @DisplayName("German locale generates non-null full names")
        void germanLocale() {
            FullNameGenerator gen = new FullNameGenerator(Locale.GERMANY);
            for (int i = 0; i < SAMPLES; i++) {
                assertNotNull(gen.generate());
            }
        }

        @Test
        @DisplayName("Japanese locale generates non-null full names")
        void japaneseLocale() {
            FullNameGenerator gen = new FullNameGenerator(Locale.JAPAN);
            for (int i = 0; i < SAMPLES; i++) {
                assertNotNull(gen.generate());
            }
        }

        @Test
        @DisplayName("isLocaleExplicitlySupported() returns true for built-in locales")
        void isExplicitlySupported() {
            assertTrue(new FullNameGenerator().isLocaleExplicitlySupported());
            assertTrue(new FullNameGenerator(Locale.GERMANY).isLocaleExplicitlySupported());
        }

        @Test
        @DisplayName("middle-name methods throw for Chinese locale")
        void middleNameUnsupportedForChinese() {
            FullNameGenerator gen = new FullNameGenerator(Locale.CHINA);
            assertThrows(UnsupportedOperationException.class, gen::generateWithMiddleName);
            assertThrows(UnsupportedOperationException.class, gen::generateWithMiddleInitial);
        }
    }

    @Nested
    @DisplayName("Seeded generation")
    class SeededTests {

        @Test
        @DisplayName("seeded generators produce identical output")
        void seededReproducibility() {
            GeneratorConfig cfg1 = GeneratorConfig.builder().seed(42L).build();
            GeneratorConfig cfg2 = GeneratorConfig.builder().seed(42L).build();
            FullNameGenerator a = new FullNameGenerator(cfg1);
            FullNameGenerator b = new FullNameGenerator(cfg2);
            for (int i = 0; i < SAMPLES; i++) {
                assertEquals(a.generate(), b.generate());
            }
        }

        @Test
        @DisplayName("seeded generate(FEMALE) produces identical output")
        void seededGenderReproducibility() {
            GeneratorConfig cfg1 = GeneratorConfig.builder().seed(99L).build();
            GeneratorConfig cfg2 = GeneratorConfig.builder().seed(99L).build();
            FullNameGenerator a = new FullNameGenerator(cfg1);
            FullNameGenerator b = new FullNameGenerator(cfg2);
            for (int i = 0; i < SAMPLES; i++) {
                assertEquals(a.generate(Gender.FEMALE), b.generate(Gender.FEMALE));
            }
        }
    }

    @Nested
    @DisplayName("Generators factory")
    class GeneratorsFactoryTest {

        @Test
        @DisplayName("Generators.ofFullName() returns non-null value")
        void ofFullNameDefault() {
            assertNotNull(Generators.ofFullName().generate());
        }

        @Test
        @DisplayName("Generators.ofFullName() uses Locale.US")
        void ofFullNameLocale() {
            assertEquals(Locale.US, Generators.ofFullName().getLocale());
        }
    }
}
