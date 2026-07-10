/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.provider;

import io.github.frikit.krandom.generator.Generator;
import io.github.frikit.krandom.generator.GeneratorConfig;
import io.github.frikit.krandom.generator.GeneratorProfile;
import io.github.frikit.krandom.generator.selection.UniqueGenerator;

import java.util.function.BiPredicate;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Mimesis-style generic provider hub with alias-based lookup and runtime registration.
 *
 * <p><b>Thread safety:</b> lookups and registrations are backed by concurrent maps, so a hub
 * instance may be shared across threads. A single registration is atomic under
 * {@link ConflictPolicy#FAIL}; the cross-map validation in
 * {@link #registerAlias(String, String, ConflictPolicy)} is best-effort when providers and
 * aliases are registered concurrently. For fully deterministic setups, complete all
 * registration before sharing the hub across threads.
 */
public final class ProviderHub {

    private final GeneratorConfig              config;
    private final GeneratorProfile             profile;
    private final Map<String, ProviderFactory> providers = new ConcurrentHashMap<>();
    private final Map<String, String>          aliases   = new ConcurrentHashMap<>();

    /**
     * Creates provider hub with default configuration.
     */
    public ProviderHub() {
        this(GeneratorConfig.defaults(), GeneratorProfile.REALISTIC);
    }

    /**
     * Creates provider hub for a specific locale.
     *
     * @param locale locale for locale-aware providers
     */
    public ProviderHub(Locale locale) {
        this(GeneratorConfig.builder().locale(locale).build(), GeneratorProfile.REALISTIC);
    }

    /**
     * Creates provider hub with explicit generator config.
     *
     * @param config generator config propagated to providers
     */
    public ProviderHub(GeneratorConfig config) {
        this(config, GeneratorProfile.REALISTIC);
    }

    /**
     * Creates provider hub from a named configuration profile.
     *
     * @param profile profile template used to construct default config
     */
    public ProviderHub(GeneratorProfile profile) {
        this(Objects.requireNonNull(profile, "profile must not be null").createConfig(), profile);
    }

    /**
     * Creates provider hub for locale using a named profile.
     *
     * @param locale  locale for locale-aware providers
     * @param profile profile template used to construct config
     */
    public ProviderHub(Locale locale, GeneratorProfile profile) {
        this(Objects.requireNonNull(profile, "profile must not be null")
                    .createConfig(Objects.requireNonNull(locale, "locale must not be null")),
             profile);
    }

    /**
     * Creates provider hub with explicit config and profile metadata.
     *
     * @param config  generator config propagated to providers
     * @param profile profile metadata for profile-aware extensions
     */
    public ProviderHub(GeneratorConfig config, GeneratorProfile profile) {
        this.config = Objects.requireNonNull(config, "config must not be null");
        this.profile = Objects.requireNonNull(profile, "profile must not be null");
        registerBuiltIns();
    }

    private static String normalize(String name) {
        Objects.requireNonNull(name, "name must not be null");
        String key = name.trim().toLowerCase(Locale.ROOT);
        if (key.isBlank()) {
            throw new IllegalArgumentException("name must not be blank");
        }
        return key;
    }

    /**
     * Resolves provider by name or alias.
     *
     * @param name provider name or alias
     * @return provider instance
     */
    public Object get(String name) {
        String canonical = resolveName(name);
        ProviderFactory factory = providers.get(canonical);
        return factory.create(config);
    }

    /**
     * Resolves typed provider by name or alias.
     *
     * @param name provider name or alias
     * @param type expected type
     * @param <T>  expected provider type
     * @return typed provider instance
     */
    public <T> T get(String name, Class<T> type) {
        Objects.requireNonNull(type, "type must not be null");
        Object provider = get(name);
        if (!type.isInstance(provider)) {
            throw new IllegalArgumentException("Provider '" + name + "' is "
                                               + provider.getClass().getName() + ", not " + type.getName());
        }
        return type.cast(provider);
    }

    /**
     * Checks whether provider name or alias is supported.
     *
     * @param name provider name or alias
     * @return true if supported
     */
    public boolean has(String name) {
        String key = normalize(name);
        return providers.containsKey(key) || aliases.containsKey(key);
    }

    /**
     * Registers provider factory using {@link ConflictPolicy#FAIL}.
     *
     * @param name    canonical provider name
     * @param factory provider factory
     */
    public void register(String name, ProviderFactory factory) {
        register(name, factory, ConflictPolicy.FAIL);
    }

    /**
     * Registers profile-aware provider factory using {@link ConflictPolicy#FAIL}.
     *
     * @param name    canonical provider name
     * @param factory profile-aware provider factory
     */
    public void register(String name, ProfiledProviderFactory factory) {
        register(name, factory, ConflictPolicy.FAIL);
    }

    /**
     * Registers provider factory.
     *
     * @param name    canonical provider name
     * @param factory provider factory
     * @param policy  conflict policy
     */
    public void register(String name, ProviderFactory factory, ConflictPolicy policy) {
        String key = normalize(name);
        ProviderFactory value = Objects.requireNonNull(factory, "factory must not be null");
        ConflictPolicy conflictPolicy = Objects.requireNonNull(policy, "policy must not be null");
        if (conflictPolicy == ConflictPolicy.FAIL) {
            if (providers.putIfAbsent(key, value) != null) {
                throw new IllegalArgumentException("Provider already registered: " + key);
            }
            return;
        }
        providers.put(key, value);
    }

    /**
     * Registers profile-aware provider factory.
     *
     * @param name    canonical provider name
     * @param factory profile-aware provider factory
     * @param policy  conflict policy
     */
    public void register(String name, ProfiledProviderFactory factory, ConflictPolicy policy) {
        ProfiledProviderFactory profiledFactory = Objects.requireNonNull(factory, "factory must not be null");
        register(name, cfg -> profiledFactory.create(profile, cfg), policy);
    }

    /**
     * Registers alias using {@link ConflictPolicy#FAIL}.
     *
     * @param alias      alias name
     * @param targetName canonical provider name
     */
    public void registerAlias(String alias, String targetName) {
        registerAlias(alias, targetName, ConflictPolicy.FAIL);
    }

    /**
     * Registers alias.
     *
     * @param alias      alias name
     * @param targetName canonical provider name
     * @param policy     conflict policy
     */
    public void registerAlias(String alias, String targetName, ConflictPolicy policy) {
        String aliasKey = normalize(alias);
        String targetKey = normalize(targetName);
        ConflictPolicy conflictPolicy = Objects.requireNonNull(policy, "policy must not be null");
        if (!providers.containsKey(targetKey)) {
            throw new IllegalArgumentException("Target provider is not registered: " + targetKey);
        }
        if (providers.containsKey(aliasKey) && !aliasKey.equals(targetKey)) {
            throw new IllegalArgumentException("Alias conflicts with canonical provider name: " + aliasKey);
        }
        if (conflictPolicy == ConflictPolicy.FAIL) {
            if (aliases.putIfAbsent(aliasKey, targetKey) != null) {
                throw new IllegalArgumentException("Alias already registered: " + aliasKey);
            }
            return;
        }
        aliases.put(aliasKey, targetKey);
    }

    /**
     * Returns canonical provider names.
     *
     * @return immutable canonical provider names
     */
    public Set<String> providerNames() {
        return Set.copyOf(providers.keySet());
    }

    /**
     * Returns alias mapping.
     *
     * @return immutable alias map (alias -> canonical name)
     */
    public Map<String, String> aliases() {
        return Map.copyOf(aliases);
    }

    /**
     * Returns generator config used by this hub.
     *
     * @return generator config
     */
    public GeneratorConfig getConfig() {
        return config;
    }

    /**
     * Returns profile metadata associated with this hub.
     */
    public GeneratorProfile getProfile() {
        return profile;
    }

    /**
     * Wraps a generator so repeated calls return unique values.
     */
    public <T> UniqueGenerator<T> unique(Generator<T> source) {
        return new UniqueGenerator<>(Objects.requireNonNull(source, "source must not be null"));
    }

    /**
     * Wraps a generator so repeated calls return unique values, with bounded retry attempts.
     */
    public <T> UniqueGenerator<T> unique(Generator<T> source, int maxAttempts) {
        return new UniqueGenerator<>(Objects.requireNonNull(source, "source must not be null"), maxAttempts);
    }

    /**
     * Wraps a generator so repeated calls return comparator-distinct values.
     */
    public <T> UniqueGenerator<T> unique(Generator<T> source, BiPredicate<T, T> comparator) {
        return new UniqueGenerator<>(Objects.requireNonNull(source, "source must not be null"),
                                     Objects.requireNonNull(comparator, "comparator must not be null"));
    }

    private String resolveName(String name) {
        String key = normalize(name);
        if (providers.containsKey(key)) {
            return key;
        }
        String canonical = aliases.get(key);
        if (canonical == null) {
            throw new IllegalArgumentException("Unknown provider '" + name + "'. "
                                               + "Known providers: " + providerNames() + ", aliases: " + aliases.keySet());
        }
        return canonical;
    }

    private void registerBuiltIns() {
        for (ProviderDescriptor<?> descriptor : ProviderCatalog.builtIns()) {
            register(descriptor.getKey(), descriptor::create, ConflictPolicy.REPLACE);
        }
        for (ProviderDescriptor<?> descriptor : ProviderCatalog.builtIns()) {
            for (String alias : descriptor.getAliases()) {
                registerAlias(alias, descriptor.getKey(), ConflictPolicy.REPLACE);
            }
        }
    }
}
