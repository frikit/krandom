/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.user.nationalid;

import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Global registry mapping locales to {@link NationalIdProvider} instances.
 *
 * <p>Pre-seeded at class-load time with every locale in
 * {@link io.github.frikit.krandom.generator.locale.SupportedLocale}. Custom providers can be added at
 * any time via {@link io.github.frikit.krandom.generator.DataRegistryContext.Builder}, replacing any existing provider for the same
 * locale key.
 *
 * <p><b>Lookup order</b>
 * <ol>
 *   <li>Exact {@code language_COUNTRY} match (e.g. {@code "en_US"})
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
public final class NationalIdRegistry {

    private static final ConcurrentHashMap<String, NationalIdProvider> REGISTRY =
        new ConcurrentHashMap<>();

    static {
        List.of(
            new UsNationalIdProvider(),
            new GbNationalIdProvider(),
            new AuNationalIdProvider(),
            new FrNationalIdProvider(),
            new DeNationalIdProvider(),
            new JpNationalIdProvider(),
            new EsNationalIdProvider(),
            new ItNationalIdProvider(),
            new BrNationalIdProvider(),
            new CnNationalIdProvider(),
            new NlNationalIdProvider(),
            new PlNationalIdProvider(),
            new RuNationalIdProvider(),
            new KoNationalIdProvider(),
            new TrNationalIdProvider(),
            new SvNationalIdProvider(),
            new NbNationalIdProvider(),
            new CsNationalIdProvider(),
            new ArSaNationalIdProvider(),
            new HiInNationalIdProvider(),
            new DaDkNationalIdProvider(),
            new FiFiNationalIdProvider(),
            new HuHuNationalIdProvider(),
            new RoRoNationalIdProvider(),
            new SkSkNationalIdProvider(),
            new UkUaNationalIdProvider(),
            new BgBgNationalIdProvider(),
            new HrHrNationalIdProvider(),
            new ElGrNationalIdProvider(),
            new ThThNationalIdProvider(),
            new ViVnNationalIdProvider(),
            new IdIdNationalIdProvider(),
            new MsMyNationalIdProvider(),
            new HeIlNationalIdProvider(),
            new CaEsNationalIdProvider()
        ).forEach(NationalIdRegistry::seedInternal);
    }

    private NationalIdRegistry() {
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
    public static @Nullable NationalIdProvider forLocale(@Nullable Locale locale) {
        if (locale == null) return null;
        String lang = locale.getLanguage();
        String country = locale.getCountry();
        if (!country.isEmpty()) {
            NationalIdProvider exact = REGISTRY.get(lang + "_" + country);
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
        return Set.copyOf(REGISTRY.keySet());
    }

    /**
     * Internal seed — bypasses null-checks since built-in providers are trusted.
     * All built-in entries are expected to have non-empty country codes.
     */
    static void seedInternal(NationalIdProvider provider) {
        String lang = provider.getLocale().getLanguage();
        String country = provider.getLocale().getCountry();
        REGISTRY.put(lang + "_" + country, provider);
        REGISTRY.putIfAbsent(lang, provider);
    }
}
