/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.object;

import io.github.frikit.krandom.generator.failure.GenerationFailureContext;
import io.github.frikit.krandom.generator.failure.GenerationFailureDiagnostic;
import io.github.frikit.krandom.generator.failure.GenerationFailureListener;
import io.github.frikit.krandom.generator.object.exception.ObjectGenerationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.Optional;

/** Central strict/lenient decision for contextual object-generation failures. */
final class ObjectGenerationFailurePolicy {

    private static final Logger LOGGER = LoggerFactory.getLogger(ObjectGenerationFailurePolicy.class);
    private static final GenerationFailureListener NOOP_LISTENER = diagnostic -> {};

    private final boolean lenient;
    private final GenerationFailureListener listener;
    private final Optional<String> replayIdentity;

    ObjectGenerationFailurePolicy(boolean lenient) {
        this(lenient, NOOP_LISTENER);
    }

    ObjectGenerationFailurePolicy(boolean lenient, GenerationFailureListener listener) {
        this(lenient, listener, Optional.empty());
    }

    ObjectGenerationFailurePolicy(boolean lenient,
                                  GenerationFailureListener listener,
                                  Optional<String> replayIdentity) {
        this.lenient = lenient;
        this.listener = Objects.requireNonNull(listener, "listener must not be null");
        this.replayIdentity = Objects.requireNonNull(replayIdentity, "replayIdentity must not be null");
    }

    <T> T handle(ObjectGenerationException failure, T fallback) {
        Objects.requireNonNull(failure, "failure must not be null");
        GenerationFailureContext context = failure.getContext().orElseThrow(
            () -> new IllegalArgumentException("Failure policy requires structured context", failure));
        Throwable cause = failure.getCause();
        String causeType = cause != null ? cause.getClass().getName() : failure.getClass().getName();
        notifyListener(new GenerationFailureDiagnostic(context, causeType, replayIdentity));
        if (!lenient) {
            throw failure;
        }

        LOGGER.debug("Ignored generation failure " + context.operation() + "/" + context.category()
                     + " at '" + context.path() + "' (declared type " + context.declaredType()
                     + ", depth " + context.depth() + ", record index " + context.recordIndex()
                     + "); cause=" + causeType);
        return fallback;
    }

    private void notifyListener(GenerationFailureDiagnostic diagnostic) {
        try {
            listener.onFailure(diagnostic);
        } catch (RuntimeException e) {
            LOGGER.warn("Generation failure listener callback was ignored; listener="
                        + listener.getClass().getName() + ", cause=" + e.getClass().getName());
        }
    }
}
