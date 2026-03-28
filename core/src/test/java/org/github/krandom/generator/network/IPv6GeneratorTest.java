/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package org.github.krandom.generator.network;

import org.apache.commons.validator.routines.InetAddressValidator;
import org.github.krandom.generator.Generator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("IPv6Generator")
class IPv6GeneratorTest {

    private static final InetAddressValidator VALIDATOR = InetAddressValidator.getInstance();
    private static final int                  SAMPLES   = 500;

    // ── RFC 4291 format ───────────────────────────────────────────────────────


    @Nested
    @DisplayName("RFC 4291 format")
    class Format {

        @RepeatedTest(SAMPLES)
        @DisplayName("accepted by InetAddressValidator as a valid IPv6 address")
        void validByCommons() {
            assertTrue(VALIDATOR.isValidInet6Address(new IPv6Generator().generate()));
        }

        @RepeatedTest(SAMPLES)
        @DisplayName("exactly 8 groups separated by colons")
        void eightGroups() {
            String ip = new IPv6Generator().generate();
            String[] groups = ip.split(":", -1);
            assertEquals(8, groups.length,
                         "Expected 8 colon-separated groups in: " + ip);
        }

        @RepeatedTest(SAMPLES)
        @DisplayName("each group is 1-4 hex characters (leading zeros suppressed per RFC 5952 §4.1)")
        void groupsAreHex() {
            String ip = new IPv6Generator().generate();
            for (String group : ip.split(":")) {
                assertTrue(group.length() >= 1 && group.length() <= 4,
                           "Group '" + group + "' has invalid length in: " + ip);
                assertTrue(group.matches("[0-9a-f]+"),
                           "Group '" + group + "' contains non-lowercase-hex chars in: " + ip);
            }
        }

        @RepeatedTest(SAMPLES)
        @DisplayName("each group value is in [0x0000, 0xFFFF]")
        void groupValueRange() {
            String ip = new IPv6Generator().generate();
            for (String group : ip.split(":")) {
                int value = Integer.parseInt(group, 16);
                assertTrue(value >= 0x0000 && value <= 0xFFFF,
                           "Group value 0x" + group + " outside [0, 0xFFFF] in: " + ip);
            }
        }
    }

    // ── RFC 5952 text representation ──────────────────────────────────────────


    @Nested
    @DisplayName("RFC 5952 text representation")
    class TextRepresentation {

        @RepeatedTest(SAMPLES)
        @DisplayName("§4.1 no leading zeros in any group (e.g., 'db8' not '0db8')")
        void noLeadingZeros() {
            String ip = new IPv6Generator().generate();
            for (String group : ip.split(":")) {
                if (group.length() > 1) {
                    assertNotEquals('0', group.charAt(0),
                                    "Leading zero in group '" + group + "' of: " + ip);
                }
            }
        }

        @RepeatedTest(SAMPLES)
        @DisplayName("§4.3 all hex digits are lowercase")
        void lowercase() {
            String ip = new IPv6Generator().generate();
            assertEquals(ip, ip.toLowerCase(),
                         "Address contains uppercase hex digits: " + ip);
        }

        @RepeatedTest(SAMPLES)
        @DisplayName("no '::' compression (full notation for unambiguous test data)")
        void noCompression() {
            String ip = new IPv6Generator().generate();
            assertFalse(ip.contains("::"),
                        "Address should not contain '::' compression: " + ip);
        }
    }

    // ── Entropy / variety ─────────────────────────────────────────────────────


    @Nested
    @DisplayName("Entropy")
    class Entropy {

        @Test
        @DisplayName("generates distinct addresses (collision rate negligible over 1000 samples)")
        void distinctAddresses() {
            IPv6Generator gen = new IPv6Generator();
            long distinct = gen.stream().limit(1000).distinct().count();
            // 128-bit space: collision probability is astronomically low
            assertTrue(distinct >= 990, "Too many collisions: only " + distinct + " distinct out of 1000");
        }

        @Test
        @DisplayName("first group covers a wide range over many samples")
        void firstGroupVariety() {
            IPv6Generator gen = new IPv6Generator();
            long distinct = gen.stream()
                               .limit(10_000)
                               .map(ip -> ip.split(":")[0])
                               .distinct()
                               .count();
            assertTrue(distinct > 1000,
                       "First group has suspiciously low variety: " + distinct + " distinct values");
        }
    }

    // ── Generator<String> interface ───────────────────────────────────────────


    @Nested
    @DisplayName("Generator<String> interface")
    class GeneratorInterface {

        @Test
        @DisplayName("generateList(10) returns 10 valid addresses")
        void generateList() {
            List<String> list = new IPv6Generator().generateList(10);
            assertEquals(10, list.size());
            list.forEach(ip -> assertTrue(VALIDATOR.isValidInet6Address(ip), "Invalid: " + ip));
        }

        @Test
        @DisplayName("stream() produces valid addresses")
        void stream() {
            new IPv6Generator().stream().limit(50)
                               .forEach(ip -> assertTrue(VALIDATOR.isValidInet6Address(ip), "Invalid: " + ip));
        }

        @Test
        @DisplayName("map() transforms address to uppercase string")
        void map() {
            Generator<String> mapped = new IPv6Generator().map(String::toUpperCase);
            String upper = mapped.generate();
            assertTrue(upper.matches("[0-9A-F:]+"), "Unexpected chars: " + upper);
        }
    }

    // ── Seeded Generation ─────────────────────────────────────────────────────


    @Nested
    @DisplayName("Seeded generation")
    class SeededGeneration {

        @Test
        @DisplayName("same seed produces same IPv6 address")
        void sameSeedSameIP() {
            org.github.krandom.generator.GeneratorConfig config1 =
                org.github.krandom.generator.GeneratorConfig.builder().seed(42L).build();
            org.github.krandom.generator.GeneratorConfig config2 =
                org.github.krandom.generator.GeneratorConfig.builder().seed(42L).build();

            IPv6Generator gen1 = new IPv6Generator(config1);
            IPv6Generator gen2 = new IPv6Generator(config2);

            assertEquals(gen1.generate(), gen2.generate());
            assertEquals(gen1.generate(), gen2.generate());
            assertEquals(gen1.generate(), gen2.generate());
        }

        @Test
        @DisplayName("different seeds produce different IPv6s")
        void differentSeedsDifferentIPs() {
            IPv6Generator gen1 = new IPv6Generator(
                org.github.krandom.generator.GeneratorConfig.builder().seed(100L).build());
            IPv6Generator gen2 = new IPv6Generator(
                org.github.krandom.generator.GeneratorConfig.builder().seed(200L).build());

            assertNotEquals(gen1.generate(), gen2.generate());
        }

        @Test
        @DisplayName("null config throws NullPointerException")
        void nullConfigThrows() {
            assertThrows(NullPointerException.class,
                         () -> new IPv6Generator((org.github.krandom.generator.GeneratorConfig) null));
        }
    }
}
