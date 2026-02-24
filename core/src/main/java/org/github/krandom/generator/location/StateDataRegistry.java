/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package org.github.krandom.generator.location;

import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Central registry for locale-specific state/province name providers.
 *
 * <p>This registry provides state data for {@link StateGenerator} and supports runtime
 * registration of custom providers to add or override state lists for any locale.
 *
 * <p>Built-in support for 6 locales is auto-loaded at class init: {@code en_US}, {@code en_CA},
 * {@code en_AU}, {@code de_DE}, {@code es_MX}, {@code it_IT}.
 *
 * <p>Custom providers registered via {@link #register(StateDataProvider)} override built-in data
 * for the same locale and enable support for additional locales.
 */
public final class StateDataRegistry {

    private static final Map<String, StateDataProvider> providers = new ConcurrentHashMap<>();

    static {
        LocaleStateData.allProviders().forEach(StateDataRegistry::register);
    }

    private StateDataRegistry() {
        throw new UnsupportedOperationException("Utility class");
    }

    /**
     * Registers or replaces the state data provider for a locale.
     *
     * @param provider the provider; must not be {@code null}
     * @throws NullPointerException if {@code provider} is {@code null}
     */
    public static void register(StateDataProvider provider) {
        if (provider == null) {
            throw new NullPointerException("provider must not be null");
        }
        providers.put(keyFor(provider.getLocale()), provider);
    }

    /**
     * Returns the provider for the given locale.
     *
     * @param locale the locale
     * @return the provider, or {@code null} if none is registered or locale is null
     */
    public static StateDataProvider forLocale(Locale locale) {
        if (locale == null) {
            return null;
        }
        return providers.get(keyFor(locale));
    }

    /**
     * Returns {@code true} if a provider is registered for the given locale.
     *
     * @param locale the locale
     * @return {@code true} if supported, {@code false} if not or if locale is null
     */
    public static boolean isRegistered(Locale locale) {
        if (locale == null) {
            return false;
        }
        return forLocale(locale) != null;
    }

    /**
     * Returns the set of all registered locale keys (for diagnostics).
     *
     * @return unmodifiable view of registered keys
     */
    public static Set<String> registeredKeys() {
        return Set.copyOf(providers.keySet());
    }

    private static String keyFor(Locale locale) {
        return locale.getLanguage() + "_" + locale.getCountry();
    }
}
