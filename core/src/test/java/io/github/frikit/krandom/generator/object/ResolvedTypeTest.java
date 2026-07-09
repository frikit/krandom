/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.object;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("ResolvedType")
class ResolvedTypeTest {

    @Test
    @DisplayName("models plain classes and ordinary arrays")
    void modelsClassesAndArrays() {
        ResolvedType scalar = ResolvedType.resolve(String.class);
        assertEquals(ResolvedType.Kind.CLASS, scalar.kind());
        assertEquals(String.class, scalar.rawClass());
        assertEquals("java.lang.String", scalar.signature());
        assertTrue(scalar.isResolved());

        ResolvedType array = ResolvedType.resolve(String[].class);
        assertEquals(ResolvedType.Kind.CLASS, array.kind());
        assertEquals(String[].class, array.rawClass());
        assertEquals(String.class, array.componentType().rawClass());
        assertTrue(array.isResolved());
    }

    @Test
    @DisplayName("retains nested parameterized arguments recursively")
    void retainsNestedParameterizedArguments() throws Exception {
        ResolvedType nested = ResolvedType.resolve(field("nested").getGenericType());
        assertEquals(ResolvedType.Kind.PARAMETERIZED, nested.kind());
        assertEquals(List.class, nested.rawClass());
        assertEquals(List.class, nested.argument(0).rawClass());
        assertEquals(String.class, nested.argument(0).argument(0).rawClass());
        assertEquals("java.util.List<java.util.List<java.lang.String>>", nested.signature());
        assertTrue(nested.isResolved());

        ResolvedType map = ResolvedType.resolve(field("mapped").getGenericType());
        assertEquals(Map.class, map.rawClass());
        assertEquals(String.class, map.argument(0).rawClass());
        assertEquals(List.class, map.argument(1).rawClass());
        assertEquals(Integer.class, map.argument(1).argument(0).rawClass());
    }

    @Test
    @DisplayName("uses effective upper and lower wildcard bounds")
    void usesWildcardBounds() throws Exception {
        ResolvedType upper = ResolvedType.resolve(field("upper").getGenericType()).argument(0);
        assertEquals(ResolvedType.Kind.WILDCARD, upper.kind());
        assertEquals(Number.class, upper.rawClass());
        assertEquals(Number.class, upper.effectiveType().rawClass());
        assertTrue(upper.isResolved());

        ResolvedType lower = ResolvedType.resolve(field("lower").getGenericType()).argument(0);
        assertEquals(ResolvedType.Kind.WILDCARD, lower.kind());
        assertEquals(Integer.class, lower.rawClass());
        assertEquals(Integer.class, lower.effectiveType().rawClass());
        assertTrue(lower.isResolved());

        ResolvedType unbounded = ResolvedType.resolve(field("unbounded").getGenericType()).argument(0);
        assertEquals(ResolvedType.Kind.WILDCARD, unbounded.kind());
        assertNull(unbounded.rawClass());
        assertFalse(unbounded.isResolved());
        assertTrue(unbounded.unresolvedReason().contains("unbounded wildcard"));
    }

    @Test
    @DisplayName("resolves variables from bindings before declared bounds")
    void resolvesTypeVariables() {
        TypeVariable<Class<Shapes>> boundedVariable = Shapes.class.getTypeParameters()[0];
        ResolvedType bounded = ResolvedType.resolve(boundedVariable);
        assertEquals(ResolvedType.Kind.TYPE_VARIABLE, bounded.kind());
        assertEquals(CharSequence.class, bounded.rawClass());
        assertEquals(CharSequence.class, bounded.effectiveType().rawClass());

        ResolvedType boundToString = ResolvedType.resolve(boundedVariable, Map.of(boundedVariable, String.class));
        assertEquals(String.class, boundToString.rawClass());
        assertEquals(String.class, boundToString.effectiveType().rawClass());

        TypeVariable<Class<Unbounded>> unboundedVariable = Unbounded.class.getTypeParameters()[0];
        ResolvedType unbounded = ResolvedType.resolve(unboundedVariable);
        assertFalse(unbounded.isResolved());
        assertTrue(unbounded.unresolvedReason().contains("unbounded type variable"));

        ResolvedType selfBound = ResolvedType.resolve(boundedVariable, Map.of(boundedVariable, boundedVariable));
        assertEquals(CharSequence.class, selfBound.rawClass());

        TypeVariable<Class<Intersection>> intersectionVariable = Intersection.class.getTypeParameters()[0];
        ResolvedType intersection = ResolvedType.resolve(intersectionVariable);
        assertFalse(intersection.isResolved());
        assertTrue(intersection.unresolvedReason().contains("multiple type-variable bounds"));
    }

