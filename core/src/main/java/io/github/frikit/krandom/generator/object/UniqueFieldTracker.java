/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.object;

import io.github.frikit.krandom.generator.Generator;
import io.github.frikit.krandom.generator.object.exception.ObjectGenerationException;

import java.lang.reflect.AnnotatedElement;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Mutable state owned by one top-level generator: generated values of configured unique fields,
 * the {@link Randomizer} instances bound to fields, and the construction adapters found per type.
 */
final class UniqueFieldTracker {

    private final Map<String, Set<Object>> valuesByField = new HashMap<>();

    /**
     * {@link Randomizer} instances bound to fields. They share this tracker's lifetime — one
     * top-level generator — so a stateful randomizer keeps its state across generated objects.
     */
    private final Map<AnnotatedElement, Generator<?>> randomizers = new HashMap<>();

    /**
     * Construction adapters found for each generated type. {@code ServiceLoader} scans the class
     * path, so a generator looks each type up once instead of once per object.
     */
    private final Map<Class<?>, Optional<ObjectConstructionAdapter>> constructionAdapters = new HashMap<>();

    Optional<ObjectConstructionAdapter> constructionAdapter(Class<?> type,
                                                            Function<Class<?>, ObjectConstructionAdapter> lookup) {
        Optional<ObjectConstructionAdapter> adapter = constructionAdapters.get(type);
        if (adapter == null) {
            adapter = Optional.ofNullable(lookup.apply(type));
            constructionAdapters.put(type, adapter);
        }
        return adapter;
    }

    Generator<?> randomizer(AnnotatedElement element, Supplier<Generator<?>> factory) {
        Generator<?> existing = randomizers.get(element);
        if (existing != null) {
            return existing;
        }
        Generator<?> created = factory.get();
        randomizers.put(element, created);
        return created;
    }

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
