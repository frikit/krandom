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
 * Registry for locale-specific street-address providers.
 */
public final class StreetAddressDataRegistry {

    private static final Map<String, StreetAddressDataProvider> providers = new ConcurrentHashMap<>();

    static {
        for (SupportedLocale supportedLocale : SupportedLocale.values()) {
            registerProvider(new BuiltInStreetAddressDataProvider(supportedLocale));
        }
    }

    private StreetAddressDataRegistry() {
        throw new UnsupportedOperationException("Utility class");
    }

    /**
     * Returns provider for locale.
     */
    public static StreetAddressDataProvider forLocale(Locale locale) {
        return RegistryLookup.findWithFallback(providers, locale);
    }

    /**
     * Returns whether locale is registered.
     */
    public static boolean isRegistered(Locale locale) {
        return RegistryLookup.containsWithFallback(providers, locale);
    }

    /**
     * Returns registered key set.
     */
    public static Set<String> registeredKeys() {
        return Set.copyOf(providers.keySet());
    }


    private static void registerProvider(StreetAddressDataProvider provider) {
        Objects.requireNonNull(provider, "provider must not be null");
        Objects.requireNonNull(provider.getLocale(), "provider.getLocale() must not be null");
        RegistryLookup.putWithLanguageFallback(providers, provider.getLocale(), provider);
    }
}
