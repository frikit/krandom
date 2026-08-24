/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.extension;

import io.github.frikit.krandom.generator.object.SemanticFieldRegistry;
import io.github.frikit.krandom.generator.provider.ProviderCatalog;
import io.github.frikit.krandom.generator.provider.ProviderDescriptor;
import io.github.frikit.krandom.generator.provider.ProviderSchemaProjection;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Immutable result of resolving the modules installed in one generator configuration.
 */
public final class KRandomExtensionRegistry {

    private static final KRandomExtensionRegistry EMPTY =
        new KRandomExtensionRegistry(List.of(), List.of(), Map.of());

    private final List<String> moduleIds;
    private final List<ProviderDescriptor<?>> providerDescriptors;
    private final Map<String, Set<String>> semanticAliases;

    private KRandomExtensionRegistry(List<String> moduleIds,
                                     List<ProviderDescriptor<?>> providerDescriptors,
                                     Map<String, Set<String>> semanticAliases) {
        this.moduleIds = List.copyOf(moduleIds);
        this.providerDescriptors = List.copyOf(providerDescriptors);
        Map<String, Set<String>> copiedAliases = new LinkedHashMap<>();
        semanticAliases.forEach((key, value) -> copiedAliases.put(key, Set.copyOf(value)));
        this.semanticAliases = Collections.unmodifiableMap(copiedAliases);
    }

    /**
     * Resolves modules in installation order and rejects all name collisions.
     *
     * <p>This factory is public so configuration adapters can preserve the same deterministic
     * contract. Applications normally obtain the registry from their {@code GeneratorConfig}.
     *
     * @param modules modules to resolve
     * @return immutable extension registry
     */
    public static KRandomExtensionRegistry resolve(List<? extends KRandomModule> modules) {
        Objects.requireNonNull(modules, "modules must not be null");
        if (modules.isEmpty()) {
            return EMPTY;
        }
        Collector collector = new Collector();
        Set<String> moduleIds = new LinkedHashSet<>();
        for (KRandomModule module : modules) {
            KRandomModule contribution = Objects.requireNonNull(module, "module must not be null");
            String id = requireModuleId(contribution.id());
            if (!moduleIds.add(id)) {
                throw new IllegalArgumentException("Module already installed: " + id);
            }
            collector.currentModuleId = id;
            contribution.configure(collector);
        }
        return new KRandomExtensionRegistry(List.copyOf(moduleIds), collector.providers, collector.semanticAliases);
    }

    /**
     * Returns module identifiers in deterministic installation order.
     *
     * @return immutable module identifiers
     */
    public List<String> getModuleIds() {
        return moduleIds;
    }

    /**
     * Returns contributed providers in deterministic registration order.
     *
     * @return immutable provider descriptors
     */
    public List<ProviderDescriptor<?>> getProviderDescriptors() {
        return providerDescriptors;
    }

    /**
     * Applies contributed semantic provider mappings and aliases to a base registry.
     *
     * @param base base semantic registry
     * @return registry containing the module contributions
     */
    public SemanticFieldRegistry applyTo(SemanticFieldRegistry base) {
        SemanticFieldRegistry source = Objects.requireNonNull(base, "base must not be null");
        if (providerDescriptors.isEmpty() && semanticAliases.isEmpty()) {
            return source;
        }
        SemanticFieldRegistry.Builder builder = source.toBuilder();
        Map<String, String> contributedProviders = new LinkedHashMap<>();
        Map<String, String> contributedAliases = new LinkedHashMap<>();
        for (ProviderDescriptor<?> descriptor : providerDescriptors) {
            for (String semanticKey : descriptor.getSemanticKeys()) {
                String normalizedKey = normalizeSemantic(semanticKey);
                String existingProvider = source.semanticProviderNameFor(semanticKey);
                if (existingProvider != null && !existingProvider.equals(descriptor.getKey())) {
                    throw new IllegalArgumentException(
                        "Semantic key '" + semanticKey + "' already uses provider '" + existingProvider + "'");
                }
                String contributedProvider = contributedProviders.putIfAbsent(normalizedKey, descriptor.getKey());
                if (contributedProvider != null && !contributedProvider.equals(descriptor.getKey())) {
                    throw new IllegalArgumentException(
                        "Semantic key '" + semanticKey + "' is contributed by multiple providers");
                }
                builder.provider(semanticKey, descriptor.getKey());
                addAlias(builder, source, contributedAliases, semanticKey, semanticKey);
            }
        }
        semanticAliases.forEach((semanticKey, aliases) -> aliases.forEach(alias ->
            addAlias(builder, source, contributedAliases, semanticKey, alias)));
        return builder.build();
    }

