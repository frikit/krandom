/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package org.github.krandom.generator;

/**
 * A generator that receives a {@link GenerationContext} describing the field
 * currently being populated.
 *
 * <p>Use this interface (instead of the zero-arg {@link Generator}) when the
 * generated value should depend on the field name, declaring class, or nesting depth.
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
 * @param <T> the type of value produced
 */
@FunctionalInterface
public interface ContextualGenerator<T> {

    /**
     * Generate a value using the supplied context.
     *
     * @param context field context; never {@code null}
     * @return generated value (may be {@code null} for reference types)
     */
    T generate(GenerationContext context);
}
