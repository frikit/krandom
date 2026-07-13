/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.user;

import io.github.frikit.krandom.generator.locale.SupportedLocale;
import org.jspecify.annotations.Nullable;

import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Global registry mapping locales to {@link LastNameDataProvider} instances.
 *
 * <p>Pre-seeded at class-load time with every built-in locale from
 * {@link io.github.frikit.krandom.generator.locale.SupportedLocale}.
 * Custom providers can be added at any time via {@link io.github.frikit.krandom.generator.DataRegistryContext.Builder},
 * replacing any existing provider for the same locale key.
 *
 * <p><b>Lookup order</b>
 * <ol>
 *   <li>Exact {@code language_COUNTRY} match (e.g. {@code "en_US"})
 *   <li>Language-only match (e.g. {@code "en"})
 *   <li>{@code null} — the caller is responsible for handling the missing case
 * </ol>
 */
public final class LastNameDataRegistry {

    private static final ConcurrentHashMap<String, LastNameDataProvider> REGISTRY =
        new ConcurrentHashMap<>();

    static {
        for (SupportedLocale supportedLocale : SupportedLocale.values()) {
            seedInternal(new BuiltInLastNameDataProvider(supportedLocale));
        }
    }

    private LastNameDataRegistry() {
    }

    /**
     * Returns {@code true} if the registry contains an entry for the given locale.
     */
    public static boolean isRegistered(Locale locale) {
        if (locale == null) return false;
        String lang = locale.getLanguage();
        String country = locale.getCountry();
        if (!country.isEmpty() && REGISTRY.containsKey(lang + "_" + country)) return true;
        return REGISTRY.containsKey(lang);
    }

    /**
     * Returns the best-matching provider for the given locale.
     *
     * @return the provider, or {@code null} if none is registered
     */
    public static @Nullable LastNameDataProvider forLocale(@Nullable Locale locale) {
        if (locale == null) return null;
        String lang = locale.getLanguage();
        String country = locale.getCountry();
        if (!country.isEmpty()) {
            LastNameDataProvider exact = REGISTRY.get(lang + "_" + country);
            if (exact != null) return exact;
        }
        return REGISTRY.get(lang);
    }

    /**
     * Returns an unmodifiable snapshot of all currently registered locale keys.
     */
    public static Set<String> registeredKeys() {
        return Set.copyOf(REGISTRY.keySet());
    }

    private static void seedInternal(LastNameDataProvider provider) {
        putProvider(provider);
    }

    private static void putProvider(LastNameDataProvider provider) {
        String lang = provider.getLocale().getLanguage();
        String country = provider.getLocale().getCountry();
        REGISTRY.put(lang + "_" + country, provider);
            REGISTRY.putIfAbsent(lang, provider);
    }


}
