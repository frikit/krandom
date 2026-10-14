/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator;

import io.github.frikit.krandom.generator.datapack.LocalDataPack;
import io.github.frikit.krandom.generator.locale.LocaleDataBundle;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import org.jspecify.annotations.Nullable;

/**
 * Immutable, config-scoped registry view for locale data providers, addressed by {@link Vocabulary}.
 *
 * <p>By default ({@link #globalDefault()}), lookups delegate to the built-in static registries.
 * Custom contexts can be built via {@link #builder()} and attached to {@link GeneratorConfig}
 * so tests and embedded runtimes can isolate registry state:
 *
 * <pre>{@code
 * DataRegistryContext context = DataRegistryContext.builder()
 *     .isolated()
 *     .register(Vocabulary.CITY, new MyCityProvider())
 *     .build();
 * CityDataProvider cities = context.provider(Vocabulary.CITY, Locale.US);
 * }</pre>
 *
 * <p>Provider lookups resolve in this order: a provider registered in this context for the exact
 * {@code language_COUNTRY} key, the global provider for that exact key, a provider registered in
 * this context for the language (a regional registration also serves as its language's fallback),
 * and finally the global language fallback. A scoped regional bundle therefore never replaces
 * another region's built-in data; for example, an {@code en_IN} bundle leaves {@code en_US} names
 * unchanged. Isolated contexts skip both global steps. {@link Vocabulary#NATIONAL_ID} is the one
 * vocabulary whose third step is the country rather than the language, because identity-number
 * formats are national.
 */
public final class DataRegistryContext {

    private static final DataRegistryContext GLOBAL_DEFAULT = new Builder().build();

    private final boolean useGlobalFallback;
    private final Map<Vocabulary<?>, Map<String, LocaleDataProvider>> providers;
    private final Map<Vocabulary<?>, Map<String, LocaleDataProvider>> providersByCountry;

    private DataRegistryContext(Builder builder) {
        this.useGlobalFallback = builder.useGlobalFallback;
        this.providers = copyOf(builder.providers);
        this.providersByCountry = copyOf(builder.providersByCountry);
    }

    /**
     * Returns the default context that delegates to the built-in static registries.
     */
    public static DataRegistryContext globalDefault() {
        return GLOBAL_DEFAULT;
    }

    /**
     * Creates a context builder.
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Resolves the provider of a vocabulary for a locale.
     *
     * @param vocabulary vocabulary to resolve
     * @param locale requested locale; {@code null} resolves to no provider
     * @param <P> provider contract of the vocabulary
     * @return the matching provider, or {@code null} when none is registered in this context or,
     *     for a non-isolated context, bundled with the library
     */
    public <P extends LocaleDataProvider> @Nullable P provider(Vocabulary<P> vocabulary, @Nullable Locale locale) {
        Objects.requireNonNull(vocabulary, "vocabulary");
        if (locale == null) {
            return null;
        }
        Map<String, LocaleDataProvider> scoped = entries(providers, vocabulary);
        String language = locale.getLanguage();
        String country = locale.getCountry();
        if (!country.isEmpty()) {
            String exactKey = language + "_" + country;
            LocaleDataProvider scopedExact = scoped.get(exactKey);
            if (scopedExact != null) {
                return vocabulary.providerType().cast(scopedExact);
            }
            if (useGlobalFallback && vocabulary.globalKeys().contains(exactKey)) {
                return vocabulary.globalProvider(locale);
            }
        }
        LocaleDataProvider fallback = vocabulary.countryIndexed() && !country.isEmpty()
                ? entries(providersByCountry, vocabulary).get(country)
                : scoped.get(language);
        if (fallback != null) {
            return vocabulary.providerType().cast(fallback);
        }
        return useGlobalFallback ? vocabulary.globalProvider(locale) : null;
    }

    /**
     * Reports whether {@link #provider} finds a provider of a vocabulary for a locale.
     *
     * @param vocabulary vocabulary to check
     * @param locale requested locale
     * @return true when a provider resolves
     */
    public boolean isRegistered(Vocabulary<?> vocabulary, @Nullable Locale locale) {
        return provider(vocabulary, locale) != null;
    }

