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
import java.util.Set;

/**
 * Immutable definition of one built-in provider and its public names.
 *
 * <p>Descriptors keep the provider type, factory, aliases, and object-field semantic keys
 * together. The catalog validates collisions before a descriptor is used to construct a hub.
 *
 * @param <T> provider implementation type
 */
public final class ProviderDescriptor<T> {

    private final String            key;
    private final Class<T>          providerType;
    private final ProviderFactory   factory;
    private final List<String>      aliases;
    private final Set<String>       semanticKeys;
    private final ProviderSafetyMetadata safetyMetadata;
    private final List<ProviderSchemaProjection<T>> schemaProjections;

    /**
     * Creates a public descriptor builder for a built-in or module provider.
     *
     * @param key canonical provider lookup key
     * @param providerType provider implementation type
     * @param factory configuration-aware provider factory
     * @param <T> provider implementation type
     * @return descriptor builder
     */
    public static <T> Builder<T> builder(String key, Class<T> providerType, ProviderFactory factory) {
        return new Builder<>(key, providerType, factory);
    }

    ProviderDescriptor(String key,
                       Class<T> providerType,
                       ProviderFactory factory,
                       List<String> aliases,
                       Set<String> semanticKeys,
                       List<ProviderSchemaProjection<T>> schemaProjections) {
        this(key,
             providerType,
             factory,
             aliases,
             semanticKeys,
             ProviderSafetyMetadata.unclassified(),
             schemaProjections);
    }

    ProviderDescriptor(String key,
                       Class<T> providerType,
                       ProviderFactory factory,
                       List<String> aliases,
                       Set<String> semanticKeys,
                       ProviderSafetyMetadata safetyMetadata,
                       List<ProviderSchemaProjection<T>> schemaProjections) {
        this.key = Objects.requireNonNull(key, "key must not be null");
        this.providerType = Objects.requireNonNull(providerType, "providerType must not be null");
        this.factory = Objects.requireNonNull(factory, "factory must not be null");
        this.aliases = List.copyOf(Objects.requireNonNull(aliases, "aliases must not be null"));
        this.semanticKeys = Set.copyOf(Objects.requireNonNull(semanticKeys, "semanticKeys must not be null"));
        this.safetyMetadata = Objects.requireNonNull(safetyMetadata, "safetyMetadata must not be null");
        this.schemaProjections = List.copyOf(
            Objects.requireNonNull(schemaProjections, "schemaProjections must not be null"));
    }

    /**
     * Returns the canonical provider key.
     *
     * @return canonical provider key
     */
    public String getKey() {
        return key;
    }

    /**
     * Returns the provider implementation type produced by {@link #create(GeneratorConfig)}.
     *
     * @return declared provider implementation type
     */
    public Class<T> getProviderType() {
        return providerType;
    }

    /**
     * Returns immutable provider aliases.
     *
     * @return aliases that resolve to {@link #getKey()}
     */
    public List<String> getAliases() {
        return aliases;
    }

    /**
     * Returns object-field semantic keys that resolve to this provider.
     *
     * @return immutable semantic-key set
     */
    public Set<String> getSemanticKeys() {
        return semanticKeys;
    }

    /**
     * Returns conservative validity and test-safety claims for this provider.
     *
     * <p>The metadata does not authorize external use of generated values. A
     * {@link ProviderTestSafety#CONFIGURATION_DEPENDENT} value requires inspecting the selected
     * {@link GeneratorConfig} policy.
     *
     * @return immutable provider safety metadata
     */
    public ProviderSafetyMetadata getSafetyMetadata() {
        return safetyMetadata;
    }

    /**
     * Returns schema references that this provider projects.
     *
     * @return immutable schema projections
     */
    public List<ProviderSchemaProjection<T>> getSchemaProjections() {
        return schemaProjections;
    }

    /**
     * Creates a provider using the supplied configuration.
     *
     * @param config configuration propagated to the provider factory
     * @return typed provider instance
     * @throws ClassCastException if the factory violates this descriptor's declared type
     */
    public T create(GeneratorConfig config) {
        Object provider = Objects.requireNonNull(factory.create(config), "provider factory must not return null");
        return providerType.cast(provider);
    }

