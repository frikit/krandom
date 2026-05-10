/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.object;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.regex.Pattern;

/**
 * Factory methods for common {@link Field}-based predicates used with
 * {@link ObjectGeneratorConfig.Builder#exclude(Predicate)}.
 *
 * <p><b>Usage</b>
 * <pre>{@code
 *   ObjectGeneratorConfig config = ObjectGeneratorConfig.builder()
 *       .exclude(FieldPredicates.named("password"))
 *       .exclude(FieldPredicates.ofType(String.class))
 *       .exclude(FieldPredicates.inClass(SensitiveData.class))
 *       .build();
 * }</pre>
 *
 * @see ObjectGeneratorConfig
 * @see Exclude
 */
public final class FieldPredicates {

    private FieldPredicates() { /* static utility */ }

    /**
     * Returns a predicate that matches fields whose name equals {@code name}.
     *
     * @param name the exact field name to match; must not be {@code null}
     * @return a predicate testing field name equality
     */
    public static Predicate<Field> named(String name) {
        Objects.requireNonNull(name, "name must not be null");
        return f -> f.getName().equals(name);
    }

    /**
     * Returns a predicate that matches fields whose name matches {@code regex}.
     *
     * <p>This is the migration equivalent for k-random's regex-based
     * {@code FieldPredicates.named(...)} behavior while preserving this library's
     * existing exact-match {@link #named(String)} contract.
     *
     * @param regex regular expression matched against the full field name; must not be {@code null}
     * @return a predicate testing regular-expression field name matching
     */
    public static Predicate<Field> nameMatches(String regex) {
        Objects.requireNonNull(regex, "regex must not be null");
        Pattern pattern = Pattern.compile(regex);
        return f -> pattern.matcher(f.getName()).matches();
    }

    /**
     * Returns a predicate that matches fields whose declared type is exactly {@code type}.
     *
     * @param type the exact type to match; must not be {@code null}
     * @return a predicate testing field type equality
     */
    public static Predicate<Field> ofType(Class<?> type) {
        Objects.requireNonNull(type, "type must not be null");
        return f -> f.getType() == type;
    }

    /**
     * Returns a predicate that matches fields declared directly in {@code owner}.
     *
     * @param owner the class that must declare the field; must not be {@code null}
     * @return a predicate testing field declaring class equality
     */
    public static Predicate<Field> inClass(Class<?> owner) {
        Objects.requireNonNull(owner, "owner must not be null");
        return f -> f.getDeclaringClass() == owner;
    }

    /**
     * Returns a predicate that matches fields annotated with any of {@code annotationTypes}.
     *
     * @param annotationTypes annotations to test; at least one is required
     * @return a predicate testing annotation presence on the field
     */
    @SafeVarargs
    public static Predicate<Field> isAnnotatedWith(Class<? extends Annotation>... annotationTypes) {
        Objects.requireNonNull(annotationTypes, "annotationTypes must not be null");
        if (annotationTypes.length == 0) {
            throw new IllegalArgumentException("at least one annotation type is required");
        }
        for (Class<? extends Annotation> annotationType : annotationTypes) {
            Objects.requireNonNull(annotationType, "annotationTypes must not contain null");
        }
        return f -> {
            for (Class<? extends Annotation> annotationType : annotationTypes) {
                if (f.isAnnotationPresent(annotationType)) {
                    return true;
                }
            }
            return false;
        };
    }

    /**
     * Returns a predicate that matches fields containing all requested Java modifiers.
     *
     * @param modifiers bitmask from {@link java.lang.reflect.Modifier}; must not be {@code 0}
     * @return a predicate testing modifier bits
     */
    public static Predicate<Field> hasModifiers(int modifiers) {
        if (modifiers == 0) {
            throw new IllegalArgumentException("modifiers must not be zero");
        }
        return f -> (f.getModifiers() & modifiers) == modifiers;
    }
}
