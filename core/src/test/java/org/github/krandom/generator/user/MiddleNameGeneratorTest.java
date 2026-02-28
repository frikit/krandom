/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package org.github.krandom.generator.user;

import org.github.krandom.generator.GeneratorConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("MiddleNameGenerator")
class MiddleNameGeneratorTest {

    @Nested
    @DisplayName("Constructors")
    class ConstructorTests {

        @Test
        @DisplayName("default constructor uses Locale.US")
        void defaultLocale() {
            assertEquals(Locale.US, new MiddleNameGenerator().getLocale());
        }

        @Test
        @DisplayName("config constructor stores locale")
        void configLocale() {
            GeneratorConfig cfg = GeneratorConfig.builder().locale(Locale.GERMANY).build();
            assertEquals(Locale.GERMANY, new MiddleNameGenerator(cfg).getLocale());
        }

        @Test
        @DisplayName("null config throws NullPointerException")
        void nullConfigThrows() {
            assertThrows(NullPointerException.class, () -> new MiddleNameGenerator((GeneratorConfig) null));
        }

        @Test
        @DisplayName("unsupported locale throws UnsupportedOperationException")
        void unsupportedLocaleThrows() {
            assertThrows(UnsupportedOperationException.class, () -> new MiddleNameGenerator(Locale.of("es", "ES")));
            assertThrows(UnsupportedOperationException.class, () -> new MiddleNameGenerator(Locale.JAPAN));
            assertThrows(UnsupportedOperationException.class, () -> new MiddleNameGenerator(Locale.CHINA));
        }
    }

    @Nested
    @DisplayName("Generation")
    class GenerationTests {

        @Test
        @DisplayName("generate returns non-empty middle name")
        void generate() {
            MiddleNameGenerator gen = new MiddleNameGenerator(Locale.US);
            for (int i = 0; i < 50; i++) {
                String middle = gen.generate();
                assertNotNull(middle);
                assertFalse(middle.isBlank());
            }
        }

        @Test
        @DisplayName("generate with gender returns non-empty middle name")
        void generateWithGender() {
            MiddleNameGenerator gen = new MiddleNameGenerator(Locale.GERMANY);
            for (int i = 0; i < 50; i++) {
                String middle = gen.generate(Gender.FEMALE);
                assertNotNull(middle);
                assertFalse(middle.isBlank());
            }
        }

        @Test
        @DisplayName("generate with null gender throws NullPointerException")
        void nullGenderThrows() {
            MiddleNameGenerator gen = new MiddleNameGenerator(Locale.US);
            assertThrows(NullPointerException.class, () -> gen.generate(null));
        }

        @Test
        @DisplayName("generateInitial returns one letter and period")
        void generateInitial() {
            MiddleNameGenerator gen = new MiddleNameGenerator(Locale.US);
            for (int i = 0; i < 50; i++) {
                assertTrue(gen.generateInitial().matches(".\\."));
            }
        }

        @Test
        @DisplayName("generateInitial with gender returns one letter and period")
        void generateInitialWithGender() {
            MiddleNameGenerator gen = new MiddleNameGenerator(Locale.ITALY);
            for (int i = 0; i < 50; i++) {
                assertTrue(gen.generateInitial(Gender.MALE).matches(".\\."));
            }
        }
    }

    @Nested
    @DisplayName("Support checks")
    class SupportChecks {

        @Test
        @DisplayName("supportsMiddleName reflects policy")
        void supportsMiddleNamePolicy() {
            assertTrue(MiddleNameGenerator.supportsMiddleName(Locale.US));
            assertTrue(MiddleNameGenerator.supportsMiddleName(Locale.GERMANY));
            assertFalse(MiddleNameGenerator.supportsMiddleName(Locale.of("es", "ES")));
            assertFalse(MiddleNameGenerator.supportsMiddleName(Locale.JAPAN));
            assertFalse(MiddleNameGenerator.supportsMiddleName(Locale.CHINA));
        }

        @Test
        @DisplayName("supportsMiddleName null locale throws NullPointerException")
        void supportsMiddleNameNullThrows() {
            assertThrows(NullPointerException.class, () -> MiddleNameGenerator.supportsMiddleName(null));
        }

        @Test
        @DisplayName("isLocaleExplicitlySupported returns true for supported locale")
        void explicitSupport() {
            assertTrue(new MiddleNameGenerator(Locale.US).isLocaleExplicitlySupported());
        }
    }
}