    ProviderDescriptor<T> withSchemaProjections(List<ProviderSchemaProjection<T>> projections) {
        return new ProviderDescriptor<>(key,
                                        providerType,
                                        factory,
                                        aliases,
                                        semanticKeys,
                                        safetyMetadata,
                                        projections);
    }

    ProviderDescriptor<T> withSafetyMetadata(ProviderSafetyMetadata metadata) {
        return new ProviderDescriptor<>(key,
                                        providerType,
                                        factory,
                                        aliases,
                                        semanticKeys,
                                        metadata,
                                        schemaProjections);
    }

    /**
     * Builder for one metadata-complete provider contribution.
     *
     * @param <T> provider implementation type
     */
    public static final class Builder<T> {

        private final String key;
        private final Class<T> providerType;
        private final ProviderFactory factory;
        private final Set<String> aliases = new LinkedHashSet<>();
        private final Set<String> semanticKeys = new LinkedHashSet<>();
        private final List<ProviderSchemaProjection<T>> schemaProjections = new ArrayList<>();
        private ProviderSafetyMetadata safetyMetadata = ProviderSafetyMetadata.unclassified();

        private Builder(String key, Class<T> providerType, ProviderFactory factory) {
            this.key = requireName("key", key);
            this.providerType = Objects.requireNonNull(providerType, "providerType must not be null");
            this.factory = Objects.requireNonNull(factory, "factory must not be null");
        }

        /**
         * Adds provider lookup aliases.
         *
         * @param values aliases
         * @return this builder
         */
        public Builder<T> aliases(String... values) {
            addNames(aliases, "alias", values);
            return this;
        }

        /**
         * Adds semantic object-field keys backed by this provider.
         *
         * @param values semantic keys
         * @return this builder
         */
        public Builder<T> semanticKeys(String... values) {
            addNames(semanticKeys, "semanticKey", values);
            return this;
        }

        /**
         * Sets provider-level validity and test-safety metadata.
         *
         * @param metadata safety metadata
         * @return this builder
         */
        public Builder<T> safetyMetadata(ProviderSafetyMetadata metadata) {
            this.safetyMetadata = Objects.requireNonNull(metadata, "metadata must not be null");
            return this;
        }

        /**
         * Adds a typed schema projection.
         *
         * @param projection schema projection
         * @return this builder
         */
        public Builder<T> schemaProjection(ProviderSchemaProjection<T> projection) {
            schemaProjections.add(Objects.requireNonNull(projection, "projection must not be null"));
            return this;
        }

        /**
         * Builds the immutable descriptor.
         *
         * @return provider descriptor
         */
        public ProviderDescriptor<T> build() {
            if (aliases.contains(key)) {
                throw new IllegalArgumentException("alias duplicates provider key: " + key);
            }
            List<ProviderSchemaProjection<T>> projections = schemaProjections.stream()
                .map(projection -> projection.getSafetyMetadata().isUnclassified() && !safetyMetadata.isUnclassified()
                                   ? projection.withSafetyMetadata(safetyMetadata)
                                   : projection)
                .toList();
            return new ProviderDescriptor<>(key,
                                            providerType,
                                            factory,
                                            List.copyOf(aliases),
                                            Set.copyOf(semanticKeys),
                                            safetyMetadata,
                                            projections);
        }

        private static void addNames(Set<String> target, String name, String... values) {
            Objects.requireNonNull(values, name + "s must not be null");
            for (String value : values) {
                String normalized = requireName(name, value);
                if (!target.add(normalized)) {
                    throw new IllegalArgumentException(name + " is duplicated: " + normalized);
                }
            }
        }

        private static String requireName(String name, String value) {
            Objects.requireNonNull(value, name + " must not be null");
            String normalized = value.trim().toLowerCase(java.util.Locale.ROOT);
            if (normalized.isEmpty() || normalized.indexOf('\n') >= 0 || normalized.indexOf('\r') >= 0) {
                throw new IllegalArgumentException(name + " must be a non-blank single-line value");
            }
            return normalized;
        }
    }
}
