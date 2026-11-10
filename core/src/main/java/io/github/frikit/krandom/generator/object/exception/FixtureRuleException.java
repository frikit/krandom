/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.object.exception;

import io.github.frikit.krandom.generator.object.FixtureRuleProblem;
import java.util.List;
import java.util.Objects;

/**
 * Thrown by {@code ObjectFaker.assertConfigurationIsValid()}, and so by a generation or population with
 * {@code ObjectFaker.strict()}, listing every problem it found at once: the root properties that have
 * no rule, include or ignore decision, and the rules that are unused, ambiguous or shadowed. The message
 * and the problems name paths, rule kinds and registration numbers, never a generated value.
 */
public class FixtureRuleException extends IllegalStateException {

    private static final long serialVersionUID = 1L;

    private final List<String> undecidedProperties;
    private final List<FixtureRuleProblem> problems;

    /**
     * Creates the exception for the problems of one {@code ObjectFaker}.
     *
     * @param rootType            the type the faker generates
     * @param undecidedProperties root properties without a rule, include or ignore decision
     * @param problems            unused, ambiguous and shadowed rules
     */
    public FixtureRuleException(
            Class<?> rootType, List<String> undecidedProperties, List<FixtureRuleProblem> problems) {
        super(message(rootType, undecidedProperties, problems));
        this.undecidedProperties = List.copyOf(undecidedProperties);
        this.problems = List.copyOf(problems);
    }

    /**
     * Returns the root properties that have no rule, include or ignore decision.
     *
     * @return property names in declaration order
     */
    public List<String> getUndecidedProperties() {
        return undecidedProperties;
    }

    /**
     * Returns the unused, ambiguous and shadowed rules.
     *
     * @return problems in the order they were found
     */
    public List<FixtureRuleProblem> getProblems() {
        return problems;
    }

    private static String message(
            Class<?> rootType, List<String> undecidedProperties, List<FixtureRuleProblem> problems) {
        Objects.requireNonNull(rootType, "rootType must not be null");
        int count = problems.size() + (undecidedProperties.isEmpty() ? 0 : 1);
        if (count == 0) {
            throw new IllegalArgumentException("a fixture rule exception needs at least one problem");
        }
        StringBuilder message = new StringBuilder("ObjectFaker for ")
                .append(rootType.getName())
                .append(" has ")
                .append(count)
                .append(count == 1 ? " fixture rule problem:" : " fixture rule problems:");
        if (!undecidedProperties.isEmpty()) {
            message.append("\n- UNDECIDED root properties ")
                    .append(undecidedProperties)
                    .append(": each needs a rule, an include or an ignore");
        }
        for (FixtureRuleProblem problem : problems) {
            message.append("\n- ").append(problem.describe());
        }
        return message.toString();
    }
}
