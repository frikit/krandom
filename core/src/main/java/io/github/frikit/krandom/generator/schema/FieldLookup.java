/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.schema;

import io.github.frikit.krandom.generator.GeneratorConfig;
import io.github.frikit.krandom.generator.provider.ConflictPolicy;
import io.github.frikit.krandom.generator.provider.ProviderCatalog;
import io.github.frikit.krandom.generator.provider.ProviderDescriptor;
import io.github.frikit.krandom.generator.provider.ProviderSafetyMetadata;
import io.github.frikit.krandom.generator.provider.ProviderSafetyPolicy;
import io.github.frikit.krandom.generator.provider.ProviderFactory;
import io.github.frikit.krandom.generator.provider.ProviderSchemaProjection;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;

/**
 * Resolves string field references to concrete value providers.
 */
public final class FieldLookup {

    private final GeneratorConfig                  config;
    private final Map<String, SchemaValueProvider> providers = new LinkedHashMap<>();
    private final Map<String, String>              aliases   = new LinkedHashMap<>();

    /**
     * Creates a lookup with generators initialized from the provided config.
     *
     * @param config generator config used for locale/seed propagation
     */
    public FieldLookup(GeneratorConfig config) {
        this.config = Objects.requireNonNull(config, "config must not be null");
        registerBuiltIns();
    }

    private static String normalize(String reference) {
        Objects.requireNonNull(reference, "reference must not be null");
        String key = reference.trim().toLowerCase(Locale.ROOT);
        if (key.isEmpty()) {
            throw new IllegalArgumentException("reference must not be blank");
        }
        return key;
    }

    /**
     * Registers a schema reference using {@link ConflictPolicy#FAIL}.
     *
     * @param reference schema token reference
     * @param provider  value provider
     */
    public void register(String reference, SchemaValueProvider provider) {
        register(reference, provider, ConflictPolicy.FAIL);
    }

    /**
     * Registers a schema reference.
     *
     * @param reference schema token reference
     * @param provider  value provider
     * @param policy    conflict policy
     */
    public void register(String reference, SchemaValueProvider provider, ConflictPolicy policy) {
        String key = normalize(reference);
        SchemaValueProvider value = Objects.requireNonNull(provider, "provider must not be null");
        ConflictPolicy conflictPolicy = Objects.requireNonNull(policy, "policy must not be null");
        if ((providers.containsKey(key) || aliases.containsKey(key)) && conflictPolicy == ConflictPolicy.FAIL) {
            throw new IllegalArgumentException("Field reference already registered: " + key);
        }
        aliases.remove(key);
        providers.put(key, value);
    }

    /**
     * Registers a schema reference backed by a provider factory.
     *
     * @param reference      schema token reference
     * @param factory        provider factory
     * @param providerType   expected provider type
     * @param valueExtractor extractor invoked on the provider instance
     * @param <T>            provider type
     */
    public <T> void registerProvider(String reference,
                                     ProviderFactory factory,
                                     Class<T> providerType,
                                     Function<? super T, ?> valueExtractor) {
        registerProvider(reference, factory, providerType, valueExtractor, ConflictPolicy.FAIL);
    }

    /**
     * Registers a schema reference backed by a provider factory.
     *
     * @param reference      schema token reference
     * @param factory        provider factory
     * @param providerType   expected provider type
     * @param valueExtractor extractor invoked on the provider instance
     * @param policy         conflict policy
     * @param <T>            provider type
     */
    public <T> void registerProvider(String reference,
                                     ProviderFactory factory,
                                     Class<T> providerType,
                                     Function<? super T, ?> valueExtractor,
                                     ConflictPolicy policy) {
        registerProvider(reference, factory, providerType, valueExtractor, JsonSchemaSupport.any(), policy);
    }

