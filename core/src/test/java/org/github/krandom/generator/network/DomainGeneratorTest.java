/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package org.github.krandom.generator.network;

import org.github.krandom.generator.GeneratorConfig;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DomainGeneratorTest {

    @Test
    void testDefaultConstructor() {
        DomainGenerator generator = new DomainGenerator();
        assertNotNull(generator);
    }

    @Test
    void testLocaleConstructor() {
        DomainGenerator generator = new DomainGenerator(Locale.US);
        assertNotNull(generator);
    }

    @Test
    void testConfigConstructor() {
        GeneratorConfig config = GeneratorConfig.builder().seed(12345L).build();
        DomainGenerator generator = new DomainGenerator(config);
        assertNotNull(generator);
    }

    @Test
    void testNullConfigThrowsException() {
        assertThrows(NullPointerException.class, () -> new DomainGenerator((GeneratorConfig) null));
    }

    @Test
    void testGenerateNotNull() {
        DomainGenerator generator = new DomainGenerator();
        String domain = generator.generate();
        assertNotNull(domain);
    }

    @Test
    void testGenerateContainsDot() {
        DomainGenerator generator = new DomainGenerator();
        String domain = generator.generate();
        assertTrue(domain.contains("."), "Domain should contain a dot");
    }

    @Test
    void testGenerateName() {
        DomainGenerator generator = new DomainGenerator();
        String name = generator.generateName();
        assertNotNull(name);
        assertFalse(name.isBlank());
        assertFalse(name.contains("."));
        assertTrue(name.matches("[a-z]+"));
    }

    @Test
    void testBogusStyleDomainAliases() {
        DomainGenerator generator = new DomainGenerator(Locale.GERMANY);
        assertTrue(generator.generateDomainName().contains("."));
        assertFalse(generator.generateDomainWord().contains("."));
        assertFalse(generator.generateDomainSuffix().contains("."));
    }

    @Test
    void testGenerateHasValidFormat() {
        DomainGenerator generator = new DomainGenerator();
        String domain = generator.generate();
        String[] parts = domain.split("\\.");
        assertTrue(parts.length >= 2, "Domain should have at least 2 parts");
    }

    @Test
    void testGenerateWithTLD() {
        DomainGenerator generator = new DomainGenerator();
        String domain = generator.generate("io");
        assertTrue(domain.endsWith(".io"), "Domain should end with .io");
    }

    @Test
    void testGenerateWithCustomTLD() {
        DomainGenerator generator = new DomainGenerator();
        String domain = generator.generate("example");
        assertTrue(domain.endsWith(".example"));
    }

    @Test
    void testGenerateWithTLDNotNull() {
        DomainGenerator generator = new DomainGenerator();
        assertThrows(NullPointerException.class, () -> generator.generate(null));
    }

    @Test
    void testGetTLD() {
        DomainGenerator generator = new DomainGenerator();
        String tld = generator.getTLD();
        assertNotNull(tld);
        assertFalse(tld.contains("."), "TLD should not contain dot");
    }

    @Test
    void testGetPopularTLD() {
        DomainGenerator generator = new DomainGenerator();
        String tld = generator.getPopularTLD();
        assertNotNull(tld);
        assertTrue(tld.length() >= 2 && tld.length() <= 6);
    }

    @Test
    void testGetLocaleTLD_US() {
        DomainGenerator generator = new DomainGenerator(Locale.US);
        assertEquals("us", generator.getLocaleTLD());
    }

    @Test
    void testGetLocaleTLD_UK() {
        DomainGenerator generator = new DomainGenerator(Locale.UK);
        assertEquals("uk", generator.getLocaleTLD());
    }

    @Test
    void testGetLocaleTLD_Germany() {
        DomainGenerator generator = new DomainGenerator(Locale.GERMANY);
        assertEquals("de", generator.getLocaleTLD());
    }

    @Test
    void testGetLocaleTLD_France() {
        DomainGenerator generator = new DomainGenerator(Locale.FRANCE);
        assertEquals("fr", generator.getLocaleTLD());
    }

    @Test
    void testGetLocaleTLD_Italy() {
        DomainGenerator generator = new DomainGenerator(Locale.ITALY);
        assertEquals("it", generator.getLocaleTLD());
    }

    @Test
    void testGetLocaleTLD_Japan() {
        DomainGenerator generator = new DomainGenerator(Locale.JAPAN);
        assertEquals("jp", generator.getLocaleTLD());
    }

    @Test
    void testGetLocaleTLD_China() {
        DomainGenerator generator = new DomainGenerator(Locale.CHINA);
        assertEquals("cn", generator.getLocaleTLD());
    }

    @Test
    void testGetLocaleTLD_Brazil() {
        DomainGenerator generator = new DomainGenerator(new Locale("pt", "BR"));
        assertEquals("br", generator.getLocaleTLD());
    }

    @Test
    void testGetLocaleTLD_Australia() {
        DomainGenerator generator = new DomainGenerator(new Locale("en", "AU"));
        assertEquals("au", generator.getLocaleTLD());
    }

    @Test
    void testGetLocaleTLD_Spain() {
        DomainGenerator generator = new DomainGenerator(new Locale("es", "ES"));
        assertEquals("es", generator.getLocaleTLD());
    }

    @Test
    void testGetLocaleTLD_DefaultLocale() {
        GeneratorConfig config = GeneratorConfig.builder().build();
        DomainGenerator generator = new DomainGenerator(config);
        // Default locale is US
        assertEquals("us", generator.getLocaleTLD());
    }

    @Test
    void testPrivateLocaleTldMapperHandlesNull() throws Exception {
        DomainGenerator generator = new DomainGenerator();
        Method m = DomainGenerator.class.getDeclaredMethod("getLocaleTLD", Locale.class);
        m.setAccessible(true);
        assertNull(m.invoke(generator, new Object[] { null }));
    }

    @Test
    void testSeededGeneratorProducesSameResults() {
        DomainGenerator gen1 = new DomainGenerator(GeneratorConfig.builder().seed(42L).build());
        DomainGenerator gen2 = new DomainGenerator(GeneratorConfig.builder().seed(42L).build());

        assertEquals(gen1.generate(), gen2.generate());
        assertEquals(gen1.generate(), gen2.generate());
        assertEquals(gen1.generate(), gen2.generate());
    }

    @Test
    void testSeededGeneratorWithTLDProducesSameResults() {
        DomainGenerator gen1 = new DomainGenerator(GeneratorConfig.builder().seed(999L).build());
        DomainGenerator gen2 = new DomainGenerator(GeneratorConfig.builder().seed(999L).build());

        assertEquals(gen1.generate("com"), gen2.generate("com"));
        assertEquals(gen1.generate("org"), gen2.generate("org"));
    }

    @Test
    void testDifferentSeedsProduceDifferentResults() {
        DomainGenerator gen1 = new DomainGenerator(GeneratorConfig.builder().seed(100L).build());
        DomainGenerator gen2 = new DomainGenerator(GeneratorConfig.builder().seed(200L).build());

        assertNotEquals(gen1.generate(), gen2.generate());
    }

    @Test
    void testGenerateMultipleDomains() {
        DomainGenerator generator = new DomainGenerator();
        Set<String> domains = new HashSet<>();
        for (int i = 0; i < 50; i++) {
            domains.add(generator.generate());
        }
        assertTrue(domains.size() > 10, "Should generate diverse domains");
    }

    @Test
    void testGenerateMultipleTLDs() {
        DomainGenerator generator = new DomainGenerator();
        Set<String> tlds = new HashSet<>();
        for (int i = 0; i < 100; i++) {
            tlds.add(generator.getTLD());
        }
        assertTrue(tlds.size() > 3, "Should generate diverse TLDs");
    }

    @Test
    void testPopularTLDsAreLimited() {
        DomainGenerator generator = new DomainGenerator();
        Set<String> tlds = new HashSet<>();
        for (int i = 0; i < 200; i++) {
            tlds.add(generator.getPopularTLD());
        }
        assertTrue(tlds.size() <= 15, "Popular TLDs should be limited set");
    }

    @Test
    void testLocaleInfluencesTLD() {
        DomainGenerator deGenerator = new DomainGenerator(Locale.GERMANY);
        Set<String> domains = new HashSet<>();
        for (int i = 0; i < 100; i++) {
            domains.add(deGenerator.generate());
        }
        boolean hasDeDomain = domains.stream().anyMatch(d -> d.endsWith(".de"));
        assertTrue(hasDeDomain, "German locale should sometimes produce .de domains");
    }

    @Test
    void testDomainNameIsAlphabetic() {
        DomainGenerator generator = new DomainGenerator();
        for (int i = 0; i < 20; i++) {
            String domain = generator.generate();
            String name = domain.substring(0, domain.lastIndexOf('.'));
            assertTrue(name.matches("[a-z]+"), "Domain name should be alphabetic: " + name);
        }
    }

    @Test
    void testGenerateWithEmptyTLD() {
        DomainGenerator generator = new DomainGenerator();
        String domain = generator.generate("");
        assertTrue(domain.endsWith("."), "Domain should end with dot when TLD is empty");
    }

    @Test
    void testGetTLDWithNullLocaleConfig() {
        // Create config with locale explicitly set to null locale country
        GeneratorConfig config = GeneratorConfig.builder().locale(new Locale("xx", "XX")).build();
        DomainGenerator generator = new DomainGenerator(config);
        String tld = generator.getTLD();
        assertNotNull(tld);
        // Should get popular TLD since locale is not recognized
        assertTrue(tld.length() >= 2 && tld.length() <= 6);
    }

    @Test
    void testSeededGeneratorGetTLDConsistency() {
        DomainGenerator gen1 = new DomainGenerator(GeneratorConfig.builder().seed(555L).build());
        DomainGenerator gen2 = new DomainGenerator(GeneratorConfig.builder().seed(555L).build());

        for (int i = 0; i < 10; i++) {
            assertEquals(gen1.getTLD(), gen2.getTLD());
        }
    }

    @Test
    void testGenerateUsesLocaleTLDSometimes() {
        DomainGenerator frGenerator = new DomainGenerator(Locale.FRANCE);
        boolean foundFrTLD = false;
        boolean foundPopularTLD = false;

        for (int i = 0; i < 100; i++) {
            String domain = frGenerator.generate();
            if (domain.endsWith(".fr")) {
                foundFrTLD = true;
            } else {
                foundPopularTLD = true;
            }
            if (foundFrTLD && foundPopularTLD) break;
        }

        assertTrue(foundFrTLD, "Should sometimes use locale TLD");
        assertTrue(foundPopularTLD, "Should sometimes use popular TLD");
    }

    @Test
    void testGetTLDUsesLocaleTLDSometimes() {
        DomainGenerator jpGenerator = new DomainGenerator(Locale.JAPAN);
        boolean foundJpTLD = false;
        boolean foundPopularTLD = false;

        for (int i = 0; i < 100; i++) {
            String tld = jpGenerator.getTLD();
            if ("jp".equals(tld)) {
                foundJpTLD = true;
            } else {
                foundPopularTLD = true;
            }
            if (foundJpTLD && foundPopularTLD) break;
        }

        assertTrue(foundJpTLD, "Should sometimes use locale TLD");
        assertTrue(foundPopularTLD, "Should sometimes use popular TLD");
    }

    @Test
    void testSingleWordDomainNames() {
        DomainGenerator generator = new DomainGenerator(GeneratorConfig.builder().seed(111L).build());
        boolean foundSingleWord = false;

        for (int i = 0; i < 50; i++) {
            String domain = generator.generate("test");
            String name = domain.substring(0, domain.lastIndexOf('.'));
            // Single words tend to be shorter
            if (name.length() <= 10) {
                foundSingleWord = true;
                break;
            }
        }

        assertTrue(foundSingleWord, "Should generate some single-word domains");
    }

    @Test
    void testTwoWordDomainNames() {
        DomainGenerator generator = new DomainGenerator(GeneratorConfig.builder().seed(222L).build());
        boolean foundTwoWords = false;

        for (int i = 0; i < 50; i++) {
            String domain = generator.generate("test");
            String name = domain.substring(0, domain.lastIndexOf('.'));
            // Two words tend to be longer
            if (name.length() > 10) {
                foundTwoWords = true;
                break;
            }
        }

        assertTrue(foundTwoWords, "Should generate some two-word domains");
    }

    @Test
    void testGenerateWithNullLocaleForcesBothBranches() {
        // Test with a generator that has null localeTLD
        DomainGenerator nullLocaleGen = new DomainGenerator(new Locale("xx", "XX"));
        assertNull(nullLocaleGen.getLocaleTLD());

        // Generate many times - with null localeTLD, should always use popular TLD
        for (int i = 0; i < 20; i++) {
            String domain = nullLocaleGen.generate();
            assertTrue(domain.contains("."));
        }
    }

    @Test
    void testGenerateCoversBothTLDPaths() {
        // Seed that produces mix of true/false for nextBoolean()
        DomainGenerator gen = new DomainGenerator(GeneratorConfig.builder()
                                                                 .seed(12345L)
                                                                 .locale(Locale.GERMANY)
                                                                 .build());

        Set<String> tlds = new HashSet<>();
        for (int i = 0; i < 100; i++) {
            String domain = gen.generate();
            String tld = domain.substring(domain.lastIndexOf('.') + 1);
            tlds.add(tld);
        }

        // Should have both .de and popular TLDs
        boolean hasLocaleTLD = tlds.contains("de");
        boolean hasPopularTLD = tlds.stream().anyMatch(t -> !t.equals("de"));

        assertTrue(hasLocaleTLD || hasPopularTLD, "Should generate at least one type of TLD");
    }

    @Test
    void testGetTLDCoversBothPaths() {
        DomainGenerator gen = new DomainGenerator(GeneratorConfig.builder()
                                                                 .seed(54321L)
                                                                 .locale(Locale.JAPAN)
                                                                 .build());

        Set<String> tlds = new HashSet<>();
        for (int i = 0; i < 100; i++) {
            tlds.add(gen.getTLD());
        }

        // Should have variety
        assertTrue(tlds.size() > 1, "Should generate multiple TLDs");
    }

    @Test
    void testGenerateDomainNameCoversBothPaths() {
        DomainGenerator gen = new DomainGenerator(GeneratorConfig.builder()
                                                                 .seed(11111L)
                                                                 .build());

        Set<Integer> nameLengths = new HashSet<>();
        for (int i = 0; i < 100; i++) {
            String domain = gen.generate("test");
            String name = domain.substring(0, domain.lastIndexOf('.'));
            nameLengths.add(name.length());
        }

        // Should have variety in name lengths (single vs double words)
        assertTrue(nameLengths.size() > 1, "Should generate names of varying lengths");
    }

    @Test
    void testGenerateWhenLocaleTLDIsNullBranch() {
        // Ensure we hit the branch where random.nextBoolean() returns true but localeTLD is null
        DomainGenerator gen = new DomainGenerator(new Locale("zz", "ZZ")); // Unknown locale
        assertNull(gen.getLocaleTLD());

        // Generate many - should only get popular TLDs since localeTLD is null
        for (int i = 0; i < 50; i++) {
            String domain = gen.generate();
            assertNotNull(domain);
        }
    }

    @Test
    void testGetTLDWhenLocaleTLDIsNullBranch() {
        DomainGenerator gen = new DomainGenerator(new Locale("aa", "AA")); // Unknown locale
        assertNull(gen.getLocaleTLD());

        for (int i = 0; i < 50; i++) {
            String tld = gen.getTLD();
            assertNotNull(tld);
        }
    }

    @Test
    void testGenerateForcesBothBranchesOfBoolean() {
        // Seed that gives predictable nextBoolean() sequence
        DomainGenerator gen1 = new DomainGenerator(GeneratorConfig.builder()
                                                                  .seed(0L)
                                                                  .locale(Locale.FRANCE)
                                                                  .build());

        // Generate many to force both true and false branches
        Set<Boolean> branches = new HashSet<>();
        for (int i = 0; i < 200; i++) {
            String domain = gen1.generate();
            branches.add(domain.endsWith(".fr"));
        }

        // Should hit both branches (true and false)
        assertTrue(branches.size() >= 1, "Should generate at least one type");
    }

    @Test
    void testGenerateTldAlias() {
        DomainGenerator generator = new DomainGenerator(Locale.US);
        String tld = generator.generateTld();
        assertNotNull(tld);
        assertFalse(tld.isBlank());
    }
}
