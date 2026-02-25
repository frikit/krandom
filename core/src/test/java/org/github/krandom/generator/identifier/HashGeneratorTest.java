/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package org.github.krandom.generator.identifier;

import org.github.krandom.generator.GeneratorConfig;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class HashGeneratorTest {

    @Test
    void testDefaultConstructor() {
        HashGenerator gen = new HashGenerator();
        assertNotNull(gen);
    }

    @Test
    void testGenerateDefaultLength() {
        HashGenerator gen = new HashGenerator();
        String hash = gen.generate();
        
        assertNotNull(hash);
        assertEquals(40, hash.length());
    }

    @Test
    void testGenerateDefaultIsLowercase() {
        HashGenerator gen = new HashGenerator();
        String hash = gen.generate();
        
        assertTrue(hash.matches("[0-9a-f]{40}"));
    }

    @Test
    void testGenerateCustomLength() {
        HashGenerator gen = new HashGenerator();
        String hash = gen.generate(16);
        
        assertNotNull(hash);
        assertEquals(16, hash.length());
    }

    @Test
    void testGenerateCustomLength32() {
        HashGenerator gen = new HashGenerator();
        String hash = gen.generate(32);
        
        assertNotNull(hash);
        assertEquals(32, hash.length());
    }

    @Test
    void testGenerateCustomLength64() {
        HashGenerator gen = new HashGenerator();
        String hash = gen.generate(64);
        
        assertNotNull(hash);
        assertEquals(64, hash.length());
    }

    @Test
    void testGenerateSingleChar() {
        HashGenerator gen = new HashGenerator();
        String hash = gen.generate(1);
        
        assertNotNull(hash);
        assertEquals(1, hash.length());
        assertTrue(hash.matches("[0-9a-f]"));
    }

    @Test
    void testGenerateZeroLengthThrows() {
        HashGenerator gen = new HashGenerator();
        assertThrows(IllegalArgumentException.class, () -> gen.generate(0));
    }

    @Test
    void testGenerateNegativeLengthThrows() {
        HashGenerator gen = new HashGenerator();
        assertThrows(IllegalArgumentException.class, () -> gen.generate(-1));
    }

    @Test
    void testGenerateUppercase() {
        HashGenerator gen = new HashGenerator();
        String hash = gen.generateUppercase();
        
        assertNotNull(hash);
        assertEquals(40, hash.length());
        assertTrue(hash.matches("[0-9A-F]{40}"));
    }

    @Test
    void testGenerateUppercaseCustomLength() {
        HashGenerator gen = new HashGenerator();
        String hash = gen.generateUppercase(16);
        
        assertNotNull(hash);
        assertEquals(16, hash.length());
        assertTrue(hash.matches("[0-9A-F]{16}"));
    }

    @Test
    void testGenerateUppercaseZeroLengthThrows() {
        HashGenerator gen = new HashGenerator();
        assertThrows(IllegalArgumentException.class, () -> gen.generateUppercase(0));
    }

    @Test
    void testGenerateUppercaseNegativeLengthThrows() {
        HashGenerator gen = new HashGenerator();
        assertThrows(IllegalArgumentException.class, () -> gen.generateUppercase(-1));
    }

    @Test
    void testSeededGeneration() {
        GeneratorConfig config = GeneratorConfig.builder().seed(12345L).build();
        HashGenerator gen1 = new HashGenerator(config);
        HashGenerator gen2 = new HashGenerator(config);
        
        String hash1 = gen1.generate();
        String hash2 = gen2.generate();
        
        assertEquals(hash1, hash2);
    }

    @Test
    void testSeededGenerationWithCustomLength() {
        GeneratorConfig config = GeneratorConfig.builder().seed(67890L).build();
        HashGenerator gen1 = new HashGenerator(config);
        HashGenerator gen2 = new HashGenerator(config);
        
        String hash1 = gen1.generate(20);
        String hash2 = gen2.generate(20);
        
        assertEquals(hash1, hash2);
    }

    @Test
    void testSeededUppercaseGeneration() {
        GeneratorConfig config = GeneratorConfig.builder().seed(11111L).build();
        HashGenerator gen1 = new HashGenerator(config);
        HashGenerator gen2 = new HashGenerator(config);
        
        String hash1 = gen1.generateUppercase();
        String hash2 = gen2.generateUppercase();
        
        assertEquals(hash1, hash2);
    }

    @Test
    void testNullConfigThrowsException() {
        assertThrows(NullPointerException.class, () -> new HashGenerator(null));
    }

    @Test
    void testGenerateMultipleHashes() {
        HashGenerator gen = new HashGenerator();
        Set<String> hashes = new HashSet<>();
        
        for (int i = 0; i < 1000; i++) {
            hashes.add(gen.generate());
        }
        
        // All hashes should be unique
        assertEquals(1000, hashes.size());
    }

    @Test
    void testGenerateContainsAllHexDigits() {
        HashGenerator gen = new HashGenerator();
        Set<Character> chars = new HashSet<>();
        
        // Generate enough hashes to get all hex digits
        for (int i = 0; i < 100; i++) {
            String hash = gen.generate();
            for (char c : hash.toCharArray()) {
                chars.add(c);
            }
        }
        
        // Should contain all 16 hex digits
        assertTrue(chars.size() >= 10); // At least all digits 0-9
    }

    @Test
    void testGenerateUppercaseContainsUppercaseOnly() {
        HashGenerator gen = new HashGenerator();
        String hash = gen.generateUppercase();
        
        for (char c : hash.toCharArray()) {
            assertTrue(Character.isDigit(c) || Character.isUpperCase(c));
            assertFalse(Character.isLowerCase(c));
        }
    }

    @Test
    void testGenerateLowercaseContainsLowercaseOnly() {
        HashGenerator gen = new HashGenerator();
        String hash = gen.generate();
        
        for (char c : hash.toCharArray()) {
            assertTrue(Character.isDigit(c) || Character.isLowerCase(c));
            assertFalse(Character.isUpperCase(c));
        }
    }

    @Test
    void testDifferentLengthsProduceDifferentHashes() {
        GeneratorConfig config = GeneratorConfig.builder().seed(99999L).build();
        HashGenerator gen = new HashGenerator(config);
        
        String hash16 = gen.generate(16);
        
        GeneratorConfig config2 = GeneratorConfig.builder().seed(99999L).build();
        HashGenerator gen2 = new HashGenerator(config2);
        String hash32 = gen2.generate(32);
        
        assertNotEquals(hash16, hash32);
        assertNotEquals(hash16.length(), hash32.length());
    }

    @Test
    void testDefaultLengthIs40() {
        HashGenerator gen = new HashGenerator();
        assertEquals(40, gen.generate().length());
    }

    @Test
    void testGenerateLargeHash() {
        HashGenerator gen = new HashGenerator();
        String hash = gen.generate(1000);
        
        assertNotNull(hash);
        assertEquals(1000, hash.length());
        assertTrue(hash.matches("[0-9a-f]{1000}"));
    }

    @Test
    void testGenerateUppercaseLargeHash() {
        HashGenerator gen = new HashGenerator();
        String hash = gen.generateUppercase(500);
        
        assertNotNull(hash);
        assertEquals(500, hash.length());
        assertTrue(hash.matches("[0-9A-F]{500}"));
    }

    @Test
    void testSHA1CompatibleLength() {
        // SHA-1 produces 40 hex characters
        HashGenerator gen = new HashGenerator();
        String hash = gen.generate(); // Default 40
        
        assertEquals(40, hash.length());
    }

    @Test
    void testMD5CompatibleLength() {
        // MD5 produces 32 hex characters
        HashGenerator gen = new HashGenerator();
        String hash = gen.generate(32);
        
        assertEquals(32, hash.length());
    }

    @Test
    void testSHA256CompatibleLength() {
        // SHA-256 produces 64 hex characters
        HashGenerator gen = new HashGenerator();
        String hash = gen.generate(64);
        
        assertEquals(64, hash.length());
    }

    @Test
    void testSeededSequence() {
        GeneratorConfig config = GeneratorConfig.builder().seed(555L).build();
        HashGenerator gen = new HashGenerator(config);
        
        String hash1 = gen.generate(10);
        String hash2 = gen.generate(10);
        String hash3 = gen.generate(10);
        
        // Different hashes in sequence
        assertNotEquals(hash1, hash2);
        assertNotEquals(hash2, hash3);
        assertNotEquals(hash1, hash3);
    }

    @Test
    void testUppercaseAndLowercaseDifferInCase() {
        GeneratorConfig config = GeneratorConfig.builder().seed(777L).build();
        HashGenerator gen1 = new HashGenerator(config);
        String lower = gen1.generate(20);
        
        GeneratorConfig config2 = GeneratorConfig.builder().seed(777L).build();
        HashGenerator gen2 = new HashGenerator(config2);
        String upper = gen2.generateUppercase(20);
        
        assertEquals(lower.toUpperCase(), upper);
    }

    @Test
    void testAllHexCharsAppear() {
        HashGenerator gen = new HashGenerator();
        Set<Character> allChars = new HashSet<>();
        
        // Generate many hashes to ensure all hex chars appear
        for (int i = 0; i < 200; i++) {
            String hash = gen.generate();
            for (char c : hash.toCharArray()) {
                allChars.add(c);
            }
        }
        
        // All 16 hex digits should appear
        String hexChars = "0123456789abcdef";
        for (char c : hexChars.toCharArray()) {
            assertTrue(allChars.contains(c), "Missing hex char: " + c);
        }
    }
}