    /**
     * Returns the locale keys a vocabulary is registered under: the keys registered in this context
     * (both {@code language_COUNTRY} and {@code language} entries) followed, for a non-isolated
     * context, by the keys of the bundled data.
     *
     * @param vocabulary vocabulary to list
     * @return immutable set of locale keys, empty when nothing resolves
     */
    public Set<String> registeredKeys(Vocabulary<?> vocabulary) {
        Objects.requireNonNull(vocabulary, "vocabulary");
        Set<String> scopedKeys = entries(providers, vocabulary).keySet();
        Set<String> globalKeys = useGlobalFallback ? vocabulary.globalKeys() : Set.of();
        if (scopedKeys.isEmpty() && globalKeys.isEmpty()) {
            return Set.of();
        }
        LinkedHashSet<String> keys = new LinkedHashSet<>(scopedKeys);
        keys.addAll(globalKeys);
        return Collections.unmodifiableSet(keys);
    }

    private static Map<String, LocaleDataProvider> entries(
            Map<Vocabulary<?>, Map<String, LocaleDataProvider>> providers, Vocabulary<?> vocabulary) {
        Map<String, LocaleDataProvider> entries = providers.get(vocabulary);
        return entries == null ? Map.of() : entries;
    }

    private static Map<Vocabulary<?>, Map<String, LocaleDataProvider>> copyOf(
            Map<Vocabulary<?>, Map<String, LocaleDataProvider>> source) {
        Map<Vocabulary<?>, Map<String, LocaleDataProvider>> copy = new LinkedHashMap<>();
        source.forEach((vocabulary, entries) -> copy.put(vocabulary, Map.copyOf(entries)));
        return Collections.unmodifiableMap(copy);
    }

    /**
     * Context builder.
     */
    public static final class Builder {

        private boolean useGlobalFallback = true;
        private final Map<Vocabulary<?>, Map<String, LocaleDataProvider>> providers = new LinkedHashMap<>();
        private final Map<Vocabulary<?>, Map<String, LocaleDataProvider>> providersByCountry = new LinkedHashMap<>();

        /**
         * Controls whether this context delegates to global static registries when no local value exists.
         */
        public Builder useGlobalFallback(boolean enabled) {
            this.useGlobalFallback = enabled;
            return this;
        }

        /**
         * Creates a fully isolated context with no global registry fallback.
         */
        public Builder isolated() {
            return useGlobalFallback(false);
        }

        /**
         * Registers all provider families defined by a locale data bundle.
         */
        public Builder registerLocaleData(LocaleDataBundle bundle) {
            return Objects.requireNonNull(bundle, "bundle").applyTo(this);
        }

        /**
         * Registers verified local data-pack providers with this context only.
         *
         * @param dataPack verified local data pack
         * @return this builder
         */
        public Builder registerDataPack(LocalDataPack dataPack) {
            return register(
                    Vocabulary.UNIVERSITY,
                    Objects.requireNonNull(dataPack, "dataPack").universityProvider());
        }

        /**
         * Registers a provider for a vocabulary in this context only.
         *
         * <p>The provider is stored under its locale's {@code language_COUNTRY} key and, unless the
         * language already has an entry, under the plain language key as well, so a regional
         * registration also serves as its language's fallback. Registering the same key twice
         * replaces the earlier provider.
         *
         * @param vocabulary vocabulary the provider supplies
         * @param provider provider whose locale and data pass the vocabulary's validation
         * @param <P> provider contract of the vocabulary
         * @return this builder
         * @throws NullPointerException when the vocabulary, the provider or its locale is null
         * @throws IllegalArgumentException when the provider's data violates the vocabulary's rules
         */
        public <P extends LocaleDataProvider> Builder register(Vocabulary<P> vocabulary, P provider) {
            Objects.requireNonNull(vocabulary, "vocabulary");
            vocabulary.validate(provider);
            Locale locale = provider.getLocale();
            Map<String, LocaleDataProvider> entries =
                    providers.computeIfAbsent(vocabulary, key -> new LinkedHashMap<>());
            String language = locale.getLanguage();
            String country = locale.getCountry();
            if (country.isEmpty()) {
                entries.put(language, provider);
                return this;
            }
            entries.put(language + "_" + country, provider);
            entries.putIfAbsent(language, provider);
            if (vocabulary.countryIndexed()) {
                providersByCountry
                        .computeIfAbsent(vocabulary, key -> new LinkedHashMap<>())
                        .putIfAbsent(country, provider);
            }
            return this;
        }

        public DataRegistryContext build() {
            return new DataRegistryContext(this);
        }
    }
}
