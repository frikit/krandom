/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package org.github.krandom.generator.user;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("AgeGenerator")
class AgeGeneratorTest {

    // ── Default constructor ────────────────────────────────────────────────────

    @Test
    @DisplayName("default constructor produces ages in [1, 100]")
    void defaultRange() {
        AgeGenerator gen = new AgeGenerator();
        for (int i = 0; i < 200; i++) {
            int age = gen.generate();
            assertTrue(age >= 1 && age <= 100,
                    "Expected age in [1,100] but got: " + age);
        }
        assertEquals(1, gen.getMinAge());
        assertEquals(100, gen.getMaxAge());
    }

    // ── Seeded constructor ─────────────────────────────────────────────────────

    @Test
    @DisplayName("seeded constructor produces reproducible output")
    void seededReproducibility() {
        AgeGenerator gen1 = new AgeGenerator(12345L);
        AgeGenerator gen2 = new AgeGenerator(12345L);
        List<Integer> list1 = gen1.generateList(50);
        List<Integer> list2 = gen2.generateList(50);
        assertEquals(list1, list2);
    }

    @Test
    @DisplayName("different seeds produce different sequences")
    void differentSeeds() {
        AgeGenerator gen1 = new AgeGenerator(111L);
        AgeGenerator gen2 = new AgeGenerator(222L);
        assertNotEquals(gen1.generateList(50), gen2.generateList(50));
    }

    // ── AgeType constructor ───────────────────────────────────────────────────

    @Test
    @DisplayName("CHILD type produces ages in [1, 12]")
    void childType() {
        AgeGenerator gen = new AgeGenerator(AgeType.CHILD);
        for (int i = 0; i < 200; i++) {
            int age = gen.generate();
            assertTrue(age >= 1 && age <= 12, "Expected child age but got: " + age);
        }
        assertEquals(1, gen.getMinAge());
        assertEquals(12, gen.getMaxAge());
    }

    @Test
    @DisplayName("TEEN type produces ages in [13, 19]")
    void teenType() {
        AgeGenerator gen = new AgeGenerator(AgeType.TEEN);
        for (int i = 0; i < 200; i++) {
            int age = gen.generate();
            assertTrue(age >= 13 && age <= 19, "Expected teen age but got: " + age);
        }
    }

    @Test
    @DisplayName("ADULT type produces ages in [18, 65]")
    void adultType() {
        AgeGenerator gen = new AgeGenerator(AgeType.ADULT);
        for (int i = 0; i < 200; i++) {
            int age = gen.generate();
            assertTrue(age >= 18 && age <= 65, "Expected adult age but got: " + age);
        }
    }

    @Test
    @DisplayName("SENIOR type produces ages in [65, 100]")
    void seniorType() {
        AgeGenerator gen = new AgeGenerator(AgeType.SENIOR);
        for (int i = 0; i < 200; i++) {
            int age = gen.generate();
            assertTrue(age >= 65 && age <= 100, "Expected senior age but got: " + age);
        }
    }

    @Test
    @DisplayName("AgeType with seed produces reproducible output")
    void ageTypeWithSeed() {
        AgeGenerator gen1 = new AgeGenerator(AgeType.ADULT, 99L);
        AgeGenerator gen2 = new AgeGenerator(AgeType.ADULT, 99L);
        assertEquals(gen1.generateList(30), gen2.generateList(30));
    }

    @Test
    @DisplayName("null AgeType throws NullPointerException")
    void nullAgeTypeThrows() {
        assertThrows(NullPointerException.class, () -> new AgeGenerator((AgeType) null));
    }

    @Test
    @DisplayName("null AgeType with seed throws NullPointerException")
    void nullAgeTypeWithSeedThrows() {
        assertThrows(NullPointerException.class, () -> new AgeGenerator(null, 42L));
    }

    // ── Custom range constructor ──────────────────────────────────────────────

    @Test
    @DisplayName("custom range produces ages within bounds")
    void customRange() {
        AgeGenerator gen = new AgeGenerator(21, 30);
        for (int i = 0; i < 200; i++) {
            int age = gen.generate();
            assertTrue(age >= 21 && age <= 30, "Expected age in [21,30] but got: " + age);
        }
        assertEquals(21, gen.getMinAge());
        assertEquals(30, gen.getMaxAge());
    }

    @Test
    @DisplayName("single-value range always produces that value")
    void singleValueRange() {
        AgeGenerator gen = new AgeGenerator(25, 25);
        for (int i = 0; i < 50; i++) {
            assertEquals(25, gen.generate());
        }
    }

    // ── Validation ────────────────────────────────────────────────────────────

    @Test
    @DisplayName("negative minAge throws IllegalArgumentException")
    void negativeMinAgeThrows() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> new AgeGenerator(-1, 10));
        assertTrue(ex.getMessage().contains("minAge"));
    }

    @Test
    @DisplayName("maxAge less than minAge throws IllegalArgumentException")
    void maxLessThanMinThrows() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> new AgeGenerator(10, 5));
        assertTrue(ex.getMessage().contains("maxAge"));
    }

    @Test
    @DisplayName("minAge of 0 is accepted")
    void zeroMinAgeAccepted() {
        AgeGenerator gen = new AgeGenerator(0, 0);
        assertEquals(0, gen.generate());
    }

    // ── generateList / stream ─────────────────────────────────────────────────

    @Test
    @DisplayName("generateList returns correct count with values in range")
    void generateListCount() {
        AgeGenerator gen = new AgeGenerator(AgeType.TEEN);
        List<Integer> ages = gen.generateList(20);
        assertEquals(20, ages.size());
        ages.forEach(age -> assertTrue(age >= 13 && age <= 19));
    }

    @Test
    @DisplayName("stream generates continuous values in range")
    void streamGeneration() {
        AgeGenerator gen = new AgeGenerator(AgeType.ADULT);
        List<Integer> ages = gen.stream().limit(30).toList();
        assertEquals(30, ages.size());
        ages.forEach(age -> assertTrue(age >= 18 && age <= 65));
    }

    // ── AgeType enum ──────────────────────────────────────────────────────────

    @Test
    @DisplayName("AgeType enum has correct bounds")
    void ageTypeBounds() {
        assertEquals(1,   AgeType.CHILD.getMinAge());
        assertEquals(12,  AgeType.CHILD.getMaxAge());
        assertEquals(13,  AgeType.TEEN.getMinAge());
        assertEquals(19,  AgeType.TEEN.getMaxAge());
        assertEquals(18,  AgeType.ADULT.getMinAge());
        assertEquals(65,  AgeType.ADULT.getMaxAge());
        assertEquals(65,  AgeType.SENIOR.getMinAge());
        assertEquals(100, AgeType.SENIOR.getMaxAge());
    }
}
