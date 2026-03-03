/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package org.github.krandom.generator.network;

import org.apache.commons.validator.routines.InetAddressValidator;
import org.github.krandom.generator.Generators;
import org.github.krandom.generator.GeneratorConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Network extension generators")
class NetworkExtensionsTest {

    private static final InetAddressValidator VALIDATOR = InetAddressValidator.getInstance();

    @Test
    @DisplayName("IPv4 private/public/cidr generation works")
    void ipv4ExtendedMethods() {
        IPv4Generator gen = new IPv4Generator(GeneratorConfig.builder().seed(42L).build());

        String privateIp = gen.generatePrivate();
        assertTrue(VALIDATOR.isValidInet4Address(privateIp));
        assertTrue(privateIp.startsWith("10.")
                || privateIp.startsWith("192.168.")
                || privateIp.matches("172\\.(1[6-9]|2\\d|3[01])\\..*"));

        String publicIp = gen.generatePublic();
        assertTrue(VALIDATOR.isValidInet4Address(publicIp));
        assertFalse(publicIp.startsWith("10."));
        assertFalse(publicIp.startsWith("192.168."));
        assertFalse(publicIp.startsWith("127."));
        assertFalse(publicIp.startsWith("169.254."));

        String cidr = gen.generateCidr();
        assertTrue(cidr.matches("(\\d{1,3}\\.){3}\\d{1,3}/\\d{1,2}"));
        int prefix = Integer.parseInt(cidr.substring(cidr.indexOf('/') + 1));
        assertTrue(prefix >= 8 && prefix <= 30);
    }

    @Test
    @DisplayName("IPv4 private helper detects private ranges")
    void ipv4PrivateHelper() {
        assertTrue(IPv4Generator.isPrivate("10.1.2.3"));
        assertTrue(IPv4Generator.isPrivate("172.16.0.1"));
        assertTrue(IPv4Generator.isPrivate("172.31.255.255"));
        assertTrue(IPv4Generator.isPrivate("192.168.1.1"));
        assertFalse(IPv4Generator.isPrivate("172.15.0.1"));
        assertFalse(IPv4Generator.isPrivate("172.32.0.1"));
        assertFalse(IPv4Generator.isPrivate("192.167.1.1"));
        assertFalse(IPv4Generator.isPrivate("8.8.8.8"));
    }

    @Test
    @DisplayName("IPv4 private generation reaches all RFC1918 ranges")
    void ipv4PrivateRangeCoverage() {
        IPv4Generator gen = new IPv4Generator();
        boolean saw10 = false;
        boolean saw172 = false;
        boolean saw192 = false;
        for (int i = 0; i < 5000 && !(saw10 && saw172 && saw192); i++) {
            String ip = gen.generatePrivate();
            saw10 |= ip.startsWith("10.");
            saw172 |= ip.matches("172\\.(1[6-9]|2\\d|3[01])\\..*");
            saw192 |= ip.startsWith("192.168.");
        }
        assertTrue(saw10 && saw172 && saw192, "Expected all RFC1918 ranges to appear");
    }

    @Test
    @DisplayName("IPv4 public generation includes edge first octets and excludes reserved private windows")
    void ipv4PublicCoverage() {
        IPv4Generator gen = new IPv4Generator();
        boolean saw172Public = false;
        boolean saw192Public = false;
        boolean sawOther = false;
        for (int i = 0; i < 20_000 && !(saw172Public && saw192Public && sawOther); i++) {
            String ip = gen.generatePublic();
            String[] parts = ip.split("\\.");
            int first = Integer.parseInt(parts[0]);
            int second = Integer.parseInt(parts[1]);
            if (first == 172) {
                saw172Public = true;
                assertTrue(second < 16 || second > 31);
            } else if (first == 192) {
                saw192Public = true;
                assertNotEquals(168, second);
            } else {
                sawOther = true;
            }
            assertFalse(ip.startsWith("127."));
            assertFalse(ip.startsWith("169.254."));
            assertFalse(IPv4Generator.isPrivate(ip));
        }
        assertTrue(saw172Public);
        assertTrue(saw192Public);
        assertTrue(sawOther);
    }

    @Test
    @DisplayName("IPv4 public branch covers both 172 second-octet paths")
    void ipv4Public172SecondOctetBranchCoverage() throws Exception {
        IPv4Generator trueBranch = new IPv4Generator(GeneratorConfig.builder().seed(1L).build());
        Field randomField = IPv4Generator.class.getDeclaredField("random");
        randomField.setAccessible(true);
        randomField.set(trueBranch, new Random() {
            private int call;

            @Override
            public int nextInt(int bound) {
                call++;
                if (call == 1) {
                    return 167; // picks first octet 172 in allowedFirstOctets.
                }
                return 0;
            }

            @Override
            public boolean nextBoolean() {
                return true;
            }
        });
        int secondTrue = Integer.parseInt(trueBranch.generatePublic().split("\\.")[1]);
        assertTrue(secondTrue < 16);

        IPv4Generator falseBranch = new IPv4Generator(GeneratorConfig.builder().seed(1L).build());
        randomField.set(falseBranch, new Random() {
            private int call;

            @Override
            public int nextInt(int bound) {
                call++;
                if (call == 1) {
                    return 167; // picks first octet 172 in allowedFirstOctets.
                }
                return 0;
            }

            @Override
            public boolean nextBoolean() {
                return false;
            }
        });
        int secondFalse = Integer.parseInt(falseBranch.generatePublic().split("\\.")[1]);
        assertTrue(secondFalse >= 32);
    }

