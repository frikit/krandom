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
 *
 * <p>Portable seeded schema failures include a value-safe replay recipe in the message and through
 * {@link #getReplayRecipe()}. The original textual seed is never included.
 */
public final class SchemaGenerationException extends RuntimeException {

    private static final long serialVersionUID = 3827203847835368239L;

    private final @Nullable GenerationFailureContext context;
    private final @Nullable String replayRecipe;

    /**
     * Creates an exception with field name and record index context.
     *
     * @param field       field name that failed
     * @param recordIndex record index in current batch
     * @param cause       root cause
     */
    public SchemaGenerationException(String field, int recordIndex, Throwable cause) {
        this(field, recordIndex, GenerationFailureCategory.SCHEMA_VALUE, GenerationOperation.GENERATE, cause, null);
    }

    SchemaGenerationException(String field, int recordIndex, Throwable cause, @Nullable String replayRecipe) {
        this(field, recordIndex, GenerationFailureCategory.SCHEMA_VALUE, GenerationOperation.GENERATE, cause, replayRecipe);
    }

    SchemaGenerationException(String field,
                              int recordIndex,
                              GenerationFailureCategory category,
                              GenerationOperation operation,
                              Throwable cause) {
        this(field, recordIndex, category, operation, cause, null);
    }

    SchemaGenerationException(String field,
                              int recordIndex,
                              GenerationFailureCategory category,
                              GenerationOperation operation,
                              Throwable cause,
                              @Nullable String replayRecipe) {
        this(new GenerationFailureContext(category, operation, field, null, null, -1, recordIndex), cause, replayRecipe);
    }

    SchemaGenerationException(GenerationFailureContext context, Throwable cause) {
        this(context, cause, null);
    }

    SchemaGenerationException(GenerationFailureContext context, Throwable cause, @Nullable String replayRecipe) {
        super(message(context, replayRecipe), cause);
        this.context = context;
        this.replayRecipe = replayRecipe;
    }

    private static String message(GenerationFailureContext context, @Nullable String replayRecipe) {
        String message;
        if (context.operation() == GenerationOperation.EXPORT_SCHEMA) {
            message = "Failed to export schema metadata for field '" + context.path() + "'";
        } else if (context.operation() == GenerationOperation.READ) {
            message = "Failed to read schema record component '" + context.path() + "' from "
                      + context.ownerType().getName();
        } else {
            message = "Failed to generate field '" + context.path() + "' for record index " + context.recordIndex();
        }
        return replayRecipe == null ? message : message + ". Replay recipe:\n" + replayRecipe;
    }

    /**
     * Returns the sanitized structured failure context.
     */
    public Optional<GenerationFailureContext> getContext() {
        return Optional.ofNullable(context);
    }

    /**
     * Returns a safe portable replay recipe when the failed schema used a portable source.
     */
    public Optional<String> getReplayRecipe() {
        return Optional.ofNullable(replayRecipe);
    }
}
