/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.object;

import io.github.frikit.krandom.generator.Generator;

/**
 * A root {@link ObjectFaker} rule's generator, which the faker installs as a field override, tagged with
 * the rule so that a generation report names it. The faker tags its rules only when a report listener is
 * configured, and the tag draws exactly what the rule draws.
 *
 * @param delegate the rule's generator
 * @param rule     the rule, by kind, path and registration number
 * @param <T>      the generated type
 */
record FixtureRuleGenerator<T>(Generator<T> delegate, FixtureRule rule) implements Generator<T> {

    @Override
    public T generate() {
        return delegate.generate();
    }
}
