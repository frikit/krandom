/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.failure;

/**
 * Stable reason categories for contextual generation failures.
 */
public enum GenerationFailureCategory {

    /** Object or value construction failed. */
    CONSTRUCTION,

    /** A declared type is not supported by the active generation policy. */
    UNSUPPORTED_TYPE,

    /** A generated value could not be assigned to its destination. */
    ASSIGNMENT,

    /** A value could not be inserted into a collection, map, or array. */
    COLLECTION_INSERTION,

    /** A custom generator could not be created or invoked. */
    CUSTOM_GENERATOR,

    /** A provider lookup or invocation failed. */
    PROVIDER,

    /** A reflective read, lookup, or invocation failed. */
    REFLECTION,

    /** A schema field value could not be generated. */
    SCHEMA_VALUE,

    /** Schema metadata could not be generated. */
    SCHEMA_METADATA
}
