/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package org.github.krandom.generator.object;

/**
 * Controls how strongly field-name semantics influence object generation.
 */
public enum ObjectGenerationSemanticMode {

    /**
     * Use semantic field-name generators when available, but allow annotations and bean validation
     * constraints to take precedence.
     */
    RELAXED,

    /**
     * Use semantic field-name generators whenever a semantic match exists, even if annotations
     * or bean validation constraints are also present.
     */
    STRICT,

    /**
     * Disable field-name semantics and fall back to structural type-based generation only.
     */
    STRUCTURAL_ONLY
}
