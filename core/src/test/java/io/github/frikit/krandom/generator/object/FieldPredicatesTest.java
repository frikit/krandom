/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.object;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("FieldPredicates")
class FieldPredicatesTest {

    @Test
    @DisplayName("null guards throw for required parameters")
    void nullGuardsThrow() {
        assertThrows(NullPointerException.class, () -> FieldPredicates.named(null));
        assertThrows(NullPointerException.class, () -> FieldPredicates.ofType(null));
        assertThrows(NullPointerException.class, () -> FieldPredicates.inClass(null));
        assertThrows(NullPointerException.class, () -> FieldPredicates.isAnnotatedWith(null));
    }

    @Test
    @DisplayName("hasModifiers(0) throws IllegalArgumentException")
    void hasModifiersZeroThrows() {
        assertThrows(IllegalArgumentException.class, () -> FieldPredicates.hasModifiers(0));
    }

    @Test
    @DisplayName("predicates match expected fields")
    void predicatesMatchExpectedFields() throws NoSuchFieldException {
        Field hidden = Sample.class.getDeclaredField("hidden");
        Field visible = Sample.class.getDeclaredField("visible");

        assertTrue(FieldPredicates.named("hidden").test(hidden));
        assertTrue(FieldPredicates.ofType(String.class).test(hidden));
        assertTrue(FieldPredicates.inClass(Sample.class).test(hidden));
        assertTrue(FieldPredicates.isAnnotatedWith(Marker.class).test(hidden));
        assertTrue(FieldPredicates.hasModifiers(Modifier.PRIVATE).test(hidden));
        assertFalse(FieldPredicates.hasModifiers(Modifier.PRIVATE).test(visible));
    }


    static class Sample {

        String visible;
        @Marker
        private String hidden;
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    @interface Marker {
    }
}
