/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.provider;

import io.github.frikit.krandom.generator.GeneratorConfig;

import java.util.List;
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
}
