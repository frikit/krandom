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
 * Global registry mapping locales to {@link HobbyDataProvider} instances.
 *
 * <p>Seeded at class-load time with every built-in
 * {@link io.github.frikit.krandom.generator.locale.SupportedLocale} that has a
 * {@code krandom/hobbies/<locale>.txt} resource. Locales without a file fall back to the bundled
 * default (English) hobbies. Custom providers can be added via {@link #register(HobbyDataProvider)}.
 *
 * <p><b>Lookup order:</b> exact {@code language_COUNTRY} match, then language-only match, then
 * {@code null}.
 */
public final class HobbyDataRegistry {

    private static final ConcurrentHashMap<String, HobbyDataProvider> REGISTRY =
        new ConcurrentHashMap<>();

    static {
        for (SupportedLocale supportedLocale : SupportedLocale.values()) {
            String path = "krandom/hobbies/" + supportedLocale.resourcePrefix() + ".txt";
            if (HobbyDataRegistry.class.getResource("/" + path) != null) {
                putProvider(new BuiltInHobbyDataProvider(supportedLocale));
            }
        }
    }

    private HobbyDataRegistry() {
    }

    /**
     * Registers a custom hobby data provider, replacing any provider for the same locale key.
     *
     * @deprecated Since 1.6, use
     * {@link io.github.frikit.krandom.generator.DataRegistryContext.Builder#registerHobbyProvider(HobbyDataProvider)}
     * for configuration-scoped registration.
     * @param provider the provider to register; must not be {@code null}
     */
    @Deprecated(since = "1.6", forRemoval = true)
    public static void register(HobbyDataProvider provider) {
        validateProvider(provider);
        putProvider(provider);
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
    public static @Nullable HobbyDataProvider forLocale(@Nullable Locale locale) {
        if (locale == null) {
            return null;
        }
        String lang = locale.getLanguage();
        String country = locale.getCountry();
        if (!country.isEmpty()) {
            HobbyDataProvider exact = REGISTRY.get(lang + "_" + country);
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

    private static void validateProvider(HobbyDataProvider provider) {
        DataRegistryContext.builder().isolated().registerHobbyProvider(provider);
    }

    private static void putProvider(HobbyDataProvider provider) {
        String lang = provider.getLocale().getLanguage();
        String country = provider.getLocale().getCountry();
        if (country.isEmpty()) {
            REGISTRY.put(lang, provider);
        } else {
            REGISTRY.put(lang + "_" + country, provider);
            REGISTRY.putIfAbsent(lang, provider);
        }
    }
}
