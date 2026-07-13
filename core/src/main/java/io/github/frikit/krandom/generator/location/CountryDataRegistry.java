/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.location;

import io.github.frikit.krandom.generator.locale.SupportedLocale;

import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Global registry mapping locales to {@link CountryDataProvider} instances.
 *
 * <p>Pre-seeded at class-load time with every built-in locale from
 * {@link io.github.frikit.krandom.generator.locale.SupportedLocale}.
 * Custom providers can be added at any time via {@link io.github.frikit.krandom.generator.DataRegistryContext.Builder}, replacing
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

    private CountryDataRegistry() {
    }

    /**
     * Returns {@code true} if the registry contains an entry for the given locale (exact
     * {@code language_COUNTRY} match or language-only match).
     */
    public static boolean isRegistered(Locale locale) {
        return RegistryLookup.containsWithFallback(REGISTRY, locale);
    }

    /**
     * Returns the best-matching provider for the given locale.
     *
     * @return the provider, or {@code null} if none is registered for the locale or its language
     */
    public static CountryDataProvider forLocale(Locale locale) {
        return RegistryLookup.findWithFallback(REGISTRY, locale);
    }

    /**
     * Returns an unmodifiable snapshot of all currently registered locale keys.
     *
     * <p>Each key is either a language code (e.g. {@code "en"}) or a {@code language_COUNTRY}
     * string (e.g. {@code "en_US"}).
     */
    public static Set<String> registeredKeys() {
        return Set.copyOf(REGISTRY.keySet());
    }

    /**
     * Internal seed — bypasses null-checks for trusted built-in providers.
     * All built-in entries are expected to have non-empty country codes.
     */
    private static void seedInternal(CountryDataProvider provider) {
        RegistryLookup.putWithLanguageFallback(REGISTRY, provider.getLocale(), provider);
    }
}
