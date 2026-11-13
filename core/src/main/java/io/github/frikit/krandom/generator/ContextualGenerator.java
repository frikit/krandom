/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator;

import org.jspecify.annotations.Nullable;

/**
 * A generator that receives a {@link GenerationContext} describing the field
 * currently being populated.
 *
 * <p>Use this interface (instead of the zero-arg {@link Generator}) when the
 * generated value should depend on the field name, declaring class, or nesting depth.
 *
 * <p>{@code T} may be a nullable type, such as {@code ContextualGenerator<@Nullable String>}, for a
 * generator that leaves a field {@code null}.
 *
 * <pre>{@code
 *   // Append the field name to every String
 *   config.override(String.class,
 *       (ContextualGenerator<String>) ctx -> ctx.getFieldName() + "_value");
 *
 *   // Different value per field on Person
 *   config.override(Person.class, "email",
 *       ctx -> ctx.getOwnerType().getSimpleName().toLowerCase() + "@example.com");
 * }</pre>
 *
 * @param <T> the type of value produced, which may be nullable
 */
@FunctionalInterface
public interface ContextualGenerator<T extends @Nullable Object> {

    /**
     * Generate a value using the supplied context.
     *
     * @param context field context; never {@code null}
     * @return generated value (may be {@code null} for reference types)
     */
    T generate(GenerationContext context);
}
