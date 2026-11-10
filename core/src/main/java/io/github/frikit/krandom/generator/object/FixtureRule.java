/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.object;

import java.io.Serializable;
import java.util.Locale;
import java.util.Objects;

/**
 * One fixture rule, identified by its kind, its path and, for an {@link ObjectFaker} rule, the order
 * in which it was registered. It names a rule and never holds the generator or a value.
 *
 * @param kind         what kind of rule it is
 * @param path         for an {@code ObjectFaker} rule, the root-relative path it was registered for, such
 *                     as {@code address.city}; for a {@code GeneratorConfig} entry, which has no path of its
 *                     own, the root-relative path where it takes effect
 * @param registration the 1-based position of an {@code ObjectFaker} rule among the rules, includes and
 *                     ignores registered on that faker, including those a profile or an {@link ObjectModel}
 *                     registers, or {@code -1} for a {@code GeneratorConfig} entry, which has none
 */
public record FixtureRule(FixtureRuleKind kind, String path, int registration) implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Validates that an {@code ObjectFaker} rule, and only such a rule, has a registration number.
     */
    public FixtureRule {
        Objects.requireNonNull(kind, "kind must not be null");
        Objects.requireNonNull(path, "path must not be null");
        if (path.isBlank()) {
            throw new IllegalArgumentException("path must not be blank");
        }
        if (kind.isRegisteredOnObjectFaker() ? registration < 1 : registration != -1) {
            throw new IllegalArgumentException(
                    kind.isRegisteredOnObjectFaker()
                            ? "an ObjectFaker rule needs a registration number >= 1"
                            : "a GeneratorConfig entry has registration -1");
        }
    }

    /**
     * An {@code ObjectFaker} rule, numbered in registration order.
     *
     * @param kind         an {@code ObjectFaker} rule kind
     * @param path         the path the rule was registered for
     * @param registration its 1-based registration number
     * @return the rule
     */
    public static FixtureRule registered(FixtureRuleKind kind, String path, int registration) {
        return new FixtureRule(kind, path, registration);
    }

    /**
     * A {@code GeneratorConfig} entry, at the path where it takes effect.
     *
     * @param kind a {@code GeneratorConfig} entry kind
     * @param path the path where the entry takes effect
     * @return the rule
     */
    public static FixtureRule configured(FixtureRuleKind kind, String path) {
        return new FixtureRule(kind, path, -1);
    }

    /**
     * Describes the rule without a value, as in {@code rule #2 'address.city'} or
     * {@code GeneratorConfig field override at 'email'}.
     *
     * @return a value-free description
     */
    public String describe() {
        String name = kind.name().toLowerCase(Locale.ROOT).replace('_', ' ');
        return kind.isRegisteredOnObjectFaker()
                ? name + " #" + registration + " '" + path + "'"
                : "GeneratorConfig " + name + " at '" + path + "'";
    }
}
