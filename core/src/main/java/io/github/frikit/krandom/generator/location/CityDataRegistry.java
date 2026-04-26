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
 * Central registry for locale-specific city name providers.
 *
 * <p>This registry provides city data for {@link CityGenerator} and supports runtime
 * registration of custom providers to add or override city lists for any locale.
 *
 * <p>Built-in support is auto-loaded at class init from
 * {@link io.github.frikit.krandom.generator.locale.SupportedLocale}.
 *
 * <p>Custom providers registered via {@link #register(CityDataProvider)} override built-in data
 * for the same locale and enable support for additional locales.
 */
public final class CityDataRegistry {

    private static final Map<String, CityDataProvider> providers = new ConcurrentHashMap<>();

    static {
        for (SupportedLocale supportedLocale : SupportedLocale.values()) {
            register(new BuiltInCityDataProvider(supportedLocale));
        }
    }

    private CityDataRegistry() {
        throw new UnsupportedOperationException("Utility class");
    }

    /**
     * Registers or replaces the city data provider for a locale.
     *
     * @param provider the provider; must not be {@code null}
     * @throws NullPointerException if {@code provider} is {@code null}
     */
    public static void register(CityDataProvider provider) {
        Objects.requireNonNull(provider, "provider must not be null");
        Objects.requireNonNull(provider.getLocale(), "provider.getLocale() must not be null");
        validateArray("cities", provider.getCities());
        RegistryLookup.putWithLanguageFallback(providers, provider.getLocale(), provider);
    }

    /**
     * Returns the provider for the given locale.
     *
     * @param locale the locale
     * @return the provider, or {@code null} if none is registered or locale is null
     */
    public static CityDataProvider forLocale(Locale locale) {
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

    private static void validateArray(String name, String[] values) {
        Objects.requireNonNull(values, name + " must not be null");
        if (values.length == 0) {
            throw new IllegalArgumentException(name + " must not be empty");
        }
        for (int i = 0; i < values.length; i++) {
            String value = values[i];
            if (value == null || value.isBlank()) {
                throw new IllegalArgumentException(name + " at index " + i + " must not be blank");
            }
        }
    }

}
