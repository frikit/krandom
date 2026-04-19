/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package org.github.krandom.generator.object;

import org.github.krandom.generator.object.exception.ObjectGenerationException;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Supplier;

/**
 * Tracks generated values for configured unique object fields.
 */
final class UniqueFieldTracker {

    private final Map<String, Set<Object>> valuesByField = new HashMap<>();

    Object nextUnique(String normalizedFieldName, Supplier<Object> supplier, int maxAttempts) {
        Objects.requireNonNull(normalizedFieldName, "normalizedFieldName must not be null");
        Objects.requireNonNull(supplier, "supplier must not be null");
        if (maxAttempts < 1) {
            throw new IllegalArgumentException("maxAttempts must be >= 1");
        }

        Set<Object> seen = valuesByField.computeIfAbsent(normalizedFieldName, ignored -> new HashSet<>());
        for (int attempt = 0; attempt < maxAttempts; attempt++) {
            Object candidate = supplier.get();
            if (candidate == null || seen.add(candidate)) {
                return candidate;
            }
        }

        throw new ObjectGenerationException(
            "Could not generate a unique value for field '" + normalizedFieldName + "' after " + maxAttempts + " attempts");
    }
}