    /**
     * Registers a schema reference backed by a provider factory with explicit JSON Schema metadata.
     *
     * @param reference      schema token reference
     * @param factory        provider factory
     * @param providerType   expected provider type
     * @param valueExtractor extractor invoked on the provider instance
     * @param jsonSchema     JSON Schema fragment for extracted values
     * @param <T>            provider type
     */
    public <T> void registerProvider(String reference,
                                     ProviderFactory factory,
                                     Class<T> providerType,
                                     Function<? super T, ?> valueExtractor,
                                     Map<String, ?> jsonSchema) {
        registerProvider(reference, factory, providerType, valueExtractor, jsonSchema, ConflictPolicy.FAIL);
    }

    /**
     * Registers a schema reference backed by a provider factory with explicit JSON Schema metadata.
     *
     * @param reference      schema token reference
     * @param factory        provider factory
     * @param providerType   expected provider type
     * @param valueExtractor extractor invoked on the provider instance
     * @param jsonSchema     JSON Schema fragment for extracted values
     * @param policy         conflict policy
     * @param <T>            provider type
     */
    public <T> void registerProvider(String reference,
                                     ProviderFactory factory,
                                     Class<T> providerType,
                                     Function<? super T, ?> valueExtractor,
                                     Map<String, ?> jsonSchema,
                                     ConflictPolicy policy) {
        String key = normalize(reference);
        ProviderFactory providerFactory = Objects.requireNonNull(factory, "factory must not be null");
        Class<T> expectedType = Objects.requireNonNull(providerType, "providerType must not be null");
        Function<? super T, ?> extractor = Objects.requireNonNull(valueExtractor, "valueExtractor must not be null");
        Map<String, ?> schema = Objects.requireNonNull(jsonSchema, "jsonSchema must not be null");
        ConflictPolicy conflictPolicy = Objects.requireNonNull(policy, "policy must not be null");
        if ((providers.containsKey(key) || aliases.containsKey(key)) && conflictPolicy == ConflictPolicy.FAIL) {
            throw new IllegalArgumentException("Field reference already registered: " + key);
        }
        Object provider = Objects.requireNonNull(providerFactory.create(config), "provider factory must not return null");
        if (!expectedType.isInstance(provider)) {
            throw new IllegalArgumentException(
                "Provider for reference '" + reference + "' is "
                + provider.getClass().getName() + ", not " + expectedType.getName());
        }
        T typedProvider = expectedType.cast(provider);
        register(key, ctx -> extractor.apply(typedProvider), schema, conflictPolicy);
    }

    /**
     * Registers a schema reference alias using {@link ConflictPolicy#FAIL}.
     *
     * @param alias           alias token
     * @param targetReference canonical target reference
     */
    public void registerAlias(String alias, String targetReference) {
        registerAlias(alias, targetReference, ConflictPolicy.FAIL);
    }

    /**
     * Registers a schema reference alias.
     *
     * @param alias           alias token
     * @param targetReference canonical target reference
     * @param policy          conflict policy
     */
    public void registerAlias(String alias, String targetReference, ConflictPolicy policy) {
        String aliasKey = normalize(alias);
        String targetKey = normalize(targetReference);
        ConflictPolicy conflictPolicy = Objects.requireNonNull(policy, "policy must not be null");
        if (!providers.containsKey(targetKey)) {
            throw new IllegalArgumentException("Target field reference is not registered: " + targetKey);
        }
        if (providers.containsKey(aliasKey) && !aliasKey.equals(targetKey)) {
            throw new IllegalArgumentException("Alias conflicts with canonical field reference: " + aliasKey);
        }
        if (aliases.containsKey(aliasKey) && conflictPolicy == ConflictPolicy.FAIL) {
            throw new IllegalArgumentException("Field alias already registered: " + aliasKey);
        }
        aliases.put(aliasKey, targetKey);
    }

    /**
     * Checks whether a canonical reference or alias is registered.
     *
     * @param reference reference or alias
     * @return true if supported
     */
    public boolean has(String reference) {
        String key = normalize(reference);
        return providers.containsKey(key) || aliases.containsKey(key);
    }

