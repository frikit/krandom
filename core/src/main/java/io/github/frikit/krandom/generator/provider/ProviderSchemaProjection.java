/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.provider;

import io.github.frikit.krandom.generator.GeneratorConfig;

import java.util.List;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiFunction;

/**
 * Typed schema reference produced from a provider instance.
 *
 * <p>The projection describes a schema token, its aliases, extractor, JSON Schema shape, and
 * safety metadata. `FieldLookup` turns this descriptor into a config-scoped value provider.
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
    private final ProviderSafetyMetadata        safetyMetadata;

    /**
     * Creates a string-shaped schema projection builder.
     *
     * @param reference canonical schema reference
     * @param extractor provider value extractor
     * @param <T> provider implementation type
     * @return projection builder
     */
    public static <T> Builder<T> builder(
        String reference,
        BiFunction<? super T, GeneratorConfig, ?> extractor) {
        return new Builder<>(reference, extractor);
    }

    ProviderSchemaProjection(String reference,
                             List<String> aliases,
                             BiFunction<? super T, GeneratorConfig, ?> extractor,
                             boolean integer,
                             String format,
                             Class<?> recordType,
                             Set<String> nullableComponents) {
        this(reference,
             aliases,
             extractor,
             integer,
             format,
             recordType,
             nullableComponents,
             ProviderSafetyMetadata.unclassified());
    }

    ProviderSchemaProjection(String reference,
                             List<String> aliases,
                             BiFunction<? super T, GeneratorConfig, ?> extractor,
                             boolean integer,
                             String format,
                             Class<?> recordType,
                             Set<String> nullableComponents,
                             ProviderSafetyMetadata safetyMetadata) {
        this.reference = Objects.requireNonNull(reference, "reference must not be null");
        this.aliases = List.copyOf(Objects.requireNonNull(aliases, "aliases must not be null"));
        this.extractor = Objects.requireNonNull(extractor, "extractor must not be null");
        this.integer = integer;
        this.format = format;
        this.recordType = recordType;
        this.nullableComponents = Set.copyOf(
            Objects.requireNonNull(nullableComponents, "nullableComponents must not be null"));
        this.safetyMetadata = Objects.requireNonNull(safetyMetadata, "safetyMetadata must not be null");
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
     * Returns validity and test-safety claims for this specific schema reference.
     *
     * @return immutable schema-reference safety metadata
     */
    public ProviderSafetyMetadata getSafetyMetadata() {
        return safetyMetadata;
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

    ProviderSchemaProjection<T> withSafetyMetadata(ProviderSafetyMetadata metadata) {
        return new ProviderSchemaProjection<>(reference,
                                              aliases,
                                              extractor,
                                              integer,
                                              format,
                                              recordType,
                                              nullableComponents,
                                              metadata);
    }

    /**
     * Builder for a typed provider schema projection.
     *
     * @param <T> provider implementation type
     */
    public static final class Builder<T> {

        private final String reference;
        private final BiFunction<? super T, GeneratorConfig, ?> extractor;
        private final List<String> aliases = new ArrayList<>();
        private boolean integer;
        private String format;
        private Class<?> recordType;
        private Set<String> nullableComponents = Set.of();
        private ProviderSafetyMetadata safetyMetadata = ProviderSafetyMetadata.unclassified();

        private Builder(String reference, BiFunction<? super T, GeneratorConfig, ?> extractor) {
            this.reference = requireName("reference", reference);
            this.extractor = Objects.requireNonNull(extractor, "extractor must not be null");
        }

        /**
         * Adds schema lookup aliases.
         *
         * @param values aliases
         * @return this builder
         */
        public Builder<T> aliases(String... values) {
            Objects.requireNonNull(values, "aliases must not be null");
            Set<String> names = new LinkedHashSet<>(aliases);
            for (String value : values) {
                String alias = requireName("alias", value);
                if (!names.add(alias)) {
                    throw new IllegalArgumentException("alias is duplicated: " + alias);
                }
            }
            aliases.clear();
            aliases.addAll(names);
            return this;
        }

        /**
         * Marks values as JSON Schema integers.
         *
         * @return this builder
         */
        public Builder<T> integer() {
            this.integer = true;
            return this;
        }

        /**
         * Sets a JSON Schema string format.
         *
         * @param value format name
         * @return this builder
         */
        public Builder<T> format(String value) {
            this.format = requireName("format", value);
            return this;
        }

        /**
         * Describes a record-shaped value and its nullable components.
         *
         * @param type record type
         * @param nullableComponentNames nullable record components
         * @return this builder
         */
        public Builder<T> record(Class<?> type, String... nullableComponentNames) {
            Class<?> value = Objects.requireNonNull(type, "type must not be null");
            if (!value.isRecord()) {
                throw new IllegalArgumentException("type must be a record: " + value.getName());
            }
            this.recordType = value;
            Objects.requireNonNull(nullableComponentNames, "nullableComponentNames must not be null");
            Set<String> names = new LinkedHashSet<>();
            for (String componentName : nullableComponentNames) {
                names.add(requireToken("nullableComponentName", componentName));
            }
            this.nullableComponents = Set.copyOf(names);
            return this;
        }

        /**
         * Sets projection-specific validity and test-safety metadata.
         *
         * @param metadata safety metadata
         * @return this builder
         */
        public Builder<T> safetyMetadata(ProviderSafetyMetadata metadata) {
            this.safetyMetadata = Objects.requireNonNull(metadata, "metadata must not be null");
            return this;
        }

        /**
         * Builds the immutable projection.
         *
         * @return schema projection
         */
        public ProviderSchemaProjection<T> build() {
            int shapes = (integer ? 1 : 0) + (format == null ? 0 : 1) + (recordType == null ? 0 : 1);
            if (shapes > 1) {
                throw new IllegalStateException("Choose only one schema shape: integer, format, or record");
            }
            if (aliases.contains(reference)) {
                throw new IllegalArgumentException("alias duplicates schema reference: " + reference);
            }
            return new ProviderSchemaProjection<>(reference,
                                                  aliases,
                                                  extractor,
                                                  integer,
                                                  format,
                                                  recordType,
                                                  nullableComponents,
                                                  safetyMetadata);
        }

        private static String requireName(String name, String value) {
            return requireToken(name, value).toLowerCase(java.util.Locale.ROOT);
        }

        private static String requireToken(String name, String value) {
            Objects.requireNonNull(value, name + " must not be null");
            String normalized = value.trim();
            if (normalized.isEmpty() || normalized.indexOf('\n') >= 0 || normalized.indexOf('\r') >= 0) {
                throw new IllegalArgumentException(name + " must be a non-blank single-line value");
            }
            return normalized;
        }
    }
}
