/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.object;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Modifier;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("TypePredicates")
class TypePredicatesTest {

    @Test
    @DisplayName("named and ofType match exact classes")
    void namedAndOfTypeMatchExactClasses() {
        assertTrue(TypePredicates.named(Sample.class.getName()).test(Sample.class));
        assertFalse(TypePredicates.named(Sample.class.getName()).test(AnnotatedSample.class));
        assertTrue(TypePredicates.ofType(Sample.class).test(Sample.class));
        assertFalse(TypePredicates.ofType(Sample.class).test(AnnotatedSample.class));
    }

    @Test
    @DisplayName("inPackage matches package prefixes including sub-packages")
    void inPackageMatchesPrefix() {
        assertTrue(TypePredicates.inPackage("java.time").test(java.time.LocalDate.class));
        assertTrue(TypePredicates.inPackage("java").test(java.time.LocalDate.class));
        assertFalse(TypePredicates.inPackage("java.util").test(java.time.LocalDate.class));
    }

    @Test
    @DisplayName("inPackage returns false for primitive classes with no package")
    void inPackageFalseForPrimitiveTypes() {
        assertFalse(TypePredicates.inPackage("java.lang").test(int.class));
    }

    @Test
    @DisplayName("inPackage(null) throws NullPointerException")
    void inPackageNullThrows() {
        assertThrows(NullPointerException.class, () -> TypePredicates.inPackage(null));
    }

    @Test
    @DisplayName("inPackage(blank) throws IllegalArgumentException")
    void inPackageBlankThrows() {
        assertThrows(IllegalArgumentException.class, () -> TypePredicates.inPackage("   "));
    }

    @Test
    @DisplayName("annotation, modifier, interface, abstract, enum, and array predicates match expected classes")
    void richerTypePredicatesMatchExpectedClasses() {
        assertTrue(TypePredicates.isAnnotatedWith(TypeMarker.class).test(AnnotatedSample.class));
        assertTrue(TypePredicates.isAnnotatedWith(OtherTypeMarker.class, TypeMarker.class).test(AnnotatedSample.class));
        assertFalse(TypePredicates.isAnnotatedWith(OtherTypeMarker.class).test(AnnotatedSample.class));
        assertTrue(TypePredicates.isInterface().test(SampleInterface.class));
        assertFalse(TypePredicates.isInterface().test(Sample.class));
        assertTrue(TypePredicates.isAbstract().test(AbstractSample.class));
        assertTrue(TypePredicates.hasModifiers(Modifier.ABSTRACT).test(AbstractSample.class));
        assertTrue(TypePredicates.isEnum().test(SampleEnum.class));
        assertTrue(TypePredicates.isArray().test(String[].class));
        assertTrue(TypePredicates.isAssignableFrom(ConcreteChild.class).test(AbstractSample.class));
        assertFalse(TypePredicates.isAssignableFrom(AbstractSample.class).test(ConcreteChild.class));
    }

    @Test
    @DisplayName("new predicate null and argument guards throw")
    void newPredicateGuardsThrow() {
        assertThrows(NullPointerException.class, () -> TypePredicates.named(null));
        assertThrows(NullPointerException.class, () -> TypePredicates.ofType(null));
        assertThrows(NullPointerException.class,
                     () -> TypePredicates.isAnnotatedWith((Class<? extends Annotation>[]) null));
        assertThrows(NullPointerException.class, () -> TypePredicates.isAnnotatedWith(TypeMarker.class, null));
        assertThrows(NullPointerException.class, () -> TypePredicates.isAssignableFrom(null));
        assertThrows(IllegalArgumentException.class, TypePredicates::isAnnotatedWith);
        assertThrows(IllegalArgumentException.class, () -> TypePredicates.hasModifiers(0));
    }

    static class Sample {
    }

    @TypeMarker
    static class AnnotatedSample {
    }

    interface SampleInterface {
    }

    abstract static class AbstractSample {
    }

    static class ConcreteChild extends AbstractSample {
    }

    enum SampleEnum {
        VALUE
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    @interface TypeMarker {
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    @interface OtherTypeMarker {
    }
}
