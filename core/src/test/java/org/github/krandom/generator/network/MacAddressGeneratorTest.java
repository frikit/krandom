/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package org.github.krandom.generator.network;

import org.github.krandom.generator.Generators;
import org.github.krandom.generator.GeneratorConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("MacAddressGenerator")
class MacAddressGeneratorTest {

    private static final int SAMPLES = 50;
    private static final String COLON_MAC_REGEX = "[0-9A-F]{2}(:[0-9A-F]{2}){5}";
    private static final String DASH_MAC_REGEX   = "[0-9A-F]{2}(-[0-9A-F]{2}){5}";
    private static final String LOWER_MAC_REGEX  = "[0-9a-f]{2}(:[0-9a-f]{2}){5}";

    @Nested
    @DisplayName("Constructors")
    class ConstructorTests {

        @Test
        @DisplayName("default constructor creates a working generator")
        void defaultConstructor() {
            assertNotNull(new MacAddressGenerator().generate());
        }

        @Test
        @DisplayName("config constructor creates a working generator")
        void configConstructor() {
            assertNotNull(new MacAddressGenerator(GeneratorConfig.defaults()).generate());
        }

        @Test
        @DisplayName("null config throws NullPointerException")
        void nullConfigThrows() {
            assertThrows(NullPointerException.class,
                    () -> new MacAddressGenerator(null));
        }
    }

    @Nested
    @DisplayName("generate()")
    class GenerateTests {

        @Test
        @DisplayName("generate() returns non-null")
        void notNull() {
            assertNotNull(new MacAddressGenerator().generate());
        }

        @Test
        @DisplayName("generate() matches XX:XX:XX:XX:XX:XX uppercase format")
        void colonUppercaseFormat() {
            for (int i = 0; i < SAMPLES; i++) {
                String mac = new MacAddressGenerator().generate();
                assertTrue(mac.matches(COLON_MAC_REGEX),
                        "Expected colon-uppercase MAC, got: " + mac);
            }
        }

        @Test
        @DisplayName("generate() has length 17")
        void length17() {
            for (int i = 0; i < SAMPLES; i++) {
                assertEquals(17, new MacAddressGenerator().generate().length());
            }
        }

        @Test
        @DisplayName("generate('-') matches XX-XX-XX-XX-XX-XX uppercase format")
        void dashSeparator() {
            for (int i = 0; i < SAMPLES; i++) {
                String mac = new MacAddressGenerator().generate('-');
                assertTrue(mac.matches(DASH_MAC_REGEX),
                        "Expected dash-uppercase MAC, got: " + mac);
            }
        }
    }

    @Nested
    @DisplayName("generateLowercase()")
    class GenerateLowercaseTests {

        @Test
        @DisplayName("generateLowercase() matches xx:xx:xx:xx:xx:xx lowercase format")
        void colonLowercaseFormat() {
            for (int i = 0; i < SAMPLES; i++) {
                String mac = new MacAddressGenerator().generateLowercase();
                assertTrue(mac.matches(LOWER_MAC_REGEX),
                        "Expected colon-lowercase MAC, got: " + mac);
            }
        }

        @Test
        @DisplayName("generateLowercase('-') uses dash separator in lowercase")
        void dashLowercaseFormat() {
            for (int i = 0; i < SAMPLES; i++) {
                String mac = new MacAddressGenerator().generateLowercase('-');
                assertTrue(mac.matches("[0-9a-f]{2}(-[0-9a-f]{2}){5}"),
                        "Expected dash-lowercase MAC, got: " + mac);
            }
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
            MacAddressGenerator a = new MacAddressGenerator(cfg1);
            MacAddressGenerator b = new MacAddressGenerator(cfg2);
            for (int i = 0; i < SAMPLES; i++) {
                assertEquals(a.generate(), b.generate());
            }
        }

        @Test
        @DisplayName("seeded generateLowercase() produces identical output")
        void seededLowercaseReproducibility() {
            GeneratorConfig cfg1 = GeneratorConfig.builder().seed(99L).build();
            GeneratorConfig cfg2 = GeneratorConfig.builder().seed(99L).build();
            MacAddressGenerator a = new MacAddressGenerator(cfg1);
            MacAddressGenerator b = new MacAddressGenerator(cfg2);
            for (int i = 0; i < SAMPLES; i++) {
                assertEquals(a.generateLowercase(), b.generateLowercase());
            }
        }
    }

    @Nested
    @DisplayName("Generators factory")
    class GeneratorsFactoryTest {

        @Test
        @DisplayName("Generators.ofMacAddress() returns non-null value")
        void ofMacAddressDefault() {
            assertNotNull(Generators.ofMacAddress().generate());
        }

        @Test
        @DisplayName("Generators.ofMacAddress() produces valid MAC format")
        void ofMacAddressFormat() {
            String mac = Generators.ofMacAddress().generate();
            assertTrue(mac.matches(COLON_MAC_REGEX));
        }
    }
}
