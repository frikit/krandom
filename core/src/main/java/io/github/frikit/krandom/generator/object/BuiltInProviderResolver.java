/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.object;

import io.github.frikit.krandom.generator.Generator;
import io.github.frikit.krandom.generator.GeneratorConfig;
import io.github.frikit.krandom.generator.provider.ProviderCatalog;
import io.github.frikit.krandom.generator.provider.ProviderDescriptor;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Immutable index for the built-in provider catalog used by semantic object generation.
 *
 * <p>Unlike {@code ProviderHub}, this resolver has no runtime registration surface and therefore
 * does not rebuild concurrent maps for every generated object.
 */
final class BuiltInProviderResolver {

    private static final Map<String, ProviderDescriptor<?>> PROVIDERS_BY_NAME = buildProvidersByName();

    private BuiltInProviderResolver() {
    }

    static Generator<?> generator(String name, GeneratorConfig config) {
        return provider(name, config, Generator.class);
    }

    static <T> T provider(String name, GeneratorConfig config, Class<T> expectedType) {
        Objects.requireNonNull(name, "name must not be null");
        Objects.requireNonNull(config, "config must not be null");
        Objects.requireNonNull(expectedType, "expectedType must not be null");
        ProviderDescriptor<?> descriptor = PROVIDERS_BY_NAME.get(name);
        if (descriptor == null) {
            throw new IllegalArgumentException("Unknown built-in provider '" + name + "'");
        }
        return expectedType.cast(descriptor.create(config));
    }

    private static Map<String, ProviderDescriptor<?>> buildProvidersByName() {
        Map<String, ProviderDescriptor<?>> providers = new HashMap<>();
        for (ProviderDescriptor<?> descriptor : ProviderCatalog.builtIns()) {
            providers.put(descriptor.getKey(), descriptor);
            for (String alias : descriptor.getAliases()) {
                providers.put(alias, descriptor);
            }
        }
        return Map.copyOf(providers);
    }
}
