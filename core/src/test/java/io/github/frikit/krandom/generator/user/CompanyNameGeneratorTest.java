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
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("CompanyNameGenerator")
class CompanyNameGeneratorTest {

    private static final int SAMPLES = 50;


    @Nested
    @DisplayName("Constructors")
    class ConstructorTests {

        @Test
        @DisplayName("default constructor creates a working generator")
        void defaultConstructor() {
            assertNotNull(new CompanyNameGenerator().generate());
        }

        @Test
        @DisplayName("config constructor creates a working generator")
        void configConstructor() {
            assertNotNull(new CompanyNameGenerator(GeneratorConfig.defaults()).generate());
        }

        @Test
        @DisplayName("null config throws NullPointerException")
        void nullConfigThrows() {
            assertThrows(NullPointerException.class,
                         () -> new CompanyNameGenerator(null));
        }
    }


    @Nested
    @DisplayName("generate()")
    class GenerateTests {

        @Test
        @DisplayName("generate() returns non-null")
        void notNull() {
            assertNotNull(new CompanyNameGenerator().generate());
        }

        @Test
        @DisplayName("generate() returns at least a two-word name")
        void atLeastTwoWords() {
            for (int i = 0; i < SAMPLES; i++) {
                String name = new CompanyNameGenerator().generate();
                assertTrue(name.split(" ").length >= 2,
                           "Expected at least two words: " + name);
            }
        }

        @Test
        @DisplayName("generate(true) returns three-part name with suffix")
        void withSuffix() {
            for (int i = 0; i < SAMPLES; i++) {
                String name = new CompanyNameGenerator().generate(true);
                assertEquals(3, name.split(" ").length,
                             "Expected prefix + noun + suffix: " + name);
            }
        }

        @Test
        @DisplayName("generate(false) returns two-part name without suffix")
        void withoutSuffix() {
            for (int i = 0; i < SAMPLES; i++) {
                String name = new CompanyNameGenerator().generate(false);
                assertEquals(2, name.split(" ").length,
                             "Expected prefix + noun only: " + name);
            }
        }

        @Test
        @DisplayName("generate() produces variety")
        void producesVariety() {
            Set<String> names = new HashSet<>();
            CompanyNameGenerator gen = new CompanyNameGenerator();
            for (int i = 0; i < 100; i++) names.add(gen.generate());
            assertTrue(names.size() >= 5, "Expected variety in generated company names");
        }

        @Test
        @DisplayName("generateSuffix() returns non-empty suffix")
        void suffixOnly() {
            String suffix = new CompanyNameGenerator().generateSuffix();
            assertNotNull(suffix);
            assertFalse(suffix.isBlank());
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
            CompanyNameGenerator a = new CompanyNameGenerator(cfg1);
            CompanyNameGenerator b = new CompanyNameGenerator(cfg2);
            for (int i = 0; i < SAMPLES; i++) {
                assertEquals(a.generate(), b.generate());
            }
        }

        @Test
        @DisplayName("seeded generate(false) produces identical output")
        void seededNoSuffixReproducibility() {
            GeneratorConfig cfg1 = GeneratorConfig.builder().seed(77L).build();
            GeneratorConfig cfg2 = GeneratorConfig.builder().seed(77L).build();
            CompanyNameGenerator a = new CompanyNameGenerator(cfg1);
            CompanyNameGenerator b = new CompanyNameGenerator(cfg2);
            for (int i = 0; i < SAMPLES; i++) {
                assertEquals(a.generate(false), b.generate(false));
            }
        }
    }


    @Nested
    @DisplayName("Generators factory")
    class GeneratorsFactoryTest {

        @Test
        @DisplayName("Generators.ofCompanyName() returns non-null value")
        void ofCompanyNameDefault() {
            assertNotNull(Generators.ofCompanyName().generate());
        }

        @Test
        @DisplayName("Generators.ofCompanyName() produces a three-part name")
        void ofCompanyNameFormat() {
            assertEquals(3, Generators.ofCompanyName().generate().split(" ").length);
        }
    }
}
