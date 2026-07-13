/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.failure;

/** Receives value-sanitized object-generation failure diagnostics synchronously. */
@FunctionalInterface
public interface GenerationFailureListener {

    /**
     * Observes one strict or lenient failure without access to generated values or the throwable.
     *
     * @param diagnostic sanitized failure diagnostic
     */
    void onFailure(GenerationFailureDiagnostic diagnostic);
}
