/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.schema;

import java.util.Locale;
import java.util.Objects;
import java.util.Random;

/**
 * Context passed to schema field providers.
 *
 * @param locale      locale used for locale-aware generators
 * @param random      deterministic random source for this schema run
 * @param recordIndex zero-based record index in batch generation
 */
public record SchemaContext(
    Locale locale,
    Random random,
    int recordIndex
) {

    /**
     * Validates context invariants.
     */
    public SchemaContext {
        Objects.requireNonNull(locale, "locale must not be null");
        Objects.requireNonNull(random, "random must not be null");
        if (recordIndex < 0) {
            throw new IllegalArgumentException("recordIndex must be >= 0, got: " + recordIndex);
        }
    }
}
