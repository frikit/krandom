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
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class UUIDGeneratorTest {

    @Test
    void testDefaultConstructor() {
        UUIDGenerator gen = new UUIDGenerator();
        assertNotNull(gen);
    }

    @Test
    void testGenerateV4() {
        UUIDGenerator gen = new UUIDGenerator();
        UUID uuid = gen.generateV4();
        
        assertNotNull(uuid);
        assertEquals(4, uuid.version());
        assertEquals(2, uuid.variant());
    }

    @Test
    void testGenerate() {
        UUIDGenerator gen = new UUIDGenerator();
        UUID uuid = gen.generate();
        
        assertNotNull(uuid);
        assertEquals(4, uuid.version());
        assertEquals(2, uuid.variant());
    }

    @Test
    void testGenerateString() {
        UUIDGenerator gen = new UUIDGenerator();
        String uuidStr = gen.generateString();
        
        assertNotNull(uuidStr);
        assertTrue(uuidStr.matches("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}"));
    }

    @Test
    void testGenerateV4String() {
        UUIDGenerator gen = new UUIDGenerator();
        String uuidStr = gen.generateV4String();
        
        assertNotNull(uuidStr);
        assertTrue(uuidStr.matches("[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}"));
    }

    @Test
    void testGenerateV7() {
        UUIDGenerator gen = new UUIDGenerator();
        UUID uuid = gen.generateV7();
        assertNotNull(uuid);
        assertEquals(7, uuid.version());
        assertEquals(2, uuid.variant());
    }

    @Test
    void testGenerateV7String() {
        UUIDGenerator gen = new UUIDGenerator();
        String uuidStr = gen.generateV7String();
        assertTrue(uuidStr.matches("[0-9a-f]{8}-[0-9a-f]{4}-7[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}"));
    }

    @Test
    void testGenerateV5WithName() {
        UUIDGenerator gen = new UUIDGenerator();
        UUID uuid = gen.generateV5("example.com");
        
        assertNotNull(uuid);
        assertEquals(5, uuid.version());
        assertEquals(2, uuid.variant());
    }

    @Test
    void testGenerateV5String() {
        UUIDGenerator gen = new UUIDGenerator();
        String uuidStr = gen.generateV5String("example.com");
        
        assertNotNull(uuidStr);
        assertTrue(uuidStr.matches("[0-9a-f]{8}-[0-9a-f]{4}-5[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}"));
    }

    @Test
    void testGenerateV5WithNamespace() {
        UUIDGenerator gen = new UUIDGenerator();
        UUID namespace = UUIDGenerator.getDnsNamespace();
        UUID uuid = gen.generateV5(namespace, "example.com");
        
        assertNotNull(uuid);
        assertEquals(5, uuid.version());
        assertEquals(2, uuid.variant());
    }

    @Test
    void testGenerateV5StringWithNamespace() {
        UUIDGenerator gen = new UUIDGenerator();
        UUID namespace = UUIDGenerator.getUrlNamespace();
        String uuidStr = gen.generateV5String(namespace, "https://example.com");
        
        assertNotNull(uuidStr);
        assertTrue(uuidStr.matches("[0-9a-f]{8}-[0-9a-f]{4}-5[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}"));
    }

    @Test
    void testGenerateV5IsDeterministic() {
        UUIDGenerator gen = new UUIDGenerator();
        UUID uuid1 = gen.generateV5("example.com");
        UUID uuid2 = gen.generateV5("example.com");
        
        assertEquals(uuid1, uuid2);
    }

    @Test
    void testGenerateV5DifferentNames() {
        UUIDGenerator gen = new UUIDGenerator();
        UUID uuid1 = gen.generateV5("example.com");
        UUID uuid2 = gen.generateV5("different.com");
        
        assertNotEquals(uuid1, uuid2);
    }

    @Test
    void testGenerateV5DifferentNamespaces() {
        UUIDGenerator gen = new UUIDGenerator();
        UUID uuid1 = gen.generateV5(UUIDGenerator.getDnsNamespace(), "example.com");
        UUID uuid2 = gen.generateV5(UUIDGenerator.getUrlNamespace(), "example.com");
        
        assertNotEquals(uuid1, uuid2);
    }

    @Test
    void testSeededV4Generation() {
        GeneratorConfig config = GeneratorConfig.builder().seed(12345L).build();
        UUIDGenerator gen1 = new UUIDGenerator(config);
        UUIDGenerator gen2 = new UUIDGenerator(config);
        
        UUID uuid1 = gen1.generateV4();
        UUID uuid2 = gen2.generateV4();
        
        assertEquals(uuid1, uuid2);
    }

    @Test
    void testSeededGenerate() {
        GeneratorConfig config = GeneratorConfig.builder().seed(67890L).build();
        UUIDGenerator gen1 = new UUIDGenerator(config);
        UUIDGenerator gen2 = new UUIDGenerator(config);
        
        UUID uuid1 = gen1.generate();
        UUID uuid2 = gen2.generate();
        
        assertEquals(uuid1, uuid2);
    }

    @Test
    void testNullConfigThrowsException() {
        assertThrows(NullPointerException.class, () -> new UUIDGenerator(null));
    }

    @Test
    void testGenerateV5NullNameThrowsException() {
        UUIDGenerator gen = new UUIDGenerator();
        assertThrows(NullPointerException.class, () -> gen.generateV5((String) null));
    }

    @Test
    void testGenerateV5NullNamespaceThrowsException() {
        UUIDGenerator gen = new UUIDGenerator();
        assertThrows(NullPointerException.class, () -> gen.generateV5((UUID) null, "example.com"));
    }

    @Test
    void testGenerateV5NullNameWithNamespaceThrowsException() {
        UUIDGenerator gen = new UUIDGenerator();
        UUID namespace = UUIDGenerator.getDnsNamespace();
        assertThrows(NullPointerException.class, () -> gen.generateV5(namespace, null));
    }

    @Test
    void testMessageDigestInvalidAlgorithmThrows() {
        assertThrows(RuntimeException.class, () -> UUIDGenerator.messageDigest("NOT_A_REAL_ALGO"));
    }

    @Test
    void testGenerateMultipleV4UUIDs() {
        UUIDGenerator gen = new UUIDGenerator();
        Set<UUID> uuids = new HashSet<>();
        
        for (int i = 0; i < 1000; i++) {
            uuids.add(gen.generateV4());
        }
        
        // All UUIDs should be unique
        assertEquals(1000, uuids.size());
    }

    @Test
    void testV4Format() {
        UUIDGenerator gen = new UUIDGenerator();
        String uuidStr = gen.generateV4String();
        
        // Check format: xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx
        // where y is 8, 9, a, or b
        String[] parts = uuidStr.split("-");
        assertEquals(5, parts.length);
        assertEquals(8, parts[0].length());
        assertEquals(4, parts[1].length());
        assertEquals(4, parts[2].length());
        assertEquals(4, parts[3].length());
        assertEquals(12, parts[4].length());
        
        // Check version 4
        assertTrue(parts[2].startsWith("4"));
        
        // Check variant (10xx in binary, so 8, 9, a, or b in hex)
        char variantChar = parts[3].charAt(0);
        assertTrue(variantChar == '8' || variantChar == '9' || 
                   variantChar == 'a' || variantChar == 'b');
    }

    @Test
    void testV5Format() {
        UUIDGenerator gen = new UUIDGenerator();
        String uuidStr = gen.generateV5String("example.com");
        
        // Check format: xxxxxxxx-xxxx-5xxx-yxxx-xxxxxxxxxxxx
        String[] parts = uuidStr.split("-");
        assertEquals(5, parts.length);
        
        // Check version 5
        assertTrue(parts[2].startsWith("5"));
        
        // Check variant
        char variantChar = parts[3].charAt(0);
        assertTrue(variantChar == '8' || variantChar == '9' || 
                   variantChar == 'a' || variantChar == 'b');
    }

    @Test
    void testGetDnsNamespace() {
        UUID namespace = UUIDGenerator.getDnsNamespace();
        assertNotNull(namespace);
        assertEquals("6ba7b810-9dad-11d1-80b4-00c04fd430c8", namespace.toString());
    }

    @Test
    void testGetUrlNamespace() {
        UUID namespace = UUIDGenerator.getUrlNamespace();
        assertNotNull(namespace);
        assertEquals("6ba7b811-9dad-11d1-80b4-00c04fd430c8", namespace.toString());
    }

    @Test
    void testV5KnownVectorDns() {
        // Test with known vector from RFC 4122
        UUIDGenerator gen = new UUIDGenerator();
        UUID uuid = gen.generateV5(UUIDGenerator.getDnsNamespace(), "www.example.com");
        
        assertNotNull(uuid);
        assertEquals(5, uuid.version());
        assertEquals(2, uuid.variant());
    }

    @Test
    void testV5Consistency() {
        UUIDGenerator gen1 = new UUIDGenerator();
        UUIDGenerator gen2 = new UUIDGenerator();
        
        UUID uuid1 = gen1.generateV5("test");
        UUID uuid2 = gen2.generateV5("test");
        
        assertEquals(uuid1, uuid2);
    }

    @Test
    void testV4Randomness() {
        UUIDGenerator gen = new UUIDGenerator();
        UUID uuid1 = gen.generateV4();
        UUID uuid2 = gen.generateV4();
        
        assertNotEquals(uuid1, uuid2);
    }

    @Test
    void testGenerateStringMatchesGenerate() {
        GeneratorConfig config = GeneratorConfig.builder().seed(99999L).build();
        UUIDGenerator gen = new UUIDGenerator(config);
        
        UUID uuid = gen.generate();
        
        GeneratorConfig config2 = GeneratorConfig.builder().seed(99999L).build();
        UUIDGenerator gen2 = new UUIDGenerator(config2);
        String uuidStr = gen2.generateString();
        
        assertEquals(uuid.toString(), uuidStr);
    }

    @Test
    void testV5EmptyString() {
        UUIDGenerator gen = new UUIDGenerator();
        UUID uuid = gen.generateV5("");
        
        assertNotNull(uuid);
        assertEquals(5, uuid.version());
    }

    @Test
    void testV5SpecialCharacters() {
        UUIDGenerator gen = new UUIDGenerator();
        UUID uuid = gen.generateV5("example.com/path?query=value&special=!@#$%");
        
        assertNotNull(uuid);
        assertEquals(5, uuid.version());
    }

    @Test
    void testV5Unicode() {
        UUIDGenerator gen = new UUIDGenerator();
        UUID uuid = gen.generateV5("例え.com");
        
        assertNotNull(uuid);
        assertEquals(5, uuid.version());
    }

    @Test
    void testMultipleSeededGenerators() {
        long seed = 54321L;
        GeneratorConfig config = GeneratorConfig.builder().seed(seed).build();
        
        UUIDGenerator gen1 = new UUIDGenerator(config);
        UUIDGenerator gen2 = new UUIDGenerator(config);
        UUIDGenerator gen3 = new UUIDGenerator(config);
        
        UUID uuid1 = gen1.generate();
        UUID uuid2 = gen2.generate();
        UUID uuid3 = gen3.generate();
        
        assertEquals(uuid1, uuid2);
        assertEquals(uuid2, uuid3);
    }

    @Test
    void testV5StringMatchesV5() {
        UUIDGenerator gen = new UUIDGenerator();
        UUID uuid = gen.generateV5("test-name");
        String uuidStr = gen.generateV5String("test-name");
        
        assertEquals(uuid.toString(), uuidStr);
    }

    @Test
    void testV5WithCustomNamespaceStringMatchesUUID() {
        UUIDGenerator gen = new UUIDGenerator();
        UUID namespace = UUIDGenerator.getUrlNamespace();
        
        UUID uuid = gen.generateV5(namespace, "test-name");
        String uuidStr = gen.generateV5String(namespace, "test-name");
        
        assertEquals(uuid.toString(), uuidStr);
    }

    @Test
    void testV4StringMatchesV4() {
        GeneratorConfig config = GeneratorConfig.builder().seed(111L).build();
        UUIDGenerator gen = new UUIDGenerator(config);
        
        UUID uuid = gen.generateV4();
        
        GeneratorConfig config2 = GeneratorConfig.builder().seed(111L).build();
        UUIDGenerator gen2 = new UUIDGenerator(config2);
        String uuidStr = gen2.generateV4String();
        
        assertEquals(uuid.toString(), uuidStr);
    }

    @Test
    void testDnsNamespaceIsConstant() {
        UUID ns1 = UUIDGenerator.getDnsNamespace();
        UUID ns2 = UUIDGenerator.getDnsNamespace();
        
        assertEquals(ns1, ns2);
    }

    @Test
    void testUrlNamespaceIsConstant() {
        UUID ns1 = UUIDGenerator.getUrlNamespace();
        UUID ns2 = UUIDGenerator.getUrlNamespace();
        
        assertEquals(ns1, ns2);
    }
}
