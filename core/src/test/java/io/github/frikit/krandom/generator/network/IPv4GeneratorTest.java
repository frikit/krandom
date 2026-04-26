/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.network;

import org.apache.commons.validator.routines.InetAddressValidator;
import io.github.frikit.krandom.generator.Generator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("IPv4Generator")
class IPv4GeneratorTest {

    private static final InetAddressValidator VALIDATOR = InetAddressValidator.getInstance();
    private static final int                  SAMPLES   = 500;

    // ── RFC 791 format ────────────────────────────────────────────────────────


    @Nested
    @DisplayName("RFC 791 format")
    class Format {

        @RepeatedTest(SAMPLES)
        @DisplayName("accepted by InetAddressValidator as a valid IPv4 address")
        void validByCommons() {
            assertTrue(VALIDATOR.isValidInet4Address(new IPv4Generator().generate()));
        }

        @RepeatedTest(SAMPLES)
        @DisplayName("dotted-decimal: exactly 3 dots, all 4 parts are decimal")
        void dottedDecimalStructure() {
            String ip = new IPv4Generator().generate();
            String[] parts = ip.split("\\.", -1);
            assertEquals(4, parts.length, "Expected 4 octets in: " + ip);
            for (String part : parts) {
                assertTrue(part.matches("\\d+"), "Non-decimal part '" + part + "' in: " + ip);
                int value = Integer.parseInt(part);
                assertTrue(value >= 0 && value <= 255, "Octet out of [0,255]: " + value + " in: " + ip);
            }
        }

        @RepeatedTest(SAMPLES)
        @DisplayName("no leading zeros in any octet (e.g. no '007')")
        void noLeadingZeros() {
            String ip = new IPv4Generator().generate();
            for (String part : ip.split("\\.")) {
                if (part.length() > 1) {
                    assertNotEquals('0', part.charAt(0),
                                    "Leading zero in octet '" + part + "' of: " + ip);
                }
            }
        }
    }

    // ── First-octet range (unicast only, RFC 3171 / RFC 1112) ─────────────────


    @Nested
    @DisplayName("First-octet range")
    class FirstOctet {

        @RepeatedTest(SAMPLES)
        @DisplayName("first octet is in [0, 223] — no multicast (224-239) or reserved (240-255)")
        void firstOctetUnicast() {
            String ip = new IPv4Generator().generate();
            int octet1 = Integer.parseInt(ip.split("\\.")[0]);
            assertTrue(octet1 >= 0 && octet1 <= 223,
                       "First octet " + octet1 + " is outside [0, 223] in: " + ip);
        }

        @Test
        @DisplayName("first octet covers the full [0, 223] range over many samples")
        void firstOctetFullRange() {
            IPv4Generator gen = new IPv4Generator();
            boolean seenLow = false;   // near 0
            boolean seenHigh = false;  // near 223
            boolean seenMid = false;   // near 128
            for (int i = 0; i < 50_000; i++) {
                int octet1 = Integer.parseInt(gen.generate().split("\\.")[0]);
                if (octet1 <= 5) seenLow = true;
                if (octet1 >= 218) seenHigh = true;
                if (octet1 >= 120 && octet1 <= 135) seenMid = true;
                if (seenLow && seenHigh && seenMid) break;
            }
            assertTrue(seenLow, "Never generated first octet near 0");
            assertTrue(seenMid, "Never generated first octet near 128");
            assertTrue(seenHigh, "Never generated first octet near 223");
        }
    }

    // ── Remaining octets ──────────────────────────────────────────────────────


    @Nested
    @DisplayName("Octets 2-4 range [0, 255]")
    class OtherOctets {

        @Test
        @DisplayName("octets 2-4 reach 255 over many samples")
        void octetsReach255() {
            IPv4Generator gen = new IPv4Generator();
            boolean[] seen255 = new boolean[3];
            for (int i = 0; i < 50_000 && !(seen255[0] && seen255[1] && seen255[2]); i++) {
                String[] parts = gen.generate().split("\\.");
                for (int j = 0; j < 3; j++) {
                    if (Integer.parseInt(parts[j + 1]) == 255) seen255[j] = true;
                }
            }
            for (int j = 0; j < 3; j++) {
                assertTrue(seen255[j], "Octet " + (j + 2) + " never reached 255");
            }
        }

        @Test
        @DisplayName("octets 2-4 reach 0 over many samples")
        void octetsReach0() {
            IPv4Generator gen = new IPv4Generator();
            boolean[] seen0 = new boolean[3];
            for (int i = 0; i < 50_000 && !(seen0[0] && seen0[1] && seen0[2]); i++) {
                String[] parts = gen.generate().split("\\.");
                for (int j = 0; j < 3; j++) {
                    if (Integer.parseInt(parts[j + 1]) == 0) seen0[j] = true;
                }
            }
            for (int j = 0; j < 3; j++) {
                assertTrue(seen0[j], "Octet " + (j + 2) + " never reached 0");
            }
        }
    }

    // ── Generator<String> interface ───────────────────────────────────────────


    @Nested
    @DisplayName("Generator<String> interface")
    class GeneratorInterface {

        @Test
        @DisplayName("generateList(10) returns 10 valid addresses")
        void generateList() {
            List<String> list = new IPv4Generator().generateList(10);
            assertEquals(10, list.size());
            list.forEach(ip -> assertTrue(VALIDATOR.isValidInet4Address(ip), "Invalid: " + ip));
        }

        @Test
        @DisplayName("stream() produces valid addresses")
        void stream() {
            new IPv4Generator().stream().limit(50)
                               .forEach(ip -> assertTrue(VALIDATOR.isValidInet4Address(ip), "Invalid: " + ip));
        }

        @Test
        @DisplayName("map() transforms to InetAddress successfully")
        void map() throws Exception {
            Generator<String> gen = new IPv4Generator();
            String ip = gen.generate();
            assertTrue(VALIDATOR.isValidInet4Address(ip));
        }
    }

    // ── Seeded Generation ─────────────────────────────────────────────────────


    @Nested
    @DisplayName("Seeded generation")
    class SeededGeneration {

        @Test
        @DisplayName("same seed produces same IP address")
        void sameSeedSameIP() {
            io.github.frikit.krandom.generator.GeneratorConfig config1 =
                io.github.frikit.krandom.generator.GeneratorConfig.builder().seed(42L).build();
            io.github.frikit.krandom.generator.GeneratorConfig config2 =
                io.github.frikit.krandom.generator.GeneratorConfig.builder().seed(42L).build();

            IPv4Generator gen1 = new IPv4Generator(config1);
            IPv4Generator gen2 = new IPv4Generator(config2);

            assertEquals(gen1.generate(), gen2.generate());
            assertEquals(gen1.generate(), gen2.generate());
            assertEquals(gen1.generate(), gen2.generate());
        }

        @Test
        @DisplayName("different seeds produce different IPs")
        void differentSeedsDifferentIPs() {
            IPv4Generator gen1 = new IPv4Generator(
                io.github.frikit.krandom.generator.GeneratorConfig.builder().seed(100L).build());
            IPv4Generator gen2 = new IPv4Generator(
                io.github.frikit.krandom.generator.GeneratorConfig.builder().seed(200L).build());

            assertNotEquals(gen1.generate(), gen2.generate());
        }

        @Test
        @DisplayName("null config throws NullPointerException")
        void nullConfigThrows() {
            assertThrows(NullPointerException.class,
                         () -> new IPv4Generator((io.github.frikit.krandom.generator.GeneratorConfig) null));
        }
    }
}
