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

    private @org.jspecify.annotations.Nullable Object lastAdded;

    private @org.jspecify.annotations.Nullable String lastAddedKey;

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

    Optional<ObjectConstructionAdapter> constructionAdapter(
            Class<?> type, Function<Class<?>, @org.jspecify.annotations.Nullable ObjectConstructionAdapter> lookup) {
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

    /**
     * The key of one field's values. A field is its owner and name, so two fields may hold the same
     * value: {@code orderId} and {@code customerId}, or {@code id} on two types.
     */
    static String fieldKey(Class<?> ownerType, String fieldName) {
        return ownerType.getName() + "." + fieldName;
    }

    /**
     * Whether this very value, not just an equal one, is what the last {@link #nextUnique} added to
     * this field. A nested object can add to the same field while its parent's value is generated,
     * and a lenient fallback can be the same cached box as a value added earlier.
     */
    boolean addedLast(String fieldKey, @org.jspecify.annotations.Nullable Object value) {
        return value == lastAdded && fieldKey.equals(lastAddedKey);
    }

    Object nextUnique(String fieldKey, Supplier<Object> supplier, int maxAttempts) {
        Objects.requireNonNull(fieldKey, "fieldKey must not be null");
        Objects.requireNonNull(supplier, "supplier must not be null");
        if (maxAttempts < 1) {
            throw new IllegalArgumentException("maxAttempts must be >= 1");
        }

        lastAdded = null;
        lastAddedKey = null;
        Set<Object> seen = valuesByField.computeIfAbsent(fieldKey, ignored -> new HashSet<>());
        for (int attempt = 0; attempt < maxAttempts; attempt++) {
            Object candidate = supplier.get();
            if (candidate == null) {
                return candidate;
            }
            if (seen.add(candidate)) {
                lastAdded = candidate;
                lastAddedKey = fieldKey;
                return candidate;
            }
        }

        throw new ExhaustedException("Could not generate a unique value for field '" + fieldKey + "' after "
                + maxAttempts + " attempts; its values may have run out, so generate more distinct values, raise "
                + "objectUniquenessMaxAttempts, or leave the field out of objectUniqueFields");
    }

    /** Gives back a value that no object ended up with. */
    void release(String fieldKey, @org.jspecify.annotations.Nullable Object value) {
        Set<Object> seen = valuesByField.get(fieldKey);
        if (seen != null) {
            seen.remove(value);
        }
    }

    /** No attempt produced a value the field has not had yet. */
    static final class ExhaustedException extends ObjectGenerationException {

        private static final long serialVersionUID = 1L;

        private ExhaustedException(String message) {
            super(message);
        }
    }
}
