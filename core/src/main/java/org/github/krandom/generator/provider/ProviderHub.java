/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package org.github.krandom.generator.provider;

import org.github.krandom.generator.GeneratorConfig;
import org.github.krandom.generator.datetime.DateGenerator;
import org.github.krandom.generator.location.StreetAddressGenerator;
import org.github.krandom.generator.network.URLGenerator;
import org.github.krandom.generator.text.WordGenerator;
import org.github.krandom.generator.user.FullNameGenerator;
import org.github.krandom.generator.finance.MoneyGenerator;
import org.github.krandom.generator.identifier.UUIDGenerator;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Mimesis-style generic provider hub with alias-based lookup and runtime registration.
 */
public final class ProviderHub {

    private final GeneratorConfig config;
    private final Map<String, ProviderFactory> providers = new LinkedHashMap<>();
    private final Map<String, String> aliases = new LinkedHashMap<>();

    /**
     * Creates provider hub with default configuration.
     */
    public ProviderHub() {
        this(GeneratorConfig.defaults());
    }

    /**
     * Creates provider hub for a specific locale.
     *
     * @param locale locale for locale-aware providers
     */
    public ProviderHub(Locale locale) {
        this(GeneratorConfig.builder().locale(locale).build());
    }

    /**
     * Creates provider hub with explicit generator config.
     *
     * @param config generator config propagated to providers
     */
    public ProviderHub(GeneratorConfig config) {
        this.config = Objects.requireNonNull(config, "config must not be null");
        registerBuiltIns();
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
     * @param <T> expected provider type
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
     * @param name canonical provider name
     * @param factory provider factory
     */
    public void register(String name, ProviderFactory factory) {
        register(name, factory, ConflictPolicy.FAIL);
    }

    /**
     * Registers provider factory.
     *
     * @param name canonical provider name
     * @param factory provider factory
     * @param policy conflict policy
     */
    public void register(String name, ProviderFactory factory, ConflictPolicy policy) {
        String key = normalize(name);
        ProviderFactory value = Objects.requireNonNull(factory, "factory must not be null");
        ConflictPolicy conflictPolicy = Objects.requireNonNull(policy, "policy must not be null");
        if (providers.containsKey(key) && conflictPolicy == ConflictPolicy.FAIL) {
            throw new IllegalArgumentException("Provider already registered: " + key);
        }
        providers.put(key, value);
    }

    /**
     * Registers alias using {@link ConflictPolicy#FAIL}.
     *
     * @param alias alias name
     * @param targetName canonical provider name
     */
    public void registerAlias(String alias, String targetName) {
        registerAlias(alias, targetName, ConflictPolicy.FAIL);
    }

    /**
     * Registers alias.
     *
     * @param alias alias name
     * @param targetName canonical provider name
     * @param policy conflict policy
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
        if (aliases.containsKey(aliasKey) && conflictPolicy == ConflictPolicy.FAIL) {
            throw new IllegalArgumentException("Alias already registered: " + aliasKey);
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

    private static String normalize(String name) {
        Objects.requireNonNull(name, "name must not be null");
        String key = name.trim().toLowerCase(Locale.ROOT);
        if (key.isBlank()) {
            throw new IllegalArgumentException("name must not be blank");
        }
        return key;
    }

    private void registerBuiltIns() {
        register("person", cfg -> new FullNameGenerator(cfg), ConflictPolicy.REPLACE);
        register("address", cfg -> new StreetAddressGenerator(cfg), ConflictPolicy.REPLACE);
        register("internet", cfg -> new URLGenerator(cfg), ConflictPolicy.REPLACE);
        register("finance", cfg -> new MoneyGenerator(cfg), ConflictPolicy.REPLACE);
        register("datetime", cfg -> new DateGenerator(cfg), ConflictPolicy.REPLACE);
        register("text", cfg -> new WordGenerator(cfg), ConflictPolicy.REPLACE);
        register("code", cfg -> new UUIDGenerator(cfg), ConflictPolicy.REPLACE);

        registerAlias("name", "person", ConflictPolicy.REPLACE);
        registerAlias("full_name", "person", ConflictPolicy.REPLACE);
        registerAlias("location", "address", ConflictPolicy.REPLACE);
        registerAlias("network", "internet", ConflictPolicy.REPLACE);
        registerAlias("url", "internet", ConflictPolicy.REPLACE);
        registerAlias("money", "finance", ConflictPolicy.REPLACE);
        registerAlias("currency", "finance", ConflictPolicy.REPLACE);
        registerAlias("date", "datetime", ConflictPolicy.REPLACE);
        registerAlias("time", "datetime", ConflictPolicy.REPLACE);
        registerAlias("word", "text", ConflictPolicy.REPLACE);
        registerAlias("sentence", "text", ConflictPolicy.REPLACE);
        registerAlias("uuid", "code", ConflictPolicy.REPLACE);
        registerAlias("identifier", "code", ConflictPolicy.REPLACE);
    }
}
