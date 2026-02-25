/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package org.github.krandom.generator.location;

import org.github.krandom.generator.Generators;
import org.github.krandom.generator.GeneratorConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("StreetAddressGenerator")
class StreetAddressGeneratorTest {

    private static final int SAMPLES = 50;

    @Nested
    @DisplayName("Constructors")
    class ConstructorTests {

        @Test
        @DisplayName("default constructor creates a working generator")
        void defaultConstructor() {
            assertNotNull(new StreetAddressGenerator().generate());
        }

        @Test
        @DisplayName("config constructor creates a working generator")
        void configConstructor() {
            GeneratorConfig cfg = GeneratorConfig.defaults();
            assertNotNull(new StreetAddressGenerator(cfg).generate());
        }

        @Test
        @DisplayName("null config throws NullPointerException")
        void nullConfigThrows() {
            assertThrows(NullPointerException.class,
                    () -> new StreetAddressGenerator(null));
        }
    }

    @Nested
    @DisplayName("generate()")
    class GenerateTests {

        @Test
        @DisplayName("generate() returns non-null")
        void notNull() {
            assertNotNull(new StreetAddressGenerator().generate());
        }

        @Test
        @DisplayName("generate() has three space-separated parts")
        void threePartAddress() {
            for (int i = 0; i < SAMPLES; i++) {
                String address = new StreetAddressGenerator().generate();
                String[] parts = address.split(" ");
                assertEquals(3, parts.length,
                        "Expected 3 parts in address: " + address);
            }
        }

        @Test
        @DisplayName("house number is a positive integer in [1, 9999]")
        void houseNumberInRange() {
            for (int i = 0; i < SAMPLES; i++) {
                String address = new StreetAddressGenerator().generate();
                int number = Integer.parseInt(address.split(" ")[0]);
                assertTrue(number >= 1 && number <= 9999,
                        "House number out of range: " + number);
            }
        }

        @Test
        @DisplayName("generate() produces variety")
        void producesVariety() {
            Set<String> addresses = new HashSet<>();
            StreetAddressGenerator gen = new StreetAddressGenerator();
            for (int i = 0; i < 100; i++) addresses.add(gen.generate());
            assertTrue(addresses.size() >= 10, "Expected variety in generated addresses");
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
            StreetAddressGenerator a = new StreetAddressGenerator(cfg1);
            StreetAddressGenerator b = new StreetAddressGenerator(cfg2);
            for (int i = 0; i < SAMPLES; i++) {
                assertEquals(a.generate(), b.generate());
            }
        }

        @Test
        @DisplayName("different seeds produce different output over many calls")
        void differentSeedsDiffer() {
            GeneratorConfig cfg1 = GeneratorConfig.builder().seed(1L).build();
            GeneratorConfig cfg2 = GeneratorConfig.builder().seed(999L).build();
            StreetAddressGenerator a = new StreetAddressGenerator(cfg1);
            StreetAddressGenerator b = new StreetAddressGenerator(cfg2);
            boolean anyDiff = false;
            for (int i = 0; i < 20; i++) {
                if (!a.generate().equals(b.generate())) {
                    anyDiff = true;
                    break;
                }
            }
            assertTrue(anyDiff, "Different seeds should produce different addresses");
        }
    }

    @Nested
    @DisplayName("Generators factory")
    class GeneratorsFactoryTest {

        @Test
        @DisplayName("Generators.ofStreetAddress() returns non-null value")
        void ofStreetAddressDefault() {
            assertNotNull(Generators.ofStreetAddress().generate());
        }

        @Test
        @DisplayName("Generators.ofStreetAddress() produces a three-part address")
        void ofStreetAddressFormat() {
            String address = Generators.ofStreetAddress().generate();
            assertEquals(3, address.split(" ").length);
        }
    }
}
