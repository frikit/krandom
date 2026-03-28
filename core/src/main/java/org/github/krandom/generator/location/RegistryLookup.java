/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package org.github.krandom.generator.location;

import java.util.Locale;
import java.util.Map;
import java.util.Objects;

final class RegistryLookup {

    private RegistryLookup() {
        throw new UnsupportedOperationException("Utility class");
    }

    static <T> void putWithLanguageFallback(Map<String, T> registry, Locale locale, T provider) {
        Objects.requireNonNull(registry, "registry");
        Objects.requireNonNull(locale, "locale");
        Objects.requireNonNull(provider, "provider");

        String language = locale.getLanguage();
        String country = locale.getCountry();
        if (country.isEmpty()) {
            registry.put(language, provider);
        } else {
            registry.put(language + "_" + country, provider);
            registry.putIfAbsent(language, provider);
        }
    }

    static <T> T findWithFallback(Map<String, T> registry, Locale locale) {
        if (locale == null) {
            return null;
        }
        String language = locale.getLanguage();
        String country = locale.getCountry();
        if (!country.isEmpty()) {
            T exact = registry.get(language + "_" + country);
            if (exact != null) {
                return exact;
            }
        }
        return registry.get(language);
    }

    static boolean containsWithFallback(Map<String, ?> registry, Locale locale) {
        if (locale == null) {
            return false;
        }
        String language = locale.getLanguage();
        String country = locale.getCountry();
        if (!country.isEmpty() && registry.containsKey(language + "_" + country)) {
            return true;
        }
        return registry.containsKey(language);
    }
}
