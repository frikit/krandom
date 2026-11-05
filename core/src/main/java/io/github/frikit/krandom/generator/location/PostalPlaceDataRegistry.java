/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.location;

import io.github.frikit.krandom.generator.locale.SupportedLocale;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.jspecify.annotations.Nullable;

/**
 * Read-only registry of the built-in postal places, which backs
 * {@link io.github.frikit.krandom.generator.Vocabulary#POSTAL_PLACE} in
 * {@link io.github.frikit.krandom.generator.DataRegistryContext#globalDefault()}.
 *
 * <p>It is seeded at class initialization with every native
 * {@link io.github.frikit.krandom.generator.locale.SupportedLocale} that has a
 * {@code krandom/postal_places/<locale>.txt} resource. A fallback variant such as {@code en_CA} has
 * none, as its cities come from another country's data.
 *
 * <p><b>Lookup order</b>
 * <ol>
 *   <li>The exact {@code language_COUNTRY} entry, such as {@code en_US}.
 *   <li>For a locale with a country, the first entry of that country, so another language of the
 *       country uses its places: {@code es_US} resolves the {@code en_US} places. A locale whose
 *       country has no places never borrows another country's through its language, so
 *       {@code en_CA}, {@code de_AT} and {@code fr_BE} resolve {@code null}.
 *   <li>For a locale without a country, the first entry of its language: {@code en} resolves the
 *       {@code en_US} places and {@code de} the {@code de_DE} ones.
 * </ol>
 */
public final class PostalPlaceDataRegistry {

    private static final Map<String, PostalPlaceDataProvider> PROVIDERS = new ConcurrentHashMap<>();
    private static final Map<String, PostalPlaceDataProvider> BY_COUNTRY = new ConcurrentHashMap<>();

    static {
        for (SupportedLocale supportedLocale : SupportedLocale.values()) {
            String resource = "/" + BuiltInPostalPlaceDataProvider.resourcePath(supportedLocale);
            if (PostalPlaceDataRegistry.class.getResource(resource) != null) {
                register(new BuiltInPostalPlaceDataProvider(supportedLocale));
            }
        }
    }

    private PostalPlaceDataRegistry() {}

    /**
     * Returns the built-in places for a locale, following the lookup order above.
     *
     * @param locale the locale
     * @return the provider, or {@code null} if none matches or {@code locale} is {@code null}
     */
    public static @Nullable PostalPlaceDataProvider forLocale(@Nullable Locale locale) {
        if (locale == null) {
            return null;
        }
        String language = locale.getLanguage();
        String country = locale.getCountry();
        if (country.isEmpty()) {
            return PROVIDERS.get(language);
        }
        PostalPlaceDataProvider exact = PROVIDERS.get(language + "_" + country);
        return exact != null ? exact : BY_COUNTRY.get(country);
    }

    /**
     * Returns {@code true} if {@link #forLocale(Locale)} resolves places for the locale.
     *
     * @param locale the locale
     * @return {@code true} if places resolve
     */
    public static boolean isRegistered(@Nullable Locale locale) {
        return forLocale(locale) != null;
    }

    /**
     * Returns the registered locale keys, {@code language_COUNTRY} and {@code language} entries.
     *
     * @return immutable snapshot of the keys
     */
    public static Set<String> registeredKeys() {
        return Set.copyOf(PROVIDERS.keySet());
    }

    private static void register(PostalPlaceDataProvider provider) {
        Locale locale = provider.getLocale();
        PROVIDERS.put(locale.getLanguage() + "_" + locale.getCountry(), provider);
        PROVIDERS.putIfAbsent(locale.getLanguage(), provider);
        BY_COUNTRY.putIfAbsent(locale.getCountry(), provider);
    }
}
