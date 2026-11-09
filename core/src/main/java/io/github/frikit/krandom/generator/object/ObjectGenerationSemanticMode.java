/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.object;

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
     * Use semantic field-name generators whenever a semantic match exists, even over {@code @Randomizer},
     * {@code @Fake} and {@code @FakeRange}, and let the coherence pass overwrite annotated fields. Bean
     * Validation constraints still take precedence when a value is generated, and the coherence pass writes no
     * derived value that breaks a field's own constraints: such a field keeps its value, and a sibling that
     * depends on it follows it where it can.
     */
    STRICT,

    /**
     * Disable field-name semantics and fall back to structural type-based generation only.
     */
    STRUCTURAL_ONLY
}
