/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.object;

/**
 * Extension point for constructing object shapes that Java reflection alone cannot model safely.
 *
 * <p>Adapters are discovered through {@link java.util.ServiceLoader}. They receive the existing
 * object-generation resolver through {@link ObjectConstructionContext}, so constructor arguments
 * retain normal override, constraint, generic-type, semantic, nullability, and cycle handling.
 *
 * <p>The Kotlin DSL supplies the adapter for immutable Kotlin classes. This interface deliberately
 * contains no Kotlin types so {@code krandom-core} remains a Java-only artifact.
 */
public interface ObjectConstructionAdapter {

    /**
     * Returns whether this adapter owns construction of {@code type}.
     *
     * @param type requested object type
     * @return {@code true} when {@link #construct(ObjectConstructionContext)} can construct it
     */
    boolean supports(Class<?> type);

    /**
     * Constructs the value described by {@code context}.
     *
     * @param context requested type and access to the standard parameter resolver
     * @return a non-null value assignable to {@link ObjectConstructionContext#getType()}
     */
    Object construct(ObjectConstructionContext<?> context);
}
