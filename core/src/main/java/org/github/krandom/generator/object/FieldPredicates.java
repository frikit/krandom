/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package org.github.krandom.generator.object;

import java.lang.reflect.Field;
import java.util.function.Predicate;

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
        return f -> f.getName().equals(name);
    }

    /**
     * Returns a predicate that matches fields whose declared type is exactly {@code type}.
     *
     * @param type the exact type to match; must not be {@code null}
     * @return a predicate testing field type equality
     */
    public static Predicate<Field> ofType(Class<?> type) {
        return f -> f.getType() == type;
    }

    /**
     * Returns a predicate that matches fields declared directly in {@code owner}.
     *
     * @param owner the class that must declare the field; must not be {@code null}
     * @return a predicate testing field declaring class equality
     */
    public static Predicate<Field> inClass(Class<?> owner) {
        return f -> f.getDeclaringClass() == owner;
    }
}
