/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.provider;

import io.github.frikit.krandom.generator.GeneratorConfig;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiFunction;

/**
 * Typed schema reference produced from a provider instance.
 *
 * <p>The projection describes a schema token, its aliases, extractor, and JSON Schema shape.
 * `FieldLookup` turns this descriptor into a config-scoped value provider.
 *
 * @param <T> provider implementation type
 */
public final class ProviderSchemaProjection<T> {

    private final String                        reference;
    private final List<String>                  aliases;
    private final BiFunction<? super T, GeneratorConfig, ?> extractor;
    private final boolean                       integer;
    private final String                        format;
    private final Class<?>                      recordType;
    private final Set<String>                   nullableComponents;

    ProviderSchemaProjection(String reference,
                             List<String> aliases,
                             BiFunction<? super T, GeneratorConfig, ?> extractor,
                             boolean integer,
                             String format,
                             Class<?> recordType,
                             Set<String> nullableComponents) {
        this.reference = Objects.requireNonNull(reference, "reference must not be null");
        this.aliases = List.copyOf(Objects.requireNonNull(aliases, "aliases must not be null"));
        this.extractor = Objects.requireNonNull(extractor, "extractor must not be null");
        this.integer = integer;
        this.format = format;
        this.recordType = recordType;
        this.nullableComponents = Set.copyOf(
            Objects.requireNonNull(nullableComponents, "nullableComponents must not be null"));
    }

    /**
     * Returns the canonical schema reference.
     *
     * @return schema reference
     */
    public String getReference() {
        return reference;
    }

    /**
     * Returns immutable aliases for this reference.
     *
     * @return schema reference aliases
     */
    public List<String> getAliases() {
        return aliases;
    }

    /**
     * Returns whether extracted values have the JSON Schema integer shape.
     *
     * @return true for integer values
     */
    public boolean isInteger() {
        return integer;
    }

    /**
     * Returns the optional JSON Schema string format.
     *
     * @return string format when this is a formatted string projection
     */
    public Optional<String> getFormat() {
        return Optional.ofNullable(format);
    }

    /**
     * Returns the optional record type used to derive JSON Schema properties.
     *
     * @return record type when this projection returns a record
     */
    public Optional<Class<?>> getRecordType() {
        return Optional.ofNullable(recordType);
    }

    /**
     * Returns nullable record component names.
     *
     * @return immutable nullable-component set
     */
    public Set<String> getNullableComponents() {
        return nullableComponents;
    }

    /**
     * Extracts one schema value from a typed provider and configuration.
     *
     * @param provider provider instance
     * @param config config propagated to the provider and projection
     * @return extracted value
     */
    public Object extract(T provider, GeneratorConfig config) {
        return extractor.apply(provider, config);
    }
}
