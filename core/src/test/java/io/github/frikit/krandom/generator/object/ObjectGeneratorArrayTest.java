/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.object;

import io.github.frikit.krandom.generator.core.model.PersonWithArrays;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("ObjectGenerator — array auto-population")
class ObjectGeneratorArrayTest {

    @Test
    @DisplayName("String[] field is auto-populated and non-null")
    void stringArrayNonNull() {
        PersonWithArrays p = new ObjectGenerator<>(PersonWithArrays.class).generate();
        assertNotNull(p.getTags(), "String[] field must not be null");
    }

    @Test
    @DisplayName("String[] field size follows shared collection defaults")
    void stringArrayLength() {
        PersonWithArrays p = new ObjectGenerator<>(PersonWithArrays.class).generate();
        assertTrue(p.getTags().length >= FieldGeneratorResolver.DEFAULT_MIN_ELEMENT_COUNT);
        assertTrue(p.getTags().length <= FieldGeneratorResolver.DEFAULT_MAX_ELEMENT_COUNT);
    }

    @Test
    @DisplayName("all String[] elements are non-null and non-empty")
    void stringArrayElementsPopulated() {
        PersonWithArrays p = new ObjectGenerator<>(PersonWithArrays.class).generate();
        for (String tag : p.getTags()) {
            assertNotNull(tag, "String[] element must not be null");
            assertFalse(tag.isEmpty(), "String[] element must not be empty");
        }
    }

    @Test
    @DisplayName("int[] field size follows shared collection defaults")
    void intArrayLength() {
        PersonWithArrays p = new ObjectGenerator<>(PersonWithArrays.class).generate();
        assertNotNull(p.getScores(), "int[] field must not be null");
        assertTrue(p.getScores().length >= FieldGeneratorResolver.DEFAULT_MIN_ELEMENT_COUNT);
        assertTrue(p.getScores().length <= FieldGeneratorResolver.DEFAULT_MAX_ELEMENT_COUNT);
    }

    @Test
    @DisplayName("Address[] field size follows shared collection defaults")
    void objectArrayPopulated() {
        PersonWithArrays p = new ObjectGenerator<>(PersonWithArrays.class).generate();
        assertNotNull(p.getAddresses(), "Address[] field must not be null");
        assertTrue(p.getAddresses().length >= FieldGeneratorResolver.DEFAULT_MIN_ELEMENT_COUNT);
        assertTrue(p.getAddresses().length <= FieldGeneratorResolver.DEFAULT_MAX_ELEMENT_COUNT);
        for (var addr : p.getAddresses()) {
            assertNotNull(addr, "each Address element must not be null");
            assertNotNull(addr.getStreet(), "each Address must have a street");
        }
    }

    // ── Array.set IllegalArgumentException path ───────────────────────────────

    @Test
    @DisplayName("lenient Array.set failure leaves primitive array slots at the JVM default")
    @SuppressWarnings({ "unchecked", "rawtypes" })
    void arraySetIllegalArgumentExceptionHandled() {
        // Supply a String where int[] expects Integer — lenient mode keeps the JVM default.
        // Raw-typed generator used here to deliberately pass the wrong value type.
        var badGen = (io.github.frikit.krandom.generator.Generator) () -> "NOT_AN_INT";
        ObjectGeneratorConfig cfg = ObjectGeneratorConfig.builder()
                                                         .override((Class) int.class, badGen)
                                                         .ignoreErrors(true)
                                                         .build();
        WithIntArray obj = assertDoesNotThrow(
            () -> new ObjectGenerator<>(WithIntArray.class, cfg).generate(),
            "Array.set IAE must not escape generateArray()");
        assertNotNull(obj.nums, "array must still be created");
        assertTrue(obj.nums.length >= FieldGeneratorResolver.DEFAULT_MIN_ELEMENT_COUNT);
        assertTrue(obj.nums.length <= FieldGeneratorResolver.DEFAULT_MAX_ELEMENT_COUNT);
        for (int v : obj.nums) {
            assertEquals(0, v, "slots must retain JVM default 0 after IAE");
        }
    }


    /**
     * Holder used to exercise the Array.set catch block.
     */
    static class WithIntArray {

        int[] nums;
    }
}
