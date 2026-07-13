/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.user;

import io.github.frikit.krandom.generator.locale.SupportedLocale;
import org.jspecify.annotations.Nullable;

import java.util.Arrays;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Global registry mapping locales to {@link ProfessionDataProvider} instances.
 *
 * <p>Pre-seeded with built-in locales from
 * {@link io.github.frikit.krandom.generator.locale.SupportedLocale}. Custom providers and
 * additional profession entries are registered per configuration via
 * {@link io.github.frikit.krandom.generator.DataRegistryContext.Builder}.
 */
public final class ProfessionDataRegistry {

    private static final ConcurrentHashMap<String, ProfessionDataProvider> REGISTRY =
        new ConcurrentHashMap<>();

    static {
        for (SupportedLocale supportedLocale : SupportedLocale.values()) {
            seedInternal(new BuiltInProfessionDataProvider(supportedLocale));
        }
    }

    private ProfessionDataRegistry() {
    }

    /**
     * Returns {@code true} if registry has exact or language-level provider for locale.
     */
    public static boolean isRegistered(Locale locale) {
        if (locale == null) return false;
        String lang = locale.getLanguage();
        String country = locale.getCountry();
        if (!country.isEmpty() && REGISTRY.containsKey(lang + "_" + country)) return true;
        return REGISTRY.containsKey(lang);
    }

    /**
     * Returns provider by exact match, then language fallback, else {@code null}.
     */
    public static @Nullable ProfessionDataProvider forLocale(@Nullable Locale locale) {
        if (locale == null) return null;
        String lang = locale.getLanguage();
        String country = locale.getCountry();
        if (!country.isEmpty()) {
            ProfessionDataProvider exact = REGISTRY.get(lang + "_" + country);
            if (exact != null) return exact;
        }
        return REGISTRY.get(lang);
    }

    /**
     * Returns unmodifiable snapshot of registered locale keys.
     */
    public static Set<String> registeredKeys() {
        return Set.copyOf(REGISTRY.keySet());
    }

    private static void putProvider(ProfessionDataProvider provider) {
        String lang = provider.getLocale().getLanguage();
        String country = provider.getLocale().getCountry();
        REGISTRY.put(lang + "_" + country, provider);
            REGISTRY.putIfAbsent(lang, provider);
    }

    private static void seedInternal(ProfessionDataProvider provider) {
        putProvider(provider);
    }

}
