/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.failure;

import org.jspecify.annotations.Nullable;

import java.io.Serializable;
import java.util.Objects;

/**
 * Sanitized, machine-readable context attached to a generation failure.
 *
 * <p>The context deliberately excludes generated values and third-party exception messages.
 * A depth or record index of {@code -1} means that dimension does not apply.
 *
 * @param category     stable failure category
 * @param operation    operation that failed
 * @param path         root-relative field, component, or schema path
 * @param ownerType    declaring/root type, or {@code null} when not applicable
 * @param declaredType declared type signature, or {@code null} when unavailable
 * @param depth        object recursion depth, or {@code -1} when not applicable
 * @param recordIndex  schema record index, or {@code -1} when not applicable
 */
public record GenerationFailureContext(
    GenerationFailureCategory category,
    GenerationOperation operation,
    String path,
    @Nullable Class<?> ownerType,
    @Nullable String declaredType,
    int depth,
    int recordIndex
) implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Validates the context without inspecting generated data.
     */
    public GenerationFailureContext {
        Objects.requireNonNull(category, "category must not be null");
        Objects.requireNonNull(operation, "operation must not be null");
        Objects.requireNonNull(path, "path must not be null");
        if (path.isBlank()) {
            throw new IllegalArgumentException("path must not be blank");
        }
        if (declaredType != null && declaredType.isBlank()) {
            throw new IllegalArgumentException("declaredType must not be blank");
        }
        if (depth < -1) {
            throw new IllegalArgumentException("depth must be >= -1");
        }
        if (recordIndex < -1) {
            throw new IllegalArgumentException("recordIndex must be >= -1");
        }
    }
}
