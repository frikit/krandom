/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package org.github.krandom.generator.object;

import org.github.krandom.generator.core.model.PersonWithArrays;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ObjectGenerator — array auto-population")
class ObjectGeneratorArrayTest {

    @Test
    @DisplayName("String[] field is auto-populated and non-null")
    void stringArrayNonNull() {
        PersonWithArrays p = new ObjectGenerator<>(PersonWithArrays.class).generate();
        assertNotNull(p.getTags(), "String[] field must not be null");
    }

    @Test
    @DisplayName("String[] field has exactly 3 elements")
    void stringArrayLength() {
        PersonWithArrays p = new ObjectGenerator<>(PersonWithArrays.class).generate();
        assertEquals(3, p.getTags().length, "String[] should have 3 elements");
    }

    @Test
    @DisplayName("all String[] elements are non-null and non-empty")
    void stringArrayElementsPopulated() {
        PersonWithArrays p = new ObjectGenerator<>(PersonWithArrays.class).generate();
        for (String tag : p.getTags()) {
            assertNotNull(tag,      "String[] element must not be null");
            assertFalse(tag.isEmpty(), "String[] element must not be empty");
        }
    }

    @Test
    @DisplayName("int[] field is auto-populated with 3 elements")
    void intArrayLength() {
        PersonWithArrays p = new ObjectGenerator<>(PersonWithArrays.class).generate();
        assertNotNull(p.getScores(), "int[] field must not be null");
        assertEquals(3, p.getScores().length, "int[] should have 3 elements");
    }

    @Test
    @DisplayName("Address[] field is auto-populated with 3 non-null entries")
    void objectArrayPopulated() {
        PersonWithArrays p = new ObjectGenerator<>(PersonWithArrays.class).generate();
        assertNotNull(p.getAddresses(), "Address[] field must not be null");
        assertEquals(3, p.getAddresses().length, "Address[] should have 3 elements");
        for (var addr : p.getAddresses()) {
            assertNotNull(addr, "each Address element must not be null");
            assertNotNull(addr.getStreet(), "each Address must have a street");
        }
    }

    // ── Array.set IllegalArgumentException path ───────────────────────────────

    /** Holder used to exercise the Array.set catch block. */
    static class WithIntArray { int[] nums; }

    @Test
    @DisplayName("Array.set IAE (incompatible element type) is caught silently; array slots default to 0")
    @SuppressWarnings({"unchecked", "rawtypes"})
    void arraySetIllegalArgumentExceptionHandled() {
        // Supply a String where int[] expects Integer — Array.set throws IAE, caught internally.
        // Raw-typed generator used here to deliberately pass the wrong value type.
        var badGen = (org.github.krandom.generator.Generator) () -> "NOT_AN_INT";
        ObjectGeneratorConfig cfg = ObjectGeneratorConfig.builder()
                .override((Class) int.class, badGen)
                .build();
        WithIntArray obj = assertDoesNotThrow(
                () -> new ObjectGenerator<>(WithIntArray.class, cfg).generate(),
                "Array.set IAE must not escape generateArray()");
        assertNotNull(obj.nums, "array must still be created");
        assertEquals(3, obj.nums.length, "array must have 3 slots");
        for (int v : obj.nums) {
            assertEquals(0, v, "slots must retain JVM default 0 after IAE");
        }
    }
}