    @Test
    @DisplayName("IPv4 public covers 192.* branch with second-octet remap")
    void ipv4Public192SecondOctetRemap() throws Exception {
        IPv4Generator generator = new IPv4Generator(GeneratorConfig.builder().seed(1L).build());
        Field randomField = IPv4Generator.class.getDeclaredField("random");
        randomField.setAccessible(true);
        randomField.set(generator, new Random() {
            private int call;

            @Override
            public int nextInt(int bound) {
                call++;
                if (call == 1) {
                    return 187; // picks first octet 192 in allowedFirstOctets.
                }
                if (call == 2) {
                    return 200; // triggers second >= 168 remap branch.
                }
                return 0;
            }
        });
        String ip = generator.generatePublic();
        int second = Integer.parseInt(ip.split("\\.")[1]);
        assertEquals(201, second);
    }

    @Test
    @DisplayName("IPv4 public covers 192.* branch without second-octet remap")
    void ipv4Public192SecondOctetNoRemap() throws Exception {
        IPv4Generator generator = new IPv4Generator(GeneratorConfig.builder().seed(1L).build());
        Field randomField = IPv4Generator.class.getDeclaredField("random");
        randomField.setAccessible(true);
        randomField.set(generator, new Random() {
            private int call;

            @Override
            public int nextInt(int bound) {
                call++;
                if (call == 1) {
                    return 187; // picks first octet 192 in allowedFirstOctets.
                }
                if (call == 2) {
                    return 100; // second < 168, no remap branch.
                }
                return 0;
            }
        });
        String ip = generator.generatePublic();
        int second = Integer.parseInt(ip.split("\\.")[1]);
        assertEquals(100, second);
    }

    @Test
    @DisplayName("IPv6 CIDR generation works")
    void ipv6Cidr() {
        IPv6Generator gen = new IPv6Generator(GeneratorConfig.builder().seed(7L).build());
        String cidr = gen.generateCidr();
        String[] parts = cidr.split("/");
        assertEquals(2, parts.length);
        assertTrue(VALIDATOR.isValidInet6Address(parts[0]));
        int prefix = Integer.parseInt(parts[1]);
        assertTrue(prefix >= 16 && prefix <= 128);
    }

    @Test
    @DisplayName("Port generator emits expected ranges")
    void portRanges() {
        assertNotNull(new PortGenerator().generate());
        PortGenerator gen = new PortGenerator(GeneratorConfig.builder().seed(11L).build());
        int any = Integer.parseInt(gen.generate());
        int system = Integer.parseInt(gen.generateSystemPort());
        int registered = Integer.parseInt(gen.generateRegisteredPort());
        int dynamic = Integer.parseInt(gen.generateDynamicPort());
        assertTrue(any >= 1 && any <= 65535);
        assertTrue(system >= 1 && system <= 1023);
        assertTrue(registered >= 1024 && registered <= 49151);
        assertTrue(dynamic >= 49152 && dynamic <= 65535);
    }

    @Test
    @DisplayName("Slug generator emits URL-safe values and slugify normalizes")
    void slugGenerator() {
        assertNotNull(new SlugGenerator().generate());
        SlugGenerator gen = new SlugGenerator(GeneratorConfig.builder().seed(22L).build());
        String slug = gen.generate();
        assertTrue(slug.matches("[a-z0-9]+(-[a-z0-9]+)+"));
        assertEquals("hello-world-2026", gen.slugify("  Hello, World! 2026  "));
        assertEquals("n-a", gen.slugify("___"));
        assertThrows(NullPointerException.class, () -> gen.slugify(null));
    }

    @Test
    @DisplayName("User agent generator supports browser and bot modes")
    void userAgentGenerator() {
        assertNotNull(new UserAgentGenerator().generate());
        UserAgentGenerator gen = new UserAgentGenerator(GeneratorConfig.builder().seed(33L).build());
        String browser = gen.generate();
        String bot = gen.generateBot();
        assertTrue(browser.startsWith("Mozilla/5.0"));
        assertTrue(bot.toLowerCase().contains("bot"));
    }

    @Test
    @DisplayName("factory methods return working generators")
    void generatorFactories() {
        assertNotNull(Generators.ofIP().generate());
        assertNotNull(Generators.ofPort().generate());
        assertNotNull(Generators.ofSlug().generate());
        assertNotNull(Generators.ofUserAgent().generate());
    }

    @Test
    @DisplayName("IP generator returns valid v4 or v6 values")
    void ipGenerator() {
        IPGenerator gen = new IPGenerator(GeneratorConfig.builder().seed(55L).build());
        boolean sawV4 = false;
        boolean sawV6 = false;
        for (int i = 0; i < 200; i++) {
            String ip = gen.generate();
            if (VALIDATOR.isValidInet4Address(ip)) {
                sawV4 = true;
            } else if (VALIDATOR.isValidInet6Address(ip)) {
                sawV6 = true;
            } else {
                fail("Generated invalid IP: " + ip);
            }
        }
        assertTrue(sawV4);
        assertTrue(sawV6);
        assertTrue(VALIDATOR.isValidInet4Address(gen.generateIPv4()));
        assertTrue(VALIDATOR.isValidInet6Address(gen.generateIPv6()));
    }
}
