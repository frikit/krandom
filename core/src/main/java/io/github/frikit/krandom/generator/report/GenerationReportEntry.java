/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.report;

import io.github.frikit.krandom.generator.object.FixtureRule;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * How object generation produced the value at one path, without the value itself.
 *
 * <p>An entry refers to the replay recipe by its format and by the names of the settings that shaped
 * the value, such as {@code object.max-depth} or {@code network.safety-policy}, rather than repeat the
 * configuration. Every field names paths, kinds, types, settings and policies, and never a generated
 * value, so an entry can be logged or kept beside a failing test.
 *
 * @param path           the path of the value, from the root type's simple name, such as
 *                       {@code Order.customer.email}; an element of a collection or array adds {@code []},
 *                       and a map key or value or an {@code Optional}'s value adds {@code .key} or
 *                       {@code .value}
 * @param seedSource     where the value's random stream comes from
 * @param recipeFormat   the recipe format and version, {@code krandom-recipe/v1}, when the configuration has
 *                       a portable replay recipe, or empty when it has none
 * @param settings       the names of the recipe settings that shaped the value, in the order they applied
 * @param step           what produced the value
 * @param generator      which generator or provider produced it: a semantic key, a type, or a class name
 * @param matchedRule    the fixture rule or configuration override that supplied the value, if any
 * @param constraints    the simple names of the Bean Validation constraints on the member, such as
 *                       {@code Size}
 * @param safetyPolicies the safety policies that bound the value, by setting name, such as
 *                       {@code network.safety-policy} to {@code TEST_SAFE_RESERVED}
 * @param decisions      the fallback decisions taken for the value, in the order they were taken
 */
public record GenerationReportEntry(
        String path,
        SeedSource seedSource,
        Optional<String> recipeFormat,
        List<String> settings,
        GenerationStep step,
        String generator,
        Optional<FixtureRule> matchedRule,
        List<String> constraints,
        Map<String, String> safetyPolicies,
        List<GenerationDecision> decisions) {

    /**
     * Validates the entry and keeps unmodifiable copies of its lists and map.
     */
    public GenerationReportEntry {
        Objects.requireNonNull(path, "path must not be null");
        Objects.requireNonNull(seedSource, "seedSource must not be null");
        Objects.requireNonNull(recipeFormat, "recipeFormat must not be null");
        Objects.requireNonNull(step, "step must not be null");
        Objects.requireNonNull(generator, "generator must not be null");
        Objects.requireNonNull(matchedRule, "matchedRule must not be null");
        if (path.isBlank()) {
            throw new IllegalArgumentException("path must not be blank");
        }
        if (generator.isBlank()) {
            throw new IllegalArgumentException("generator must not be blank");
        }
        settings = List.copyOf(settings);
        constraints = List.copyOf(constraints);
        safetyPolicies = Collections.unmodifiableMap(
                new LinkedHashMap<>(Objects.requireNonNull(safetyPolicies, "safetyPolicies must not be null")));
        decisions = List.copyOf(decisions);
    }
}
