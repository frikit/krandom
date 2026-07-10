/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.object;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Tracks types currently being constructed to detect and break circular references.
 *
 * <p>When a type is first encountered during object-graph generation it is marked
 * <em>in-progress</em>. If the same type is requested again before its construction
 * completes (i.e. a cycle exists in the object graph), the previously cached partial
 * instance (or {@code null} if none is available yet) is returned to break the cycle.
 *
 * <p>A single {@link ObjectPool} instance is shared across the entire object graph
 * constructed from one root {@link ObjectGenerator} call.
 *
 * <p><b>Thread safety:</b> Not thread-safe. A new pool is created for each
 * top-level {@link ObjectGenerator#generate()} invocation.
 */
final class ObjectPool {

    private final int                          maxCachedPerType;
    private final Set<PoolKey>                 inProgress = new HashSet<>();
    private final Map<PoolKey, Deque<Object>>  instances  = new HashMap<>();

    ObjectPool() {
        this(ObjectGeneratorConfig.DEFAULT_OBJECT_POOL_SIZE);
    }

    ObjectPool(int maxCachedPerType) {
        if (maxCachedPerType < 0) {
            throw new IllegalArgumentException("maxCachedPerType must be >= 0, was: " + maxCachedPerType);
        }
        this.maxCachedPerType = maxCachedPerType;
    }

    /**
     * Returns {@code true} if construction of an instance of {@code type} has begun
     * but not yet completed — indicating a circular reference.
     *
     * @param type the type to check
     * @return {@code true} if the type is currently being constructed
     */
    boolean isInProgress(Class<?> type) {
        return isInProgress(type, type.getTypeName());
    }

    /**
     * Returns whether construction of a resolved generic type has begun but not yet completed.
     *
     * <p>The raw class alone cannot distinguish {@code Box<String>} from {@code Box<List<Integer>>}
     * in one object graph. Callers that retain generic resolution therefore provide the resolved
     * type signature as part of the cycle key.
     *
     * @param type raw runtime type
     * @param typeSignature resolved generic type signature
     * @return {@code true} when the same resolved type is currently being constructed
     */
    boolean isInProgress(Class<?> type, String typeSignature) {
        return inProgress.contains(key(type, typeSignature));
    }

    /**
     * Marks {@code type} as in-progress (construction has started).
     *
     * @param type the type being constructed
     */
    void begin(Class<?> type) {
        begin(type, type.getTypeName());
    }

    /**
     * Marks a resolved generic type as in-progress.
     *
     * @param type raw runtime type
     * @param typeSignature resolved generic type signature
     */
    void begin(Class<?> type, String typeSignature) {
        inProgress.add(key(type, typeSignature));
    }

    /**
     * Marks construction of {@code type} as complete and caches the result.
     *
     * @param type     the type that was constructed
     * @param instance the constructed instance; ignored if {@code null}
     */
    void end(Class<?> type, Object instance) {
        end(type, type.getTypeName(), instance);
    }

    /**
     * Marks a resolved generic type as complete and caches its result.
     *
     * @param type raw runtime type
     * @param typeSignature resolved generic type signature
     * @param instance completed instance; ignored if {@code null}
     */
    void end(Class<?> type, String typeSignature, Object instance) {
        PoolKey key = key(type, typeSignature);
        inProgress.remove(key);
        if (instance == null || maxCachedPerType == 0) {
            return;
        }
        Deque<Object> perType = instances.computeIfAbsent(key, ignored -> new ArrayDeque<>());
        perType.addLast(instance);
        while (perType.size() > maxCachedPerType) {
            perType.removeFirst();
        }
    }

    /**
     * Returns a previously completed instance of {@code type}, or {@code null} if
     * no completed instance is cached yet.
     *
     * @param type the type to look up
     * @return a cached instance, or {@code null}
     */
    Object getCached(Class<?> type) {
        return getCached(type, type.getTypeName());
    }

    /**
     * Returns a completed instance matching a resolved generic type, if one is cached.
     *
     * @param type raw runtime type
     * @param typeSignature resolved generic type signature
     * @return a cached instance, or {@code null}
     */
    Object getCached(Class<?> type, String typeSignature) {
        Deque<Object> perType = instances.get(key(type, typeSignature));
        return perType == null ? null : perType.peekLast();
    }

    private static PoolKey key(Class<?> type, String typeSignature) {
        return new PoolKey(
            Objects.requireNonNull(type, "type must not be null"),
            Objects.requireNonNull(typeSignature, "typeSignature must not be null"));
    }

    private record PoolKey(Class<?> type, String typeSignature) {
    }
}
