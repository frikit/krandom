/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.examples.jpms.closedconsumer;

import io.github.frikit.krandom.generator.object.ObjectGenerator;
import io.github.frikit.krandom.generator.object.exception.ObjectGenerationException;

/** Verifies the diagnostic emitted when a named consumer package is not open. */
public final class ClosedConsumer {

    private static final String REQUIRED_DIRECTIVE =
        "opens io.github.frikit.krandom.examples.jpms.closedconsumer to io.github.frikit.krandom;";

    private ClosedConsumer() {}

    /** Runs the executable consumer contract. */
    public static void main(String[] args) {
        try {
            new ObjectGenerator<>(PrivateFixture.class).generate();
            throw new AssertionError("Object generation unexpectedly crossed a closed module boundary");
        } catch (ObjectGenerationException expected) {
            if (!expected.getMessage().contains(REQUIRED_DIRECTIVE)) {
                throw new AssertionError("Missing actionable JPMS opens diagnostic: " + expected.getMessage());
            }
        }
    }

    static final class PrivateFixture {

        private String value;
    }
}
