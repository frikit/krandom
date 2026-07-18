/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.user;

import java.util.Objects;

/**
 * A coherent university fixture loaded from a local data pack.
 *
 * @param name   institution name
 * @param degree degree name or abbreviation
 * @param prefix institution prefix
 * @param suffix institution suffix
 * @param place  institution location
 */
public record UniversityData(String name, String degree, String prefix, String suffix, String place) {

    /**
     * Validates that every fixture component has usable text.
     */
    public UniversityData {
        name = requireText(name, "name");
        degree = requireText(degree, "degree");
        prefix = requireText(prefix, "prefix");
        suffix = requireText(suffix, "suffix");
        place = requireText(place, "place");
    }

    private static String requireText(String value, String label) {
        Objects.requireNonNull(value, label + " must not be null");
        if (value.isBlank()) {
            throw new IllegalArgumentException(label + " must not be blank");
        }
        return value;
    }
}
