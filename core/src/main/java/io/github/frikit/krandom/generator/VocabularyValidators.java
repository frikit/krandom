/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator;

import io.github.frikit.krandom.generator.user.UniversityData;
import java.util.List;
import java.util.Objects;

/**
 * Validation rules the built-in vocabularies apply when a provider is registered.
 */
final class VocabularyValidators {

    private VocabularyValidators() {}

    static void array(String name, String[] values) {
        Objects.requireNonNull(values, name);
        if (values.length == 0) {
            throw new IllegalArgumentException(name + " must not be empty");
        }
        for (int i = 0; i < values.length; i++) {
            String value = values[i];
            if (value == null || value.isBlank()) {
                throw new IllegalArgumentException(name + " at index " + i + " must not be blank");
            }
        }
    }

    static void label(String name, String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(name + " must not be blank");
        }
    }

    static void nullableEntries(String name, String[] values) {
        Objects.requireNonNull(values, name);
        for (int i = 0; i < values.length; i++) {
            if (values[i] == null) {
                throw new IllegalArgumentException(name + " at index " + i + " must not be null");
            }
        }
    }

    static void textValues(String name, List<String> values) {
        Objects.requireNonNull(values, name);
        if (values.isEmpty()) {
            throw new IllegalArgumentException(name + " must not be empty");
        }
        for (int i = 0; i < values.size(); i++) {
            String value = values.get(i);
            if (value == null || value.isBlank()) {
                throw new IllegalArgumentException(name + " at index " + i + " must not be blank");
            }
        }
    }

    static void pronounSets(List<String> pronounSets) {
        textValues("pronounSets", pronounSets);
        for (int i = 0; i < pronounSets.size(); i++) {
            String pronounSet = pronounSets.get(i);
            int separator = pronounSet.indexOf('/');
            if (separator <= 0 || separator == pronounSet.length() - 1 || separator != pronounSet.lastIndexOf('/')) {
                throw new IllegalArgumentException("pronounSet at index " + i + " must use subject/object form");
            }
        }
    }

    static void bloodTypeDistribution(List<String> types, List<Integer> weights) {
        textValues("types", types);
        Objects.requireNonNull(weights, "weights");
        if (types.size() != weights.size()) {
            throw new IllegalArgumentException("types and weights length must match");
        }
        for (int i = 0; i < weights.size(); i++) {
            Integer weight = weights.get(i);
            if (weight == null || weight <= 0) {
                throw new IllegalArgumentException("weight at index " + i + " must be > 0");
            }
        }
    }

    static void twelveTextValues(String name, List<String> values) {
        textValues(name, values);
        if (values.size() != 12) {
            throw new IllegalArgumentException(name + " must contain exactly 12 values");
        }
    }

    static void professionArrays(String[] professions, int[] weights) {
        Objects.requireNonNull(professions, "professions");
        Objects.requireNonNull(weights, "weights");
        if (professions.length == 0) {
            throw new IllegalArgumentException("professions must not be empty");
        }
        if (professions.length != weights.length) {
            throw new IllegalArgumentException("professions and weights length must match");
        }
        for (int i = 0; i < professions.length; i++) {
            if (professions[i] == null || professions[i].isBlank()) {
                throw new IllegalArgumentException("profession at index " + i + " must not be blank");
            }
            if (weights[i] <= 0) {
                throw new IllegalArgumentException("weight at index " + i + " must be > 0");
            }
        }
    }

    static void universityData(UniversityData[] universities) {
        Objects.requireNonNull(universities, "universities");
        if (universities.length == 0) {
            throw new IllegalArgumentException("universities must not be empty");
        }
        for (int index = 0; index < universities.length; index++) {
            if (universities[index] == null) {
                throw new IllegalArgumentException("university at index " + index + " must not be null");
            }
        }
    }
}
