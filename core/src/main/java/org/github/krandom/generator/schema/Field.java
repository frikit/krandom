/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package org.github.krandom.generator.schema;

import org.github.krandom.generator.GeneratorConfig;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

/**
 * Mimesis-style field resolver and composition helper.
 */
public final class Field {

    private final GeneratorConfig config;
    private final FieldLookup lookup;

    /**
     * Creates field resolver with default configuration.
     */
    public Field() {
        this(GeneratorConfig.defaults());
    }

    /**
     * Creates field resolver for the provided locale.
     *
     * @param locale locale to propagate to providers
     */
    public Field(Locale locale) {
        this(GeneratorConfig.builder().locale(locale).build());
    }

    /**
     * Creates field resolver with custom configuration.
     *
     * @param config generator config
     */
    public Field(GeneratorConfig config) {
        this.config = Objects.requireNonNull(config, "config must not be null");
        this.lookup = new FieldLookup(config);
    }

    /**
     * Resolves a string reference into a value provider.
     *
     * @param reference field reference (for example {@code person.full_name})
     * @return value provider
     */
    public SchemaValueProvider bind(String reference) {
        return lookup.resolve(reference);
    }

    /**
     * Alias for {@link #bind(String)}.
     *
     * @param reference field reference
     * @return value provider
     */
    public SchemaValueProvider call(String reference) {
        return bind(reference);
    }

    /**
     * Creates provider that always returns a constant.
     *
     * @param value constant value
     * @return constant provider
     */
    public SchemaValueProvider constant(Object value) {
        return ctx -> value;
    }

    /**
     * Creates list provider from a string reference with size range.
     *
     * @param reference field reference
     * @param min minimum size
     * @param max maximum size
     * @return list provider
     */
    public SchemaValueProvider list(String reference, int min, int max) {
        return list(bind(reference), min, max);
    }

    /**
     * Creates list provider from arbitrary provider with size range.
     *
     * @param provider value provider for each item
     * @param min minimum size
     * @param max maximum size
     * @return list provider
     */
    public SchemaValueProvider list(SchemaValueProvider provider, int min, int max) {
        Objects.requireNonNull(provider, "provider must not be null");
        if (min < 0) {
            throw new IllegalArgumentException("min must be >= 0, got: " + min);
        }
        if (max < min) {
            throw new IllegalArgumentException("max must be >= min, got: " + max + " < " + min);
        }
        return context -> {
            int size = min == max ? min : min + context.random().nextInt(max - min + 1);
            List<Object> values = new ArrayList<>(size);
            for (int i = 0; i < size; i++) {
                values.add(provider.generate(context));
            }
            return values;
        };
    }

    /**
     * Creates nested-object provider from field mapping.
     *
     * @param nestedFields nested field mapping
     * @return nested object provider
     */
    public SchemaValueProvider nested(Map<String, SchemaValueProvider> nestedFields) {
        Objects.requireNonNull(nestedFields, "nestedFields must not be null");
        if (nestedFields.isEmpty()) {
            throw new IllegalArgumentException("nestedFields must not be empty");
        }
        Map<String, SchemaValueProvider> copy = new LinkedHashMap<>(nestedFields);
        return context -> {
            Map<String, Object> nested = new LinkedHashMap<>(copy.size());
            for (Map.Entry<String, SchemaValueProvider> entry : copy.entrySet()) {
                String name = validateFieldName(entry.getKey());
                SchemaValueProvider provider = Objects.requireNonNull(entry.getValue(),
                        "provider for nested field '" + name + "' must not be null");
                nested.put(name, provider.generate(context));
            }
            return nested;
        };
    }

    /**
     * Returns supported lookup keys.
     *
     * @return supported string references
     */
    public java.util.Set<String> supportedReferences() {
        return lookup.supportedReferences();
    }

    /**
     * Returns the field resolver config.
     *
     * @return config
     */
    public GeneratorConfig getConfig() {
        return config;
    }

    private static String validateFieldName(String name) {
        Objects.requireNonNull(name, "field name must not be null");
        if (name.isBlank()) {
            throw new IllegalArgumentException("field name must not be blank");
        }
        return name;
    }
}
