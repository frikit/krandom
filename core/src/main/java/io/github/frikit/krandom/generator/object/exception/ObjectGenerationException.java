/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.object.exception;

import io.github.frikit.krandom.generator.GeneratorConfig;

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

    public ObjectGenerationException(String message) {
        super(message);
    }

    public ObjectGenerationException(String message, Throwable cause) {
        super(message, cause);
    }
}
