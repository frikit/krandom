/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.location;

import io.github.frikit.krandom.generator.locale.SupportedLocale;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.jspecify.annotations.Nullable;

/**
 * Global, read-only registry mapping locales to {@link AddressPurposeDataProvider} instances.
 *
 * <p>Seeded at class-load time with every built-in
 * {@link io.github.frikit.krandom.generator.locale.SupportedLocale}; regional variants use the dataset
 * their {@code resourcePrefix()} names. Other locales are not registered, so address purposes fall back
 * to the bundled English {@code default.txt} data.
 *
 * <p><b>Lookup order:</b> exact {@code language_COUNTRY} match, then language-only match, then
 * {@code null}.
 */
public final class AddressPurposeDataRegistry {

    private static final ConcurrentHashMap<String, AddressPurposeDataProvider> REGISTRY = new ConcurrentHashMap<>();

    static {
        for (SupportedLocale supportedLocale : SupportedLocale.values()) {
            putProvider(new BuiltInAddressPurposeDataProvider(supportedLocale));
        }
    }

    private AddressPurposeDataRegistry() {}

    /**
     * Returns {@code true} if the registry contains an entry for the given locale.
     *
     * @param locale locale to look up; may be {@code null}
     * @return whether an exact or language-level entry exists
     */
    public static boolean isRegistered(@Nullable Locale locale) {
        if (locale == null) {
            return false;
        }
        String lang = locale.getLanguage();
        String country = locale.getCountry();
        if (!country.isEmpty() && REGISTRY.containsKey(lang + "_" + country)) {
            return true;
        }
        return REGISTRY.containsKey(lang);
    }

    /**
     * Returns the best-matching provider for the given locale, or {@code null} if none is registered.
     *
     * @param locale locale to look up; may be {@code null}
     * @return matching provider, or {@code null}
     */
    public static @Nullable AddressPurposeDataProvider forLocale(@Nullable Locale locale) {
        if (locale == null) {
            return null;
        }
        String lang = locale.getLanguage();
        String country = locale.getCountry();
        if (!country.isEmpty()) {
            AddressPurposeDataProvider exact = REGISTRY.get(lang + "_" + country);
            if (exact != null) {
                return exact;
            }
        }
        return REGISTRY.get(lang);
    }

    /**
     * Returns an unmodifiable snapshot of all currently registered locale keys.
     *
     * @return registered {@code language} and {@code language_COUNTRY} keys
     */
    public static Set<String> registeredKeys() {
        return Set.copyOf(REGISTRY.keySet());
    }

    private static void putProvider(AddressPurposeDataProvider provider) {
        String lang = provider.getLocale().getLanguage();
        String country = provider.getLocale().getCountry();
        REGISTRY.put(lang + "_" + country, provider);
        REGISTRY.putIfAbsent(lang, provider);
    }
}
