/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package org.github.krandom.generator.object;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
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

    private final Set<Class<?>> inProgress = new HashSet<>();
    private final Map<Class<?>, Object> instances = new HashMap<>();

    /**
     * Returns {@code true} if construction of an instance of {@code type} has begun
     * but not yet completed — indicating a circular reference.
     *
     * @param type the type to check
     * @return {@code true} if the type is currently being constructed
     */
    boolean isInProgress(Class<?> type) {
        return inProgress.contains(type);
    }

    /**
     * Marks {@code type} as in-progress (construction has started).
     *
     * @param type the type being constructed
     */
    void begin(Class<?> type) {
        inProgress.add(type);
    }

    /**
     * Marks construction of {@code type} as complete and caches the result.
     *
     * @param type     the type that was constructed
     * @param instance the constructed instance; ignored if {@code null}
     */
    void end(Class<?> type, Object instance) {
        inProgress.remove(type);
        if (instance != null) {
            instances.put(type, instance);
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
        return instances.get(type);
    }
}
