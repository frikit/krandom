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
        @DisplayName("null gender throws NullPointerException")
        void nullGenderThrows() {
            assertThrows(NullPointerException.class,
                    () -> new FullNameGenerator().generate(null));
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
