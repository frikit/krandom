/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.object;

import java.io.Serializable;
import java.util.Objects;

/**
 * A problem that {@link ObjectFaker#assertConfigurationIsValid()} finds in a fixture rule, and that
 * {@link ObjectFaker#strict()} reports before generating.
 *
 * @param category what is wrong with the rule
 * @param rule     the rule, by kind, path and registration order
 * @param reason   why, naming the paths and rules involved and never a value
 */
public record FixtureRuleProblem(Category category, FixtureRule rule, String reason) implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Validates the problem.
     */
    public FixtureRuleProblem {
        Objects.requireNonNull(category, "category must not be null");
        Objects.requireNonNull(rule, "rule must not be null");
        Objects.requireNonNull(reason, "reason must not be null");
        if (reason.isBlank()) {
            throw new IllegalArgumentException("reason must not be blank");
        }
    }

    /** What is wrong with a rule. */
    public enum Category {

        /** The rule can never take part in generation. */
        UNUSED,

        /** The rule applies at more routes than its path names, with no precedence between them. */
        AMBIGUOUS,

        /** Another rule replaces or overwrites what this rule produces. */
        SHADOWED
    }

    /**
     * Describes the problem on one line, as in {@code SHADOWED rule #1 'email': replaced by ...}.
     *
     * @return a value-free description
     */
    public String describe() {
        return category + " " + rule.describe() + ": " + reason;
    }
}