    private static void addAlias(SemanticFieldRegistry.Builder builder,
                                 SemanticFieldRegistry source,
                                 Map<String, String> contributedAliases,
                                 String semanticKey,
                                 String alias) {
        String existingKey = source.semanticKeyForFieldName(alias);
        String normalizedKey = normalizeSemantic(semanticKey);
        if (existingKey != null && !existingKey.equals(normalizedKey)) {
            throw new IllegalArgumentException(
                "Semantic alias '" + alias + "' already resolves to key '" + existingKey + "'");
        }
        String normalizedAlias = normalizeSemantic(alias);
        String contributedKey = contributedAliases.putIfAbsent(normalizedAlias, normalizedKey);
        if (contributedKey != null && !contributedKey.equals(normalizedKey)) {
            throw new IllegalArgumentException(
                "Semantic alias '" + alias + "' is contributed for multiple keys");
        }
        builder.alias(semanticKey, alias);
    }

    private static String requireModuleId(String id) {
        Objects.requireNonNull(id, "module id must not be null");
        String normalized = id.trim();
        if (normalized.isEmpty() || normalized.indexOf('\n') >= 0 || normalized.indexOf('\r') >= 0) {
            throw new IllegalArgumentException("module id must be a non-blank single-line value");
        }
        return normalized;
    }

    private static String normalizeName(String name) {
        return Objects.requireNonNull(name, "name must not be null").trim().toLowerCase(Locale.ROOT);
    }

    private static String normalizeSemantic(String value) {
        StringBuilder normalized = new StringBuilder(value.length());
        for (int i = 0; i < value.length(); i++) {
            char ch = value.charAt(i);
            if (Character.isLetterOrDigit(ch)) {
                normalized.append(Character.toLowerCase(ch));
            }
        }
        return normalized.toString();
    }

    private static final class Collector implements KRandomModuleContext {

        private final List<ProviderDescriptor<?>> providers = new ArrayList<>();
        private final Map<String, Set<String>> semanticAliases = new LinkedHashMap<>();
        private final Set<String> providerNames = builtInProviderNames();
        private final Set<String> schemaNames = builtInSchemaNames();
        private String currentModuleId = "<unknown>";

        @Override
        public void registerProvider(ProviderDescriptor<?> descriptor) {
            ProviderDescriptor<?> contribution = Objects.requireNonNull(descriptor, "descriptor must not be null");
            addUnique(providerNames, contribution.getKey(), "provider");
            contribution.getAliases().forEach(alias -> addUnique(providerNames, alias, "provider"));
            for (ProviderSchemaProjection<?> projection : contribution.getSchemaProjections()) {
                addUnique(schemaNames, projection.getReference(), "schema reference");
                projection.getAliases().forEach(alias -> addUnique(schemaNames, alias, "schema reference"));
            }
            providers.add(contribution);
        }

        @Override
        public void registerSemanticAliases(String semanticKey, String... fieldNames) {
            String key = normalizeSemantic(Objects.requireNonNull(semanticKey, "semanticKey must not be null"));
            if (key.isEmpty()) {
                throw new IllegalArgumentException("semanticKey must contain at least one letter or digit");
            }
            Objects.requireNonNull(fieldNames, "fieldNames must not be null");
            if (fieldNames.length == 0) {
                throw new IllegalArgumentException("fieldNames must not be empty");
            }
            Set<String> aliases = semanticAliases.computeIfAbsent(key, ignored -> new LinkedHashSet<>());
            for (String fieldName : fieldNames) {
                String alias = normalizeSemantic(Objects.requireNonNull(fieldName, "fieldName must not be null"));
                if (alias.isEmpty()) {
                    throw new IllegalArgumentException("fieldName must contain at least one letter or digit");
                }
                semanticAliases.forEach((existingKey, existingAliases) -> {
                    if (!existingKey.equals(key) && existingAliases.contains(alias)) {
                        throw new IllegalArgumentException(
                            "Semantic alias '" + fieldName + "' is contributed for multiple keys");
                    }
                });
                aliases.add(alias);
            }
        }

        private void addUnique(Set<String> names, String name, String kind) {
            String normalized = normalizeName(name);
            if (!names.add(normalized)) {
                throw new IllegalArgumentException(
                    "Module '" + currentModuleId + "' conflicts with existing " + kind + " name: " + normalized);
            }
        }

        private static Set<String> builtInProviderNames() {
            Set<String> names = new LinkedHashSet<>();
            for (ProviderDescriptor<?> descriptor : ProviderCatalog.builtIns()) {
                names.add(normalizeName(descriptor.getKey()));
                descriptor.getAliases().forEach(alias -> names.add(normalizeName(alias)));
            }
            return names;
        }

        private static Set<String> builtInSchemaNames() {
            Set<String> names = new LinkedHashSet<>();
            for (ProviderDescriptor<?> descriptor : ProviderCatalog.schemaBuiltIns()) {
                for (ProviderSchemaProjection<?> projection : descriptor.getSchemaProjections()) {
                    names.add(normalizeName(projection.getReference()));
                    projection.getAliases().forEach(alias -> names.add(normalizeName(alias)));
                }
            }
            return names;
        }
    }
}
