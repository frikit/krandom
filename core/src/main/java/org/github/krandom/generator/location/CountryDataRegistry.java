/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package org.github.krandom.generator.location;

import org.github.krandom.generator.locale.SupportedLocale;

import java.util.Collections;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Global registry mapping locales to {@link CountryDataProvider} instances.
 *
 * <p>Pre-seeded at class-load time with every built-in locale from
 * {@link org.github.krandom.generator.locale.SupportedLocale}.
 * Custom providers can be added at any time via {@link #register(CountryDataProvider)}, replacing
 * any existing provider for the same locale key.
 *
 * <p><b>Lookup order</b>
 * <ol>
 *   <li>Exact {@code language_COUNTRY} match (e.g. {@code "en_GB"})
 *   <li>Language-only match (e.g. {@code "en"})
 *   <li>{@code null} — the caller is responsible for handling the missing case (typically by
 *       throwing {@link UnsupportedOperationException})
 * </ol>
 *
 * <p><b>Language-level fallback</b><br>
 * The first provider registered for a given language becomes the language-level fallback for that
 * language. Subsequent registrations for the same language update the exact key only, leaving the
 * language fallback untouched — unless the new provider's locale has no country component (e.g.
 * {@code Locale.of("en")}), in which case it explicitly replaces the language-level entry.
 */
public final class CountryDataRegistry {

    private static final ConcurrentHashMap<String, CountryDataProvider> REGISTRY =
            new ConcurrentHashMap<>();

    static {
        for (SupportedLocale supportedLocale : SupportedLocale.values()) {
            seedInternal(new BuiltInCountryDataProvider(supportedLocale));
        }
    }

    private CountryDataRegistry() {}

    /**
     * Registers a custom country data provider, making it available to {@link CountryGenerator}.
     *
     * <p>If a provider already exists for the same exact locale key, it is replaced. A
     * language-only key (e.g. {@code "en"}) is set only when no prior entry exists for that
     * language — meaning the first registration for a language becomes its language-level fallback.
     * To explicitly override the language-level fallback, register a provider whose
     * {@link CountryDataProvider#getLocale()} has no country component (e.g.
     * {@code Locale.of("en")}).
     *
     * @param provider the provider to register; must not be {@code null}, and
     *                 {@link CountryDataProvider#getLocale()} must not be {@code null}
     */
    public static void register(CountryDataProvider provider) {
        Objects.requireNonNull(provider, "provider");
        Objects.requireNonNull(provider.getLocale(), "provider.getLocale()");
        String lang = provider.getLocale().getLanguage();
        String country = provider.getLocale().getCountry();
        if (country.isEmpty()) {
            // Explicit language-only registration — replaces the language fallback.
            REGISTRY.put(lang, provider);
        } else {
            REGISTRY.put(lang + "_" + country, provider);
            // First registration for this language becomes the language-level fallback.
            REGISTRY.putIfAbsent(lang, provider);
        }
    }

    /**
     * Returns {@code true} if the registry contains an entry for the given locale (exact
     * {@code language_COUNTRY} match or language-only match).
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
     * @return the provider, or {@code null} if none is registered for the locale or its language
     */
    public static CountryDataProvider forLocale(Locale locale) {
        if (locale == null) return null;
        String lang = locale.getLanguage();
        String country = locale.getCountry();
        if (!country.isEmpty()) {
            CountryDataProvider exact = REGISTRY.get(lang + "_" + country);
            if (exact != null) return exact;
        }
        return REGISTRY.get(lang);
    }

    /**
     * Returns an unmodifiable snapshot of all currently registered locale keys.
     *
     * <p>Each key is either a language code (e.g. {@code "en"}) or a {@code language_COUNTRY}
     * string (e.g. {@code "en_US"}).
     */
    public static Set<String> registeredKeys() {
        return Collections.unmodifiableSet(REGISTRY.keySet());
    }

    /**
     * Internal seed — bypasses null-checks for trusted built-in providers.
     * All built-in entries are expected to have non-empty country codes.
     */
    private static void seedInternal(CountryDataProvider provider) {
        String lang = provider.getLocale().getLanguage();
        String country = provider.getLocale().getCountry();
        REGISTRY.put(lang + "_" + country, provider);
        REGISTRY.putIfAbsent(lang, provider);
    }
}
