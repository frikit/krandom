/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.location;

import io.github.frikit.krandom.generator.locale.SupportedLocale;

import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Central registry for locale-specific state/province name providers.
 *
 * <p>This registry provides state data for {@link StateGenerator} and supports runtime
 * registration of custom providers to add or override state lists for any locale.
 *
 * <p>Built-in support is auto-loaded at class init from
 * {@link io.github.frikit.krandom.generator.locale.SupportedLocale}.
 *
 * <p>Custom providers registered via {@link io.github.frikit.krandom.generator.DataRegistryContext.Builder} override built-in data
 * for the same locale and enable support for additional locales.
 */
public final class StateDataRegistry {

    private static final Map<String, StateDataProvider> providers = new ConcurrentHashMap<>();

    static {
        for (SupportedLocale supportedLocale : SupportedLocale.values()) {
            registerProvider(new BuiltInStateDataProvider(supportedLocale));
        }
    }

    private StateDataRegistry() {
        throw new UnsupportedOperationException("Utility class");
    }

    /**
     * Returns the provider for the given locale.
     *
     * @param locale the locale
     * @return the provider, or {@code null} if none is registered or locale is null
     */
    public static StateDataProvider forLocale(Locale locale) {
        return RegistryLookup.findWithFallback(providers, locale);
    }

    /**
     * Returns {@code true} if a provider is registered for the given locale.
     *
     * @param locale the locale
     * @return {@code true} if supported, {@code false} if not or if locale is null
     */
    public static boolean isRegistered(Locale locale) {
        return RegistryLookup.containsWithFallback(providers, locale);
    }

    /**
     * Returns the set of all registered locale keys (for diagnostics).
     *
     * @return unmodifiable view of registered keys
     */
    public static Set<String> registeredKeys() {
        return Set.copyOf(providers.keySet());
    }



    private static void registerProvider(StateDataProvider provider) {
        Objects.requireNonNull(provider, "provider must not be null");
        Objects.requireNonNull(provider.getLocale(), "provider.getLocale() must not be null");
        RegistryLookup.putWithLanguageFallback(providers, provider.getLocale(), provider);
    }
}
