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
 * Registry for locale-specific street-address providers.
 */
public final class StreetAddressDataRegistry {

    private static final Map<String, StreetAddressDataProvider> providers = new ConcurrentHashMap<>();

    static {
        LocaleStreetAddressData.allProviders().forEach(StreetAddressDataRegistry::register);
    }

    private StreetAddressDataRegistry() {
        throw new UnsupportedOperationException("Utility class");
    }

    /**
     * Registers or replaces a provider.
     *
     * @param provider provider to register
     */
    public static void register(StreetAddressDataProvider provider) {
        if (provider == null) {
            throw new NullPointerException("provider must not be null");
        }
        if (provider.getLocale() == null) {
            throw new NullPointerException("provider.getLocale() must not be null");
        }
        validateArray("streetNames", provider.getStreetNames());
        validateArray("streetTypesShort", provider.getStreetTypesShort());
        validateArray("streetTypesLong", provider.getStreetTypesLong());
        providers.put(keyFor(provider.getLocale()), provider);
    }

    /**
     * Returns provider for locale.
     */
    public static StreetAddressDataProvider forLocale(Locale locale) {
        if (locale == null) {
            return null;
        }
        return providers.get(keyFor(locale));
    }

    /**
     * Returns whether locale is registered.
     */
    public static boolean isRegistered(Locale locale) {
        if (locale == null) {
            return false;
        }
        return forLocale(locale) != null;
    }

    /**
     * Returns registered key set.
     */
    public static Set<String> registeredKeys() {
        return Set.copyOf(providers.keySet());
    }

    private static String keyFor(Locale locale) {
        return locale.getLanguage() + "_" + locale.getCountry();
    }

    private static void validateArray(String name, String[] values) {
        if (values == null) {
            throw new NullPointerException(name + " must not be null");
        }
        if (values.length == 0) {
            throw new IllegalArgumentException(name + " must not be empty");
        }
        for (String value : values) {
            if (value == null || value.isBlank()) {
                throw new IllegalArgumentException(name + " must not contain blank values");
            }
        }
    }
}

