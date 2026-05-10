/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.object;

import java.lang.annotation.Annotation;
import java.lang.reflect.Modifier;
import java.util.Objects;
import java.util.function.Predicate;

/**
 * Factory methods for common {@link Class}-based predicates used with
 * {@link ObjectGeneratorConfig.Builder#excludeType(Predicate)}.
 */
public final class TypePredicates {

    private TypePredicates() { /* static utility */ }

    /**
     * Returns a predicate that matches classes whose fully qualified name equals {@code name}.
     *
     * @param name fully qualified class name; must not be {@code null}
     * @return a predicate testing class name equality
     */
    public static Predicate<Class<?>> named(String name) {
        Objects.requireNonNull(name, "name must not be null");
        return type -> type.getName().equals(name);
    }

    /**
     * Returns a predicate that matches exactly {@code type}.
     *
     * @param type the exact type to match; must not be {@code null}
     * @return a predicate testing class equality
     */
    public static Predicate<Class<?>> ofType(Class<?> type) {
        Objects.requireNonNull(type, "type must not be null");
        return candidate -> candidate == type;
    }

    /**
     * Returns a predicate that matches classes in the given package.
     *
     * <p>The match is package-prefix based, so sub-packages are included.
     * For example, {@code inPackage("java.time")} matches
     * {@code java.time.LocalDate} and {@code java.time.chrono.JapaneseDate}.
     *
     * @param packageName package prefix to match; must not be {@code null} or blank
     * @return a predicate testing class package prefix
     */
    public static Predicate<Class<?>> inPackage(String packageName) {
        Objects.requireNonNull(packageName, "packageName must not be null");
        String normalized = packageName.trim();
        if (normalized.isEmpty()) {
            throw new IllegalArgumentException("packageName must not be blank");
        }
        return type -> {
            Package pkg = type.getPackage();
            return pkg != null && pkg.getName().startsWith(normalized);
        };
    }

    /**
     * Returns a predicate that matches classes annotated with any of {@code annotationTypes}.
     *
     * @param annotationTypes annotations to test; at least one is required
     * @return a predicate testing annotation presence on the class
     */
    @SafeVarargs
    public static Predicate<Class<?>> isAnnotatedWith(Class<? extends Annotation>... annotationTypes) {
        Objects.requireNonNull(annotationTypes, "annotationTypes must not be null");
        if (annotationTypes.length == 0) {
            throw new IllegalArgumentException("at least one annotation type is required");
        }
        for (Class<? extends Annotation> annotationType : annotationTypes) {
            Objects.requireNonNull(annotationType, "annotationTypes must not contain null");
        }
        return type -> {
            for (Class<? extends Annotation> annotationType : annotationTypes) {
                if (type.isAnnotationPresent(annotationType)) {
                    return true;
                }
            }
            return false;
        };
    }

    /**
     * Returns a predicate that matches interface types.
     *
     * @return a predicate testing {@link Class#isInterface()}
     */
    public static Predicate<Class<?>> isInterface() {
        return Class::isInterface;
    }

    /**
     * Returns a predicate that matches abstract classes and interfaces.
     *
     * @return a predicate testing {@link Modifier#ABSTRACT}
     */
    public static Predicate<Class<?>> isAbstract() {
        return hasModifiers(Modifier.ABSTRACT);
    }

    /**
     * Returns a predicate that matches classes containing all requested Java modifiers.
     *
     * @param modifiers bitmask from {@link Modifier}; must not be {@code 0}
     * @return a predicate testing modifier bits
     */
    public static Predicate<Class<?>> hasModifiers(int modifiers) {
        if (modifiers == 0) {
            throw new IllegalArgumentException("modifiers must not be zero");
        }
        return type -> (type.getModifiers() & modifiers) == modifiers;
    }

    /**
     * Returns a predicate that matches enum types.
     *
     * @return a predicate testing {@link Class#isEnum()}
     */
    public static Predicate<Class<?>> isEnum() {
        return Class::isEnum;
    }

    /**
     * Returns a predicate that matches array types.
     *
     * @return a predicate testing {@link Class#isArray()}
     */
    public static Predicate<Class<?>> isArray() {
        return Class::isArray;
    }

    /**
     * Returns a predicate that matches candidate classes assignable from {@code type}.
     *
     * <p>This mirrors k-random's predicate direction:
     * {@code candidate.isAssignableFrom(type)}.
     *
     * @param type source type to test assignability from; must not be {@code null}
     * @return a predicate testing assignability from {@code type}
     */
    public static Predicate<Class<?>> isAssignableFrom(Class<?> type) {
        Objects.requireNonNull(type, "type must not be null");
        return candidate -> candidate.isAssignableFrom(type);
    }
}
