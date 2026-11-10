/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.object;

import java.util.List;
import java.util.Optional;

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
     * <p>The adapter stands in for a constructor. Object generation then fills the type's other mutable
     * fields, those that no parameter the adapter resolved or asked about through the context names, as it
     * fills a class that it builds through a constructor. A Kotlin {@code object}'s one instance is returned
     * as it is.
     *
     * @param context requested type and access to the standard parameter resolver
     * @return a non-null value assignable to {@link ObjectConstructionContext#getType()}
     */
    Object construct(ObjectConstructionContext<?> context);

    /**
     * Returns the direct subclasses that this adapter's language declares for a sealed {@code type}.
     *
     * <p>Object generation generates a sealed interface or abstract class as one of its concrete
     * subclasses. It asks the adapter that {@linkplain #supports supports} the type first, and reads
     * {@link Class#getPermittedSubclasses()} only when the adapter returns an empty {@code Optional}.
     * Kotlin metadata, for example, lists the subclasses of a sealed class whatever JVM target it was
     * compiled for. A present but empty list declares a sealed type with no subclass, which fails
     * generation.
     *
     * @param type an interface or abstract class that this adapter supports
     * @return the direct subclasses in any order, or an empty {@code Optional} when this adapter does
     *     not declare {@code type} sealed
     */
    default Optional<List<Class<?>>> sealedSubclasses(Class<?> type) {
        return Optional.empty();
    }
}
