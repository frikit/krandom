/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package org.github.krandom.generator.schema;

import org.github.krandom.generator.GeneratorConfig;
import org.github.krandom.generator.provider.ConflictPolicy;
import org.github.krandom.generator.provider.ProviderFactory;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

/**
 * Mimesis-style field resolver and composition helper.
 */
public final class Field {

    private final GeneratorConfig config;
    private final FieldLookup     lookup;
    private final SchemaTemplateEngine templateEngine;

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
        this.templateEngine = new SchemaTemplateEngine(this.lookup, this.config);
    }

    /**
     * Creates field resolver from an existing lookup registry.
     *
     * @param lookup field lookup registry
     */
    public Field(FieldLookup lookup) {
        this.lookup = Objects.requireNonNull(lookup, "lookup must not be null");
        this.config = lookup.getConfig();
        this.templateEngine = new SchemaTemplateEngine(this.lookup, this.config);
    }

    private static String validateFieldName(String name) {
        Objects.requireNonNull(name, "field name must not be null");
        if (name.isBlank()) {
            throw new IllegalArgumentException("field name must not be blank");
        }
        return name;
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
     * Creates a string template provider backed by the current field registry.
     *
     * <p>{@code {{token}}} placeholders resolve through {@link FieldLookup}, while literal
     * {@code #} and {@code ?} segments use the same placeholder semantics as
     * {@link org.github.krandom.generator.text.TemplateStringGenerator}.
     *
     * @param template template string
     * @return string-producing template provider
     */
    public SchemaValueProvider template(String template) {
        return templateEngine.template(template);
    }

    /**
     * Creates a recursive payload-shell provider backed by the current field registry.
     *
     * <p>Strings inside the payload shell support {@code {{token}}}, {@code #}, and {@code ?}
     * placeholders. Strings that contain only a single {@code {{token}}} resolve to the raw
     * generated value instead of a stringified value, which keeps numbers and nested objects typed
     * correctly inside templated payloads.
     *
     * @param template payload shell template
     * @return payload-producing template provider
     */
    public SchemaValueProvider template(Object template) {
        return templateEngine.templatePayload(template);
    }

    /**
     * Registers a custom schema token using {@link ConflictPolicy#FAIL}.
     *
     * @param reference schema token reference
     * @param provider  provider backing the token
     * @return this field resolver for fluent configuration
     */
    public Field register(String reference, SchemaValueProvider provider) {
        lookup.register(reference, provider);
        return this;
    }

    /**
     * Registers a custom schema token.
     *
     * @param reference schema token reference
     * @param provider  provider backing the token
     * @param policy    conflict policy
     * @return this field resolver for fluent configuration
     */
    public Field register(String reference, SchemaValueProvider provider, ConflictPolicy policy) {
        lookup.register(reference, provider, policy);
        return this;
    }

    /**
     * Registers a provider-backed schema token using {@link ConflictPolicy#FAIL}.
     *
     * @param reference      schema token reference
     * @param factory        provider factory
     * @param providerType   expected provider type
     * @param valueExtractor extractor invoked on the provider instance
     * @param <T>            provider type
     * @return this field resolver for fluent configuration
     */
    public <T> Field registerProvider(String reference,
                                      ProviderFactory factory,
                                      Class<T> providerType,
                                      Function<? super T, ?> valueExtractor) {
        lookup.registerProvider(reference, factory, providerType, valueExtractor);
        return this;
    }

    /**
     * Registers a provider-backed schema token.
     *
     * @param reference      schema token reference
     * @param factory        provider factory
     * @param providerType   expected provider type
     * @param valueExtractor extractor invoked on the provider instance
     * @param policy         conflict policy
     * @param <T>            provider type
     * @return this field resolver for fluent configuration
     */
    public <T> Field registerProvider(String reference,
                                      ProviderFactory factory,
                                      Class<T> providerType,
                                      Function<? super T, ?> valueExtractor,
                                      ConflictPolicy policy) {
        lookup.registerProvider(reference, factory, providerType, valueExtractor, policy);
        return this;
    }

    /**
     * Registers a token alias using {@link ConflictPolicy#FAIL}.
     *
     * @param alias           alias token
     * @param targetReference canonical target token
     * @return this field resolver for fluent configuration
     */
    public Field registerAlias(String alias, String targetReference) {
        lookup.registerAlias(alias, targetReference);
        return this;
    }

    /**
     * Registers a token alias.
     *
     * @param alias           alias token
     * @param targetReference canonical target token
     * @param policy          conflict policy
     * @return this field resolver for fluent configuration
     */
    public Field registerAlias(String alias, String targetReference, ConflictPolicy policy) {
        lookup.registerAlias(alias, targetReference, policy);
        return this;
    }

    /**
     * Creates provider that always returns a constant.
     *
     * @param value constant value
     * @return constant provider
     */
    public SchemaValueProvider constant(Object value) {
        return SchemaValueProvider.withJsonSchema(ctx -> value, JsonSchemaSupport.infer(value));
    }

    /**
     * Creates list provider from a string reference with size range.
     *
     * @param reference field reference
     * @param min       minimum size
     * @param max       maximum size
     * @return list provider
     */
    public SchemaValueProvider list(String reference, int min, int max) {
        return list(bind(reference), min, max);
    }

    /**
     * Creates list provider from arbitrary provider with size range.
     *
     * @param provider value provider for each item
     * @param min      minimum size
     * @param max      maximum size
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
        return SchemaValueProvider.withJsonSchema(context -> {
            int size = min == max ? min : min + context.random().nextInt(max - min + 1);
            List<Object> values = new ArrayList<>(size);
            for (int i = 0; i < size; i++) {
                values.add(provider.generate(context));
            }
            return values;
        }, JsonSchemaSupport.array(provider.jsonSchema()));
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
        Map<String, Map<String, Object>> properties = new LinkedHashMap<>(copy.size());
        for (Map.Entry<String, SchemaValueProvider> entry : copy.entrySet()) {
            String name = validateFieldName(entry.getKey());
            SchemaValueProvider provider = Objects.requireNonNull(entry.getValue(),
                                                                  "provider for nested field '" + name + "' must not be null");
            properties.put(name, provider.jsonSchema());
        }
        return SchemaValueProvider.withJsonSchema(context -> {
            Map<String, Object> nested = new LinkedHashMap<>(copy.size());
            for (Map.Entry<String, SchemaValueProvider> entry : copy.entrySet()) {
                String name = validateFieldName(entry.getKey());
                SchemaValueProvider provider = Objects.requireNonNull(entry.getValue(),
                                                                      "provider for nested field '" + name + "' must not be null");
                nested.put(name, provider.generate(context));
            }
            return nested;
        }, JsonSchemaSupport.object(properties));
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
}