    /**
     * Resolves a string reference to a provider.
     *
     * @param reference field reference
     * @return resolved value provider
     */
    public SchemaValueProvider resolve(String reference) {
        String canonical = resolveName(reference);
        SchemaValueProvider provider = providers.get(canonical);
        if (provider == null) {
            throw new IllegalArgumentException(
                "Unknown field reference '" + reference + "'. Supported references: " + supportedReferences());
        }
        return provider;
    }

    /**
     * Returns supported string references.
     *
     * @return immutable reference set
     */
    public Set<String> supportedReferences() {
        return Collections.unmodifiableSet(new LinkedHashSet<>(providers.keySet()));
    }

    /**
     * Returns the current alias mapping.
     *
     * @return immutable alias map (alias -&gt; canonical reference)
     */
    public Map<String, String> aliases() {
        return Collections.unmodifiableMap(new LinkedHashMap<>(aliases));
    }

    /**
     * Returns the generator config used by this lookup.
     *
     * @return generator config
     */
    public GeneratorConfig getConfig() {
        return config;
    }

    private String resolveName(String reference) {
        String key = normalize(reference);
        if (providers.containsKey(key)) {
            return key;
        }
        String canonical = aliases.get(key);
        if (canonical == null) {
            throw new IllegalArgumentException(
                "Unknown field reference '" + reference + "'. Supported references: " + supportedReferences()
                + ", aliases: " + aliases.keySet());
        }
        return canonical;
    }

    private void register(String reference,
                          SchemaValueProvider provider,
                          Map<String, ?> jsonSchema,
                          ConflictPolicy policy) {
        register(reference, SchemaValueProvider.withJsonSchema(provider, jsonSchema), policy);
    }

    private void registerBuiltInDescriptor(ProviderDescriptor<?> descriptor) {
        registerBuiltInDescriptorTyped(descriptor);
    }

    private <T> void registerBuiltInDescriptorTyped(ProviderDescriptor<T> descriptor) {
        T provider = descriptor.create(config);
        for (ProviderSchemaProjection<T> projection : descriptor.getSchemaProjections()) {
            register(projection.getReference(),
                     context -> projection.extract(provider, config),
                     jsonSchemaFor(projection),
                     ConflictPolicy.REPLACE);
            for (String alias : projection.getAliases()) {
                registerAlias(alias, projection.getReference(), ConflictPolicy.REPLACE);
            }
        }
    }

    private Map<String, ?> jsonSchemaFor(ProviderSchemaProjection<?> projection) {
        Map<String, Object> schema;
        if (projection.getRecordType().isPresent()) {
            schema = JsonSchemaSupport.record(projection.getRecordType().orElseThrow(), projection.getNullableComponents());
        } else if (projection.isInteger()) {
            schema = JsonSchemaSupport.integer();
        } else {
            schema = projection.getFormat().map(JsonSchemaSupport::stringFormat).orElseGet(JsonSchemaSupport::string);
        }
        return withSafetyMetadata(schema, projection.getSafetyMetadata());
    }

    private Map<String, Object> withSafetyMetadata(Map<String, Object> schema, ProviderSafetyMetadata metadata) {
        if (metadata.safetyPolicy().isEmpty()) {
            return schema;
        }
        ProviderSafetyPolicy safetyPolicy = metadata.safetyPolicy().orElseThrow();
        Map<String, Object> policy = Map.of("setting", safetyPolicy.getSetting(),
                                            "selected", safetyPolicy.selectedValue(config));
        Map<String, Object> safety = new LinkedHashMap<>();
        safety.put("formatValidity", metadata.formatValidity().name());
        safety.put("checksumValidity", metadata.checksumValidity().name());
        safety.put("semanticPlausibility", metadata.semanticPlausibility().name());
        safety.put("testSafety", metadata.testSafety().name());
        safety.put("policy", policy);
        Map<String, Object> extended = new LinkedHashMap<>(schema);
        extended.put("x-krandom-safety", Collections.unmodifiableMap(safety));
        return Collections.unmodifiableMap(extended);
    }

    private void registerBuiltIns() {
        for (ProviderDescriptor<?> descriptor : ProviderCatalog.schemaBuiltIns()) {
            registerBuiltInDescriptor(descriptor);
        }
    }
}
