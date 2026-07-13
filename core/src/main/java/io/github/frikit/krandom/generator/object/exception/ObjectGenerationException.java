/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.object.exception;

import io.github.frikit.krandom.generator.GeneratorConfig;
import io.github.frikit.krandom.generator.failure.GenerationFailureContext;
import org.jspecify.annotations.Nullable;

import java.util.Objects;
import java.util.Optional;

/**
 * Thrown when {@code ObjectGenerator} cannot populate a type.
 *
 * <p>Typical causes:
 * <ul>
 *   <li>No no-arg constructor on a non-record class.</li>
 *   <li>Inaccessible constructor or field.</li>
 *   <li>Constructor/setter threw an exception during population.</li>
 * </ul>
 * <p>
 * Set {@code GeneratorConfig.objectIgnoreErrors(true)} to silently leave fields {@code null}
 * instead of propagating this exception.
 */
public class ObjectGenerationException extends RuntimeException {

    private static final long serialVersionUID = -3926598568919673888L;

    private final @Nullable GenerationFailureContext context;

    public ObjectGenerationException(String message) {
        super(message);
        this.context = null;
    }

    public ObjectGenerationException(String message, Throwable cause) {
        super(message, cause);
        this.context = null;
    }

    /**
     * Creates a failure with sanitized structured context.
     *
     * @param message sanitized human-readable message
     * @param context machine-readable generation context
     * @param cause   original cause
     */
    public ObjectGenerationException(String message, GenerationFailureContext context, Throwable cause) {
        super(message, cause);
        this.context = Objects.requireNonNull(context, "context must not be null");
    }

    /**
     * Returns structured context when the failure originated at a migrated generation boundary.
     */
    public Optional<GenerationFailureContext> getContext() {
        return Optional.ofNullable(context);
    }
}
