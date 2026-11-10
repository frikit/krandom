/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.report;

/** What produced a reported value. */
public enum GenerationStep {

    /** A configuration override or an {@code ObjectFaker} rule, named by the entry's matched rule. */
    OVERRIDE,

    /** A {@code @Randomizer} generator. */
    RANDOMIZER,

    /** A {@code @Fake} provider. */
    FAKE,

    /** A {@code @FakeRange} range. */
    FAKE_RANGE,

    /** A generator for the member's Bean Validation constraints. */
    CONSTRAINT,

    /** A provider chosen by the member's name, under the configured semantic mode. */
    SEMANTIC,

    /** A built-in generator for the member's type. */
    BUILT_IN,

    /** One of an enum's constants. */
    ENUM,

    /** An array, collection or map, whose elements have entries of their own. */
    COLLECTION,

    /** An {@code Optional}, whose value has an entry of its own unless it is empty. */
    OPTIONAL,

    /** An object that the engine builds, with entries of its own for its members. */
    OBJECT,

    /** A {@code null} that the configuration or a {@code @Null} constraint asks for. */
    NULL,

    /** A type's default, {@code null} or zero, that a fallback decision left. */
    DEFAULT,

    /**
     * The value the member held before generation tried to set it, which a failed assignment leaves: its
     * initializer, or the value of the object given to {@code populate(existing)}.
     */
    KEPT,

    /** An {@code ObjectFaker} rule that runs after generation, at its own path. */
    RULE,

    /** An {@code ObjectFaker} ignore that clears a nested path after generation. */
    IGNORE,

    /** The coherence pass, which aligns a member with its siblings after generation. */
    COHERENCE
}
