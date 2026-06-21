/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.vehicle;

import io.github.frikit.krandom.generator.GeneratorConfig;
import io.github.frikit.krandom.generator.Generators;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Vehicle generators")
class VehicleGeneratorTest {

    @Nested
    @DisplayName("VinGenerator")
    class Vin {

        @RepeatedTest(300)
        @DisplayName("generate() is 17 valid chars with a correct check digit")
        void generateValid() {
            String vin = new VinGenerator().generate();
            assertTrue(vin.matches("[A-HJ-NPR-Z0-9]{17}"), vin); // no I, O, Q
            char[] chars = vin.toCharArray();
            assertEquals(VinGenerator.computeCheckChar(chars), chars[8], vin);
        }

        @Test
        @DisplayName("generate() can surface an 'X' check digit across seeded draws")
        void surfacesXCheckDigit() {
            VinGenerator gen = new VinGenerator(GeneratorConfig.builder().seed(2L).build());
            Set<Character> checkChars = new HashSet<>();
            for (int i = 0; i < 5000; i++) {
                checkChars.add(gen.generate().charAt(8));
            }
            assertTrue(checkChars.contains('X'), "an X check digit should occur over many draws");
        }

        @Test
        @DisplayName("transliterate covers digits and letters")
        void transliterate() {
            assertEquals(0, VinGenerator.transliterate('0'));
            assertEquals(5, VinGenerator.transliterate('5'));
            assertEquals(1, VinGenerator.transliterate('A'));
            assertEquals(9, VinGenerator.transliterate('Z'));
        }

        @Test
        @DisplayName("computeCheckChar covers both the digit and 'X' branches")
        void computeCheckCharBranches() {
            char[] allZero = "00000000000000000".toCharArray();
            assertEquals('0', VinGenerator.computeCheckChar(allZero)); // remainder 0

            char[] remainderTen = "D0000000000000000".toCharArray(); // 4 * weight 8 = 32, 32 % 11 = 10
            assertEquals('X', VinGenerator.computeCheckChar(remainderTen));
        }

        @Test
        @DisplayName("same seed is reproducible")
        void reproducible() {
            List<String> a = new VinGenerator(GeneratorConfig.builder().seed(4L).build()).generateList(20);
            List<String> b = new VinGenerator(GeneratorConfig.builder().seed(4L).build()).generateList(20);
            assertEquals(a, b);
        }

        @Test
        @DisplayName("null config is rejected")
        void nullConfig() {
            assertThrows(NullPointerException.class, () -> new VinGenerator(null));
        }
    }

    @Nested
    @DisplayName("VehicleGenerator")
    class Vehicle {

        private static final Set<String> MAKES = Set.of(
            "Toyota", "Honda", "Ford", "Chevrolet", "Volkswagen",
            "BMW", "Mercedes-Benz", "Nissan", "Hyundai", "Tesla");

        @RepeatedTest(200)
        @DisplayName("generate() is '<make> <model>' with a known make")
        void generateMakeModel() {
            String v = new VehicleGenerator().generate();
            int space = v.indexOf(' ');
            assertTrue(space > 0, v);
            assertTrue(MAKES.contains(v.substring(0, space)), v);
            assertTrue(v.length() > space + 1, v); // a non-empty model follows
        }

        @Test
        @DisplayName("make() returns a known make and is reproducible")
        void make() {
            assertTrue(MAKES.contains(new VehicleGenerator().make()));
            assertEquals(
                new VehicleGenerator(GeneratorConfig.builder().seed(1L).build()).make(),
                new VehicleGenerator(GeneratorConfig.builder().seed(1L).build()).make());
        }

        @RepeatedTest(100)
        @DisplayName("model() returns a non-empty model")
        void model() {
            assertTrue(new VehicleGenerator().model().length() > 0);
        }

        @RepeatedTest(100)
        @DisplayName("licensePlate() matches the AAA 0000 mask")
        void licensePlate() {
            assertTrue(new VehicleGenerator().licensePlate().matches("[A-Z]{3} \\d{4}"));
        }

        @Test
        @DisplayName("same seed is reproducible")
        void reproducible() {
            List<String> a = new VehicleGenerator(GeneratorConfig.builder().seed(6L).build()).generateList(20);
            List<String> b = new VehicleGenerator(GeneratorConfig.builder().seed(6L).build()).generateList(20);
            assertEquals(a, b);
        }

        @Test
        @DisplayName("null config is rejected")
        void nullConfig() {
            assertThrows(NullPointerException.class, () -> new VehicleGenerator(null));
        }
    }

    @Nested
    @DisplayName("Generators facade")
    class Facade {

        @Test
        @DisplayName("ofVin / ofVehicle (with and without config) produce valid values")
        void facadeFactories() {
            assertTrue(Generators.ofVin().generate().matches("[A-HJ-NPR-Z0-9]{17}"));
            assertTrue(Generators.ofVin(GeneratorConfig.builder().seed(1L).build())
                                 .generate().matches("[A-HJ-NPR-Z0-9]{17}"));
            assertTrue(Generators.ofVehicle().generate().contains(" "));
            assertTrue(Generators.ofVehicle(GeneratorConfig.builder().seed(1L).build())
                                 .licensePlate().matches("[A-Z]{3} \\d{4}"));
        }
    }
}
