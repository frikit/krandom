/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.file;

import io.github.frikit.krandom.generator.locale.SupportedLocale;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Global, read-only registry mapping locales to {@link DirectoryNameDataProvider} instances.
 *
 * <p>Seeded at class-load time with every built-in
 * {@link io.github.frikit.krandom.generator.locale.SupportedLocale} that has a {@code krandom/directory_names/<locale>.txt} resource.
 * Locales without data are not registered, so directory names fall back to the bundled English
 * {@code default.txt} data.
 *
 * <p><b>Lookup order:</b> exact {@code language_COUNTRY} match, then language-only match, then
 * {@code null}.
 */
public final class DirectoryNameDataRegistry {

    private static final List<String> REQUIRED_RESOURCE_DIRECTORIES = List.of(
        "krandom/directory_names/");

    private static final ConcurrentHashMap<String, DirectoryNameDataProvider> REGISTRY =
        new ConcurrentHashMap<>();

    static {
        for (SupportedLocale supportedLocale : SupportedLocale.values()) {
            if (hasBuiltInResources(supportedLocale.resourcePrefix())) {
                putProvider(new BuiltInDirectoryNameDataProvider(supportedLocale));
            }
        }
    }

    private DirectoryNameDataRegistry() {
    }

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
    public static @Nullable DirectoryNameDataProvider forLocale(@Nullable Locale locale) {
        if (locale == null) {
            return null;
        }
        String lang = locale.getLanguage();
        String country = locale.getCountry();
        if (!country.isEmpty()) {
            DirectoryNameDataProvider exact = REGISTRY.get(lang + "_" + country);
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

    static boolean hasBuiltInResources(String resourcePrefix) {
        return REQUIRED_RESOURCE_DIRECTORIES.stream()
            .allMatch(directory -> DirectoryNameDataRegistry.class.getResource("/" + directory + resourcePrefix + ".txt") != null);
    }

    private static void putProvider(DirectoryNameDataProvider provider) {
        String lang = provider.getLocale().getLanguage();
        String country = provider.getLocale().getCountry();
        REGISTRY.put(lang + "_" + country, provider);
        REGISTRY.putIfAbsent(lang, provider);
    }
}
