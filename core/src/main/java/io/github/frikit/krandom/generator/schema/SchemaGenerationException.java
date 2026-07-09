/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.schema;

import io.github.frikit.krandom.generator.failure.GenerationFailureCategory;
import io.github.frikit.krandom.generator.failure.GenerationFailureContext;
import io.github.frikit.krandom.generator.failure.GenerationOperation;
import org.jspecify.annotations.Nullable;

import java.util.Optional;

/**
 * Exception thrown when schema field generation fails.
 */
public final class SchemaGenerationException extends RuntimeException {

    private static final long serialVersionUID = 3827203847835368239L;

    private final @Nullable GenerationFailureContext context;

    /**
     * Creates an exception with field name and record index context.
     *
     * @param field       field name that failed
     * @param recordIndex record index in current batch
     * @param cause       root cause
     */
    public SchemaGenerationException(String field, int recordIndex, Throwable cause) {
        this(field, recordIndex, GenerationFailureCategory.SCHEMA_VALUE, GenerationOperation.GENERATE, cause);
    }

    SchemaGenerationException(String field,
                              int recordIndex,
                              GenerationFailureCategory category,
                              GenerationOperation operation,
                              Throwable cause) {
        super(message(field, recordIndex, operation), cause);
        this.context = new GenerationFailureContext(
            category, operation, field, null, null, -1, recordIndex);
    }

    private static String message(String field, int recordIndex, GenerationOperation operation) {
        if (operation == GenerationOperation.EXPORT_SCHEMA) {
            return "Failed to export schema metadata for field '" + field + "'";
        }
        return "Failed to generate field '" + field + "' for record index " + recordIndex;
    }

    /**
     * Returns the sanitized structured failure context.
     */
    public Optional<GenerationFailureContext> getContext() {
        return Optional.ofNullable(context);
    }
}
