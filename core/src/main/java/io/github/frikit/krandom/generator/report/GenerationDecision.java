/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.report;

/** A fallback decision taken for a reported value. */
public enum GenerationDecision {

    /** A nested object past {@code objectMaxDepth} is left as the type's default. */
    DEPTH_CUT,

    /** A type already being generated on the path is not generated again, which ends a cycle. */
    CYCLE_CUT,

    /** A type that no generator supports is left at its default, as {@code objectIgnoreErrors(true)} asks. */
    UNSUPPORTED_TYPE,

    /** Constraints that no value meets are left at the type's default, as {@code objectIgnoreErrors(true)} asks. */
    CONSTRAINT_CONFLICT,

    /**
     * A failure that {@code objectIgnoreErrors(true)} passes over. A collection that rejects an element, a
     * collection type or a nested object that cannot be built, or a unique value that ran out leaves the
     * value at its default. A value that cannot be stored is reported again after the entry that made it: a
     * member keeps the value it held, and an array element its default. The
     * {@code GenerationFailureListener} receives the failure's diagnostic.
     */
    FAILURE_FALLBACK,

    /** A nullable member is {@code null}, as {@code objectNullProbability} drew. */
    NULL_PROBABILITY,

    /** An {@code Optional} is empty, as {@code objectOptionalEmptyProbability} drew. */
    OPTIONAL_EMPTY,

    /**
     * The member's name has a semantic provider, but an annotation or a Bean Validation constraint supplies
     * the value instead: constraints always come first, and in {@code RELAXED} mode annotations do too.
     */
    SEMANTIC_DEFERRED,

    /**
     * In {@code RELAXED} mode the coherence pass leaves this annotated or constrained member as it was
     * generated, where {@code STRICT} mode could align it with its siblings.
     */
    CONSTRAINT_KEPT,

    /** The coherence pass changed this member to agree with its siblings. */
    COHERENCE_MOVE
}
