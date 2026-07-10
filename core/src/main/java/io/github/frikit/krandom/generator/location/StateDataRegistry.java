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
 * <p>Custom providers registered via {@link #register(StateDataProvider)} override built-in data
 * for the same locale and enable support for additional locales.
 */
public final class StateDataRegistry {

    private static final Map<String, StateDataProvider> providers = new ConcurrentHashMap<>();

    static {
        for (SupportedLocale supportedLocale : SupportedLocale.values()) {
            register(new BuiltInStateDataProvider(supportedLocale));
        }
    }

    private StateDataRegistry() {
        throw new UnsupportedOperationException("Utility class");
    }

    /**
     * Registers or replaces the state data provider for a locale.
     *
     * @deprecated Since 1.6, use
     * {@link io.github.frikit.krandom.generator.DataRegistryContext.Builder#registerStateProvider(StateDataProvider)}
     * for configuration-scoped registration.
     * @param provider the provider; must not be {@code null}
     * @throws NullPointerException if {@code provider} is {@code null}
     */
    @Deprecated(since = "1.6", forRemoval = true)
    public static void register(StateDataProvider provider) {
        Objects.requireNonNull(provider, "provider must not be null");
        Objects.requireNonNull(provider.getLocale(), "provider.getLocale() must not be null");
        validateStates(provider.getStates());
        validateAbbreviations(provider.getAbbreviations());
        RegistryLookup.putWithLanguageFallback(providers, provider.getLocale(), provider);
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

    private static void validateStates(String[] states) {
        Objects.requireNonNull(states, "states must not be null");
        if (states.length == 0) {
            throw new IllegalArgumentException("states must not be empty");
        }
        for (int i = 0; i < states.length; i++) {
            String value = states[i];
            if (value == null || value.isBlank()) {
                throw new IllegalArgumentException("states at index " + i + " must not be blank");
            }
        }
    }

    private static void validateAbbreviations(String[] abbreviations) {
        Objects.requireNonNull(abbreviations, "abbreviations must not be null");
        for (int i = 0; i < abbreviations.length; i++) {
            if (abbreviations[i] == null) {
                throw new IllegalArgumentException("abbreviations at index " + i + " must not be null");
            }
        }
    }

}
