/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator;

/**
 * Provides context about the field currently being generated.
 *
 * <p>An instance is passed to every {@link ContextualGenerator} at generation time,
 * allowing the generator to produce values that depend on the field name, the declaring
 * class, or the current nesting depth.
 *
 * <pre>{@code
 *   config.override(String.class, ctx -> ctx.getFieldName() + "_value");
 *   config.override(Person.class, "name", ctx -> "Alice_" + ctx.getDepth());
 * }</pre>
 */
public final class GenerationContext {

    private final String   fieldName;
    private final Class<?> ownerType;
    private final int      depth;

    public GenerationContext(String fieldName, Class<?> ownerType, int depth) {
        this.fieldName = fieldName;
        this.ownerType = ownerType;
        this.depth = depth;
    }

    /**
     * Name of the field being populated.
     */
    public String getFieldName() {
        return fieldName;
    }

    /**
     * Class that declares the field.
     */
    public Class<?> getOwnerType() {
        return ownerType;
    }

    /**
     * Current nesting depth.
     * {@code 0} means the field belongs to the root object passed to
     * {@link io.github.frikit.krandom.generator.object.ObjectGenerator}.
     */
    public int getDepth() {
        return depth;
    }
}
