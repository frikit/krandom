/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.failure;

/**
 * Operation being performed when generation failed.
 */
public enum GenerationOperation {

    /** Construct an object or generator. */
    CONSTRUCT,

    /** Generate a value. */
    GENERATE,

    /** Read a field or component. */
    READ,

    /** Assign a field, component, or rule value. */
    ASSIGN,

    /** Insert an element or entry into a container. */
    INSERT,

    /** Apply an object-faker rule. */
    APPLY_RULE,

    /** Align related semantic fields. */
    ALIGN_SEMANTICS,

    /** Export schema metadata. */
    EXPORT_SCHEMA
}
