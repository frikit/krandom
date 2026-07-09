/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.failure;

import java.util.Objects;
import java.util.Optional;

/**
 * Value-sanitized diagnostic emitted when object generation encounters a contextual failure.
 *
 * @param context        structured failure context without generated values
 * @param causeType      fully qualified cause class name, never the cause message
 * @param replayIdentity optional replay-recipe identity; empty until a recipe is available
 */
public record GenerationFailureDiagnostic(GenerationFailureContext context,
                                          String causeType,
                                          Optional<String> replayIdentity) {

    public GenerationFailureDiagnostic {
        Objects.requireNonNull(context, "context must not be null");
        Objects.requireNonNull(causeType, "causeType must not be null");
        Objects.requireNonNull(replayIdentity, "replayIdentity must not be null");
    }
}
