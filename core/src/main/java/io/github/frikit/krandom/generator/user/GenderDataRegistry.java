/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.user;

import io.github.frikit.krandom.generator.locale.SupportedLocale;
import org.jspecify.annotations.Nullable;

import java.util.Collections;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Global registry mapping locales to {@link GenderDataProvider} instances.
 *
 * <p>Pre-seeded at class-load time with every built-in locale from
 * {@link io.github.frikit.krandom.generator.locale.SupportedLocale}.
 * Custom providers can be added at any time via {@link #register(GenderDataProvider)},
 * replacing any existing provider for the same locale key.
 *
 * <p><b>Lookup order</b>
 * <ol>
 *   <li>Exact {@code language_COUNTRY} match (e.g. {@code "en_US"})
 *   <li>Language-only match (e.g. {@code "en"})
 *   <li>{@code null} — the caller is responsible for handling the missing case
 * </ol>
 */
public final class GenderDataRegistry {

    private static final ConcurrentHashMap<String, GenderDataProvider> REGISTRY =
        new ConcurrentHashMap<>();

    static {
        for (SupportedLocale supportedLocale : SupportedLocale.values()) {
            seedInternal(new BuiltInGenderDataProvider(supportedLocale));
        }
    }

    private GenderDataRegistry() {
    }

    /**
     * Registers a custom gender-label data provider.
     *
     * <p>If a provider already exists for the same exact locale key, it is replaced.
     * A language-only locale (e.g. {@code Locale.of("en")}) explicitly replaces the
     * language-level fallback.
     *
     * @param provider the provider to register; must not be {@code null}
     */
    public static void register(GenderDataProvider provider) {
        Objects.requireNonNull(provider, "provider");
        validateProvider(provider);
        putProvider(provider);
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
    public static @Nullable GenderDataProvider forLocale(@Nullable Locale locale) {
        if (locale == null) return null;
        String lang = locale.getLanguage();
        String country = locale.getCountry();
        if (!country.isEmpty()) {
            GenderDataProvider exact = REGISTRY.get(lang + "_" + country);
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

    private static void seedInternal(GenderDataProvider provider) {
        putProvider(provider);
    }

    private static void putProvider(GenderDataProvider provider) {
        String lang = provider.getLocale().getLanguage();
        String country = provider.getLocale().getCountry();
        if (country.isEmpty()) {
            REGISTRY.put(lang, provider);
        } else {
            REGISTRY.put(lang + "_" + country, provider);
            REGISTRY.putIfAbsent(lang, provider);
        }
    }

    private static void validateProvider(GenderDataProvider provider) {
        Objects.requireNonNull(provider.getLocale(), "provider.getLocale()");
        validateLabel("maleLabel", provider.getMaleLabel());
        validateLabel("femaleLabel", provider.getFemaleLabel());
    }

    private static void validateLabel(String name, String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(name + " must not be blank");
        }
    }
}
