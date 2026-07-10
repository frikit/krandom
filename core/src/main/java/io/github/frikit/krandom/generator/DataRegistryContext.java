/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator;

import io.github.frikit.krandom.generator.location.CityDataProvider;
import io.github.frikit.krandom.generator.location.CityDataRegistry;
import io.github.frikit.krandom.generator.location.CountryDataProvider;
import io.github.frikit.krandom.generator.location.CountryDataRegistry;
import io.github.frikit.krandom.generator.location.StateDataProvider;
import io.github.frikit.krandom.generator.location.StateDataRegistry;
import io.github.frikit.krandom.generator.location.StreetAddressDataProvider;
import io.github.frikit.krandom.generator.location.StreetAddressDataRegistry;
import io.github.frikit.krandom.generator.locale.LocaleDataBundle;
import io.github.frikit.krandom.generator.measurement.MeasurementDataProvider;
import io.github.frikit.krandom.generator.measurement.MeasurementDataRegistry;
import io.github.frikit.krandom.generator.user.FirstNameDataProvider;
import io.github.frikit.krandom.generator.user.FirstNameDataRegistry;
import io.github.frikit.krandom.generator.user.GenderDataProvider;
import io.github.frikit.krandom.generator.user.GenderDataRegistry;
import io.github.frikit.krandom.generator.user.LastNameDataProvider;
import io.github.frikit.krandom.generator.user.LastNameDataRegistry;
import io.github.frikit.krandom.generator.user.ProfessionDataProvider;
import io.github.frikit.krandom.generator.user.ProfessionDataRegistry;
import io.github.frikit.krandom.generator.user.SuffixDataProvider;
import io.github.frikit.krandom.generator.user.SuffixDataRegistry;
import io.github.frikit.krandom.generator.user.TitleDataProvider;
import io.github.frikit.krandom.generator.user.TitleDataRegistry;
import io.github.frikit.krandom.generator.user.nationalid.NationalIdProvider;
import io.github.frikit.krandom.generator.user.nationalid.NationalIdRegistry;
import io.github.frikit.krandom.generator.weather.WeatherDataProvider;
import io.github.frikit.krandom.generator.weather.WeatherDataRegistry;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Immutable, config-scoped registry view for locale data providers.
 *
 * <p>By default ({@link #globalDefault()}), lookups delegate to existing static registries.
 * Custom contexts can be built via {@link #builder()} and attached to {@link GeneratorConfig}
 * so tests and embedded runtimes can isolate registry state.
 */
public final class DataRegistryContext {

    private static final DataRegistryContext GLOBAL_DEFAULT = new Builder().build();

    private final boolean useGlobalFallback;

    private final Map<String, FirstNameDataProvider>     firstNames;
    private final Map<String, LastNameDataProvider>      lastNames;
    private final Map<String, GenderDataProvider>        genders;
    private final Map<String, TitleDataProvider>         titles;
    private final Map<String, SuffixDataProvider>        suffixes;
    private final Map<String, ProfessionDataProvider>    professions;
    private final Map<String, CityDataProvider>          cities;
    private final Map<String, StateDataProvider>         states;
    private final Map<String, CountryDataProvider>       countries;
    private final Map<String, StreetAddressDataProvider> streetAddresses;
    private final Map<String, NationalIdProvider>        nationalIds;
    private final Map<String, WeatherDataProvider>       weather;
    private final Map<String, MeasurementDataProvider>   measurements;

    private DataRegistryContext(Builder builder) {
        this.useGlobalFallback = builder.useGlobalFallback;
        this.firstNames = Map.copyOf(builder.firstNames);
        this.lastNames = Map.copyOf(builder.lastNames);
        this.genders = Map.copyOf(builder.genders);
        this.titles = Map.copyOf(builder.titles);
        this.suffixes = Map.copyOf(builder.suffixes);
        this.professions = Map.copyOf(builder.professions);
        this.cities = Map.copyOf(builder.cities);
        this.states = Map.copyOf(builder.states);
        this.countries = Map.copyOf(builder.countries);
        this.streetAddresses = Map.copyOf(builder.streetAddresses);
        this.nationalIds = Map.copyOf(builder.nationalIds);
        this.weather = Map.copyOf(builder.weather);
        this.measurements = Map.copyOf(builder.measurements);
    }

    /**
     * Returns the global context that delegates to legacy static registries.
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

    public FirstNameDataProvider firstNameProvider(Locale locale) {
        FirstNameDataProvider provider = findWithFallback(firstNames, locale);
        if (provider != null || !useGlobalFallback) {
            return provider;
        }
        return FirstNameDataRegistry.forLocale(locale);
    }

    public boolean isFirstNameRegistered(Locale locale) {
        return firstNameProvider(locale) != null;
    }

    public Set<String> firstNameRegisteredKeys() {
        return mergeKeys(firstNames.keySet(), useGlobalFallback ? FirstNameDataRegistry.registeredKeys() : Set.of());
    }

    public LastNameDataProvider lastNameProvider(Locale locale) {
        LastNameDataProvider provider = findWithFallback(lastNames, locale);
        if (provider != null || !useGlobalFallback) {
            return provider;
        }
        return LastNameDataRegistry.forLocale(locale);
    }

    public boolean isLastNameRegistered(Locale locale) {
        return lastNameProvider(locale) != null;
    }

    public Set<String> lastNameRegisteredKeys() {
        return mergeKeys(lastNames.keySet(), useGlobalFallback ? LastNameDataRegistry.registeredKeys() : Set.of());
    }

    public GenderDataProvider genderProvider(Locale locale) {
        GenderDataProvider provider = findWithFallback(genders, locale);
        if (provider != null || !useGlobalFallback) {
            return provider;
        }
        return GenderDataRegistry.forLocale(locale);
    }

    public boolean isGenderRegistered(Locale locale) {
        return genderProvider(locale) != null;
    }

    public Set<String> genderRegisteredKeys() {
        return mergeKeys(genders.keySet(), useGlobalFallback ? GenderDataRegistry.registeredKeys() : Set.of());
    }

    public TitleDataProvider titleProvider(Locale locale) {
        TitleDataProvider provider = findWithFallback(titles, locale);
        if (provider != null || !useGlobalFallback) {
            return provider;
        }
        return TitleDataRegistry.forLocale(locale);
    }

    public boolean isTitleRegistered(Locale locale) {
        return titleProvider(locale) != null;
    }

    public Set<String> titleRegisteredKeys() {
        return mergeKeys(titles.keySet(), useGlobalFallback ? TitleDataRegistry.registeredKeys() : Set.of());
    }

    public SuffixDataProvider suffixProvider(Locale locale) {
        SuffixDataProvider provider = findWithFallback(suffixes, locale);
        if (provider != null || !useGlobalFallback) {
            return provider;
        }
        return SuffixDataRegistry.forLocale(locale);
    }

    public boolean isSuffixRegistered(Locale locale) {
        return suffixProvider(locale) != null;
    }

    public Set<String> suffixRegisteredKeys() {
        return mergeKeys(suffixes.keySet(), useGlobalFallback ? SuffixDataRegistry.registeredKeys() : Set.of());
    }

    public ProfessionDataProvider professionProvider(Locale locale) {
        ProfessionDataProvider provider = findWithFallback(professions, locale);
        if (provider != null || !useGlobalFallback) {
            return provider;
        }
        return ProfessionDataRegistry.forLocale(locale);
    }

    public boolean isProfessionRegistered(Locale locale) {
        return professionProvider(locale) != null;
    }

    public Set<String> professionRegisteredKeys() {
        return mergeKeys(professions.keySet(), useGlobalFallback ? ProfessionDataRegistry.registeredKeys() : Set.of());
    }

    public CityDataProvider cityProvider(Locale locale) {
        CityDataProvider provider = findWithFallback(cities, locale);
        if (provider != null || !useGlobalFallback) {
            return provider;
        }
        return CityDataRegistry.forLocale(locale);
    }

    public boolean isCityRegistered(Locale locale) {
        return cityProvider(locale) != null;
    }

    public Set<String> cityRegisteredKeys() {
        return mergeKeys(cities.keySet(), useGlobalFallback ? CityDataRegistry.registeredKeys() : Set.of());
    }

    public StateDataProvider stateProvider(Locale locale) {
        StateDataProvider provider = findWithFallback(states, locale);
        if (provider != null || !useGlobalFallback) {
            return provider;
        }
        return StateDataRegistry.forLocale(locale);
    }

    public boolean isStateRegistered(Locale locale) {
        return stateProvider(locale) != null;
    }

    public Set<String> stateRegisteredKeys() {
        return mergeKeys(states.keySet(), useGlobalFallback ? StateDataRegistry.registeredKeys() : Set.of());
    }

    public CountryDataProvider countryProvider(Locale locale) {
        CountryDataProvider provider = findWithFallback(countries, locale);
        if (provider != null || !useGlobalFallback) {
            return provider;
        }
        return CountryDataRegistry.forLocale(locale);
    }

    public boolean isCountryRegistered(Locale locale) {
        return countryProvider(locale) != null;
    }

    public Set<String> countryRegisteredKeys() {
        return mergeKeys(countries.keySet(), useGlobalFallback ? CountryDataRegistry.registeredKeys() : Set.of());
    }

    public StreetAddressDataProvider streetAddressProvider(Locale locale) {
        StreetAddressDataProvider provider = findWithFallback(streetAddresses, locale);
        if (provider != null || !useGlobalFallback) {
            return provider;
        }
        return StreetAddressDataRegistry.forLocale(locale);
    }

    public boolean isStreetAddressRegistered(Locale locale) {
        return streetAddressProvider(locale) != null;
    }

    public Set<String> streetAddressRegisteredKeys() {
        return mergeKeys(streetAddresses.keySet(), useGlobalFallback ? StreetAddressDataRegistry.registeredKeys() : Set.of());
    }

    public NationalIdProvider nationalIdProvider(Locale locale) {
        NationalIdProvider provider = findWithFallback(nationalIds, locale);
        if (provider != null || !useGlobalFallback) {
            return provider;
        }
        return NationalIdRegistry.forLocale(locale);
    }

    public boolean isNationalIdRegistered(Locale locale) {
        return nationalIdProvider(locale) != null;
    }

    public Set<String> nationalIdRegisteredKeys() {
        return mergeKeys(nationalIds.keySet(), useGlobalFallback ? NationalIdRegistry.registeredKeys() : Set.of());
    }

    /**
     * Returns the weather provider for a locale, using this context's fallback policy.
     *
     * @param locale requested locale
     * @return matching provider, or {@code null} when none is registered
     */
    public WeatherDataProvider weatherProvider(Locale locale) {
        WeatherDataProvider provider = findWithFallback(weather, locale);
        if (provider != null || !useGlobalFallback) {
            return provider;
        }
        return WeatherDataRegistry.forLocale(locale);
    }

    /**
     * Returns whether this context can resolve weather data for a locale.
     *
     * @param locale requested locale
     * @return true when a provider is available
     */
    public boolean isWeatherRegistered(Locale locale) {
        return weatherProvider(locale) != null;
    }

    /**
     * Returns immutable weather locale keys visible to this context.
     *
     * @return immutable locale-key snapshot
     */
    public Set<String> weatherRegisteredKeys() {
        return mergeKeys(weather.keySet(), useGlobalFallback ? WeatherDataRegistry.registeredKeys() : Set.of());
    }

    /**
     * Returns the measurement provider for a locale, using this context's fallback policy.
     *
     * @param locale requested locale
     * @return matching provider, or {@code null} when none is registered
     */
    public MeasurementDataProvider measurementProvider(Locale locale) {
        MeasurementDataProvider provider = findWithFallback(measurements, locale);
        if (provider != null || !useGlobalFallback) {
            return provider;
        }
        return MeasurementDataRegistry.forLocale(locale);
    }

    /**
     * Reports whether this context can resolve a measurement vocabulary for the locale.
     *
     * @param locale requested locale
     * @return true when a provider is available
     */
    public boolean isMeasurementRegistered(Locale locale) {
        return measurementProvider(locale) != null;
    }

    /**
     * Returns immutable measurement locale keys visible to this context.
     *
     * @return immutable locale-key snapshot
     */
    public Set<String> measurementRegisteredKeys() {
        return mergeKeys(measurements.keySet(), useGlobalFallback ? MeasurementDataRegistry.registeredKeys() : Set.of());
    }

    private static <T> void putWithLanguageFallback(Map<String, T> registry, Locale locale, T provider) {
        Objects.requireNonNull(registry, "registry");
        Objects.requireNonNull(locale, "locale");
        Objects.requireNonNull(provider, "provider");
        String language = locale.getLanguage();
        String country = locale.getCountry();
        if (country.isEmpty()) {
            registry.put(language, provider);
        } else {
            registry.put(language + "_" + country, provider);
            registry.putIfAbsent(language, provider);
        }
    }

    private static <T> T findWithFallback(Map<String, T> registry, Locale locale) {
        if (locale == null) {
            return null;
        }
        String language = locale.getLanguage();
        String country = locale.getCountry();
        if (!country.isEmpty()) {
            T exact = registry.get(language + "_" + country);
            if (exact != null) {
                return exact;
            }
        }
        return registry.get(language);
    }

    private static Set<String> mergeKeys(Set<String> localKeys, Set<String> globalKeys) {
        if (localKeys.isEmpty() && globalKeys.isEmpty()) {
            return Set.of();
        }
        LinkedHashSet<String> keys = new LinkedHashSet<>(localKeys);
        keys.addAll(globalKeys);
        return Collections.unmodifiableSet(keys);
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

    private static void validateLabel(String name, String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(name + " must not be blank");
        }
    }

    private static void validateNullableEntries(String name, String[] values) {
        Objects.requireNonNull(values, name);
        for (int i = 0; i < values.length; i++) {
            if (values[i] == null) {
                throw new IllegalArgumentException(name + " at index " + i + " must not be null");
            }
        }
    }

    private static void validateTextValues(String name, java.util.List<String> values) {
        Objects.requireNonNull(values, name);
        if (values.isEmpty()) {
            throw new IllegalArgumentException(name + " must not be empty");
        }
        for (int i = 0; i < values.size(); i++) {
            String value = values.get(i);
            if (value == null || value.isBlank()) {
                throw new IllegalArgumentException(name + " at index " + i + " must not be blank");
            }
        }
    }

    private static void validateProfessionArrays(String[] professions, int[] weights) {
        Objects.requireNonNull(professions, "professions");
        Objects.requireNonNull(weights, "weights");
        if (professions.length == 0) {
            throw new IllegalArgumentException("professions must not be empty");
        }
        if (professions.length != weights.length) {
            throw new IllegalArgumentException("professions and weights length must match");
        }
        for (int i = 0; i < professions.length; i++) {
            if (professions[i] == null || professions[i].isBlank()) {
                throw new IllegalArgumentException("profession at index " + i + " must not be blank");
            }
            if (weights[i] <= 0) {
                throw new IllegalArgumentException("weight at index " + i + " must be > 0");
            }
        }
    }

    /**
     * Context builder.
     */
    public static final class Builder {

        private boolean useGlobalFallback = true;

        private final Map<String, FirstNameDataProvider>     firstNames     = new LinkedHashMap<>();
        private final Map<String, LastNameDataProvider>      lastNames      = new LinkedHashMap<>();
        private final Map<String, GenderDataProvider>        genders        = new LinkedHashMap<>();
        private final Map<String, TitleDataProvider>         titles         = new LinkedHashMap<>();
        private final Map<String, SuffixDataProvider>        suffixes       = new LinkedHashMap<>();
        private final Map<String, ProfessionDataProvider>    professions    = new LinkedHashMap<>();
        private final Map<String, CityDataProvider>          cities         = new LinkedHashMap<>();
        private final Map<String, StateDataProvider>         states         = new LinkedHashMap<>();
        private final Map<String, CountryDataProvider>       countries      = new LinkedHashMap<>();
        private final Map<String, StreetAddressDataProvider> streetAddresses = new LinkedHashMap<>();
        private final Map<String, NationalIdProvider>        nationalIds    = new LinkedHashMap<>();
        private final Map<String, WeatherDataProvider>       weather        = new LinkedHashMap<>();
        private final Map<String, MeasurementDataProvider>   measurements   = new LinkedHashMap<>();

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

        public Builder registerFirstNameProvider(FirstNameDataProvider provider) {
            Objects.requireNonNull(provider, "provider");
            Objects.requireNonNull(provider.getLocale(), "provider.getLocale()");
            validateArray("maleFirstNames", provider.getMaleFirstNames());
            validateArray("femaleFirstNames", provider.getFemaleFirstNames());
            putWithLanguageFallback(firstNames, provider.getLocale(), provider);
            return this;
        }

        public Builder registerLastNameProvider(LastNameDataProvider provider) {
            Objects.requireNonNull(provider, "provider");
            Objects.requireNonNull(provider.getLocale(), "provider.getLocale()");
            validateArray("lastNames", provider.getLastNames());
            putWithLanguageFallback(lastNames, provider.getLocale(), provider);
            return this;
        }

        public Builder registerGenderProvider(GenderDataProvider provider) {
            Objects.requireNonNull(provider, "provider");
            Objects.requireNonNull(provider.getLocale(), "provider.getLocale()");
            validateLabel("maleLabel", provider.getMaleLabel());
            validateLabel("femaleLabel", provider.getFemaleLabel());
            putWithLanguageFallback(genders, provider.getLocale(), provider);
            return this;
        }

        public Builder registerTitleProvider(TitleDataProvider provider) {
            Objects.requireNonNull(provider, "provider");
            Objects.requireNonNull(provider.getLocale(), "provider.getLocale()");
            validateArray("titles", provider.getTitles());
            putWithLanguageFallback(titles, provider.getLocale(), provider);
            return this;
        }

        public Builder registerSuffixProvider(SuffixDataProvider provider) {
            Objects.requireNonNull(provider, "provider");
            Objects.requireNonNull(provider.getLocale(), "provider.getLocale()");
            validateArray("suffixes", provider.getSuffixes());
            putWithLanguageFallback(suffixes, provider.getLocale(), provider);
            return this;
        }

        public Builder registerProfessionProvider(ProfessionDataProvider provider) {
            Objects.requireNonNull(provider, "provider");
            Objects.requireNonNull(provider.getLocale(), "provider.getLocale()");
            validateProfessionArrays(provider.getProfessions(), provider.getWeights());
            putWithLanguageFallback(professions, provider.getLocale(), provider);
            return this;
        }

        public Builder registerCityProvider(CityDataProvider provider) {
            Objects.requireNonNull(provider, "provider");
            Objects.requireNonNull(provider.getLocale(), "provider.getLocale()");
            validateArray("cities", provider.getCities());
            putWithLanguageFallback(cities, provider.getLocale(), provider);
            return this;
        }

        public Builder registerStateProvider(StateDataProvider provider) {
            Objects.requireNonNull(provider, "provider");
            Objects.requireNonNull(provider.getLocale(), "provider.getLocale()");
            validateArray("states", provider.getStates());
            validateNullableEntries("abbreviations", provider.getAbbreviations());
            putWithLanguageFallback(states, provider.getLocale(), provider);
            return this;
        }

        public Builder registerCountryProvider(CountryDataProvider provider) {
            Objects.requireNonNull(provider, "provider");
            Objects.requireNonNull(provider.getLocale(), "provider.getLocale()");
            validateArray("countries", provider.getCountries());
            putWithLanguageFallback(countries, provider.getLocale(), provider);
            return this;
        }

        public Builder registerStreetAddressProvider(StreetAddressDataProvider provider) {
            Objects.requireNonNull(provider, "provider");
            Objects.requireNonNull(provider.getLocale(), "provider.getLocale()");
            validateArray("streetNames", provider.getStreetNames());
            validateArray("streetTypesShort", provider.getStreetTypesShort());
            validateArray("streetTypesLong", provider.getStreetTypesLong());
            putWithLanguageFallback(streetAddresses, provider.getLocale(), provider);
            return this;
        }

        public Builder registerNationalIdProvider(NationalIdProvider provider) {
            Objects.requireNonNull(provider, "provider");
            Objects.requireNonNull(provider.getLocale(), "provider.getLocale()");
            putWithLanguageFallback(nationalIds, provider.getLocale(), provider);
            return this;
        }

        /**
         * Registers a locale-specific weather provider in this context.
         *
         * @param provider weather vocabulary provider
         * @return this builder
         */
        public Builder registerWeatherProvider(WeatherDataProvider provider) {
            Objects.requireNonNull(provider, "provider");
            Objects.requireNonNull(provider.getLocale(), "provider.getLocale()");
            validateTextValues("conditions", provider.getConditions());
            putWithLanguageFallback(weather, provider.getLocale(), provider);
            return this;
        }

        /**
         * Registers a locale-specific measurement provider in this context.
         *
         * @param provider measurement vocabulary provider
         * @return this builder
         */
        public Builder registerMeasurementProvider(MeasurementDataProvider provider) {
            Objects.requireNonNull(provider, "provider");
            Objects.requireNonNull(provider.getLocale(), "provider.getLocale()");
            validateTextValues("units", provider.getUnits());
            putWithLanguageFallback(measurements, provider.getLocale(), provider);
            return this;
        }

        public DataRegistryContext build() {
            return new DataRegistryContext(this);
        }
    }
}