    @Test
    @DisplayName("models generic arrays and reports recursive bounds")
    void modelsGenericArraysAndRecursiveBounds() throws Exception {
        ResolvedType array = ResolvedType.resolve(field("genericArray").getGenericType());
        assertEquals(ResolvedType.Kind.GENERIC_ARRAY, array.kind());
        assertEquals(CharSequence[].class, array.rawClass());
        assertEquals(CharSequence.class, array.componentType().rawClass());
        assertTrue(array.isResolved());

        ResolvedType unresolvedArray = ResolvedType.resolve(
            Unbounded.class.getDeclaredField("values").getGenericType());
        assertNull(unresolvedArray.rawClass());
        assertFalse(unresolvedArray.isResolved());
        assertTrue(unresolvedArray.unresolvedReason().contains("generic array component"));

        ResolvedType ambiguousArray = ResolvedType.resolve(
            AmbiguousArray.class.getDeclaredField("values").getGenericType());
        assertEquals(List[].class, ambiguousArray.rawClass());
        assertFalse(ambiguousArray.componentType().isResolved());
        assertFalse(ambiguousArray.isResolved());

        TypeVariable<Class<Recursive>> recursiveVariable = Recursive.class.getTypeParameters()[0];
        ResolvedType recursive = ResolvedType.resolve(recursiveVariable);
        assertFalse(recursive.isResolved());
        assertTrue(recursive.effectiveType().argument(0).unresolvedReason().contains("recursive type bound"));
    }

    @Test
    @DisplayName("rejects invalid API inputs and unknown Type implementations")
    void validatesInputs() {
        assertThrows(NullPointerException.class, () -> ResolvedType.resolve(null));
        assertThrows(NullPointerException.class, () -> ResolvedType.resolve(String.class, null));

        Type unknown = new Type() {
            @Override
            public String getTypeName() {
                return "unknown.Type";
            }
        };
        ResolvedType unresolved = ResolvedType.resolve(unknown);
        assertEquals(ResolvedType.Kind.UNKNOWN, unresolved.kind());
        assertFalse(unresolved.isResolved());
        assertTrue(unresolved.unresolvedReason().contains("unsupported Type implementation"));
        assertThrows(IndexOutOfBoundsException.class, () -> unresolved.argument(0));

        ParameterizedType withoutRawClass = new ParameterizedType() {
            @Override
            public Type[] getActualTypeArguments() {
                return new Type[]{ String.class };
            }

            @Override
            public Type getRawType() {
                return unknown;
            }

            @Override
            public Type getOwnerType() {
                return null;
            }
        };
        ResolvedType unresolvedParameterized = ResolvedType.resolve(withoutRawClass);
        assertNull(unresolvedParameterized.rawClass());
        assertTrue(unresolvedParameterized.unresolvedReason().contains("no concrete raw class"));

        WildcardType withoutBounds = new WildcardType() {
            @Override
            public Type[] getUpperBounds() {
                return new Type[0];
            }

            @Override
            public Type[] getLowerBounds() {
                return new Type[0];
            }
        };
        assertFalse(ResolvedType.resolve(withoutBounds).isResolved());
    }

    private static Field field(String name) throws NoSuchFieldException {
        return Shapes.class.getDeclaredField(name);
    }

    private static final class Shapes<T extends CharSequence> {

        List<List<String>> nested;
        Map<String, List<Integer>> mapped;
        List<? extends Number> upper;
        List<? super Integer> lower;
        List<?> unbounded;
        T[] genericArray;
    }

    private static final class Unbounded<T> {

        T[] values;
    }

    private static final class Intersection<T extends CharSequence & Comparable<T>> {
    }

    private static final class AmbiguousArray<T extends List<?>> {

        T[] values;
    }

    private static final class Recursive<T extends Comparable<T>> {
    }
}
