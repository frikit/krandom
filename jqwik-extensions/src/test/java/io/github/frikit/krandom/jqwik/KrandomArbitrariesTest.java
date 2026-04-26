/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.jqwik;

import io.github.frikit.krandom.generator.GeneratorConfig;
import io.github.frikit.krandom.generator.Generators;
import net.jqwik.api.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class KrandomArbitrariesTest {

    @Test
    @DisplayName("fromGenerator wraps a scalar generator")
    void fromGeneratorScalar() {
        Arbitrary<String> emails = KrandomArbitraries.fromGenerator(Generators.ofEmail());
        List<String> samples = emails.sampleStream().limit(20).toList();
        assertEquals(20, samples.size());
        samples.forEach(s -> assertTrue(s.contains("@"), "email should contain @: " + s));
    }

    @Test
    @DisplayName("fromGenerator wraps an int generator")
    void fromGeneratorInt() {
        Arbitrary<Integer> ints = KrandomArbitraries.fromGenerator(Generators.ofInt(1, 100));
        List<Integer> samples = ints.sampleStream().limit(50).toList();
        assertEquals(50, samples.size());
        samples.forEach(i -> assertTrue(i >= 1 && i < 100, "int out of range: " + i));
    }

    @Test
    @DisplayName("fromFactory creates Arbitrary from supplier")
    void fromFactory() {
        Arbitrary<String> names = KrandomArbitraries.fromFactory(Generators::ofFullName);
        List<String> samples = names.sampleStream().limit(20).toList();
        assertEquals(20, samples.size());
        samples.forEach(s -> assertNotNull(s));
    }

    @Test
    @DisplayName("forType generates random object instances")
    void forType() {
        Arbitrary<SamplePojo> arb = KrandomArbitraries.forType(SamplePojo.class);
        List<SamplePojo> samples = arb.sampleStream().limit(20).toList();
        assertEquals(20, samples.size());
        samples.forEach(p -> {
            assertNotNull(p);
            assertNotNull(p.name);
        });
    }

    @Test
    @DisplayName("forType with config respects configuration")
    void forTypeWithConfig() {
        GeneratorConfig config = GeneratorConfig.builder()
            .seed(42L)
            .build();
        Arbitrary<SamplePojo> arb = KrandomArbitraries.forType(SamplePojo.class, config);
        List<SamplePojo> samples = arb.sampleStream().limit(10).toList();
        assertEquals(10, samples.size());
        samples.forEach(p -> assertNotNull(p));
    }

    @Test
    @DisplayName("fromGenerator rejects null")
    void fromGeneratorNullThrows() {
        assertThrows(NullPointerException.class,
            () -> KrandomArbitraries.fromGenerator(null));
    }

    @Test
    @DisplayName("fromFactory rejects null")
    void fromFactoryNullThrows() {
        assertThrows(NullPointerException.class,
            () -> KrandomArbitraries.fromFactory(null));
    }

    @Test
    @DisplayName("forType rejects null type")
    void forTypeNullThrows() {
        assertThrows(NullPointerException.class,
            () -> KrandomArbitraries.forType(null));
    }

    @Test
    @DisplayName("forType with config rejects null type")
    void forTypeWithConfigNullTypeThrows() {
        assertThrows(NullPointerException.class,
            () -> KrandomArbitraries.forType(null, GeneratorConfig.defaults()));
    }

    @Test
    @DisplayName("forType with config rejects null config")
    void forTypeWithConfigNullConfigThrows() {
        assertThrows(NullPointerException.class,
            () -> KrandomArbitraries.forType(SamplePojo.class, null));
    }

    /** Simple POJO for ObjectGenerator-based tests. */
    public static class SamplePojo {
        public String name = "";
        public int age = 0;
        public String email = "";
    }
}
