/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator;

import io.github.frikit.krandom.generator.commerce.CommerceDataProvider;
import io.github.frikit.krandom.generator.commerce.CommerceDataRegistry;
import io.github.frikit.krandom.generator.commerce.RestaurantTypeDataProvider;
import io.github.frikit.krandom.generator.commerce.RestaurantTypeDataRegistry;
import io.github.frikit.krandom.generator.database.DatabaseColumnDataProvider;
import io.github.frikit.krandom.generator.database.DatabaseColumnDataRegistry;
import io.github.frikit.krandom.generator.datapack.LocalDataPack;
import io.github.frikit.krandom.generator.file.DirectoryNameDataProvider;
import io.github.frikit.krandom.generator.file.DirectoryNameDataRegistry;
import io.github.frikit.krandom.generator.finance.BankAccountDataProvider;
import io.github.frikit.krandom.generator.finance.BankAccountDataRegistry;
import io.github.frikit.krandom.generator.finance.BankNameDataProvider;
import io.github.frikit.krandom.generator.finance.BankNameDataRegistry;
import io.github.frikit.krandom.generator.finance.BankTypeDataProvider;
import io.github.frikit.krandom.generator.finance.BankTypeDataRegistry;
import io.github.frikit.krandom.generator.finance.FinancialTermDataProvider;
import io.github.frikit.krandom.generator.finance.FinancialTermDataRegistry;
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
import io.github.frikit.krandom.generator.text.TextWordDataProvider;
import io.github.frikit.krandom.generator.text.TextWordDataRegistry;
import io.github.frikit.krandom.generator.user.CompanyBuzzwordDataProvider;
import io.github.frikit.krandom.generator.user.CompanyBuzzwordDataRegistry;
import io.github.frikit.krandom.generator.user.CompanyCatchPhraseDataProvider;
import io.github.frikit.krandom.generator.user.CompanyCatchPhraseDataRegistry;
import io.github.frikit.krandom.generator.user.CompanyNameDataProvider;
import io.github.frikit.krandom.generator.user.CompanyNameDataRegistry;
import io.github.frikit.krandom.generator.user.EducationalAttainmentDataProvider;
import io.github.frikit.krandom.generator.user.EducationalAttainmentDataRegistry;
import io.github.frikit.krandom.generator.user.FirstNameDataProvider;
import io.github.frikit.krandom.generator.user.FirstNameDataRegistry;
import io.github.frikit.krandom.generator.user.GenderDataProvider;
import io.github.frikit.krandom.generator.user.GenderDataRegistry;
import io.github.frikit.krandom.generator.user.BloodTypeDataProvider;
import io.github.frikit.krandom.generator.user.BloodTypeDataRegistry;
import io.github.frikit.krandom.generator.user.ChineseZodiacDataProvider;
import io.github.frikit.krandom.generator.user.ChineseZodiacDataRegistry;
import io.github.frikit.krandom.generator.user.HobbyDataProvider;
import io.github.frikit.krandom.generator.user.HobbyDataRegistry;
import io.github.frikit.krandom.generator.user.IndustryDataProvider;
import io.github.frikit.krandom.generator.user.IndustryDataRegistry;
import io.github.frikit.krandom.generator.user.JobFieldDataProvider;
import io.github.frikit.krandom.generator.user.JobFieldDataRegistry;
import io.github.frikit.krandom.generator.user.JobTypeDataProvider;
import io.github.frikit.krandom.generator.user.JobTypeDataRegistry;
import io.github.frikit.krandom.generator.user.LastNameDataProvider;
import io.github.frikit.krandom.generator.user.LastNameDataRegistry;
import io.github.frikit.krandom.generator.user.MaritalStatusDataProvider;
import io.github.frikit.krandom.generator.user.MaritalStatusDataRegistry;
import io.github.frikit.krandom.generator.user.NationalityDataProvider;
import io.github.frikit.krandom.generator.user.NationalityDataRegistry;
import io.github.frikit.krandom.generator.user.PositionDataProvider;
import io.github.frikit.krandom.generator.user.PositionDataRegistry;
import io.github.frikit.krandom.generator.user.ProfessionDataProvider;
import io.github.frikit.krandom.generator.user.ProfessionDataRegistry;
import io.github.frikit.krandom.generator.user.PronounDataProvider;
import io.github.frikit.krandom.generator.user.PronounDataRegistry;
import io.github.frikit.krandom.generator.user.SeniorityDataProvider;
import io.github.frikit.krandom.generator.user.SeniorityDataRegistry;
import io.github.frikit.krandom.generator.user.SuffixDataProvider;
import io.github.frikit.krandom.generator.user.SuffixDataRegistry;
import io.github.frikit.krandom.generator.user.TitleDataProvider;
import io.github.frikit.krandom.generator.user.TitleDataRegistry;
import io.github.frikit.krandom.generator.user.UniversityData;
import io.github.frikit.krandom.generator.user.UniversityDataProvider;
import io.github.frikit.krandom.generator.user.ZodiacDataProvider;
import io.github.frikit.krandom.generator.user.ZodiacDataRegistry;
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
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Immutable, config-scoped registry view for locale data providers.
 *
 * <p>By default ({@link #globalDefault()}), lookups delegate to the built-in static registries.
 * Custom contexts can be built via {@link #builder()} and attached to {@link GeneratorConfig}
 * so tests and embedded runtimes can isolate registry state.
 *
 * <p>Provider lookups resolve in this order: a provider registered in this context for the exact
 * {@code language_COUNTRY} key, the global provider for that exact key, a provider registered in
 * this context for the language (a regional registration also serves as its language's fallback),
 * and finally the global language fallback. A scoped regional bundle therefore never replaces
 * another region's built-in data; for example, an {@code en_IN} bundle leaves {@code en_US} names
 * unchanged. Isolated contexts skip both global steps.
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
    private final Map<String, NationalIdProvider>        nationalIdsByCountry;
    private final Map<String, WeatherDataProvider>       weather;
    private final Map<String, MeasurementDataProvider>   measurements;
    private final Map<String, FinancialTermDataProvider> financialTerms;
    private final Map<String, RestaurantTypeDataProvider> restaurantTypes;
    private final Map<String, HobbyDataProvider>          hobbies;
    private final Map<String, NationalityDataProvider>    nationalities;
    private final Map<String, PronounDataProvider>        pronouns;
    private final Map<String, BloodTypeDataProvider>      bloodTypes;
    private final Map<String, ChineseZodiacDataProvider>  chineseZodiacs;
    private final Map<String, ZodiacDataProvider>         zodiacs;
    private final Map<String, UniversityDataProvider>     universities;
    private final Map<String, IndustryDataProvider> industries;
    private final Map<String, JobFieldDataProvider> jobFields;
    private final Map<String, SeniorityDataProvider> seniorities;
    private final Map<String, PositionDataProvider> positions;
    private final Map<String, EducationalAttainmentDataProvider> educationalAttainments;
    private final Map<String, MaritalStatusDataProvider> maritalStatuses;
    private final Map<String, JobTypeDataProvider> jobTypes;
    private final Map<String, CompanyNameDataProvider> companyNames;
    private final Map<String, CompanyBuzzwordDataProvider> companyBuzzwords;
    private final Map<String, CompanyCatchPhraseDataProvider> companyCatchPhrases;
    private final Map<String, TextWordDataProvider> textWords;
    private final Map<String, CommerceDataProvider> commerce;
    private final Map<String, DatabaseColumnDataProvider> databaseColumns;
    private final Map<String, DirectoryNameDataProvider> directoryNames;
    private final Map<String, BankNameDataProvider> bankNames;
    private final Map<String, BankTypeDataProvider> bankTypes;
    private final Map<String, BankAccountDataProvider> bankAccounts;

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
        this.nationalIdsByCountry = Map.copyOf(builder.nationalIdsByCountry);
        this.weather = Map.copyOf(builder.weather);
        this.measurements = Map.copyOf(builder.measurements);
        this.financialTerms = Map.copyOf(builder.financialTerms);
        this.restaurantTypes = Map.copyOf(builder.restaurantTypes);
        this.hobbies = Map.copyOf(builder.hobbies);
        this.nationalities = Map.copyOf(builder.nationalities);
        this.pronouns = Map.copyOf(builder.pronouns);
        this.bloodTypes = Map.copyOf(builder.bloodTypes);
        this.chineseZodiacs = Map.copyOf(builder.chineseZodiacs);
        this.zodiacs = Map.copyOf(builder.zodiacs);
        this.universities = Map.copyOf(builder.universities);
        this.industries = Map.copyOf(builder.industries);
        this.jobFields = Map.copyOf(builder.jobFields);
        this.seniorities = Map.copyOf(builder.seniorities);
        this.positions = Map.copyOf(builder.positions);
        this.educationalAttainments = Map.copyOf(builder.educationalAttainments);
        this.maritalStatuses = Map.copyOf(builder.maritalStatuses);
        this.jobTypes = Map.copyOf(builder.jobTypes);
        this.companyNames = Map.copyOf(builder.companyNames);
        this.companyBuzzwords = Map.copyOf(builder.companyBuzzwords);
        this.companyCatchPhrases = Map.copyOf(builder.companyCatchPhrases);
        this.textWords = Map.copyOf(builder.textWords);
        this.commerce = Map.copyOf(builder.commerce);
        this.databaseColumns = Map.copyOf(builder.databaseColumns);
        this.directoryNames = Map.copyOf(builder.directoryNames);
        this.bankNames = Map.copyOf(builder.bankNames);
        this.bankTypes = Map.copyOf(builder.bankTypes);
        this.bankAccounts = Map.copyOf(builder.bankAccounts);
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

    public FirstNameDataProvider firstNameProvider(Locale locale) {
        return resolve(firstNames, locale, FirstNameDataRegistry::forLocale, FirstNameDataRegistry::registeredKeys);
    }

    public boolean isFirstNameRegistered(Locale locale) {
        return firstNameProvider(locale) != null;
    }

    public Set<String> firstNameRegisteredKeys() {
        return mergeKeys(firstNames.keySet(), useGlobalFallback ? FirstNameDataRegistry.registeredKeys() : Set.of());
    }

    public LastNameDataProvider lastNameProvider(Locale locale) {
        return resolve(lastNames, locale, LastNameDataRegistry::forLocale, LastNameDataRegistry::registeredKeys);
    }

    public boolean isLastNameRegistered(Locale locale) {
        return lastNameProvider(locale) != null;
    }

    public Set<String> lastNameRegisteredKeys() {
        return mergeKeys(lastNames.keySet(), useGlobalFallback ? LastNameDataRegistry.registeredKeys() : Set.of());
    }

    public GenderDataProvider genderProvider(Locale locale) {
        return resolve(genders, locale, GenderDataRegistry::forLocale, GenderDataRegistry::registeredKeys);
    }

    public boolean isGenderRegistered(Locale locale) {
        return genderProvider(locale) != null;
    }

    public Set<String> genderRegisteredKeys() {
        return mergeKeys(genders.keySet(), useGlobalFallback ? GenderDataRegistry.registeredKeys() : Set.of());
    }

    public TitleDataProvider titleProvider(Locale locale) {
        return resolve(titles, locale, TitleDataRegistry::forLocale, TitleDataRegistry::registeredKeys);
    }

    public boolean isTitleRegistered(Locale locale) {
        return titleProvider(locale) != null;
    }

    public Set<String> titleRegisteredKeys() {
        return mergeKeys(titles.keySet(), useGlobalFallback ? TitleDataRegistry.registeredKeys() : Set.of());
    }

    public SuffixDataProvider suffixProvider(Locale locale) {
        return resolve(suffixes, locale, SuffixDataRegistry::forLocale, SuffixDataRegistry::registeredKeys);
    }

    public boolean isSuffixRegistered(Locale locale) {
        return suffixProvider(locale) != null;
    }

    public Set<String> suffixRegisteredKeys() {
        return mergeKeys(suffixes.keySet(), useGlobalFallback ? SuffixDataRegistry.registeredKeys() : Set.of());
    }

    public ProfessionDataProvider professionProvider(Locale locale) {
        return resolve(professions, locale, ProfessionDataRegistry::forLocale, ProfessionDataRegistry::registeredKeys);
    }

    public boolean isProfessionRegistered(Locale locale) {
        return professionProvider(locale) != null;
    }

    public Set<String> professionRegisteredKeys() {
        return mergeKeys(professions.keySet(), useGlobalFallback ? ProfessionDataRegistry.registeredKeys() : Set.of());
    }

    public CityDataProvider cityProvider(Locale locale) {
        return resolve(cities, locale, CityDataRegistry::forLocale, CityDataRegistry::registeredKeys);
    }

    public boolean isCityRegistered(Locale locale) {
        return cityProvider(locale) != null;
    }

    public Set<String> cityRegisteredKeys() {
        return mergeKeys(cities.keySet(), useGlobalFallback ? CityDataRegistry.registeredKeys() : Set.of());
    }

    public StateDataProvider stateProvider(Locale locale) {
        return resolve(states, locale, StateDataRegistry::forLocale, StateDataRegistry::registeredKeys);
    }

    public boolean isStateRegistered(Locale locale) {
        return stateProvider(locale) != null;
    }

    public Set<String> stateRegisteredKeys() {
        return mergeKeys(states.keySet(), useGlobalFallback ? StateDataRegistry.registeredKeys() : Set.of());
    }

    public CountryDataProvider countryProvider(Locale locale) {
        return resolve(countries, locale, CountryDataRegistry::forLocale, CountryDataRegistry::registeredKeys);
    }

    public boolean isCountryRegistered(Locale locale) {
        return countryProvider(locale) != null;
    }

    public Set<String> countryRegisteredKeys() {
        return mergeKeys(countries.keySet(), useGlobalFallback ? CountryDataRegistry.registeredKeys() : Set.of());
    }

    public StreetAddressDataProvider streetAddressProvider(Locale locale) {
        return resolve(streetAddresses, locale, StreetAddressDataRegistry::forLocale, StreetAddressDataRegistry::registeredKeys);
    }

    public boolean isStreetAddressRegistered(Locale locale) {
        return streetAddressProvider(locale) != null;
    }

    public Set<String> streetAddressRegisteredKeys() {
        return mergeKeys(streetAddresses.keySet(), useGlobalFallback ? StreetAddressDataRegistry.registeredKeys() : Set.of());
    }

    /**
     * Returns the national-ID provider for a locale.
     *
     * <p>National identifiers are country-specific, so a locale with a country resolves the scoped
     * exact key, the global exact key, the first scoped provider of the same country, and finally
     * the global provider of the same country; it never falls back to a provider of another country
     * through its language. A locale without a country resolves the scoped, then the global
     * language-level entry.
     *
     * @param locale requested locale
     * @return matching provider, or {@code null} when none is registered
     */
    public NationalIdProvider nationalIdProvider(Locale locale) {
        if (locale == null) {
            return null;
        }
        String country = locale.getCountry();
        if (country.isEmpty()) {
            return resolve(nationalIds, locale, NationalIdRegistry::forLocale, NationalIdRegistry::registeredKeys);
        }
        String exactKey = locale.getLanguage() + "_" + country;
        NationalIdProvider scopedExact = nationalIds.get(exactKey);
        if (scopedExact != null) {
            return scopedExact;
        }
        if (useGlobalFallback && NationalIdRegistry.registeredKeys().contains(exactKey)) {
            return NationalIdRegistry.forLocale(locale);
        }
        NationalIdProvider scopedCountry = nationalIdsByCountry.get(country);
        if (scopedCountry != null || !useGlobalFallback) {
            return scopedCountry;
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
        return resolve(weather, locale, WeatherDataRegistry::forLocale, WeatherDataRegistry::registeredKeys);
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
        return resolve(measurements, locale, MeasurementDataRegistry::forLocale, MeasurementDataRegistry::registeredKeys);
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

    /**
     * Returns the financial-term provider for a locale, using this context's fallback policy.
     *
     * @param locale requested locale
     * @return matching provider, or {@code null} when none is registered
     */
    public FinancialTermDataProvider financialTermProvider(Locale locale) {
        return resolve(financialTerms, locale, FinancialTermDataRegistry::forLocale, FinancialTermDataRegistry::registeredKeys);
    }

    /**
     * Reports whether this context can resolve financial-term vocabulary for the locale.
     *
     * @param locale requested locale
     * @return true when a provider is available
     */
    public boolean isFinancialTermRegistered(Locale locale) {
        return financialTermProvider(locale) != null;
    }

    /**
     * Returns immutable financial-term locale keys visible to this context.
     *
     * @return immutable locale-key snapshot
     */
    public Set<String> financialTermRegisteredKeys() {
        return mergeKeys(financialTerms.keySet(), useGlobalFallback ? FinancialTermDataRegistry.registeredKeys() : Set.of());
    }

    /**
     * Returns the restaurant-type provider for a locale, using this context's fallback policy.
     *
     * @param locale requested locale
     * @return matching provider, or {@code null} when none is registered
     */
    public RestaurantTypeDataProvider restaurantTypeProvider(Locale locale) {
        return resolve(restaurantTypes, locale, RestaurantTypeDataRegistry::forLocale, RestaurantTypeDataRegistry::registeredKeys);
    }

    /**
     * Reports whether this context can resolve restaurant-type vocabulary for the locale.
     *
     * @param locale requested locale
     * @return true when a provider is available
     */
    public boolean isRestaurantTypeRegistered(Locale locale) {
        return restaurantTypeProvider(locale) != null;
    }

    /**
     * Returns immutable restaurant-type locale keys visible to this context.
     *
     * @return immutable locale-key snapshot
     */
    public Set<String> restaurantTypeRegisteredKeys() {
        return mergeKeys(restaurantTypes.keySet(), useGlobalFallback ? RestaurantTypeDataRegistry.registeredKeys() : Set.of());
    }

    /**
     * Returns the hobby provider for a locale, using this context's fallback policy.
     *
     * @param locale requested locale
     * @return matching provider, or {@code null} when none is registered
     */
    public HobbyDataProvider hobbyProvider(Locale locale) {
        return resolve(hobbies, locale, HobbyDataRegistry::forLocale, HobbyDataRegistry::registeredKeys);
    }

    /**
     * Reports whether this context can resolve hobby vocabulary for the locale.
     *
     * @param locale requested locale
     * @return true when a provider is available
     */
    public boolean isHobbyRegistered(Locale locale) {
        return hobbyProvider(locale) != null;
    }

    /**
     * Returns immutable hobby locale keys visible to this context.
     *
     * @return immutable locale-key snapshot
     */
    public Set<String> hobbyRegisteredKeys() {
        return mergeKeys(hobbies.keySet(), useGlobalFallback ? HobbyDataRegistry.registeredKeys() : Set.of());
    }

    /**
     * Returns the nationality provider for a locale, using this context's fallback policy.
     *
     * @param locale requested locale
     * @return matching provider, or {@code null} when none is registered
     */
    public NationalityDataProvider nationalityProvider(Locale locale) {
        return resolve(nationalities, locale, NationalityDataRegistry::forLocale, NationalityDataRegistry::registeredKeys);
    }

    /**
     * Reports whether this context can resolve nationality vocabulary for the locale.
     *
     * @param locale requested locale
     * @return true when a provider is available
     */
    public boolean isNationalityRegistered(Locale locale) {
        return nationalityProvider(locale) != null;
    }

    /**
     * Returns immutable nationality locale keys visible to this context.
     *
     * @return immutable locale-key snapshot
     */
    public Set<String> nationalityRegisteredKeys() {
        return mergeKeys(nationalities.keySet(), useGlobalFallback ? NationalityDataRegistry.registeredKeys() : Set.of());
    }

    /**
     * Returns the pronoun provider for a locale, using this context's fallback policy.
     *
     * @param locale requested locale
     * @return matching provider, or {@code null} when none is registered
     */
    public PronounDataProvider pronounProvider(Locale locale) {
        return resolve(pronouns, locale, PronounDataRegistry::forLocale, PronounDataRegistry::registeredKeys);
    }

    /**
     * Reports whether this context can resolve pronoun vocabulary for the locale.
     *
     * @param locale requested locale
     * @return true when a provider is available
     */
    public boolean isPronounRegistered(Locale locale) {
        return pronounProvider(locale) != null;
    }

    /**
     * Returns immutable pronoun locale keys visible to this context.
     *
     * @return immutable locale-key snapshot
     */
    public Set<String> pronounRegisteredKeys() {
        return mergeKeys(pronouns.keySet(), useGlobalFallback ? PronounDataRegistry.registeredKeys() : Set.of());
    }

    /**
     * Returns the blood-type provider for a locale, using this context's fallback policy.
     *
     * @param locale requested locale
     * @return matching provider, or {@code null} when none is registered
     */
    public BloodTypeDataProvider bloodTypeProvider(Locale locale) {
        return resolve(bloodTypes, locale, BloodTypeDataRegistry::forLocale, BloodTypeDataRegistry::registeredKeys);
    }

    /**
     * Reports whether this context can resolve a blood-type distribution for the locale.
     *
     * @param locale requested locale
     * @return true when a provider is available
     */
    public boolean isBloodTypeRegistered(Locale locale) {
        return bloodTypeProvider(locale) != null;
    }

    /**
     * Returns immutable blood-type locale keys visible to this context.
     *
     * @return immutable locale-key snapshot
     */
    public Set<String> bloodTypeRegisteredKeys() {
        return mergeKeys(bloodTypes.keySet(), useGlobalFallback ? BloodTypeDataRegistry.registeredKeys() : Set.of());
    }

    /**
     * Returns the Chinese-zodiac provider for a locale, using this context's fallback policy.
     *
     * @param locale requested locale
     * @return matching provider, or {@code null} when none is registered
     */
    public ChineseZodiacDataProvider chineseZodiacProvider(Locale locale) {
        return resolve(chineseZodiacs, locale, ChineseZodiacDataRegistry::forLocale, ChineseZodiacDataRegistry::registeredKeys);
    }

    /**
     * Reports whether this context can resolve Chinese-zodiac vocabulary for the locale.
     *
     * @param locale requested locale
     * @return true when a provider is available
     */
    public boolean isChineseZodiacRegistered(Locale locale) {
        return chineseZodiacProvider(locale) != null;
    }

    /**
     * Returns immutable Chinese-zodiac locale keys visible to this context.
     *
     * @return immutable locale-key snapshot
     */
    public Set<String> chineseZodiacRegisteredKeys() {
        return mergeKeys(chineseZodiacs.keySet(), useGlobalFallback ? ChineseZodiacDataRegistry.registeredKeys() : Set.of());
    }

    /**
     * Returns the Western-zodiac provider for a locale, using this context's fallback policy.
     *
     * @param locale requested locale
     * @return matching provider, or {@code null} when none is registered
     */
    public ZodiacDataProvider zodiacProvider(Locale locale) {
        return resolve(zodiacs, locale, ZodiacDataRegistry::forLocale, ZodiacDataRegistry::registeredKeys);
    }

    /**
     * Reports whether this context can resolve Western-zodiac vocabulary for the locale.
     *
     * @param locale requested locale
     * @return true when a provider is available
     */
    public boolean isZodiacRegistered(Locale locale) {
        return zodiacProvider(locale) != null;
    }

    /**
     * Returns immutable Western-zodiac locale keys visible to this context.
     *
     * @return immutable locale-key snapshot
     */
    public Set<String> zodiacRegisteredKeys() {
        return mergeKeys(zodiacs.keySet(), useGlobalFallback ? ZodiacDataRegistry.registeredKeys() : Set.of());
    }

    /**
     * Returns the locally registered university provider for a locale.
     *
     * <p>University fixtures intentionally never use global fallback. They must come from a
     * verified local data pack attached to this context.
     *
     * @param locale requested locale
     * @return matching provider, or {@code null} when no local pack is registered
     */
    public UniversityDataProvider universityProvider(Locale locale) {
        return resolve(universities, locale, ignored -> null, Set::of);
    }

    /**
     * Reports whether a local university data pack is available for a locale.
     *
     * @param locale requested locale
     * @return true when a local provider is registered
     */
    public boolean isUniversityRegistered(Locale locale) {
        return universityProvider(locale) != null;
    }

    /**
     * Returns the locale keys supplied by local University data packs.
     *
     * @return immutable locale-key snapshot
     */
    public Set<String> universityRegisteredKeys() {
        return mergeKeys(universities.keySet(), Set.of());
    }

    /**
     * Returns the industry provider for a locale, using this context's fallback policy.
     *
     * @param locale requested locale
     * @return matching provider, or {@code null} when none is registered
     */
    public IndustryDataProvider industryProvider(Locale locale) {
        return resolve(industries, locale, IndustryDataRegistry::forLocale, IndustryDataRegistry::registeredKeys);
    }

    /**
     * Reports whether this context can resolve industry vocabulary for the locale.
     *
     * @param locale requested locale
     * @return true when a provider is available
     */
    public boolean isIndustryRegistered(Locale locale) {
        return industryProvider(locale) != null;
    }

    /**
     * Returns immutable industry locale keys visible to this context.
     *
     * @return immutable locale-key snapshot
     */
    public Set<String> industryRegisteredKeys() {
        return mergeKeys(industries.keySet(), useGlobalFallback ? IndustryDataRegistry.registeredKeys() : Set.of());
    }

    /**
     * Returns the job-field provider for a locale, using this context's fallback policy.
     *
     * @param locale requested locale
     * @return matching provider, or {@code null} when none is registered
     */
    public JobFieldDataProvider jobFieldProvider(Locale locale) {
        return resolve(jobFields, locale, JobFieldDataRegistry::forLocale, JobFieldDataRegistry::registeredKeys);
    }

    /**
     * Reports whether this context can resolve job-field vocabulary for the locale.
     *
     * @param locale requested locale
     * @return true when a provider is available
     */
    public boolean isJobFieldRegistered(Locale locale) {
        return jobFieldProvider(locale) != null;
    }

    /**
     * Returns immutable job-field locale keys visible to this context.
     *
     * @return immutable locale-key snapshot
     */
    public Set<String> jobFieldRegisteredKeys() {
        return mergeKeys(jobFields.keySet(), useGlobalFallback ? JobFieldDataRegistry.registeredKeys() : Set.of());
    }

    /**
     * Returns the seniority provider for a locale, using this context's fallback policy.
     *
     * @param locale requested locale
     * @return matching provider, or {@code null} when none is registered
     */
    public SeniorityDataProvider seniorityProvider(Locale locale) {
        return resolve(seniorities, locale, SeniorityDataRegistry::forLocale, SeniorityDataRegistry::registeredKeys);
    }

    /**
     * Reports whether this context can resolve seniority vocabulary for the locale.
     *
     * @param locale requested locale
     * @return true when a provider is available
     */
    public boolean isSeniorityRegistered(Locale locale) {
        return seniorityProvider(locale) != null;
    }

    /**
     * Returns immutable seniority locale keys visible to this context.
     *
     * @return immutable locale-key snapshot
     */
    public Set<String> seniorityRegisteredKeys() {
        return mergeKeys(seniorities.keySet(), useGlobalFallback ? SeniorityDataRegistry.registeredKeys() : Set.of());
    }

    /**
     * Returns the position provider for a locale, using this context's fallback policy.
     *
     * @param locale requested locale
     * @return matching provider, or {@code null} when none is registered
     */
    public PositionDataProvider positionProvider(Locale locale) {
        return resolve(positions, locale, PositionDataRegistry::forLocale, PositionDataRegistry::registeredKeys);
    }

    /**
     * Reports whether this context can resolve position vocabulary for the locale.
     *
     * @param locale requested locale
     * @return true when a provider is available
     */
    public boolean isPositionRegistered(Locale locale) {
        return positionProvider(locale) != null;
    }

    /**
     * Returns immutable position locale keys visible to this context.
     *
     * @return immutable locale-key snapshot
     */
    public Set<String> positionRegisteredKeys() {
        return mergeKeys(positions.keySet(), useGlobalFallback ? PositionDataRegistry.registeredKeys() : Set.of());
    }

    /**
     * Returns the educational-attainment provider for a locale, using this context's fallback policy.
     *
     * @param locale requested locale
     * @return matching provider, or {@code null} when none is registered
     */
    public EducationalAttainmentDataProvider educationalAttainmentProvider(Locale locale) {
        return resolve(educationalAttainments, locale, EducationalAttainmentDataRegistry::forLocale, EducationalAttainmentDataRegistry::registeredKeys);
    }

    /**
     * Reports whether this context can resolve educational-attainment vocabulary for the locale.
     *
     * @param locale requested locale
     * @return true when a provider is available
     */
    public boolean isEducationalAttainmentRegistered(Locale locale) {
        return educationalAttainmentProvider(locale) != null;
    }

    /**
     * Returns immutable educational-attainment locale keys visible to this context.
     *
     * @return immutable locale-key snapshot
     */
    public Set<String> educationalAttainmentRegisteredKeys() {
        return mergeKeys(educationalAttainments.keySet(), useGlobalFallback ? EducationalAttainmentDataRegistry.registeredKeys() : Set.of());
    }

    /**
     * Returns the marital-status provider for a locale, using this context's fallback policy.
     *
     * @param locale requested locale
     * @return matching provider, or {@code null} when none is registered
     */
    public MaritalStatusDataProvider maritalStatusProvider(Locale locale) {
        return resolve(maritalStatuses, locale, MaritalStatusDataRegistry::forLocale, MaritalStatusDataRegistry::registeredKeys);
    }

    /**
     * Reports whether this context can resolve marital-status vocabulary for the locale.
     *
     * @param locale requested locale
     * @return true when a provider is available
     */
    public boolean isMaritalStatusRegistered(Locale locale) {
        return maritalStatusProvider(locale) != null;
    }

    /**
     * Returns immutable marital-status locale keys visible to this context.
     *
     * @return immutable locale-key snapshot
     */
    public Set<String> maritalStatusRegisteredKeys() {
        return mergeKeys(maritalStatuses.keySet(), useGlobalFallback ? MaritalStatusDataRegistry.registeredKeys() : Set.of());
    }

    /**
     * Returns the job-type provider for a locale, using this context's fallback policy.
     *
     * @param locale requested locale
     * @return matching provider, or {@code null} when none is registered
     */
    public JobTypeDataProvider jobTypeProvider(Locale locale) {
        return resolve(jobTypes, locale, JobTypeDataRegistry::forLocale, JobTypeDataRegistry::registeredKeys);
    }

    /**
     * Reports whether this context can resolve job-type vocabulary for the locale.
     *
     * @param locale requested locale
     * @return true when a provider is available
     */
    public boolean isJobTypeRegistered(Locale locale) {
        return jobTypeProvider(locale) != null;
    }

    /**
     * Returns immutable job-type locale keys visible to this context.
     *
     * @return immutable locale-key snapshot
     */
    public Set<String> jobTypeRegisteredKeys() {
        return mergeKeys(jobTypes.keySet(), useGlobalFallback ? JobTypeDataRegistry.registeredKeys() : Set.of());
    }

    /**
     * Returns the company-name provider for a locale, using this context's fallback policy.
     *
     * @param locale requested locale
     * @return matching provider, or {@code null} when none is registered
     */
    public CompanyNameDataProvider companyNameProvider(Locale locale) {
        return resolve(companyNames, locale, CompanyNameDataRegistry::forLocale, CompanyNameDataRegistry::registeredKeys);
    }

    /**
     * Reports whether this context can resolve company-name vocabulary for the locale.
     *
     * @param locale requested locale
     * @return true when a provider is available
     */
    public boolean isCompanyNameRegistered(Locale locale) {
        return companyNameProvider(locale) != null;
    }

    /**
     * Returns immutable company-name locale keys visible to this context.
     *
     * @return immutable locale-key snapshot
     */
    public Set<String> companyNameRegisteredKeys() {
        return mergeKeys(companyNames.keySet(), useGlobalFallback ? CompanyNameDataRegistry.registeredKeys() : Set.of());
    }

    /**
     * Returns the company-buzzword provider for a locale, using this context's fallback policy.
     *
     * @param locale requested locale
     * @return matching provider, or {@code null} when none is registered
     */
    public CompanyBuzzwordDataProvider companyBuzzwordProvider(Locale locale) {
        return resolve(companyBuzzwords, locale, CompanyBuzzwordDataRegistry::forLocale, CompanyBuzzwordDataRegistry::registeredKeys);
    }

    /**
     * Reports whether this context can resolve company-buzzword vocabulary for the locale.
     *
     * @param locale requested locale
     * @return true when a provider is available
     */
    public boolean isCompanyBuzzwordRegistered(Locale locale) {
        return companyBuzzwordProvider(locale) != null;
    }

    /**
     * Returns immutable company-buzzword locale keys visible to this context.
     *
     * @return immutable locale-key snapshot
     */
    public Set<String> companyBuzzwordRegisteredKeys() {
        return mergeKeys(companyBuzzwords.keySet(), useGlobalFallback ? CompanyBuzzwordDataRegistry.registeredKeys() : Set.of());
    }

    /**
     * Returns the company catch-phrase provider for a locale, using this context's fallback policy.
     *
     * @param locale requested locale
     * @return matching provider, or {@code null} when none is registered
     */
    public CompanyCatchPhraseDataProvider companyCatchPhraseProvider(Locale locale) {
        return resolve(companyCatchPhrases, locale, CompanyCatchPhraseDataRegistry::forLocale, CompanyCatchPhraseDataRegistry::registeredKeys);
    }

    /**
     * Reports whether this context can resolve company catch-phrase vocabulary for the locale.
     *
     * @param locale requested locale
     * @return true when a provider is available
     */
    public boolean isCompanyCatchPhraseRegistered(Locale locale) {
        return companyCatchPhraseProvider(locale) != null;
    }

    /**
     * Returns immutable company catch-phrase locale keys visible to this context.
     *
     * @return immutable locale-key snapshot
     */
    public Set<String> companyCatchPhraseRegisteredKeys() {
        return mergeKeys(companyCatchPhrases.keySet(), useGlobalFallback ? CompanyCatchPhraseDataRegistry.registeredKeys() : Set.of());
    }

    /**
     * Returns the text-word provider for a locale, using this context's fallback policy.
     *
     * @param locale requested locale
     * @return matching provider, or {@code null} when none is registered
     */
    public TextWordDataProvider textWordProvider(Locale locale) {
        return resolve(textWords, locale, TextWordDataRegistry::forLocale, TextWordDataRegistry::registeredKeys);
    }

    /**
     * Reports whether this context can resolve text-word vocabulary for the locale.
     *
     * @param locale requested locale
     * @return true when a provider is available
     */
    public boolean isTextWordRegistered(Locale locale) {
        return textWordProvider(locale) != null;
    }

    /**
     * Returns immutable text-word locale keys visible to this context.
     *
     * @return immutable locale-key snapshot
     */
    public Set<String> textWordRegisteredKeys() {
        return mergeKeys(textWords.keySet(), useGlobalFallback ? TextWordDataRegistry.registeredKeys() : Set.of());
    }

    /**
     * Returns the commerce provider for a locale, using this context's fallback policy.
     *
     * @param locale requested locale
     * @return matching provider, or {@code null} when none is registered
     */
    public CommerceDataProvider commerceProvider(Locale locale) {
        return resolve(commerce, locale, CommerceDataRegistry::forLocale, CommerceDataRegistry::registeredKeys);
    }

    /**
     * Reports whether this context can resolve commerce vocabulary for the locale.
     *
     * @param locale requested locale
     * @return true when a provider is available
     */
    public boolean isCommerceRegistered(Locale locale) {
        return commerceProvider(locale) != null;
    }

    /**
     * Returns immutable commerce locale keys visible to this context.
     *
     * @return immutable locale-key snapshot
     */
    public Set<String> commerceRegisteredKeys() {
        return mergeKeys(commerce.keySet(), useGlobalFallback ? CommerceDataRegistry.registeredKeys() : Set.of());
    }

    /**
     * Returns the database-column provider for a locale, using this context's fallback policy.
     *
     * @param locale requested locale
     * @return matching provider, or {@code null} when none is registered
     */
    public DatabaseColumnDataProvider databaseColumnProvider(Locale locale) {
        return resolve(databaseColumns, locale, DatabaseColumnDataRegistry::forLocale, DatabaseColumnDataRegistry::registeredKeys);
    }

    /**
     * Reports whether this context can resolve database-column vocabulary for the locale.
     *
     * @param locale requested locale
     * @return true when a provider is available
     */
    public boolean isDatabaseColumnRegistered(Locale locale) {
        return databaseColumnProvider(locale) != null;
    }

    /**
     * Returns immutable database-column locale keys visible to this context.
     *
     * @return immutable locale-key snapshot
     */
    public Set<String> databaseColumnRegisteredKeys() {
        return mergeKeys(databaseColumns.keySet(), useGlobalFallback ? DatabaseColumnDataRegistry.registeredKeys() : Set.of());
    }

    /**
     * Returns the directory-name provider for a locale, using this context's fallback policy.
     *
     * @param locale requested locale
     * @return matching provider, or {@code null} when none is registered
     */
    public DirectoryNameDataProvider directoryNameProvider(Locale locale) {
        return resolve(directoryNames, locale, DirectoryNameDataRegistry::forLocale, DirectoryNameDataRegistry::registeredKeys);
    }

    /**
     * Reports whether this context can resolve directory-name vocabulary for the locale.
     *
     * @param locale requested locale
     * @return true when a provider is available
     */
    public boolean isDirectoryNameRegistered(Locale locale) {
        return directoryNameProvider(locale) != null;
    }

    /**
     * Returns immutable directory-name locale keys visible to this context.
     *
     * @return immutable locale-key snapshot
     */
    public Set<String> directoryNameRegisteredKeys() {
        return mergeKeys(directoryNames.keySet(), useGlobalFallback ? DirectoryNameDataRegistry.registeredKeys() : Set.of());
    }

    /**
     * Returns the bank-name provider for a locale, using this context's fallback policy.
     *
     * @param locale requested locale
     * @return matching provider, or {@code null} when none is registered
     */
    public BankNameDataProvider bankNameProvider(Locale locale) {
        return resolve(bankNames, locale, BankNameDataRegistry::forLocale, BankNameDataRegistry::registeredKeys);
    }

    /**
     * Reports whether this context can resolve bank-name vocabulary for the locale.
     *
     * @param locale requested locale
     * @return true when a provider is available
     */
    public boolean isBankNameRegistered(Locale locale) {
        return bankNameProvider(locale) != null;
    }

    /**
     * Returns immutable bank-name locale keys visible to this context.
     *
     * @return immutable locale-key snapshot
     */
    public Set<String> bankNameRegisteredKeys() {
        return mergeKeys(bankNames.keySet(), useGlobalFallback ? BankNameDataRegistry.registeredKeys() : Set.of());
    }

    /**
     * Returns the bank-type provider for a locale, using this context's fallback policy.
     *
     * @param locale requested locale
     * @return matching provider, or {@code null} when none is registered
     */
    public BankTypeDataProvider bankTypeProvider(Locale locale) {
        return resolve(bankTypes, locale, BankTypeDataRegistry::forLocale, BankTypeDataRegistry::registeredKeys);
    }

    /**
     * Reports whether this context can resolve bank-type vocabulary for the locale.
     *
     * @param locale requested locale
     * @return true when a provider is available
     */
    public boolean isBankTypeRegistered(Locale locale) {
        return bankTypeProvider(locale) != null;
    }

    /**
     * Returns immutable bank-type locale keys visible to this context.
     *
     * @return immutable locale-key snapshot
     */
    public Set<String> bankTypeRegisteredKeys() {
        return mergeKeys(bankTypes.keySet(), useGlobalFallback ? BankTypeDataRegistry.registeredKeys() : Set.of());
    }

    /**
     * Returns the bank-account provider for a locale, using this context's fallback policy.
     *
     * @param locale requested locale
     * @return matching provider, or {@code null} when none is registered
     */
    public BankAccountDataProvider bankAccountProvider(Locale locale) {
        return resolve(bankAccounts, locale, BankAccountDataRegistry::forLocale, BankAccountDataRegistry::registeredKeys);
    }

    /**
     * Reports whether this context can resolve bank-account vocabulary for the locale.
     *
     * @param locale requested locale
     * @return true when a provider is available
     */
    public boolean isBankAccountRegistered(Locale locale) {
        return bankAccountProvider(locale) != null;
    }

    /**
     * Returns immutable bank-account locale keys visible to this context.
     *
     * @return immutable locale-key snapshot
     */
    public Set<String> bankAccountRegisteredKeys() {
        return mergeKeys(bankAccounts.keySet(), useGlobalFallback ? BankAccountDataRegistry.registeredKeys() : Set.of());
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

    /**
     * Resolves one provider family with the documented precedence: scoped exact, global exact,
     * scoped language, then global language. A scoped regional registration therefore only acts
     * as a language fallback for locales that have no exact match anywhere.
     */
    private <T> T resolve(Map<String, T> scoped,
                          Locale locale,
                          Function<Locale, T> globalLookup,
                          Supplier<Set<String>> globalKeys) {
        if (locale == null) {
            return null;
        }
        String language = locale.getLanguage();
        String country = locale.getCountry();
        if (!country.isEmpty()) {
            String exactKey = language + "_" + country;
            T scopedExact = scoped.get(exactKey);
            if (scopedExact != null) {
                return scopedExact;
            }
            if (useGlobalFallback && globalKeys.get().contains(exactKey)) {
                return globalLookup.apply(locale);
            }
        }
        T scopedLanguage = scoped.get(language);
        if (scopedLanguage != null || !useGlobalFallback) {
            return scopedLanguage;
        }
        return globalLookup.apply(locale);
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

    private static void validatePronounSets(java.util.List<String> pronounSets) {
        validateTextValues("pronounSets", pronounSets);
        for (int i = 0; i < pronounSets.size(); i++) {
            String pronounSet = pronounSets.get(i);
            int separator = pronounSet.indexOf('/');
            if (separator <= 0 || separator == pronounSet.length() - 1 || separator != pronounSet.lastIndexOf('/')) {
                throw new IllegalArgumentException("pronounSet at index " + i + " must use subject/object form");
            }
        }
    }

    private static void validateBloodTypeDistribution(
        java.util.List<String> types, java.util.List<Integer> weights) {
        validateTextValues("types", types);
        Objects.requireNonNull(weights, "weights");
        if (types.size() != weights.size()) {
            throw new IllegalArgumentException("types and weights length must match");
        }
        for (int i = 0; i < weights.size(); i++) {
            Integer weight = weights.get(i);
            if (weight == null || weight <= 0) {
                throw new IllegalArgumentException("weight at index " + i + " must be > 0");
            }
        }
    }

    private static void validateTwelveTextValues(String name, java.util.List<String> values) {
        validateTextValues(name, values);
        if (values.size() != 12) {
            throw new IllegalArgumentException(name + " must contain exactly 12 values");
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

    private static void validateUniversityData(UniversityData[] universities) {
        Objects.requireNonNull(universities, "universities");
        if (universities.length == 0) {
            throw new IllegalArgumentException("universities must not be empty");
        }
        for (int index = 0; index < universities.length; index++) {
            if (universities[index] == null) {
                throw new IllegalArgumentException("university at index " + index + " must not be null");
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
        private final Map<String, NationalIdProvider>        nationalIdsByCountry = new LinkedHashMap<>();
        private final Map<String, WeatherDataProvider>       weather        = new LinkedHashMap<>();
        private final Map<String, MeasurementDataProvider>   measurements   = new LinkedHashMap<>();
        private final Map<String, FinancialTermDataProvider> financialTerms = new LinkedHashMap<>();
        private final Map<String, RestaurantTypeDataProvider> restaurantTypes = new LinkedHashMap<>();
        private final Map<String, HobbyDataProvider>          hobbies         = new LinkedHashMap<>();
        private final Map<String, NationalityDataProvider>    nationalities   = new LinkedHashMap<>();
        private final Map<String, PronounDataProvider>        pronouns        = new LinkedHashMap<>();
        private final Map<String, BloodTypeDataProvider>      bloodTypes      = new LinkedHashMap<>();
        private final Map<String, ChineseZodiacDataProvider>  chineseZodiacs  = new LinkedHashMap<>();
        private final Map<String, ZodiacDataProvider>         zodiacs        = new LinkedHashMap<>();
        private final Map<String, UniversityDataProvider>     universities   = new LinkedHashMap<>();
        private final Map<String, IndustryDataProvider> industries = new LinkedHashMap<>();
        private final Map<String, JobFieldDataProvider> jobFields = new LinkedHashMap<>();
        private final Map<String, SeniorityDataProvider> seniorities = new LinkedHashMap<>();
        private final Map<String, PositionDataProvider> positions = new LinkedHashMap<>();
        private final Map<String, EducationalAttainmentDataProvider> educationalAttainments = new LinkedHashMap<>();
        private final Map<String, MaritalStatusDataProvider> maritalStatuses = new LinkedHashMap<>();
        private final Map<String, JobTypeDataProvider> jobTypes = new LinkedHashMap<>();
        private final Map<String, CompanyNameDataProvider> companyNames = new LinkedHashMap<>();
        private final Map<String, CompanyBuzzwordDataProvider> companyBuzzwords = new LinkedHashMap<>();
        private final Map<String, CompanyCatchPhraseDataProvider> companyCatchPhrases = new LinkedHashMap<>();
        private final Map<String, TextWordDataProvider> textWords = new LinkedHashMap<>();
        private final Map<String, CommerceDataProvider> commerce = new LinkedHashMap<>();
        private final Map<String, DatabaseColumnDataProvider> databaseColumns = new LinkedHashMap<>();
        private final Map<String, DirectoryNameDataProvider> directoryNames = new LinkedHashMap<>();
        private final Map<String, BankNameDataProvider> bankNames = new LinkedHashMap<>();
        private final Map<String, BankTypeDataProvider> bankTypes = new LinkedHashMap<>();
        private final Map<String, BankAccountDataProvider> bankAccounts = new LinkedHashMap<>();

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
            return registerUniversityProvider(Objects.requireNonNull(dataPack, "dataPack").universityProvider());
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
            String country = provider.getLocale().getCountry();
            if (!country.isEmpty()) {
                nationalIdsByCountry.putIfAbsent(country, provider);
            }
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

        /**
         * Registers a locale-specific financial-term provider in this context.
         *
         * @param provider financial-term vocabulary provider
         * @return this builder
         */
        public Builder registerFinancialTermProvider(FinancialTermDataProvider provider) {
            Objects.requireNonNull(provider, "provider");
            Objects.requireNonNull(provider.getLocale(), "provider.getLocale()");
            validateTextValues("terms", provider.getTerms());
            putWithLanguageFallback(financialTerms, provider.getLocale(), provider);
            return this;
        }

        /**
         * Registers a locale-specific restaurant-type provider in this context.
         *
         * @param provider restaurant-type vocabulary provider
         * @return this builder
         */
        public Builder registerRestaurantTypeProvider(RestaurantTypeDataProvider provider) {
            Objects.requireNonNull(provider, "provider");
            Objects.requireNonNull(provider.getLocale(), "provider.getLocale()");
            validateTextValues("types", provider.getTypes());
            putWithLanguageFallback(restaurantTypes, provider.getLocale(), provider);
            return this;
        }

        /**
         * Registers a locale-specific hobby provider in this context.
         *
         * @param provider hobby vocabulary provider
         * @return this builder
         */
        public Builder registerHobbyProvider(HobbyDataProvider provider) {
            Objects.requireNonNull(provider, "provider");
            Objects.requireNonNull(provider.getLocale(), "provider.getLocale()");
            validateTextValues("hobbies", provider.getHobbies());
            putWithLanguageFallback(hobbies, provider.getLocale(), provider);
            return this;
        }

        /**
         * Registers a locale-specific nationality provider in this context.
         *
         * @param provider nationality vocabulary provider
         * @return this builder
         */
        public Builder registerNationalityProvider(NationalityDataProvider provider) {
            Objects.requireNonNull(provider, "provider");
            Objects.requireNonNull(provider.getLocale(), "provider.getLocale()");
            validateTextValues("nationalities", provider.getNationalities());
            putWithLanguageFallback(nationalities, provider.getLocale(), provider);
            return this;
        }

        /**
         * Registers a locale-specific pronoun provider in this context.
         *
         * @param provider pronoun vocabulary provider
         * @return this builder
         */
        public Builder registerPronounProvider(PronounDataProvider provider) {
            Objects.requireNonNull(provider, "provider");
            Objects.requireNonNull(provider.getLocale(), "provider.getLocale()");
            validatePronounSets(provider.getPronounSets());
            putWithLanguageFallback(pronouns, provider.getLocale(), provider);
            return this;
        }

        /**
         * Registers a locale-specific blood-type distribution provider in this context.
         *
         * @param provider blood-type distribution provider
         * @return this builder
         */
        public Builder registerBloodTypeProvider(BloodTypeDataProvider provider) {
            Objects.requireNonNull(provider, "provider");
            Objects.requireNonNull(provider.getLocale(), "provider.getLocale()");
            validateBloodTypeDistribution(provider.getTypes(), provider.getWeights());
            putWithLanguageFallback(bloodTypes, provider.getLocale(), provider);
            return this;
        }

        /**
         * Registers a locale-specific Chinese-zodiac provider in this context.
         *
         * @param provider Chinese-zodiac vocabulary provider
         * @return this builder
         */
        public Builder registerChineseZodiacProvider(ChineseZodiacDataProvider provider) {
            Objects.requireNonNull(provider, "provider");
            Objects.requireNonNull(provider.getLocale(), "provider.getLocale()");
            validateTwelveTextValues("animals", provider.getAnimals());
            putWithLanguageFallback(chineseZodiacs, provider.getLocale(), provider);
            return this;
        }

        /**
         * Registers a locale-specific Western-zodiac provider in this context.
         *
         * @param provider Western-zodiac vocabulary provider
         * @return this builder
         */
        public Builder registerZodiacProvider(ZodiacDataProvider provider) {
            Objects.requireNonNull(provider, "provider");
            Objects.requireNonNull(provider.getLocale(), "provider.getLocale()");
            validateTwelveTextValues("signs", provider.getSigns());
            putWithLanguageFallback(zodiacs, provider.getLocale(), provider);
            return this;
        }

        /**
         * Registers a locale-specific University fixture provider in this context.
         *
         * @param provider University fixture provider
         * @return this builder
         */
        public Builder registerUniversityProvider(UniversityDataProvider provider) {
            Objects.requireNonNull(provider, "provider");
            Objects.requireNonNull(provider.getLocale(), "provider.getLocale()");
            validateUniversityData(provider.getUniversities());
            putWithLanguageFallback(universities, provider.getLocale(), provider);
            return this;
        }

        /**
         * Registers a locale-specific industry provider in this context.
         *
         * @param provider industry vocabulary provider
         * @return this builder
         */
        public Builder registerIndustryProvider(IndustryDataProvider provider) {
            Objects.requireNonNull(provider, "provider");
            Objects.requireNonNull(provider.getLocale(), "provider.getLocale()");
            validateTextValues("industries", provider.getIndustries());
            putWithLanguageFallback(industries, provider.getLocale(), provider);
            return this;
        }

        /**
         * Registers a locale-specific job-field provider in this context.
         *
         * @param provider job-field vocabulary provider
         * @return this builder
         */
        public Builder registerJobFieldProvider(JobFieldDataProvider provider) {
            Objects.requireNonNull(provider, "provider");
            Objects.requireNonNull(provider.getLocale(), "provider.getLocale()");
            validateTextValues("jobFields", provider.getJobFields());
            putWithLanguageFallback(jobFields, provider.getLocale(), provider);
            return this;
        }

        /**
         * Registers a locale-specific seniority provider in this context.
         *
         * @param provider seniority vocabulary provider
         * @return this builder
         */
        public Builder registerSeniorityProvider(SeniorityDataProvider provider) {
            Objects.requireNonNull(provider, "provider");
            Objects.requireNonNull(provider.getLocale(), "provider.getLocale()");
            validateTextValues("seniorities", provider.getSeniorities());
            putWithLanguageFallback(seniorities, provider.getLocale(), provider);
            return this;
        }

        /**
         * Registers a locale-specific position provider in this context.
         *
         * @param provider position vocabulary provider
         * @return this builder
         */
        public Builder registerPositionProvider(PositionDataProvider provider) {
            Objects.requireNonNull(provider, "provider");
            Objects.requireNonNull(provider.getLocale(), "provider.getLocale()");
            validateTextValues("positions", provider.getPositions());
            putWithLanguageFallback(positions, provider.getLocale(), provider);
            return this;
        }

        /**
         * Registers a locale-specific educational-attainment provider in this context.
         *
         * @param provider educational-attainment vocabulary provider
         * @return this builder
         */
        public Builder registerEducationalAttainmentProvider(EducationalAttainmentDataProvider provider) {
            Objects.requireNonNull(provider, "provider");
            Objects.requireNonNull(provider.getLocale(), "provider.getLocale()");
            validateTextValues("educationalAttainments", provider.getEducationalAttainments());
            putWithLanguageFallback(educationalAttainments, provider.getLocale(), provider);
            return this;
        }

        /**
         * Registers a locale-specific marital-status provider in this context.
         *
         * @param provider marital-status vocabulary provider
         * @return this builder
         */
        public Builder registerMaritalStatusProvider(MaritalStatusDataProvider provider) {
            Objects.requireNonNull(provider, "provider");
            Objects.requireNonNull(provider.getLocale(), "provider.getLocale()");
            validateTextValues("maritalStatuses", provider.getMaritalStatuses());
            putWithLanguageFallback(maritalStatuses, provider.getLocale(), provider);
            return this;
        }

        /**
         * Registers a locale-specific job-type provider in this context.
         *
         * @param provider job-type vocabulary provider
         * @return this builder
         */
        public Builder registerJobTypeProvider(JobTypeDataProvider provider) {
            Objects.requireNonNull(provider, "provider");
            Objects.requireNonNull(provider.getLocale(), "provider.getLocale()");
            validateTextValues("jobTypes", provider.getJobTypes());
            putWithLanguageFallback(jobTypes, provider.getLocale(), provider);
            return this;
        }

        /**
         * Registers a locale-specific company-name provider in this context.
         *
         * @param provider company-name vocabulary provider
         * @return this builder
         */
        public Builder registerCompanyNameProvider(CompanyNameDataProvider provider) {
            Objects.requireNonNull(provider, "provider");
            Objects.requireNonNull(provider.getLocale(), "provider.getLocale()");
            validateTextValues("prefixes", provider.getPrefixes());
            validateTextValues("nouns", provider.getNouns());
            validateTextValues("suffixes", provider.getSuffixes());
            validateLabel("nameFormat", provider.getNameFormat());
            validateLabel("legalNameFormat", provider.getLegalNameFormat());
            putWithLanguageFallback(companyNames, provider.getLocale(), provider);
            return this;
        }

        /**
         * Registers a locale-specific company-buzzword provider in this context.
         *
         * @param provider company-buzzword vocabulary provider
         * @return this builder
         */
        public Builder registerCompanyBuzzwordProvider(CompanyBuzzwordDataProvider provider) {
            Objects.requireNonNull(provider, "provider");
            Objects.requireNonNull(provider.getLocale(), "provider.getLocale()");
            validateTextValues("verbs", provider.getVerbs());
            validateTextValues("adjectives", provider.getAdjectives());
            validateTextValues("nouns", provider.getNouns());
            validateLabel("format", provider.getFormat());
            putWithLanguageFallback(companyBuzzwords, provider.getLocale(), provider);
            return this;
        }

        /**
         * Registers a locale-specific company catch-phrase provider in this context.
         *
         * @param provider company catch-phrase vocabulary provider
         * @return this builder
         */
        public Builder registerCompanyCatchPhraseProvider(CompanyCatchPhraseDataProvider provider) {
            Objects.requireNonNull(provider, "provider");
            Objects.requireNonNull(provider.getLocale(), "provider.getLocale()");
            validateTextValues("adjectives", provider.getAdjectives());
            validateTextValues("nouns", provider.getNouns());
            validateTextValues("taglines", provider.getTaglines());
            validateLabel("format", provider.getFormat());
            putWithLanguageFallback(companyCatchPhrases, provider.getLocale(), provider);
            return this;
        }

        /**
         * Registers a locale-specific text-word provider in this context.
         *
         * @param provider text-word vocabulary provider
         * @return this builder
         */
        public Builder registerTextWordProvider(TextWordDataProvider provider) {
            Objects.requireNonNull(provider, "provider");
            Objects.requireNonNull(provider.getLocale(), "provider.getLocale()");
            validateTextValues("words", provider.getWords());
            putWithLanguageFallback(textWords, provider.getLocale(), provider);
            return this;
        }

        /**
         * Registers a locale-specific commerce provider in this context.
         *
         * @param provider commerce vocabulary provider
         * @return this builder
         */
        public Builder registerCommerceProvider(CommerceDataProvider provider) {
            Objects.requireNonNull(provider, "provider");
            Objects.requireNonNull(provider.getLocale(), "provider.getLocale()");
            validateTextValues("adjectives", provider.getAdjectives());
            validateTextValues("materials", provider.getMaterials());
            validateTextValues("products", provider.getProducts());
            validateTextValues("departments", provider.getDepartments());
            validateTextValues("colors", provider.getColors());
            validateLabel("productNameFormat", provider.getProductNameFormat());
            validateLabel("productDescriptionFormat", provider.getProductDescriptionFormat());
            putWithLanguageFallback(commerce, provider.getLocale(), provider);
            return this;
        }

        /**
         * Registers a locale-specific database-column provider in this context.
         *
         * @param provider database-column vocabulary provider
         * @return this builder
         */
        public Builder registerDatabaseColumnProvider(DatabaseColumnDataProvider provider) {
            Objects.requireNonNull(provider, "provider");
            Objects.requireNonNull(provider.getLocale(), "provider.getLocale()");
            validateTextValues("columns", provider.getColumns());
            putWithLanguageFallback(databaseColumns, provider.getLocale(), provider);
            return this;
        }

        /**
         * Registers a locale-specific directory-name provider in this context.
         *
         * @param provider directory-name vocabulary provider
         * @return this builder
         */
        public Builder registerDirectoryNameProvider(DirectoryNameDataProvider provider) {
            Objects.requireNonNull(provider, "provider");
            Objects.requireNonNull(provider.getLocale(), "provider.getLocale()");
            validateTextValues("directoryNames", provider.getDirectoryNames());
            putWithLanguageFallback(directoryNames, provider.getLocale(), provider);
            return this;
        }

        /**
         * Registers a locale-specific bank-name provider in this context.
         *
         * @param provider bank-name vocabulary provider
         * @return this builder
         */
        public Builder registerBankNameProvider(BankNameDataProvider provider) {
            Objects.requireNonNull(provider, "provider");
            Objects.requireNonNull(provider.getLocale(), "provider.getLocale()");
            validateTextValues("bankNames", provider.getBankNames());
            putWithLanguageFallback(bankNames, provider.getLocale(), provider);
            return this;
        }

        /**
         * Registers a locale-specific bank-type provider in this context.
         *
         * @param provider bank-type vocabulary provider
         * @return this builder
         */
        public Builder registerBankTypeProvider(BankTypeDataProvider provider) {
            Objects.requireNonNull(provider, "provider");
            Objects.requireNonNull(provider.getLocale(), "provider.getLocale()");
            validateTextValues("bankTypes", provider.getBankTypes());
            putWithLanguageFallback(bankTypes, provider.getLocale(), provider);
            return this;
        }

        /**
         * Registers a locale-specific bank-account provider in this context.
         *
         * @param provider bank-account vocabulary provider
         * @return this builder
         */
        public Builder registerBankAccountProvider(BankAccountDataProvider provider) {
            Objects.requireNonNull(provider, "provider");
            Objects.requireNonNull(provider.getLocale(), "provider.getLocale()");
            validateTextValues("accountNames", provider.getAccountNames());
            validateTextValues("transactionTypes", provider.getTransactionTypes());
            putWithLanguageFallback(bankAccounts, provider.getLocale(), provider);
            return this;
        }

        public DataRegistryContext build() {
            return new DataRegistryContext(this);
        }
    }
}
