/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.user;

import io.github.frikit.krandom.generator.DataRegistryContext;
import io.github.frikit.krandom.generator.locale.SupportedLocale;
import org.jspecify.annotations.Nullable;

import java.util.Locale;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Global registry mapping locales to {@link BloodTypeDataProvider} instances.
 *
 * <p>At class-load time it is seeded with every built-in
 * {@link io.github.frikit.krandom.generator.locale.SupportedLocale} that <em>has</em> a
 * {@code krandom/bloodtypes/<locale>.txt} resource. Locales without a file are intentionally not
 * registered, so {@link BloodTypeGenerator} falls back to the bundled global distribution. Custom
 * providers can be added at any time via {@link io.github.frikit.krandom.generator.DataRegistryContext.Builder}.
 *
 * <p><b>Lookup order</b>
 * <ol>
 *   <li>Exact {@code language_COUNTRY} match (e.g. {@code "en_US"})
 *   <li>Language-only match (e.g. {@code "en"})
 *   <li>{@code null} — the caller handles the missing case
 * </ol>
 */
public final class BloodTypeDataRegistry {

    private static final ConcurrentHashMap<String, BloodTypeDataProvider> REGISTRY =
        new ConcurrentHashMap<>();

    static {
        for (SupportedLocale supportedLocale : SupportedLocale.values()) {
            String path = "krandom/bloodtypes/" + supportedLocale.resourcePrefix() + ".txt";
            if (BloodTypeDataRegistry.class.getResource("/" + path) != null) {
                putProvider(new BuiltInBloodTypeDataProvider(supportedLocale));
            }
        }
    }

    private BloodTypeDataRegistry() {
    }

    /**
     * Returns {@code true} if the registry contains an entry for the given locale.
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
     */
    public static @Nullable BloodTypeDataProvider forLocale(@Nullable Locale locale) {
        if (locale == null) {
            return null;
        }
        String lang = locale.getLanguage();
        String country = locale.getCountry();
        if (!country.isEmpty()) {
            BloodTypeDataProvider exact = REGISTRY.get(lang + "_" + country);
            if (exact != null) {
                return exact;
            }
        }
        return REGISTRY.get(lang);
    }

    /**
     * Returns an unmodifiable snapshot of all currently registered locale keys.
     */
    public static Set<String> registeredKeys() {
        return Set.copyOf(REGISTRY.keySet());
    }

    private static void putProvider(BloodTypeDataProvider provider) {
        String lang = provider.getLocale().getLanguage();
        String country = provider.getLocale().getCountry();
        REGISTRY.put(lang + "_" + country, provider);
            REGISTRY.putIfAbsent(lang, provider);
    }
}
