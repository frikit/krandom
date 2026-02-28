/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package org.github.krandom.generator.object;

import java.util.Objects;
import java.util.function.Predicate;

/**
 * Factory methods for common {@link Class}-based predicates used with
 * {@link ObjectGeneratorConfig.Builder#excludeType(Predicate)}.
 */
public final class TypePredicates {

    private TypePredicates() { /* static utility */ }

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
}
