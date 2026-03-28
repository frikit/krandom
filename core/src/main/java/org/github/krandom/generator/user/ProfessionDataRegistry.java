/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package org.github.krandom.generator.user;

import org.github.krandom.generator.locale.SupportedLocale;

import java.util.Arrays;
import java.util.Collections;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Global registry mapping locales to {@link ProfessionDataProvider} instances.
 *
 * <p>Pre-seeded with built-in locales from
 * {@link org.github.krandom.generator.locale.SupportedLocale}. Custom providers can override any
 * locale via {@link #register(ProfessionDataProvider)}. Additional profession entries can be
 * appended via {@link #append(Locale, String[], int[])}.
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
     * Registers (or overrides) a provider for its locale.
     */
    public static void register(ProfessionDataProvider provider) {
        Objects.requireNonNull(provider, "provider");
        validateProvider(provider);
        putProvider(provider);
    }

    /**
     * Appends profession entries to the existing locale provider, preserving existing entries.
     *
     * <p>If the locale is not yet registered, a new provider is created from the appended data.
     */
    public static void append(Locale locale, String[] professions, int[] weights) {
        Objects.requireNonNull(locale, "locale");
        validateArrays(professions, weights);

        ProfessionDataProvider existing = forLocale(locale);
        String[] mergedProfessions;
        int[] mergedWeights;
        if (existing == null || !localeMatches(existing.getLocale(), locale)) {
            mergedProfessions = professions.clone();
            mergedWeights = weights.clone();
        } else {
            String[] baseProfessions = existing.getProfessions();
            int[] baseWeights = existing.getWeights();
            mergedProfessions = Arrays.copyOf(baseProfessions, baseProfessions.length + professions.length);
            System.arraycopy(professions, 0, mergedProfessions, baseProfessions.length, professions.length);
            mergedWeights = Arrays.copyOf(baseWeights, baseWeights.length + weights.length);
            System.arraycopy(weights, 0, mergedWeights, baseWeights.length, weights.length);
        }

        putProvider(new BasicProfessionDataProvider(locale, mergedProfessions, mergedWeights));
    }

    /**
     * Convenience overload: appended professions get uniform weight 1.
     */
    public static void append(Locale locale, String[] professions) {
        Objects.requireNonNull(professions, "professions");
        int[] weights = new int[professions.length];
        Arrays.fill(weights, 1);
        append(locale, professions, weights);
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
    public static ProfessionDataProvider forLocale(Locale locale) {
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
        return Collections.unmodifiableSet(REGISTRY.keySet());
    }

    private static void putProvider(ProfessionDataProvider provider) {
        String lang = provider.getLocale().getLanguage();
        String country = provider.getLocale().getCountry();
        if (country.isEmpty()) {
            REGISTRY.put(lang, provider);
        } else {
            REGISTRY.put(lang + "_" + country, provider);
            REGISTRY.putIfAbsent(lang, provider);
        }
    }

    private static void seedInternal(ProfessionDataProvider provider) {
        putProvider(provider);
    }

    private static void validateProvider(ProfessionDataProvider provider) {
        Objects.requireNonNull(provider.getLocale(), "provider.getLocale()");
        validateArrays(provider.getProfessions(), provider.getWeights());
    }

    private static void validateArrays(String[] professions, int[] weights) {
        Objects.requireNonNull(professions, "professions");
        Objects.requireNonNull(weights, "weights");
        if (professions.length == 0) {
            throw new IllegalArgumentException("professions must not be empty");
        }
        if (professions.length != weights.length) {
            throw new IllegalArgumentException("professions and weights length must match");
        }
        for (int i = 0; i < professions.length; i++) {
            String profession = professions[i];
            if (profession == null || profession.isBlank()) {
                throw new IllegalArgumentException("profession at index " + i + " must not be blank");
            }
            if (weights[i] <= 0) {
                throw new IllegalArgumentException("weight at index " + i + " must be > 0");
            }
        }
    }

    private static boolean localeMatches(Locale a, Locale b) {
        return a.getLanguage().equals(b.getLanguage())
               && a.getCountry().equals(b.getCountry());
    }

    private static final class BasicProfessionDataProvider implements ProfessionDataProvider {

        private final Locale   locale;
        private final String[] professions;
        private final int[]    weights;

        private BasicProfessionDataProvider(Locale locale, String[] professions, int[] weights) {
            this.locale = locale;
            this.professions = professions;
            this.weights = weights;
        }

        @Override
        public Locale getLocale() {
            return locale;
        }

        @Override
        public String[] getProfessions() {
            return professions.clone();
        }

        @Override
        public int[] getWeights() {
            return weights.clone();
        }
    }
}
