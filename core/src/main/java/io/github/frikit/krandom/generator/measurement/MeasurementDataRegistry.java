/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.measurement;

import io.github.frikit.krandom.generator.locale.SupportedLocale;
import org.jspecify.annotations.Nullable;

import java.util.Collections;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Global registry mapping locales to {@link MeasurementDataProvider} instances.
 *
 * <p>Seeded at class-load time with every built-in
 * {@link io.github.frikit.krandom.generator.locale.SupportedLocale} that has a
 * {@code krandom/measurement/<locale>.txt} resource. Locales without a file fall back to the bundled
 * default (English) units. Custom providers can be added via
 * {@link #register(MeasurementDataProvider)}.
 *
 * <p><b>Lookup order:</b> exact {@code language_COUNTRY} match, then language-only match, then
 * {@code null}.
 */
public final class MeasurementDataRegistry {

    private static final ConcurrentHashMap<String, MeasurementDataProvider> REGISTRY =
        new ConcurrentHashMap<>();

    static {
        for (SupportedLocale supportedLocale : SupportedLocale.values()) {
            String path = "krandom/measurement/" + supportedLocale.resourcePrefix() + ".txt";
            if (MeasurementDataRegistry.class.getResource("/" + path) != null) {
                putProvider(new BuiltInMeasurementDataProvider(supportedLocale));
            }
        }
    }

    private MeasurementDataRegistry() {
    }

    /**
     * Registers a custom measurement data provider, replacing any provider for the same locale key.
     *
     * @param provider the provider to register; must not be {@code null}
     */
    public static void register(MeasurementDataProvider provider) {
        Objects.requireNonNull(provider, "provider");
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
    public static @Nullable MeasurementDataProvider forLocale(@Nullable Locale locale) {
        if (locale == null) {
            return null;
        }
        String lang = locale.getLanguage();
        String country = locale.getCountry();
        if (!country.isEmpty()) {
            MeasurementDataProvider exact = REGISTRY.get(lang + "_" + country);
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
        return Collections.unmodifiableSet(REGISTRY.keySet());
    }

    private static void putProvider(MeasurementDataProvider provider) {
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
