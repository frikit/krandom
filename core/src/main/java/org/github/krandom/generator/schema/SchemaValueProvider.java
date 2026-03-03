/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package org.github.krandom.generator.schema;

/**
 * Produces a schema field value using generation context.
 */
@FunctionalInterface
public interface SchemaValueProvider {

    /**
     * Generates a value for the current schema field.
     *
     * @param context schema generation context
     * @return generated value
     */
    Object generate(SchemaContext context);
}
