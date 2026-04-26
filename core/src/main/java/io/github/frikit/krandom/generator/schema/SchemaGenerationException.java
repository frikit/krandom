/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.schema;

/**
 * Exception thrown when schema field generation fails.
 */
public final class SchemaGenerationException extends RuntimeException {

    /**
     * Creates an exception with field name and record index context.
     *
     * @param field       field name that failed
     * @param recordIndex record index in current batch
     * @param cause       root cause
     */
    public SchemaGenerationException(String field, int recordIndex, Throwable cause) {
        super("Failed to generate field '" + field + "' for record index " + recordIndex, cause);
    }
}
