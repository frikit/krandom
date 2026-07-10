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
 * Global registry mapping locales to {@link FirstNameDataProvider} instances.
 *
 * <p>Pre-seeded at class-load time with every built-in locale from
 * {@link io.github.frikit.krandom.generator.locale.SupportedLocale}.
 * Custom providers can be added at any time via {@link #register(FirstNameDataProvider)},
 * replacing any existing provider for the same locale key.
 *
 * <p><b>Lookup order</b>
 * <ol>
 *   <li>Exact {@code language_COUNTRY} match (e.g. {@code "en_US"})
 *   <li>Language-only match (e.g. {@code "en"})
 *   <li>{@code null} — the caller is responsible for handling the missing case
 * </ol>
 */
public final class FirstNameDataRegistry {

    private static final ConcurrentHashMap<String, FirstNameDataProvider> REGISTRY =
        new ConcurrentHashMap<>();

    static {
        for (SupportedLocale supportedLocale : SupportedLocale.values()) {
            seedInternal(new BuiltInFirstNameDataProvider(supportedLocale));
        }
    }

    private FirstNameDataRegistry() {
    }

    /**
     * Registers a custom first-name data provider.
     *
     * <p>If a provider already exists for the same exact locale key, it is replaced.
     * A language-only locale (e.g. {@code Locale.of("en")}) explicitly replaces the
     * language-level fallback.
     *
     * @deprecated Since 1.6, use
     * {@link io.github.frikit.krandom.generator.DataRegistryContext.Builder#registerFirstNameProvider(FirstNameDataProvider)}
     * for configuration-scoped registration.
     * @param provider the provider to register; must not be {@code null}
     */
    @Deprecated(since = "1.6", forRemoval = true)
    public static void register(FirstNameDataProvider provider) {
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
    public static @Nullable FirstNameDataProvider forLocale(@Nullable Locale locale) {
        if (locale == null) return null;
        String lang = locale.getLanguage();
        String country = locale.getCountry();
        if (!country.isEmpty()) {
            FirstNameDataProvider exact = REGISTRY.get(lang + "_" + country);
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

    private static void seedInternal(FirstNameDataProvider provider) {
        putProvider(provider);
    }

    private static void putProvider(FirstNameDataProvider provider) {
        String lang = provider.getLocale().getLanguage();
        String country = provider.getLocale().getCountry();
        if (country.isEmpty()) {
            REGISTRY.put(lang, provider);
        } else {
            REGISTRY.put(lang + "_" + country, provider);
            REGISTRY.putIfAbsent(lang, provider);
        }
    }

    private static void validateProvider(FirstNameDataProvider provider) {
        Objects.requireNonNull(provider.getLocale(), "provider.getLocale()");
        validateArray("maleFirstNames", provider.getMaleFirstNames());
        validateArray("femaleFirstNames", provider.getFemaleFirstNames());
    }

    private static void validateArray(String name, String[] values) {
        Objects.requireNonNull(values, name);
        if (values.length == 0) {
            throw new IllegalArgumentException(name + " must not be empty");
        }
        for (int i = 0; i < values.length; i++) {
            String value = values[i];
            if (value == null || value.isBlank()) {
                throw new IllegalArgumentException(name + " at index " + i + " must not be blank");
            }
        }
    }
}
