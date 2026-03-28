/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package org.github.krandom.generator.user;

import org.github.krandom.generator.locale.SupportedLocale;

import java.util.Collections;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Global registry mapping locales to {@link SuffixDataProvider} instances.
 *
 * <p>Pre-seeded at class-load time with every built-in locale from
 * {@link org.github.krandom.generator.locale.SupportedLocale}.
 * Custom providers can be added at any time via {@link #register(SuffixDataProvider)}, replacing
 * any existing provider for the same locale key.
 *
 * <p><b>Lookup order</b>
 * <ol>
 *   <li>Exact {@code language_COUNTRY} match (e.g. {@code "en_US"})
 *   <li>Language-only match (e.g. {@code "en"})
 *   <li>{@code null} — the caller is responsible for handling the missing case
 * </ol>
 */
public final class SuffixDataRegistry {

    private static final ConcurrentHashMap<String, SuffixDataProvider> REGISTRY =
            new ConcurrentHashMap<>();

    static {
        for (SupportedLocale supportedLocale : SupportedLocale.values()) {
            seedInternal(new BuiltInSuffixDataProvider(supportedLocale));
        }
    }

    private SuffixDataRegistry() {}

    /**
     * Registers a custom suffix data provider.
     *
     * <p>If a provider already exists for the same exact locale key, it is replaced. A
     * language-only key (e.g. {@code "en"}) explicitly replaces the language-level fallback.
     *
     * @param provider the provider to register; must not be {@code null}
     */
    public static void register(SuffixDataProvider provider) {
        Objects.requireNonNull(provider, "provider");
        Objects.requireNonNull(provider.getLocale(), "provider.getLocale()");
        String lang    = provider.getLocale().getLanguage();
        String country = provider.getLocale().getCountry();
        if (country.isEmpty()) {
            REGISTRY.put(lang, provider);
        } else {
            REGISTRY.put(lang + "_" + country, provider);
            REGISTRY.putIfAbsent(lang, provider);
        }
    }

    /**
     * Returns {@code true} if the registry contains an entry for the given locale.
     */
    public static boolean isRegistered(Locale locale) {
        if (locale == null) return false;
        String lang    = locale.getLanguage();
        String country = locale.getCountry();
        if (!country.isEmpty() && REGISTRY.containsKey(lang + "_" + country)) return true;
        return REGISTRY.containsKey(lang);
    }

    /**
     * Returns the best-matching provider for the given locale.
     *
     * @return the provider, or {@code null} if none is registered
     */
    public static SuffixDataProvider forLocale(Locale locale) {
        if (locale == null) return null;
        String lang    = locale.getLanguage();
        String country = locale.getCountry();
        if (!country.isEmpty()) {
            SuffixDataProvider exact = REGISTRY.get(lang + "_" + country);
            if (exact != null) return exact;
        }
        return REGISTRY.get(lang);
    }

    /**
     * Returns an unmodifiable snapshot of all currently registered locale keys.
     */
    public static Set<String> registeredKeys() {
        return Collections.unmodifiableSet(REGISTRY.keySet());
    }

    private static void seedInternal(SuffixDataProvider provider) {
        String lang    = provider.getLocale().getLanguage();
        String country = provider.getLocale().getCountry();
        REGISTRY.put(lang + "_" + country, provider);
        REGISTRY.putIfAbsent(lang, provider);
    }
}
