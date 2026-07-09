/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.object;

import io.github.frikit.krandom.generator.failure.GenerationFailureContext;
import io.github.frikit.krandom.generator.object.exception.ObjectGenerationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

/** Central strict/lenient decision for contextual object-generation failures. */
final class ObjectGenerationFailurePolicy {

    private static final Logger LOGGER = LoggerFactory.getLogger(ObjectGenerationFailurePolicy.class);

    private final boolean lenient;

    ObjectGenerationFailurePolicy(boolean lenient) {
        this.lenient = lenient;
    }

    <T> T handle(ObjectGenerationException failure, T fallback) {
        Objects.requireNonNull(failure, "failure must not be null");
        if (!lenient) {
            throw failure;
        }

        GenerationFailureContext context = failure.getContext().orElseThrow(
            () -> new IllegalArgumentException("Lenient failures require structured context", failure));
        Throwable cause = failure.getCause();
        String causeType = cause != null ? cause.getClass().getName() : failure.getClass().getName();
        LOGGER.debug("Ignored generation failure " + context.operation() + "/" + context.category()
                     + " at '" + context.path() + "' (declared type " + context.declaredType()
                     + ", depth " + context.depth() + ", record index " + context.recordIndex()
                     + "); cause=" + causeType);
        return fallback;
    }
}
